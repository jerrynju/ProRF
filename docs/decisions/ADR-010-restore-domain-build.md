# ADR-010: Restore RF Domain Build and Green Test Suite

**Date:** 2026-06-13
**Status:** Accepted
**Author:** Claude (automated, per ProRF Agent Build Spec v1.0)

---

## Context

A build verification of the layered (L0–L4) modules revealed that the L3 RF
domain layer did not compile, and consequently its entire test suite — the
spec's mandated self-verification mechanism (Build Spec §3 Step 4, §11
"Definition of Done → Test") — had never been able to run.

Two distinct defects were masking each other:

1. **`Cable.kt` missing import (main source).**
   `Cable.Executor.execute()` called `parameters.gainDb(...)` to read the
   per-connector loss (a `DB` quantity), but the file imported only the
   sibling extension `db` and not `gainDb`. Because all the package-level
   `internal` quantity accessors in `RfQuantityParams.kt` live in
   `com.prorf.domains.rf` while the node lives in
   `com.prorf.domains.rf.nodes`, the reference was unresolved and
   `:domains:rf:compileKotlin` failed.

2. **`Double?` vs `Double` in the test helper (test source).**
   `RfTestValues.outputValue(...)` returns `Double?` (nullable — correct, so
   deletion/edge tests can assert a *missing* output). Eight call sites fed
   that nullable directly into JUnit's
   `assertEquals(double, double, double)`, which requires a non-null
   primitive. These errors only surfaced once defect #1 was fixed and the
   test source finally reached compilation.

## Decision

- Added `import com.prorf.domains.rf.gainDb` to `Cable.kt`. No behavioural
  change — `gainDb` is the dimension-checked `DB` accessor, exactly the
  semantics the connector-loss parameter requires.
- Added a non-null companion helper `requireOutputValue(...)` to
  `RfTestValues.kt` that throws if the output is absent, and switched the
  eight assertion sites that need a guaranteed value to use it.
  `outputValue(...)` stays nullable so node-/edge-deletion tests can still
  assert the *absence* of an output.

## Rationale

- Both fixes are minimal and behaviour-preserving: the production fix is a
  pure import; the test fix keeps the existing nullable contract intact while
  giving assertion sites a non-null path. This honours the spec's "先跑起来"
  (get it running first) and "ONE TASK ONLY" principles.
- No layering boundaries are touched: the import is within L3, and the test
  helper is L3 test-only.

## Alternatives Considered

- **Move connector loss to a generic `db(...)` read:** rejected — `gainDb`
  performs the dimension assertion (`DB`) that the parameter system mandates
  (Build Spec §7), so it is the correct accessor.
- **Make `outputValue` non-null and throw:** rejected — the deletion and
  edge tests legitimately need to observe a missing output as `null`.
- **Append `!!` at each assertion site:** rejected — a named helper documents
  the "must exist" intent and keeps the call sites readable.

## Consequences

- `:domains:rf` now compiles and its full test suite runs.
- Verified green: **43 tests pass** across the pure-JVM modules
  (`:domains:rf` 26, `:engineering` 12, `:platform` 5); `:serialization` and
  `:services` compile with no tests yet.
- The Android-dependent modules (`:ui`, `:apps:prorf-android`) were not
  verified here — no Android SDK is present in this environment. They are
  unaffected by these changes.

## Tests

No new test *cases* added; the change restores the ability to compile and run
the existing 26 RF-domain cases (link-budget chain execution, attenuator,
amplifier, cable, filter, parameter edits, edge/node creation & deletion).

## Follow-ups (not in this task)

- `:serialization` has no tests despite the spec's strong emphasis on
  reproducible/diff-able workflow JSON (Build Spec §8, §11 "Reproducibility").
  A round-trip serialize→deserialize→execute test is the natural next task.
