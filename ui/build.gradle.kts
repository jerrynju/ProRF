plugins {
    id("prorf.android-library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.prorf.ui"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation(project(":platform"))
    implementation(project(":engineering"))
}
