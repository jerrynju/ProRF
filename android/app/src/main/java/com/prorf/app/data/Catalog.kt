package com.prorf.app.data

import java.util.Locale

object Catalog {

    fun isZh(): Boolean = Locale.getDefault().language == "zh"

    val modules: Map<String, ModuleSpec> = listOf(
        ModuleSpec("tx_source", NodeKind.TX, "信号源", "Signal Source", "sig",
            listOf(ParamSpec("powerDbm", "输出功率", "Output Power", "dBm", 0.0, -40.0, 60.0))),
        ModuleSpec("pa", NodeKind.TX, "功率放大器", "Power Amplifier", "amp",
            listOf(
                ParamSpec("gainDb", "增益", "Gain", "dB", 30.0, 0.0, 60.0),
                ParamSpec("p1dbDbm", "P1dB", "P1dB", "dBm", 33.0, 10.0, 50.0, infoOnly = true),
            )),
        ModuleSpec("ant_tx", NodeKind.TX, "天线(发射)", "TX Antenna", "ant",
            listOf(ParamSpec("gainDbi", "增益", "Gain", "dBi", 45.0, 0.0, 65.0))),
        ModuleSpec("cable", NodeKind.LOSS, "电缆/波导", "Cable", "cab",
            listOf(
                ParamSpec("lossDb", "损耗", "Loss", "dB", 2.0, 0.0, 20.0),
                ParamSpec("lengthM", "长度", "Length", "m", 10.0, 0.0, 1000.0, infoOnly = true),
            )),
        ModuleSpec("filter", NodeKind.LOSS, "滤波器", "Filter", "flt",
            listOf(
                ParamSpec("lossDb", "插损", "Insertion Loss", "dB", 1.0, 0.0, 10.0),
                ParamSpec("bwMHz", "带宽", "Bandwidth", "MHz", 100.0, 0.1, 1000.0, infoOnly = true),
            )),
        ModuleSpec("attenuator", NodeKind.LOSS, "衰减器", "Attenuator", "att",
            listOf(ParamSpec("lossDb", "衰减量", "Attenuation", "dB", 6.0, 0.0, 60.0))),
        ModuleSpec("fspl", NodeKind.PROPAGATION, "自由空间损耗", "Free Space Loss", "spc"),
        ModuleSpec("rain", NodeKind.PROPAGATION, "雨衰", "Rain Attenuation", "rain",
            listOf(ParamSpec("lossDb", "雨衰量", "Rain Loss", "dB", 2.0, 0.0, 30.0))),
        ModuleSpec("atmospheric", NodeKind.PROPAGATION, "大气吸收", "Atmospheric Loss", "atm",
            listOf(ParamSpec("lossDb", "损耗量", "Loss", "dB", 0.5, 0.0, 10.0))),
        ModuleSpec("ant_rx", NodeKind.RX, "天线(接收)", "RX Antenna", "anr",
            listOf(ParamSpec("gainDbi", "增益", "Gain", "dBi", 45.0, 0.0, 65.0))),
        ModuleSpec("lna", NodeKind.RX, "低噪放LNA", "LNA", "lna",
            listOf(
                ParamSpec("gainDb", "增益", "Gain", "dB", 20.0, 5.0, 40.0),
                ParamSpec("nfDb", "噪声系数", "Noise Figure", "dB", 1.5, 0.5, 10.0),
                ParamSpec("oip3", "OIP3", "OIP3", "dBm", 30.0, 5.0, 50.0, infoOnly = true),
            )),
        ModuleSpec("mixer", NodeKind.RX, "混频器", "Mixer", "mix",
            listOf(
                ParamSpec("lossDb", "转换损耗", "Conversion Loss", "dB", 6.0, 3.0, 15.0),
                ParamSpec("nfDb", "噪声系数", "Noise Figure", "dB", 8.0, 1.0, 20.0),
            )),
        ModuleSpec("receiver", NodeKind.RX, "接收机", "Receiver", "rcv",
            listOf(
                ParamSpec("nfDb", "系统NF", "System NF", "dB", 3.0, 0.5, 15.0),
                ParamSpec("bwMHz", "带宽", "Bandwidth", "MHz", 36.0, 0.1, 1000.0),
            )),
    ).associateBy { it.id }

    fun kindLabel(kind: NodeKind, zh: Boolean = isZh()) = when (kind) {
        NodeKind.TX -> if (zh) "发射" else "TX"
        NodeKind.LOSS -> if (zh) "损耗" else "LOSS"
        NodeKind.PROPAGATION -> if (zh) "传播" else "PROP"
        NodeKind.RX -> if (zh) "接收" else "RX"
    }

    private fun t(zh: Boolean, cn: String, en: String) = if (zh) cn else en

    /** Built-in link templates; node/workflow names localized at instantiation time. */
    fun sampleWorkflows(zh: Boolean = isZh()): List<Workflow> = listOf(
        Workflow(
            id = "wf-sat", name = t(zh, "卫星通信下行链路示例", "Satellite Downlink Example"),
            tags = listOf(t(zh, "卫星", "Satellite"), t(zh, "Ku频段", "Ku-band")),
            updatedAt = "2026-05-20 14:30",
            globals = GlobalParams(14000.0, 36.0, 38000.0, 290.0),
            nodes = listOf(
                RfNode("s1", NodeKind.TX, "pa", t(zh, "发射机", "Transmitter"), mapOf("gainDb" to 43.0, "p1dbDbm" to 45.0)),
                RfNode("s2", NodeKind.TX, "ant_tx", t(zh, "天线(发射)", "TX Antenna"), mapOf("gainDbi" to 45.0)),
                RfNode("s3", NodeKind.PROPAGATION, "fspl", t(zh, "自由空间", "Free Space")),
                RfNode("s4", NodeKind.RX, "ant_rx", t(zh, "天线(接收)", "RX Antenna"), mapOf("gainDbi" to 45.0)),
                RfNode("s5", NodeKind.RX, "receiver", t(zh, "接收机", "Receiver"), mapOf("nfDb" to 3.0, "bwMHz" to 36.0)),
            ),
        ),
        Workflow(
            id = "wf-5g", name = t(zh, "5G宏站基站链路", "5G Macro Site Link"),
            tags = listOf("5G", "3.5GHz"), fav = true,
            updatedAt = "2026-05-18 09:45",
            globals = GlobalParams(3500.0, 100.0, 0.5, 290.0),
            nodes = listOf(
                RfNode("g1", NodeKind.TX, "tx_source", t(zh, "信号源", "Signal Source"), mapOf("powerDbm" to 20.0)),
                RfNode("g2", NodeKind.TX, "pa", t(zh, "PA放大器", "PA Amplifier"), mapOf("gainDb" to 28.0, "p1dbDbm" to 33.0)),
                RfNode("g3", NodeKind.TX, "ant_tx", t(zh, "阵列天线", "Array Antenna"), mapOf("gainDbi" to 20.0)),
                RfNode("g4", NodeKind.PROPAGATION, "fspl", t(zh, "自由空间", "Free Space")),
                RfNode("g5", NodeKind.RX, "ant_rx", t(zh, "手机天线", "Handset Antenna"), mapOf("gainDbi" to 2.0)),
                RfNode("g6", NodeKind.RX, "lna", t(zh, "低噪放", "LNA"), mapOf("gainDb" to 15.0, "nfDb" to 2.0, "oip3" to 25.0)),
                RfNode("g7", NodeKind.RX, "receiver", t(zh, "接收机", "Receiver"), mapOf("nfDb" to 5.0, "bwMHz" to 100.0)),
            ),
        ),
        Workflow(
            id = "wf-mw", name = t(zh, "微波回传链路", "Microwave Backhaul Link"),
            tags = listOf(t(zh, "微波", "Microwave"), "18GHz"),
            updatedAt = "2026-05-15 16:22",
            globals = GlobalParams(18000.0, 28.0, 20.0, 290.0),
            nodes = listOf(
                RfNode("m1", NodeKind.TX, "pa", t(zh, "发射机", "Transmitter"), mapOf("gainDb" to 25.0, "p1dbDbm" to 28.0)),
                RfNode("m2", NodeKind.LOSS, "cable", t(zh, "馈线损耗", "Feeder Loss"), mapOf("lossDb" to 2.0, "lengthM" to 15.0)),
                RfNode("m3", NodeKind.TX, "ant_tx", t(zh, "抛物面天线", "Parabolic Antenna"), mapOf("gainDbi" to 38.0)),
                RfNode("m4", NodeKind.PROPAGATION, "fspl", t(zh, "自由空间", "Free Space")),
                RfNode("m5", NodeKind.RX, "ant_rx", t(zh, "抛物面天线", "Parabolic Antenna"), mapOf("gainDbi" to 38.0)),
                RfNode("m6", NodeKind.RX, "receiver", t(zh, "接收机", "Receiver"), mapOf("nfDb" to 4.0, "bwMHz" to 28.0)),
            ),
        ),
        Workflow(
            id = "wf-radar", name = t(zh, "雷达系统链路", "Radar System Link"),
            tags = listOf(t(zh, "雷达", "Radar"), "X-band"),
            updatedAt = "2026-05-12 10:15",
            globals = GlobalParams(9500.0, 10.0, 100.0, 290.0),
            nodes = listOf(
                RfNode("r1", NodeKind.TX, "tx_source", t(zh, "信号源", "Signal Source"), mapOf("powerDbm" to 10.0)),
                RfNode("r2", NodeKind.TX, "pa", t(zh, "功率放大器", "Power Amplifier"), mapOf("gainDb" to 40.0, "p1dbDbm" to 44.0)),
                RfNode("r3", NodeKind.TX, "ant_tx", t(zh, "发射天线", "TX Antenna"), mapOf("gainDbi" to 35.0)),
                RfNode("r4", NodeKind.PROPAGATION, "fspl", t(zh, "自由空间", "Free Space")),
                RfNode("r5", NodeKind.PROPAGATION, "atmospheric", t(zh, "大气吸收", "Atmospheric Loss"), mapOf("lossDb" to 1.0)),
                RfNode("r6", NodeKind.RX, "ant_rx", t(zh, "接收天线", "RX Antenna"), mapOf("gainDbi" to 35.0)),
                RfNode("r7", NodeKind.RX, "lna", t(zh, "低噪放LNA", "LNA"), mapOf("gainDb" to 25.0, "nfDb" to 1.2, "oip3" to 30.0)),
                RfNode("r8", NodeKind.RX, "receiver", t(zh, "接收机", "Receiver"), mapOf("nfDb" to 3.0, "bwMHz" to 10.0)),
            ),
        ),
    )

    /** Stable template ids → emoji; titles/descriptions come from string resources. */
    val templates: List<Pair<String, String>> = listOf(
        "sat" to "🛰️", "5g" to "📶", "mw" to "📡", "radar" to "🎯", "blank" to "✨",
    )
}
