package com.prorf.services.capability

/**
 * All feature gating must go through this interface.
 * Never write `if (isProUser)` — always `capabilityService.has(Capability.X)`.
 * This decouples business logic from subscription state, enabling easy A/B testing
 * and capability override during development.
 */
interface CapabilityService {
    fun has(capabilityId: String): Boolean
}

/**
 * Capability identifiers — single source of truth for feature gating.
 * Free-tier capabilities are listed in FREE_TIER.
 */
object Capability {
    const val BASIC_SIMULATION = "core.basic_simulation"
    const val SAVE_WORKFLOW = "core.save_workflow"
    const val TEMPLATE_LIBRARY = "core.template_library"
    const val SWEEP = "core.sweep"
    const val MONTE_CARLO = "core.monte_carlo"
    const val EXPORT_PDF = "export.pdf"
    const val EXPORT_EXCEL = "export.excel"
    const val MULTI_DOMAIN = "core.multi_domain"

    val FREE_TIER = setOf(
        BASIC_SIMULATION,
        SAVE_WORKFLOW,
        TEMPLATE_LIBRARY,
    )
}

/** Free-tier implementation — no subscription check needed. */
class FreeCapabilityService : CapabilityService {
    override fun has(capabilityId: String): Boolean = capabilityId in Capability.FREE_TIER
}

/** Development override — all capabilities unlocked. Never ship this as default. */
class UnlockedCapabilityService : CapabilityService {
    override fun has(capabilityId: String): Boolean = true
}
