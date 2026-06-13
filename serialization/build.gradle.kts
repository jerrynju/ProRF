plugins {
    id("prorf.kotlin-library")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":platform"))
    implementation(project(":engineering"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
