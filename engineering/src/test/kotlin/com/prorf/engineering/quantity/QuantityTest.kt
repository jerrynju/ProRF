package com.prorf.engineering.quantity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QuantityTest {

    @Test
    fun `Quantity stores value and unit`() {
        val q = 30.0.dBm()
        assertEquals(30.0, q.value, 1e-9)
        assertEquals(PhysicalUnit.DBM, q.unit)
        assertEquals(Dimension.POWER, q.dimension)
    }

    @Test
    fun `dBm converts to Watts correctly`() {
        val oneMw = 0.0.dBm()
        assertEquals(1e-3, oneMw.toSi(), 1e-9)

        val oneW = 30.0.dBm()
        assertEquals(1.0, oneW.toSi(), 1e-9)

        val tenW = 40.0.dBm()
        assertEquals(10.0, tenW.toSi(), 1e-6)
    }

    @Test
    fun `convertTo same dimension succeeds`() {
        val freqMHz = 1000.0.MHz()
        val freqGHz = freqMHz.convertTo(PhysicalUnit.GHZ)
        assertEquals(1.0, freqGHz.value, 1e-9)
        assertEquals(PhysicalUnit.GHZ, freqGHz.unit)
    }

    @Test
    fun `convertTo different dimension throws`() {
        val power = 30.0.dBm()
        assertThrows(IllegalArgumentException::class.java) {
            power.convertTo(PhysicalUnit.MHZ)
        }
    }

    @Test
    fun `km to meters conversion`() {
        val dist = 1.0.km()
        assertEquals(1000.0, dist.toSi(), 1e-9)
        val meters = dist.convertTo(PhysicalUnit.METER)
        assertEquals(1000.0, meters.value, 1e-9)
    }

    @Test
    fun `GHz to Hz conversion`() {
        val freq = 14.0.GHz()
        assertEquals(14e9, freq.toSi(), 1.0)
    }

    @Test
    fun `plus adds same-dimension quantities`() {
        val a = 10.0.dB()
        val b = 5.0.dB()
        val sum = a + b
        assertEquals(15.0, sum.value, 1e-9)
    }

    @Test
    fun `minus subtracts same-dimension quantities`() {
        val a = 30.0.dB()
        val b = 10.0.dB()
        val diff = a - b
        assertEquals(20.0, diff.value, 1e-9)
    }

    @Test
    fun `unaryMinus negates value`() {
        val q = 5.0.dB()
        assertEquals(-5.0, (-q).value, 1e-9)
    }

    @Test
    fun `plus mismatched dimensions throws`() {
        val power = 30.0.dBm()
        val gain = 10.0.dB()
        assertThrows(IllegalArgumentException::class.java) {
            power + gain
        }
    }

    @Test
    fun `toString includes value and unit symbol`() {
        val q = 30.0.dBm()
        assertTrue(q.toString().contains("30.0"))
        assertTrue(q.toString().contains("dBm"))
    }

    @Test
    fun `Quantity equality is value and unit based`() {
        assertEquals(30.0.dBm(), 30.0.dBm())
        assertNotEquals(30.0.dBm(), 30.0.dB())
    }
}
