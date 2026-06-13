# ADR-005: M4 Workflow Persistence and Template System

**Date:** 2026-06-13  
**Status:** Accepted  
**Context:** M4 Productization — adding persistence, templates, and ViewModel wiring.

---

## Context

M0–M3 established the platform kernel (DAG engine, engineering units, UI canvas, RF domain nodes).
M4 completes the product loop: a user must be able to open the app, load a workflow, run it, and see results.

Three capabilities are needed:
1. **Persistence** — workflows survive app restart
2. **Templates** — pre-built RF chains so new users see value immediately
3. **ViewModel wiring** — connect platform execution to Compose UI

---

## Decisions

### 1. File-based persistence (JSON, one file per workflow)

- **Format:** `<filesDir>/workflows/<uuid>.json` using the existing `WorkflowSerializer`
- **Name storage:** `graph.metadata["name"]` — the serialization format already supports `metadata`; no schema change needed
- **Alternatives considered:**
  - Room/SQLite — too heavy for M4; adds a compile-time dependency with no query benefit (we always load full graphs)
  - Single monolithic JSON — harder to delete, rename, and diff individual workflows

### 2. Template materialization on first open

Templates (IDs prefixed `template:`) are immutable `WorkflowGraph` constants in `domains/rf/WorkflowTemplates`.
When a user opens a template for the first time, the editor materializes a UUID-keyed copy and saves it.  
Subsequent opens use the saved copy.

**Alternative considered:** keep templates virtual (never save). Rejected because it would prevent users from saving edits to a template-derived workflow.

### 3. AndroidViewModel + ViewModelProvider.Factory for workflowId

Navigation passes `workflowId: String` to `WorkflowEditorScreen`. ViewModels cannot take constructor
parameters from the default factory, so a `Factory` inner class is used. No Hilt or other DI framework
was introduced — M4 scope is too narrow to justify it.

### 4. Capability gating preserved

`CapabilityService` is instantiated in `ProRfApp` (currently `FreeCapabilityService` — all free-tier
capabilities unlocked). The editor does not call `CapabilityService` directly; future save/sweep/export
features will gate through the service before executing.

### 5. Auto-save on move/add-node; explicit run

Node positions are saved immediately when dragged (auto-save in ViewModel). Execution is user-triggered
via the FAB. This follows the "canvas = source of truth, engine = on-demand" model.

---

## Layer compliance

| Layer | Violated? | Notes |
|-------|-----------|-------|
| L0 platform — no RF concepts | No | DagExecutionEngine unchanged |
| L1 engineering — no UI | No | Quantity/PhysicalUnit untouched |
| L2 ui — no business logic | No | Inspector only receives outputs; does not call executors |
| L3 domains/rf — no Android | No | WorkflowTemplates is pure Kotlin |
| L4 app shell — no domain logic | No | ViewModel accesses PluginRegistry via ProRfApp |

---

## Next steps (M5)

- Parameter editing in Inspector (live re-run on change)
- Edge creation UI on the canvas
- Export to PDF (gated by `Capability.EXPORT_PDF`)
- SubscriptionCapabilityService backed by Play Billing
