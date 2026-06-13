package com.prorf.platform.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class NodeRenameTest {

    @Test
    fun `renaming a node sets its label`() {
        val node = NodeInstance(id = "n1", typeId = "rf.amplifier")
        val renamed = node.copy(label = "My LNA")
        assertEquals("My LNA", renamed.label)
    }

    @Test
    fun `renaming preserves parameters and position`() {
        val node = NodeInstance(
            id = "n1",
            typeId = "rf.amplifier",
            parameters = mapOf("gainDb" to 25.0),
            position = NodePosition(100f, 200f),
        )
        val renamed = node.copy(label = "LNA Front-end")
        assertEquals("LNA Front-end", renamed.label)
        assertEquals(25.0, renamed.parameters["gainDb"])
        assertEquals(100f, renamed.position.x)
        assertEquals(200f, renamed.position.y)
    }

    @Test
    fun `clearing label restores null`() {
        val node = NodeInstance(id = "n1", typeId = "rf.amplifier", label = "My LNA")
        val cleared = node.copy(label = null)
        assertNull(cleared.label)
    }

    @Test
    fun `blank label treated as null by rename convention`() {
        val label = "   ".ifBlank { null }
        assertNull(label)
    }

    @Test
    fun `default label is null`() {
        val node = NodeInstance(id = "n1", typeId = "rf.amplifier")
        assertNull(node.label)
    }
}
