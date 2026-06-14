package com.prorf.dsl

import com.prorf.platform.graph.WorkflowGraph

/**
 * Facade for ProRF DSL v1.
 *
 * DSL source is parsed to an AST, validated against a schema registry, then
 * compiled to the platform WorkflowGraph IR. Execution is intentionally outside
 * this module.
 */
class ProRfDsl(
    private val schemaRegistry: DslSchemaRegistry = DslSchemaRegistry.default(),
) {
    fun parse(source: String): WorkflowDocumentAst = DslParser(source).parse()

    fun validate(document: WorkflowDocumentAst): List<DslDiagnostic> =
        DslValidator(schemaRegistry).validate(document)

    fun compile(source: String): WorkflowGraph {
        val document = parse(source)
        val diagnostics = validate(document)
        val errors = diagnostics.filter { it.severity == DslSeverity.ERROR }
        if (errors.isNotEmpty()) {
            throw DslCompilationException(errors)
        }
        return WorkflowGraphCompiler().compile(document)
    }
}

class DslCompilationException(
    val diagnostics: List<DslDiagnostic>,
) : IllegalArgumentException(diagnostics.joinToString(separator = "\n") { it.toString() })
