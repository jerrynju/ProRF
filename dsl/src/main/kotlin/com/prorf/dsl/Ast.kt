package com.prorf.dsl

data class WorkflowDocumentAst(
    val name: String,
    val imports: List<String> = emptyList(),
    val variables: List<DslAssignment> = emptyList(),
    val nodes: List<DslNode> = emptyList(),
    val edges: List<DslEdge> = emptyList(),
    val scenarios: List<DslScenario> = emptyList(),
    val outputs: List<DslReference> = emptyList(),
)

data class DslNode(
    val id: String,
    val type: String,
    val parameters: List<DslAssignment> = emptyList(),
)

data class DslAssignment(
    val path: DslReference,
    val expression: DslExpression,
)

data class DslEdge(
    val from: DslReference,
    val to: DslReference,
)

data class DslScenario(
    val name: String,
    val overrides: List<DslAssignment> = emptyList(),
)

data class DslReference(val parts: List<String>) {
    init {
        require(parts.isNotEmpty()) { "Reference must contain at least one part." }
    }

    val head: String get() = parts.first()
    override fun toString(): String = parts.joinToString(".")
}

data class DslExpression(val text: String) {
    val quantity: DslQuantity? = parseQuantity(text)

    companion object {
        private val quantityPattern = Regex("""^([-+]?\d+(?:\.\d+)?)\s+([A-Za-z][A-Za-z0-9/_]*)$""")

        private fun parseQuantity(text: String): DslQuantity? {
            val match = quantityPattern.matchEntire(text.trim()) ?: return null
            return DslQuantity(match.groupValues[1].toDouble(), match.groupValues[2])
        }
    }
}

data class DslQuantity(val value: Double, val unit: String)
