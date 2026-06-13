# ADR-004: UI Canvas Architecture (M2)

**Date:** 2026-06-13  
**Status:** Accepted  
**Author:** Claude (automated, per ProRF Agent Build Spec v1.0 — M2)

---

## Context

M2 requires a workflow canvas UI in `:ui` (L2). The constraints from the spec:

- L2 must not call computation logic or executor internals
- L2 must not import RF concepts
- Node Card must show: title, parameter summary, status, output summary
- Inspector must show: inputs, parameters, outputs, diagnostics
- UI state must not hold business state

---

## Decision

### Module: `:ui` (Android library, Compose)

The `:ui` module depends only on `:platform` and `:engineering` — never on `:domains:rf`. RF-specific display (node type icons, formula descriptions) is the responsibility of `:apps:prorf-android` or future domain-specific UI packs.

### Component hierarchy

```
WorkflowCanvas          — zoomable/pannable canvas; renders nodes + edge beziers
  └─ NodeCardView       — single node box: title, output summary, status dot
Inspector               — side panel for selected node; reads NodeDefinition + execution outputs
ParameterEditor         — form for editing NodeInstance parameters; type-driven inputs
UiNodeCard (data)       — view-layer model; derived from NodeInstance, adds UI state
```

### State and callbacks

All composables are **stateless** (no ViewModels in `:ui`). State lives in the app shell (`:apps:prorf-android`). Composables receive data and emit callbacks:

```
WorkflowCanvas(nodes, edges, onNodeSelected, onNodeMoved)
Inspector(nodeInstance, definition, executionOutputs, onParameterChanged)
ParameterEditor(parameters, currentValues, onValueChanged)
```

This makes the UI components independently testable with fake data.

### Edge rendering

Edges are rendered as cubic bezier curves on a `Canvas` overlay that sits behind the node cards in `Box` layout. The control point heuristic (50% horizontal interpolation) handles left-to-right and crossed connections without layout computation.

### Node position

`UiNodeCard.x/y` are canvas-space pixel offsets (Float). The app shell is responsible for converting between canvas space and `NodeInstance.position` (which uses the same Float pair but is a platform concern). The UI layer never touches `NodeInstance` directly during drag.

---

## Consequences

**Good:**
- `:ui` can be unit-tested on JVM without an Android emulator (composable previews)
- Adding a new node type requires zero changes to `:ui` — the canvas only needs `displayName` from `NodeDefinition`
- Inspector and ParameterEditor work for any domain that follows the platform node model

**Accepted costs (M2 scope)**:
- No zoom/pan gesture handling yet — canvas is fixed viewport
- No drag-to-connect port wiring (edge creation is manual for now)
- Inspector is read-only for outputs; ParameterEditor requires a separate integration in the app shell to propagate changes back to `NodeInstance`

---

## Alternatives Considered

**A: Custom Canvas-only rendering (no Compose `Box` for nodes)**  
Rejected. Full-canvas approach makes accessibility, text rendering, and touch target sizing harder. Compose `Box` + offset gives native widget behaviour for free.

**B: Put ViewModels in `:ui`**  
Rejected per spec: "UI 不允许保存业务状态" (UI must not hold business state). ViewModels in `:ui` would couple it to Android lifecycle and make it untestable on JVM.
