package com.prorf.dsl

enum class DslSeverity {
    ERROR,
    WARNING,
}

data class DslDiagnostic(
    val code: String,
    val message: String,
    val severity: DslSeverity = DslSeverity.ERROR,
) {
    override fun toString(): String = "$severity: $code: $message"
}
