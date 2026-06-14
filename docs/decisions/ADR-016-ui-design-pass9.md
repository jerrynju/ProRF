# ADR-016: UI Design Pass 9 — Canvas Zoom, Workflow Rename, Library Grid, Node Type Labels, Gradient Bars

**Date:** 2026-06-14  
**Status:** Accepted  
**Layer affected:** L2 (UI), L4 (App Shell)

---

## Context

Pass 9 addresses usability gaps surfaced from using the canvas with many nodes and from reference UI prototype review:

1. The canvas has zoom placeholder buttons that did nothing — operators needed zoom to inspect crowded diagrams.
2. Workflow names are set at creation but cannot be changed — common need during iterative design.
3. The Library screen's grouped list is information-dense but hard to scan quickly — a compact grid mode helps discovery.
4. Node cards in idle state showed a cryptic 3-char abbreviation ("SRC", "AMP") that adds friction to understanding a new workflow.
5. Analysis bar chart fills were flat colors — gradient fills add depth and visual hierarchy.

---

## Decisions

### 1. Canvas zoom via `scale: Float` parameter

**Decision:** Thread a `scale: Float = 1f` parameter through `WorkflowCanvas`. All pixel coordinate calculations multiply by `s = scale.coerceIn(0.4f, 2.5f)`. Node card visuals use `graphicsLayer { scaleX=sc; scaleY=sc; transformOrigin=TransformOrigin(0f,0f) }` (scale from top-left). Drag deltas are divided by `sc` to keep stored positions in unscaled dp space.

**Alternatives considered:**
- Compose `graphicsLayer` on the entire canvas — would scale the whole Box including the pan gesture target; interaction breaks.
- Canvas-level transform matrix — more mathematically correct but requires rewriting all hit detection.
- Pinch-to-zoom gesture — ideal long-term; deferred because it requires coordinating with the pan gesture and there's no `detectTransformGestures` composable as simple as detectDrag.

**Known tradeoff:** Tap hit-detection area stays at original layout size while visuals scale. At 2x zoom, the tappable area appears smaller than the rendered card. Acceptable for toolbar-button-only zoom (not yet pinch); to be revisited when pinch zoom is added.

**Zoom bounds:** 0.4x–2.5x. Grid dots clamp radius to 0.6–1.4x so they remain visible but not overwhelming at extremes.

### 2. Workflow rename via ViewModel + dialog

**Decision:** `renameWorkflow(newName: String)` in `WorkflowEditorViewModel` follows the same pattern as `renameNode()`. TopAppBar title is a `clickable` `Column` with a tiny edit icon (12dp, 50% alpha) as affordance. A separate `RenameWorkflowDialog` composable mirrors the node rename flow.

**Alternative:** Inline editable field directly in TopAppBar — rejected because TopAppBar height constraints and keyboard avoidance make the dialog cleaner.

### 3. Library grid/list toggle

**Decision:** `var isGridView by remember { mutableStateOf(false) }` in `LibraryScreen`. Icon button in TopAppBar toggles between `GridView` and `ViewList` icons. Grid uses `LazyVerticalGrid(GridCells.Fixed(2))` with a `NodeTypeGridCard` composable (48dp circle + centered 2-line name + in/out pill badges). List mode retains the existing grouped `LazyColumn`.

**Rationale:** Two-column grid matches the mobile UI reference prototype. The fixed 2-column layout works well on phone widths (360–430dp); no adaptive column logic needed at this stage.

**Import note:** Both `androidx.compose.foundation.lazy.items` and `androidx.compose.foundation.lazy.grid.items` are in scope — no collision because they're extension functions on different receiver types (`LazyListScope` vs. `LazyGridScope`).

### 4. Full type name in NodeCard idle state

**Decision:** When `card.outputSummary` is empty, show `card.typeId.substringAfterLast('.')` (e.g. "SignalSource", "FreeSpacePathLoss") with `TextOverflow.Ellipsis`, instead of the previous 3-char abbreviation.

**Rationale:** The abbreviations ("SRC", "FSPL") require mental decoding. The full class-name substring is self-documenting, and 180dp wide cards can accommodate most names. The dead `categoryAbbr()` helper function remains in the file for future use (e.g., grid icon badges) but is no longer called for the idle row.

### 5. Gradient bars in AnalysisScreen

**Decision:** `CategoryBarRow` uses `Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.45f)))` passed to `drawRoundRect(brush = gradient, …)`. Gradient fades from full category color at the left edge to ~45% at the right — subtly communicating the proportional "fill" without a hard edge.

**Alternative:** Vertical gradient (top-full, bottom-fade) — tried, but the bars are too short (height ≈ 0.65 × available space) for vertical gradient to read clearly.

---

## Files changed

| File | Change |
|------|--------|
| `ui/canvas/WorkflowCanvas.kt` | `scale` param; all coords × s; graphicsLayer on node cards; drag ÷ sc |
| `apps/prorf-android/viewmodel/WorkflowEditorViewModel.kt` | `renameWorkflow()` |
| `apps/prorf-android/ui/WorkflowEditorScreen.kt` | `canvasScale` state; functional zoom buttons in CanvasToolbar; `RenameWorkflowDialog`; clickable title |
| `ui/canvas/NodeCardView.kt` | Full type name (not abbr) in idle state |
| `apps/prorf-android/ui/LibraryScreen.kt` | Grid/list toggle; `NodeTypeGridCard` composable |
| `apps/prorf-android/ui/AnalysisScreen.kt` | `Brush.horizontalGradient` for bar fill |

---

## Risks

- **Zoom hit-area drift** — at high zoom levels, tappable targets don't match visual size. Mitigated by keeping zoom constrained to toolbar buttons only (no accidental large zooms).
- **Scale factor not persisted** — `canvasScale` is UI state lost on screen recreation/rotation. Acceptable for now; could be added to SavedStateHandle if users complain.
