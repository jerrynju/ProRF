package com.prorf.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readText

class ExampleDslFilesTest {
    private val dsl = ProRfDsl()
    private val examplesDir = locateExamplesDir()

    @Test
    fun `valid examples compile to workflow graph IR`() {
        val files = listProrfFiles(examplesDir.resolve("valid"))

        assertEquals(4, files.size)
        files.forEach { file ->
            val graph = dsl.compile(file.readText())
            assertTrue(graph.nodes.isNotEmpty(), "${file.name} should define nodes")
            assertEquals("prorf-dsl-v1", graph.metadata["source"])
        }
    }

    @Test
    fun `invalid examples fail with expected diagnostics`() {
        val expected = mapOf(
            "unit_mismatch.prorf" to "UNIT_MISMATCH",
            "cycle.prorf" to "GRAPH_CYCLE",
        )

        val files = listProrfFiles(examplesDir.resolve("invalid"))
        assertEquals(expected.keys, files.map { it.name }.toSet())

        files.forEach { file ->
            val ex = assertThrows(DslCompilationException::class.java) {
                dsl.compile(file.readText())
            }
            assertTrue(
                ex.diagnostics.any { it.code == expected.getValue(file.name) },
                "${file.name} should fail with ${expected.getValue(file.name)}, got ${ex.diagnostics}",
            )
        }
    }

    private fun listProrfFiles(dir: Path): List<Path> =
        Files.list(dir).use { stream ->
            stream
                .filter { it.extension == "prorf" }
                .sorted()
                .toList()
        }

    private fun locateExamplesDir(): Path {
        val fromModule = Path.of("examples")
        if (Files.isDirectory(fromModule.resolve("valid"))) return fromModule

        val fromRoot = Path.of("dsl", "examples")
        if (Files.isDirectory(fromRoot.resolve("valid"))) return fromRoot

        error("Cannot locate DSL examples directory.")
    }
}
