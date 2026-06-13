package com.prorf.app

import android.app.Application
import com.prorf.domains.rf.RfDomainPlugin
import com.prorf.platform.plugin.PluginRegistry

/**
 * L4 App Shell — Android Application class.
 * Bootstraps the platform: registers domain plugins, configures capability service.
 * Single entry point for all startup wiring.
 */
class ProRfApp : Application() {

    lateinit var pluginRegistry: PluginRegistry
        private set

    override fun onCreate() {
        super.onCreate()
        pluginRegistry = PluginRegistry().also { registry ->
            RfDomainPlugin.register(registry)
        }
    }
}
