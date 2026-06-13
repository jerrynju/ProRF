pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ProRF-Platform"

// L0 — Platform Core (no RF, no Android)
include(":platform")

// L1 — Engineering Foundation
include(":engineering")

// L2 — UI System (Compose, no business logic)
include(":ui")

// L3 — RF Domain Pack
include(":domains:rf")

// Services (Capability gating)
include(":services")

// Serialization (workflow file format)
include(":serialization")

// L4 — Android App Shell
include(":apps:prorf-android")  // Scaffolded in M4; existing monolith at android/ until full migration
