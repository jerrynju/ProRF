# ADR-002: Engineering Quantity / Unit System

**Date:** 2026-06-13  
**Status:** Accepted  
**Author:** Claude (automated, per ProRF Agent Build Spec v1.0 — M1)

---

## Context

RF calculations rely on many physical quantities: power in dBm, frequency in MHz/GHz, distance in km, noise temperature in Kelvin, gain in dB. The original monolith stores all of these as naked `Double`, making silent unit errors possible (e.g. passing MHz where Hz is expected, or mixing dBm addition with linear-scale multiplication).

The spec mandates: **every value that represents a physical quantity must carry `value + unit + dimension`.**

---

## Decision

Implement a `Quantity(value: Double, unit: PhysicalUnit)` type in `:engineering` (L1).

Key design points:
- `PhysicalUnit` carries `toSi` and `fromSi` lambdas — conversion is explicit and centralised
- `Dimension` enum provides the "what is this measuring" check; `convertTo()` throws at runtime if dimensions mismatch
- Operator `+` / `-` also dimension-check, preventing `power_dBm + gain_dB` at runtime
- Extension constructors (`30.0.dBm()`, `14.0.GHz()`, etc.) keep call sites readable

### Units defined (M1)

| Symbol | Dimension | Notes |
|--------|-----------|-------|
| dBm, W | POWER | dBm ↔ W via log/linear |
| dB, dBi | GAIN | linear dimension (identity SI) |
| Hz, MHz, GHz | FREQUENCY | scale factors |
| m, km | DISTANCE | scale factors |
| K | TEMPERATURE | Kelvin only, no Celsius |

---

## Consequences

**Good:**
- Unit errors become runtime failures rather than silent numerical bugs
- `Quantity.convertTo()` replaces ad-hoc `* 1e6` / `/ 1000` conversions scattered through domain code
- `:domains:rf` nodes receive named Quantity values, improving formula readability

**Accepted costs:**
- RF executor nodes currently pass `Map<String, Any>` (raw Double) at the platform boundary — the Quantity type is available for use in formulas but is not yet enforced at port boundaries (deferred to a future `TypedPort` ADR)
- `dB` and `dBi` share `DIMENSION.GAIN` — may need separate dimensions if antenna gain vs. cable loss comparison becomes necessary

---

## Alternatives Considered

**A: Use JSR-385 Units of Measurement API (javax.measure)**  
Rejected. Adds a heavy dependency to a JVM-only L1 module that may later target KMM; the custom implementation covers all needed RF units with ~100 lines.

**B: Inline unit annotations as strings ("dBm", "MHz")**  
Rejected per spec: "禁止 string-based unit handling".

---

## Test Coverage

`QuantityTest.kt` (`:engineering`) covers: construction, SI conversion, `convertTo`, arithmetic, dimension mismatch throwing.
