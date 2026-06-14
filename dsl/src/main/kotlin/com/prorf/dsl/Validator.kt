package com.prorf.dsl

class DslValidator(
    private val schemaRegistry: DslSchemaRegistry,
) {
    fun validate(document: WorkflowDocumentAst): List<DslDiagnostic> = buildList {
        val nodeIds = document.nodes.map { it.id }
        val duplicateNodeIds = nodeIds.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        duplicateNodeIds.forEach {
            add(error("DUPLICATE_NODE", "Node '$it' is declared more than once."))
        }

        val nodeById = document.nodes.associateBy { it.id }
        val variableDimensions = document.variables.mapNotNull { assignment ->
            val dimension = inferExpressionDimension(assignment.expression, emptyMap())
            if (dimension != null) assignment.path.toString() to dimension else null
        }.toMap()

        document.variables.forEach { assignment ->
            val expression = assignment.expression
            val quantity = expression.quantity
            if (quantity != null && DslUnits.dimensionOf(quantity.unit) == null) {
                add(error("UNKNOWN_UNIT", "Variable '${assignment.path}' uses unknown unit '${quantity.unit}'."))
            }
        }

        document.nodes.forEach { node ->
            val schema = schemaRegistry.findNode(node.type)
            if (schema == null) {
                add(error("UNKNOWN_NODE_TYPE", "Node '${node.id}' uses unknown type '${node.type}'."))
                return@forEach
            }
            node.parameters.forEach { assignment ->
                val key = assignment.path.toString()
                val parameter = schema.parameters[key]
                if (parameter == null) {
                    add(error("UNKNOWN_PARAMETER", "Node '${node.id}' has no parameter '$key'."))
                } else {
                    validateExpressionDimension(
                        assignment.expression,
                        parameter.dimension,
                        "Parameter '${node.id}.$key'",
                        variableDimensions,
                    )?.let(::add)
                }
            }
        }

        document.edges.forEach { edge ->
            val fromPort = resolvePort(edge.from, nodeById, isOutput = true, endpoint = "source")
            val toPort = resolvePort(edge.to, nodeById, isOutput = false, endpoint = "target")
            fromPort.diagnostic?.let(::add)
            toPort.diagnostic?.let(::add)
            if (fromPort.dimension != null && toPort.dimension != null && fromPort.dimension != toPort.dimension) {
                add(error("PORT_DIMENSION_MISMATCH", "Cannot connect ${edge.from} (${fromPort.dimension}) to ${edge.to} (${toPort.dimension})."))
            }
        }

        document.scenarios.forEach { scenario ->
            scenario.overrides.forEach { override ->
                validateOverride(override, document, nodeById, variableDimensions)?.let(::add)
            }
        }

        document.outputs.forEach { output ->
            val resolved = resolvePort(output, nodeById, isOutput = true, endpoint = "output")
            resolved.diagnostic?.let(::add)
        }

        addAll(validateDag(document.edges))
    }

    private fun validateExpressionDimension(
        expression: DslExpression,
        expected: DslDimension,
        label: String,
        variableDimensions: Map<String, DslDimension>,
    ): DslDiagnostic? {
        val quantity = expression.quantity
        if (quantity != null && DslUnits.dimensionOf(quantity.unit) == null) {
            return error("UNKNOWN_UNIT", "$label uses unknown unit '${quantity.unit}'.")
        }
        val actual = inferExpressionDimension(expression, variableDimensions) ?: return null
        return if (actual != expected) {
            error("UNIT_MISMATCH", "$label expects $expected but got $actual from '${expression.text}'.")
        } else {
            null
        }
    }

    private fun inferExpressionDimension(
        expression: DslExpression,
        variableDimensions: Map<String, DslDimension>,
    ): DslDimension? {
        val quantity = expression.quantity
        if (quantity != null) {
            return DslUnits.dimensionOf(quantity.unit)
        }
        return variableDimensions[expression.text]
    }

    private fun validateOverride(
        override: DslAssignment,
        document: WorkflowDocumentAst,
        nodeById: Map<String, DslNode>,
        variableDimensions: Map<String, DslDimension>,
    ): DslDiagnostic? {
        if (override.path.parts.size == 1) {
            val variable = document.variables.firstOrNull { it.path.toString() == override.path.toString() }
                ?: return error("UNKNOWN_OVERRIDE_TARGET", "Scenario override '${override.path}' does not match a variable.")
            val expected = inferExpressionDimension(variable.expression, variableDimensions) ?: return null
            return validateExpressionDimension(override.expression, expected, "Scenario override '${override.path}'", variableDimensions)
        }

        val node = nodeById[override.path.head]
            ?: return error("UNKNOWN_OVERRIDE_TARGET", "Scenario override '${override.path}' references unknown node '${override.path.head}'.")
        val schema = schemaRegistry.findNode(node.type) ?: return null
        val parameterName = override.path.parts.drop(1).joinToString(".")
        val parameter = schema.parameters[parameterName]
            ?: return error("UNKNOWN_PARAMETER", "Scenario override '${override.path}' references unknown parameter '$parameterName'.")
        return validateExpressionDimension(override.expression, parameter.dimension, "Scenario override '${override.path}'", variableDimensions)
    }

    private fun resolvePort(
        reference: DslReference,
        nodeById: Map<String, DslNode>,
        isOutput: Boolean,
        endpoint: String,
    ): ResolvedPort {
        val node = nodeById[reference.head]
            ?: return ResolvedPort(null, error("UNKNOWN_NODE", "Edge $endpoint '$reference' references unknown node '${reference.head}'."))
        val schema = schemaRegistry.findNode(node.type)
            ?: return ResolvedPort(null, null)
        val portName = when {
            reference.parts.size >= 3 && reference.parts[1] in setOf("input", "output") ->
                reference.parts.drop(2).joinToString(".")
            reference.parts.size >= 2 -> reference.parts.drop(1).joinToString(".")
            else -> return ResolvedPort(null, error("INVALID_PORT_REFERENCE", "Port reference '$reference' must include a port name."))
        }
        val schemaPortName = portName.replace(Regex("""\[\d+]"""), "")
        val port = if (isOutput) schema.outputs[schemaPortName] else schema.inputs[schemaPortName]
        return if (port == null) {
            val direction = if (isOutput) "output" else "input"
            ResolvedPort(null, error("UNKNOWN_PORT", "Node '${node.id}' has no $direction port '$portName'."))
        } else {
            ResolvedPort(port.dimension, null)
        }
    }

    private fun validateDag(edges: List<DslEdge>): List<DslDiagnostic> {
        val adjacency = edges.groupBy({ it.from.head }, { it.to.head })
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()

        fun dfs(node: String): Boolean {
            if (node in visiting) return true
            if (node in visited) return false
            visiting += node
            adjacency[node].orEmpty().forEach { next ->
                if (dfs(next)) return true
            }
            visiting -= node
            visited += node
            return false
        }

        return adjacency.keys
            .firstOrNull { dfs(it) }
            ?.let { listOf(error("GRAPH_CYCLE", "Workflow graph must be a DAG; cycle detected near '$it'.")) }
            .orEmpty()
    }

    private data class ResolvedPort(
        val dimension: DslDimension?,
        val diagnostic: DslDiagnostic?,
    )

    private fun error(code: String, message: String): DslDiagnostic =
        DslDiagnostic(code, message, DslSeverity.ERROR)
}
