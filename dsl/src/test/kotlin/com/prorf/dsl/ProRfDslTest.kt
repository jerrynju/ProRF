package com.prorf.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ProRfDslTest {
    private val dsl = ProRfDsl()

    @Test
    fun `parses and compiles RF link budget`() {
        val graph = dsl.compile(
            """
            workflow "RF_Link_Budget_v1" {
                imports {
                    rf.std.v1
                }

                variables {
                    f0 = 2.4 GHz
                    temp = 290 K
                }

                nodes {
                    tx: Transmitter {
                        power = 20 dBm
                        frequency = f0
                    }

                    ch: FreeSpacePath {
                        distance = 3 km
                        frequency = f0
                    }

                    rx: Receiver {
                        noiseFigure = 2.0 dB
                    }
                }

                edges {
                    tx.output.power -> ch.input.power
                    ch.output.power -> rx.input.power
                }

                scenarios {
                    nominal
                    high_loss {
                        ch.distance = 5 km
                    }
                }

                outputs {
                    rx.snr
                    rx.link_margin
                }
            }
            """.trimIndent()
        )

        assertEquals("RF_Link_Budget_v1", graph.name)
        assertEquals(3, graph.nodes.size)
        assertEquals(2, graph.edges.size)
        assertEquals("power", graph.edges.first().fromPortId)
        assertEquals("high_loss,nominal".split(",").toSet(), graph.metadata["scenarios"]!!.split(",").toSet())
    }

    @Test
    fun `rejects unit mismatch`() {
        val ex = assertThrows(DslCompilationException::class.java) {
            dsl.compile(
                """
                workflow "bad_units" {
                    nodes {
                        ch: FreeSpacePath {
                            distance = 10 dBm
                            frequency = 2.4 GHz
                        }
                    }
                }
                """.trimIndent()
            )
        }

        assertEquals("UNIT_MISMATCH", ex.diagnostics.first().code)
    }

    @Test
    fun `checks variable reference dimensions`() {
        val ex = assertThrows(DslCompilationException::class.java) {
            dsl.compile(
                """
                workflow "bad_variable_ref" {
                    variables {
                        f0 = 3 km
                    }
                    nodes {
                        tx: Transmitter {
                            power = 20 dBm
                            frequency = f0
                        }
                    }
                }
                """.trimIndent()
            )
        }

        assertEquals("UNIT_MISMATCH", ex.diagnostics.first().code)
    }

    @Test
    fun `rejects graph cycles`() {
        val ex = assertThrows(DslCompilationException::class.java) {
            dsl.compile(
                """
                workflow "cycle" {
                    nodes {
                        a: PowerAmplifier {
                            gain = 1 dB
                        }
                        b: PowerAmplifier {
                            gain = 1 dB
                        }
                    }
                    edges {
                        a.power -> b.power
                        b.power -> a.power
                    }
                }
                """.trimIndent()
            )
        }

        assertEquals(true, ex.diagnostics.any { it.code == "GRAPH_CYCLE" })
    }

    @Test
    fun `keeps arithmetic and function expressions stable`() {
        val document = dsl.parse(
            """
            workflow "expressions" {
                variables {
                    gain = 10 + 3
                    nf = log(2)
                    margin = tx.power - 3 dB
                }
            }
            """.trimIndent()
        )

        assertEquals("10 + 3", document.variables[0].expression.text)
        assertEquals("log(2)", document.variables[1].expression.text)
        assertEquals("tx.power - 3 dB", document.variables[2].expression.text)
    }

    @Test
    fun `parses indexed port references`() {
        val document = dsl.parse(
            """
            workflow "indexed_ports" {
                nodes {
                    splitter: PowerAmplifier {
                        gain = 1 dB
                    }
                    rx1: Receiver {
                        noiseFigure = 2 dB
                    }
                }
                edges {
                    splitter.output.power[1] -> rx1.input.power
                }
            }
            """.trimIndent()
        )

        assertEquals("splitter.output.power[1]", document.edges.first().from.toString())
    }

    @Test
    fun `validates indexed ports against base port schema`() {
        val graph = dsl.compile(
            """
            workflow "indexed_ports_compile" {
                nodes {
                    splitter: PowerAmplifier {
                        gain = 1 dB
                    }
                    rx1: Receiver {
                        noiseFigure = 2 dB
                    }
                }
                edges {
                    splitter.output.power[1] -> rx1.input.power
                }
            }
            """.trimIndent()
        )

        assertEquals("power[1]", graph.edges.first().fromPortId)
    }
}
