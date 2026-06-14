plugins {
    id("prorf.android-app")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.prorf.app"
    buildFeatures {
        compose = true
    }
    defaultConfig {
        applicationId = "com.prorf.app"
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation(project(":platform"))
    implementation(project(":engineering"))
    implementation(project(":domains:rf"))
    implementation(project(":services"))
    implementation(project(":serialization"))
    implementation(project(":dsl"))
    implementation(project(":ui"))
}
