# ADR-010: RF Quantity Parameter Boundary

**Date:** 2026-06-13  
**Status:** Accepted  
**Context:** Build Spec v1.0 section 7 requires every physical parameter to carry value + unit + dimension.

---

## Context

The engineering module already had `Quantity`, `PhysicalUnit`, and `Dimension`, but RF nodes and templates still passed physical values as naked `Double` values in `NodeInstance.parameters`. That left the most important execution path open to silent unit mistakes.

This was a spec violation because RF parameters such as transmit power, gain, frequency, bandwidth, distance, and temperature must be explicit quantities.

---

## Decision

Introduce a strict RF domain boundary for parameter access:

- RF node definitions now use `ParameterDefinition(..., dataType = "quantity", defaultValue = Quantity(...))`.
- RF executors read parameters through RF-domain helpers such as `powerDbm`, `gainDb`, `frequencyMhz`, `distanceKm`, and `bandwidthMhz`.
- RF executors return `Quantity` outputs for RF power, gains/losses, sensitivity, margin, and noise floor.
- RF workflow templates now materialize `Quantity` parameters instead of numeric literals.
- Workflow serialization encodes `Quantity` values with value, unit symbol, and dimension, then restores them during deserialization.
- The generic UI parameter editor displays a quantity's numeric value and preserves its original unit when edited.

The platform engine remains domain-neutral. It still transports `Map<String, Any>` and does not import RF or engineering semantics.

---

## Alternatives Considered

**A: Move typed ports and typed parameters into `:platform` now**  
Rejected for this increment. It would broaden the task into a platform redesign. The current change gives RF execution a strict boundary while preserving the existing platform abstraction.

**B: Keep accepting `Number` and infer units in RF nodes**  
Rejected. That preserves the exact class of bug the spec is trying to prevent.

**C: Serialize quantities as nested JSON objects immediately**  
Deferred. The current schema stores parameters as string values, so a stable `q:value|unit|dimension` encoding gives a smaller compatible step. A future schema version can move to structured parameter records.

---

## Risks

- Existing persisted workflows with old naked numeric parameters can still deserialize, but RF execution will reject them until a migration maps legacy keys to expected units.
- The `Map<String, Any>` platform boundary still cannot enforce typed values at compile time.
- `dB/m` is represented as a simple engineering unit for MVP formulas, not a fully compositional unit algebra.

---

## Verification

RF tests were updated to construct `Quantity` parameters and assert `Quantity` outputs. Gradle execution in this environment was blocked before task output: Java was only available through Android Studio JBR, and Gradle tasks repeatedly timed out without producing build logs. Static checks found no remaining RF `ParameterDefinition` entries using `double` and no RF executor casts from `Number`.
