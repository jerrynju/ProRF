# ADR-012: UI Design Pass 5 — Analysis Screen, Parameter Summaries, Inspector Header

## Status
Done — 2026-06-14

## Context
UI Design Pass 4 added the search-enhanced AddNodeDialog, CanvasToolbar, and ChainListHeader with final-output badge. The prototype images show four navigation tabs (Workflows | Library | Analysis | Settings) and a chain view with parameter summaries per node row. The inspector header used a left stripe pattern that was less visually prominent than the circular icon design shown in the prototype.

## Decisions

### D1 — Analysis Tab (4th navigation item)
Added `Screen.Analysis` to the sealed interface in `ProRfNavHost` and a `BarChart` icon nav item. The `AnalysisScreen` shows:
- Stat cards: saved workflow count, total nodes across all workflows, registered node type count
- Node category bar chart (Source / Active / Passive / Channel / Receiver)
- RF engineering reference card (thermal noise floor, FSPL formula, Friss NF)
- Saved workflow metric rows (name + node + edge counts)

No ViewModel needed — workflow summaries load via `LaunchedEffect` + `withContext(IO)` in the composable.

### D2 — ChainNodeRow parameter summary
`ChainListView` now receives `nodeInstances: List<NodeInstance>` from `WorkflowEditorState.Ready.graph.nodes`. An `instanceMap` is built once via `remember`. `ChainNodeRow` accepts `nodeParams: Map<String, Any>` and calls `buildParamSummary()` to render the first 2 parameters as "G: 25.0  NF: 3.0" below the type label. Common RF parameter keys are mapped to single-letter abbreviations (G, NF, L, P, f, d, BW, SNR, T) in L4 where domain knowledge is allowed.

### D3 — Inspector NodeHeader redesign
Replaced the left-stripe + flat-text pattern with a **circular category icon** (42dp, filled catColor, white abbreviation text) alongside the name/type column. Port count chips (Nin / Nout) now appear inline with the type badge. The description moves to a soft-tinted box below the row, making it visually separate from the editable name field. Unused `IntrinsicSize` and `fillMaxHeight` imports removed.

## Alternatives Considered

- **ViewModel for AnalysisScreen**: Not needed since `WorkflowRepository.list()` is fast and `LaunchedEffect` provides sufficient lifecycle handling.
- **Pass NodeDefinition to ChainNodeRow**: Would allow showing unit symbols (e.g. "dB", "dBm") alongside values. Deferred — requires passing the plugin registry or a definition map into the chain view. `formatSummaryValue()` already appends the unit when the value is a `Quantity`.
- **Keep stripe in Inspector header**: The circular icon matches the prototype and the chip pattern already used in ChainNodeRow, making the visual language consistent.

## Risk
- `withContext(IO)` in `AnalysisScreen` composable works for file I/O but bypasses ViewModel lifecycle awareness. If the files change while the screen is open, the list doesn't refresh. Acceptable for v0.1.
