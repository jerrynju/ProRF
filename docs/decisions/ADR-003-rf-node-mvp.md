# ADR-003: RF Domain MVP — Node Set and Execution Model

**Date:** 2026-06-13  
**Status:** Accepted  
**Author:** Claude (automated, per ProRF Agent Build Spec v1.0 — M3)

---

## Context

M3 requires a minimal but complete set of RF nodes capable of running a realistic link budget chain (transmitter → propagation → receiver). The nodes must follow the L3 domain rules:
- `NodeDefinition` = schema only (typeId, display name, ports, parameter types)
- `NodeExecutor` = pure computation (`Map<String, Any>` in → `Map<String, Any>` out)
- No UI logic, no Android imports

---

## Decision

### Implemented nodes (M3 complete)

| Node | TYPE_ID | Category |
|------|---------|----------|
| SignalSource | `rf.signal_source` | Source |
| NoiseSource | `rf.noise_source` | Source |
| Amplifier | `rf.amplifier` | Active |
| Attenuator | `rf.attenuator` | Passive |
| Cable | `rf.cable` | Passive |
| Filter | `rf.filter` | Passive |
| FreeSpacePathLoss | `rf.fspl` | Channel |
| Receiver | `rf.receiver` | Receiver |

### Key implementation decisions

**NoiseSource:** outputs thermal noise power P = kTB (dBm) from noise temperature (K) and bandwidth (MHz). Uses Boltzmann constant `k = 1.380649e-23 J/K`. This matches the Receiver's internal noise floor calculation, making the two composable.

**Cable:** parameterised by loss-per-meter + length + connector count/loss. Enables accurate multi-section coax modeling vs. a single lump-sum Attenuator. The Attenuator remains useful for general loss budgeting.

**Filter:** MVP only models in-band insertion loss. Out-of-band rejection (stopband, transition band) is not computed — the parameter `centerFreqMHz` is retained as metadata for future spectrum-aware execution.

**Receiver margin computation:** `margin = receivedPower - sensitivity` where `sensitivity = noiseFloor(kTB) + NF`. This is the standard link budget close-out metric.

### Port convention

All RF signal ports use the `RfPort` constants (`rf_in`, `rf_out`) from the shared port file. This prevents typos and enables the platform edge system to validate port connections.

---

## Consequences

**Good:**
- Full Ku-band GEO satellite link budget chain is executable: `RfChainExecutionTest` passes
- All 8 nodes are registered via `RfDomainPlugin.register()` — zero wiring changes needed in L4

**Deferred:**
- Noise Figure cascading (Friis formula) across a multi-stage chain — currently each node reports its own NF independently
- Frequency-dependent cable loss — `lossPerMeterDb` is currently frequency-independent
- Sensitivity threshold (required SNR/Eb/No) — Receiver outputs raw noise floor, not a minimum decodable signal level

---

## Alternatives Considered

**A: Implement Friis cascade in a dedicated ChainAnalyzer executor**  
Deferred to M3+. Requires multi-output chain aggregation that the DAG engine supports but no UI yet exists to display it.

**B: Use Quantity type at port boundaries**  
Deferred. Platform `Map<String, Any>` keeps L0 dependency-free; a TypedPort wrapper could enforce this without changing the core API in a future ADR.
