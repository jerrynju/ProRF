package com.prorf.dsl

import com.prorf.platform.graph.Edge
import com.prorf.platform.graph.NodeInstance
import com.prorf.platform.graph.WorkflowGraph

class WorkflowGraphCompiler {
    fun compile(document: WorkflowDocumentAst): WorkflowGraph {
        val nodes = document.nodes.map { node ->
            NodeInstance(
                id = node.id,
                typeId = node.type,
                parameters = node.parameters.associate { it.path.toString() to it.expression.text },
                label = node.id,
            )
        }
        val edges = document.edges.mapIndexed { index, edge ->
            Edge(
                id = "edge_${index + 1}",
                fromNodeId = edge.from.head,
                fromPortId = portName(edge.from),
                toNodeId = edge.to.head,
                toPortId = portName(edge.to),
            )
        }
        return WorkflowGraph(
            id = document.name,
            name = document.name,
            nodes = nodes,
            edges = edges,
            metadata = buildMap {
                put("source", "prorf-dsl-v1")
                if (document.imports.isNotEmpty()) put("imports", document.imports.joinToString(","))
                if (document.outputs.isNotEmpty()) put("outputs", document.outputs.joinToString(","))
                if (document.scenarios.isNotEmpty()) put("scenarios", document.scenarios.joinToString(",") { it.name })
            },
        )
    }

    private fun portName(reference: DslReference): String =
        if (reference.parts.size >= 3 && reference.parts[1] in setOf("input", "output")) {
            reference.parts.drop(2).joinToString(".")
        } else {
            reference.parts.drop(1).joinToString(".")
        }
}
