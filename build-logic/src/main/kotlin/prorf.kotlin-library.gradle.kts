import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

// Target Java 17 bytecode, compiled by whatever JDK (>=17) runs the build.
// We intentionally do NOT pin a strict jvmToolchain(17): that forces Gradle to
// locate/download a JDK 17 even when a newer, compatible JDK is already present,
// which breaks builds in environments without toolchain auto-provisioning.
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
