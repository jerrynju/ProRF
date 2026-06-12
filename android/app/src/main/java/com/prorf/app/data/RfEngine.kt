package com.prorf.app.data

import java.util.Locale
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

object RfEngine {

    fun fspl(freqMHz: Double, distKm: Double): Double {
        if (distKm <= 0 || freqMHz <= 0) return 0.0
        return 32.44 + 20 * log10(max(freqMHz, 1.0)) + 20 * log10(max(distKm, 0.001))
    }

    /** Thermal noise density: 10·log₁₀(k·T) + 30  →  dBm/Hz.  At 290 K ≈ −174.0 dBm/Hz. */
    private fun thermalNoiseDensity(tempK: Double): Double {
        val k = 1.380649e-23  // Boltzmann constant (J/K)
        return 10 * log10(k * max(tempK, 1.0)) + 30
    }

    fun noiseFloor(bwMHz: Double, nfDb: Double = 0.0, tempK: Double = 290.0): Double =
        thermalNoiseDensity(tempK) + 10 * log10(max(bwMHz, 0.001) * 1e6) + nfDb

    fun evaluate(nodes: List<RfNode>, g: GlobalParams): LinkResult {
        // Locale-safe formatting helper to avoid comma decimals in certain locales
        fun f(v: Double, d: Int = 1) = "%.${d}f".format(Locale.US, v)

        // Empty node list: return safe defaults
        if (nodes.isEmpty()) {
            val nf = noiseFloor(g.bandwidthMHz, 3.0, g.temperatureK)
            return LinkResult(
                eirp = 0.0, rxPwr = 0.0, sensitivity = nf, margin = 0.0,
                snr = 0.0, noiseFloor = nf, isValid = false,
                trace = emptyList(), chart = emptyList(),
            )
        }

        var pwr = 0.0
        val trace = ArrayList<TraceEntry>(nodes.size)
        var rxPwr: Double? = null
        var sensitivity = -100.0
        var margin = 0.0
        var rxNfDb = 3.0
        var rxAntGain = 0.0
        var fsplNodeId: String? = null
        // Receiver's own IF bandwidth — defaults to global BW, updated when receiver node is processed
        var rxBwMHz = g.bandwidthMHz

        // Friis cascade tracking: each pair is (noise factor linear, gain linear) for RX chain elements
        data class RxStage(val nfLinear: Double, val gainLinear: Double)
        val rxChain = ArrayList<RxStage>()

        fun p(n: RfNode, k: String, d: Double) = n.params[k] ?: d
        fun dBToLinear(db: Double) = 10.0.pow(db / 10.0)

        for (n in nodes) {
            var gain = 0.0
            var loss = 0.0
            var lbl: String
            when (n.moduleId) {
                "tx_source" -> { pwr = p(n, "powerDbm", 0.0); lbl = "${f(pwr)} dBm" }
                "pa" -> { gain = p(n, "gainDb", 30.0); pwr += gain; lbl = "+${f(gain)} dB" }
                "cable", "attenuator" -> { loss = p(n, "lossDb", 2.0); pwr -= loss; lbl = "−${f(loss)} dB" }
                "filter" -> { loss = p(n, "lossDb", 1.0); pwr -= loss; lbl = "−${f(loss)} dB" }
                "ant_tx" -> { gain = p(n, "gainDbi", 45.0); pwr += gain; lbl = "+${f(gain, 0)} dBi" }
                "fspl" -> { loss = fspl(g.frequencyMHz, g.distanceKm); pwr -= loss; lbl = "−${f(loss)} dB"; fsplNodeId = n.id }
                "rain" -> { loss = p(n, "lossDb", 2.0); pwr -= loss; lbl = "−${f(loss)} dB" }
                "atmospheric" -> { loss = p(n, "lossDb", 1.0); pwr -= loss; lbl = "−${f(loss)} dB" }
                "ant_rx" -> {
                    gain = p(n, "gainDbi", 45.0); rxAntGain = gain; pwr += gain; lbl = "+${f(gain, 0)} dBi"
                    rxChain.add(RxStage(1.0, dBToLinear(gain)))  // ideal antenna: NF = 0 dB
                }
                "lna" -> {
                    val gainDb = p(n, "gainDb", 20.0)
                    val nfDb = p(n, "nfDb", 1.5)
                    gain = gainDb; pwr += gain; lbl = "+${f(gain)} dB"
                    rxChain.add(RxStage(dBToLinear(nfDb), dBToLinear(gainDb)))
                }
                "mixer" -> {
                    val lossDb = p(n, "lossDb", 6.0)
                    val nfDb = p(n, "nfDb", lossDb)  // mixer NF ≈ conversion loss for passive
                    loss = lossDb; pwr -= loss; lbl = "−${f(loss)} dB"
                    rxChain.add(RxStage(dBToLinear(nfDb), dBToLinear(-lossDb)))
                }
                "receiver" -> {
                    val termNfDb = p(n, "nfDb", 3.0)
                    // Use receiver's own IF bandwidth if set; otherwise fall back to global BW
                    rxBwMHz = p(n, "bwMHz", g.bandwidthMHz)
                    rxChain.add(RxStage(dBToLinear(termNfDb), 1.0))
                    // Friis cascade: F_sys = F1 + (F2-1)/G1 + (F3-1)/(G1*G2) + ...
                    rxNfDb = if (rxChain.isEmpty()) termNfDb else {
                        var cascaded = rxChain[0].nfLinear
                        var cumGain = rxChain[0].gainLinear
                        for (i in 1 until rxChain.size) {
                            cascaded += (rxChain[i].nfLinear - 1.0) / max(cumGain, 0.001)
                            cumGain *= rxChain[i].gainLinear
                        }
                        10 * log10(max(cascaded, 1.0))
                    }
                    rxPwr = pwr
                    sensitivity = noiseFloor(rxBwMHz, rxNfDb, g.temperatureK)
                    margin = pwr - sensitivity
                    lbl = "${f(pwr)} dBm"
                }
                else -> lbl = "—"
            }
            trace.add(TraceEntry(n.id, n.name, n.kind, gain, loss, pwr, lbl))
        }

        // EIRP = power after the last TX node
        var eirp = 0.0
        for (i in trace.indices.reversed()) {
            if (nodes[i].kind == NodeKind.TX) { eirp = trace[i].pwr; break }
        }

        val nfFloor = noiseFloor(rxBwMHz, rxNfDb, g.temperatureK)
        // Fix: exclude RX-kind entries from fixed loss to avoid double-counting
        // (RX gains are already handled by rxAntGain in the chart formula)
        val firstRxIdx = trace.indexOfFirst { it.kind == NodeKind.RX }
        val fixedBeforeRx = if (firstRxIdx >= 0) {
            trace.take(firstRxIdx).filter { it.nodeId != fsplNodeId }.sumOf { it.loss }
        } else {
            trace.filter { it.nodeId != fsplNodeId }.sumOf { it.loss }
        }

        val chart = (0 until 32).map { i ->
            val dMin = log10(0.01)
            val dMax = log10(max(g.distanceKm, 0.01) * 8)
            val d = 10.0.pow(dMin + (dMax - dMin) * i / 31.0)
            val rxP = eirp - fixedBeforeRx - fspl(g.frequencyMHz, d) + rxAntGain
            ChartPoint(d, rxP - nfFloor, rxP - sensitivity)
        }

        return LinkResult(
            eirp = eirp,
            rxPwr = rxPwr ?: pwr,
            sensitivity = sensitivity,
            margin = margin,
            snr = (rxPwr ?: pwr) - nfFloor,
            noiseFloor = nfFloor,
            isValid = margin > 0,
            trace = trace,
            chart = chart,
        )
    }

    fun fmt(v: Double, digits: Int = 1): String = "%.${digits}f".format(Locale.US, v)
    fun fmtSigned(v: Double, digits: Int = 1): String = (if (v >= 0) "+" else "−") + "%.${digits}f".format(Locale.US, abs(v))
}
