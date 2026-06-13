# ADR-010: Build Toolchain Portability + Latent Compile Fixes

**Date:** 2026-06-13
**Status:** Accepted
**Author:** Claude (automated, per ProRF Agent Build Spec v1.0)

---

## Context

The layered multi-module restructure (ADR-001) was committed, but the core JVM
modules (L0 `:platform`, L1 `:engineering`, L3 `:domains:rf`, plus `:services`
and `:serialization`) had **never actually been compiled in a clean
environment**. Attempting a build surfaced three blocking problems, in order:

1. **Toolchain hard-pin.** `build-logic/.../prorf.kotlin-library.gradle.kts`
   declared `kotlin { jvmToolchain(17) }`. This forces Gradle to locate (or
   auto-provision) a *JDK 17 specifically*. On a host that ships only JDK 21
   with no toolchain download repository configured, the build fails before
   compiling a single file:

   > Cannot find a Java installation … matching {languageVersion=17}.
   > Toolchain download repositories have not been configured.

2. **Real compile bug, previously masked.** Once compilation could proceed,
   `domains/rf/.../nodes/Cable.kt` failed: it calls
   `parameters.gainDb("connectorLossDb", 0.5)` but never imported the `gainDb`
   helper (every sibling node imports the helpers it uses). The toolchain
   failure had been hiding this genuine error.

3. **Latent test compile errors.** Eight `assertEquals(expected, actual, delta)`
   call sites across `RfChainExecutionTest`, `NewNodeTest`, and
   `ParameterEditTest` passed the `Double?` return of the `outputValue(...)`
   test helper into JUnit's primitive `assertEquals(double, double, double)`
   overload. Every *other* call site in the suite already used `!!`; these eight
   simply omitted it.

Net effect: nothing in the core stack could build or test. This blocks the
spec's Definition of Done (Test: unit test passed; Computational: execution
output valid) for the entire platform.

---

## Decision

**1. Target Java 17 bytecode without pinning a separate JDK 17 toolchain.**
Replace the strict toolchain with explicit compiler/Java targets:

```kotlin
kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

This produces identical Java 17 bytecode but compiles with *any* JDK ≥ 17 that
runs the build, so the modules build in CI, on contributor machines, and in
ephemeral cloud containers without a toolchain-provisioning network round-trip.
It also removes the deprecated `kotlinOptions.jvmTarget` warning path.

**2. Add the missing `import com.prorf.domains.rf.gainDb` to `Cable.kt`** —
`connectorLossDb` is a `DB` quantity, so `gainDb` is the correct reader helper.

**3. Add the omitted `!!` to the eight failing test assertions**, matching the
convention every other call site already follows. `outputValue` legitimately
returns `Double?` (a map lookup can miss); callers assert non-null at the point
of comparison.

---

## Verification

```
gradle :platform:test :engineering:test :domains:rf:test \
       :serialization:build :services:build
→ BUILD SUCCESSFUL
```

Test totals (JUnit 5): **43 tests, 0 failures, 0 errors**
- platform: 5 · engineering: 12 · domains:rf: 26 (5+7+5+4+4+... across 6 suites)

The Android modules (L2 `:ui`, L4 `:apps:prorf-android`) require an Android SDK
that is not present in this build environment; they are intentionally out of
scope here. Their convention plugins use AGP's own `compileOptions` /
`kotlinOptions` and do **not** force a host JDK toolchain, so they are
unaffected by change (1).

---

## Alternatives Considered

**A. Configure the Foojay toolchain resolver and keep `jvmToolchain(17)`.**
Rejected as the default. It keeps an exact-JDK pin but adds a settings plugin
and forces every clean build to download a full JDK 17 over the network — heavy
for ephemeral containers and a new failure surface when the network policy is
restrictive. The chosen approach has zero network dependency.

**B. Change the pin to `jvmToolchain(21)`.**
Rejected. It would silently raise the bytecode floor to Java 21, narrowing the
set of JDKs that can *run* the artifacts, with no benefit. The project targets
17.

**C. Make `outputValue` return non-null `Double` (throw on miss).**
Reasonable, but it would leave ~12 now-redundant `!!` operators across the suite
(unnecessary-non-null warnings) and changes a helper's contract to fix a
caller-side omission. Adding `!!` at the eight sites is the smaller, more
consistent change.

---

## Risks

- The `java {}` block assumes the Kotlin JVM plugin applies the Java plugin
  (it does). Confirmed by a green build.
- Bytecode target is unchanged (17), so no runtime-compatibility risk versus the
  previous intent.
