package com.prorf.domains.rf

import com.prorf.domains.rf.nodes.Amplifier
import com.prorf.domains.rf.nodes.Attenuator
import com.prorf.domains.rf.nodes.Cable
import com.prorf.domains.rf.nodes.Filter
import com.prorf.domains.rf.nodes.FreeSpacePathLoss
import com.prorf.domains.rf.nodes.NoiseSource
import com.prorf.domains.rf.nodes.Receiver
import com.prorf.domains.rf.nodes.SignalSource
import com.prorf.platform.plugin.PluginRegistry

/**
 * L3 RF Domain Pack entry point.
 * Call register() once at app startup to make all RF nodes available
 * to the platform execution engine.
 */
object RfDomainPlugin {
    fun register(registry: PluginRegistry) {
        // Source
        registry.register(SignalSource.definition, SignalSource.Executor())
        registry.register(NoiseSource.definition, NoiseSource.Executor())
        // Passive
        registry.register(Attenuator.definition, Attenuator.Executor())
        registry.register(Cable.definition, Cable.Executor())
        registry.register(Filter.definition, Filter.Executor())
        // Active
        registry.register(Amplifier.definition, Amplifier.Executor())
        // Channel
        registry.register(FreeSpacePathLoss.definition, FreeSpacePathLoss.Executor())
        // Receiver
        registry.register(Receiver.definition, Receiver.Executor())
    }
}
