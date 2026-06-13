# ADR-001: Layered Multi-Module Architecture

**Date:** 2026-06-13  
**Status:** Accepted  
**Author:** Claude (automated, per ProRF Agent Build Spec v1.0)

---

## Context

The original ProRF app (in `android/`) is a monolithic Android project where RF formulas, UI logic, and data models are co-located in `data/RfEngine.kt` and `data/Models.kt`. This worked for the MVP but creates several problems for the stated long-term goal:

> "一个'工程工作流计算平台内核 + RF领域插件'的可扩展系统"  
> (A scalable system: "engineering workflow compute platform core + RF domain plugin")

Problems with the monolith:
- RF formulas are a single `object RfEngine` — untestable in isolation, impossible to extend with new node types without editing core logic
- No unit system — all values are naked `Double`, making unit errors silent
- No plugin boundary — adding a microwave domain would require forking the app
- UI logic and computation are interleaved, blocking future domain-agnostic canvas

---

## Decision

Restructure the project as a Gradle multi-module build with five hard layers:

| Layer | Module | Rule |
|-------|--------|------|
| L0 | `:platform` | No RF, no Android, no UI. Only: graph model, execution engine, plugin registry |
| L1 | `:engineering` | Typed quantity system (value + unit + dimension). No RF terms |
| L2 | `:ui` | Compose UI only. No domain logic, no execution internals |
| L3 | `:domains:rf` | RF node definitions + executors. Depends only on L0 + L1 |
| L4 | `:apps:prorf-android` | Android shell. Wires L0–L3 together. Dependency arrow: L4 → L3 → L1 → L0 |

Supporting modules:
- `:services` — `CapabilityService` for feature gating (replaces `if (isProUser)`)
- `:serialization` — stable `WorkflowDocument` JSON format with migration hooks
- `:build-logic` — Gradle convention plugins for consistent module setup

### Key design rules (enforced by module boundaries):
- `:platform` cannot import `:domains:rf` or any Android class
- `:domains:rf` cannot import `:ui` or any Compose class
- UI composables cannot call executor logic directly
- All feature gating goes through `CapabilityService.has(id)`, never inline booleans

---

## Consequences

**Good:**
- New domains (microwave, optical, power) = new `:domains:X` module, zero changes to platform
- RF computation is unit-testable without Android emulator (pure JVM)
- `Quantity(value, unit)` makes unit errors a compile-time/runtime failure, not silent bugs
- `WorkflowDocument.schemaVersion` + `migrate()` enables safe file format evolution
- `CapabilityService` decouples subscription state from business logic

**Accepted costs:**
- More files and build scripts than the monolith
- Existing `android/` monolith is not deleted — it is preserved as the shipping app until M4 wires the new modules into `apps/prorf-android/`

---

## Alternatives Considered

**A: Keep monolith, add interfaces inside it**  
Rejected. Gradle module boundaries are enforced at compile time; package-level conventions are not. Without a hard boundary, L0/L3 co-mingling always resurfaces under time pressure.

**B: Feature modules per screen (Home, Editor, Results)**  
Rejected. Screen decomposition is a UI concern; the spec requires domain decomposition. The two can coexist but domain modules must come first.

**C: Immediate rewrite of `android/` into `apps/prorf-android/`**  
Deferred to M4. Moving working code before the new modules are stable risks regression with no safety net. The monolith stays functional while the platform is built.

---

## Migration Path (Milestones)

```
M0: Platform Skeleton   → :platform, :build-logic            (this ADR)
M1: Engineering         → :engineering Quantity/Unit system
M2: UI Canvas MVP       → :ui WorkflowCanvas, Inspector
M3: RF Domain MVP       → :domains:rf 5 core nodes, chain execution
M4: Productization      → :apps:prorf-android wired up, android/ deprecated
```

All M0 + M3 work is complete as of this ADR.
