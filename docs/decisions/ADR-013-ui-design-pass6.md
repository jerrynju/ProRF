# ADR-013: UI Design Pass 6 — Chain Flow Indicators, Inspector Summary & Canvas Improvements

**Date:** 2026-06-14  
**Status:** Implemented  
**Scope:** `ui/`, `apps/prorf-android/ui/`

---

## Context

Following UI Design Passes 1–5, the core screens (chain list, canvas, inspector, library, analysis) were functional but had several visual gaps compared to the target design:

1. ChainConnector between chain-list rows was a plain "↓" text — no visual weight, no data context.
2. Inspector panel showed computed results only under a Results tab — key output value required tapping away from Params.
3. Canvas edges were uniformly primary-blue regardless of the source/target node categories.
4. Empty canvas workflows showed nothing, confusing new users.
5. `nodeTypeColor()` in Inspector used case-sensitive matching (`contains("signal")`) which silently failed for capitalized typeIds like `"SignalSource"`, making all category icons render gray.

---

## Decisions

### D1 — ChainConnector: Canvas arrow + flowing value

Replace the "↓" `Text` with a Canvas-drawn vertical line + chevron arrowhead. Optionally display the source node's primary output value beside the arrow.

**Why:** Matches the mobile design reference image (chain connector with signal level labels). The flowing value shows data movement without requiring the user to open the Inspector.

**Trade-off:** The flowing value duplicates what ChainNodeRow already shows on the right side. Kept because the inline-connector placement makes the flow direction clearer.

### D2 — Inspector: QuickSummaryBanner

Insert a tinted banner between the NodeHeader and the tab row when `executionOutputs` is non-empty. Shows the primary output port's name (from `NodeDefinition.outputs`) and formatted value in `titleMedium` bold.

**Why:** The key result was buried under the "Results" tab. For typical usage (inspect a node → check output), the banner surfaces the answer without a tab switch. The tint uses the category color (blue/green/orange/purple/red) to reinforce node identity.

**Trade-off:** Adds vertical height to the Inspector header area. Acceptable because the panel is 280dp wide with vertical scroll; the tab body still has ample room.

### D3 — WorkflowCanvas: Category-colored edges

Each edge now derives its color from the source node's category (via `edgeNodeColor(from.typeId)`) and the arrowhead uses the destination category color.

**Why:** Uniform blue edges made all data flows look identical. Category-colored edges make it visually obvious what *kind* of data is flowing (signal source output = blue, amplifier output = green, etc.).

**Trade-off:** `edgeNodeColor()` is duplicated from `NodeCardView.categoryColor()` (private). This is acceptable as both are in the L2 `ui` module and the logic is trivially simple. A future refactor could move it to a shared `ui/util/` location.

### D4 — Empty Canvas Overlay

When the workflow has no nodes, a centered overlay (circle icon + title + hint text) renders above the canvas grid.

**Why:** New users seeing a blank dot grid with no prompt is confusing. The overlay echoes the WorkflowListScreen empty state pattern.

### D5 — Bug fix: `nodeTypeColor()` case-insensitive matching

Changed all `contains("signal")` calls to `contains("Signal", ignoreCase = true)` in `Inspector.kt#nodeTypeColor`.

**Why:** Kotlin `String.contains()` is case-sensitive. TypeIds like `"SignalSource"` → `"SignalSource".contains("signal")` = `false` because of 'S' vs 's'. All nodes were rendering the fallback gray color (#64748B) in the Inspector circular icon and QuickSummaryBanner.

---

## Alternatives Considered

- **Edge value labels directly on canvas:** Drawing text at bezier midpoints requires `drawIntoCanvas` + native `android.graphics.Paint`, bypassing the Material theme. Deferred to a future pass.
- **Gradient strokes on edges:** Compose `drawPath` doesn't support gradient strokes natively; would need multiple path segments. Not worth the complexity over flat category colors.
- **Theme toggle (dark/light):** Requires lifting state from `ProRfTheme` through `MainActivity` into `SettingsScreen`. Deferred; the theme already reads `isSystemInDarkTheme()`.

---

## Files Changed

| File | Change |
|------|--------|
| `apps/prorf-android/ui/WorkflowEditorScreen.kt` | ChainConnector → Canvas arrow + flowingValue; empty canvas overlay; new imports (Path, StrokeCap, StrokeJoin, Stroke, AccountTree) |
| `ui/inspector/Inspector.kt` | QuickSummaryBanner composable; call site in Inspector body; `nodeTypeColor` ignoreCase fix |
| `ui/canvas/WorkflowCanvas.kt` | `edgeNodeColor()` helper; category-colored edge + arrowhead; removed unused `edgeColor` variable |
