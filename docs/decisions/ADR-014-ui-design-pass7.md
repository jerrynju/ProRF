# ADR-014: UI Design Pass 7 — Value Hierarchy & Visual Precision

**Date:** 2026-06-14  
**Status:** Done  
**Author:** Automated scheduled task

---

## Context

Six rounds of UI polish have established the ProRF design system: custom theme, node cards with category stripes, tabbed Inspector, ChainListView with bar chart, Analysis screen with stat cards. Prototype images (docs/ChatGPT Image 2026年6月13日 22_*.png) show a light-themed mobile engineering UI with clear value hierarchy — prominent numeric outputs, clean unit badges, and polished chart bars.

Pass 7 focuses on **visual precision** without adding new screens or features.

---

## Decisions

### D1 — NodeCardView: split output value into num + unit

**Before:** output summary shown as a single `labelLarge` text blob (e.g. `"-25.3 dBm"`).  
**After:** numeric part shown in `labelLarge Bold` with full category color; unit shown in `8sp` at 65% opacity to the right, baseline-aligned.

**Why:** The numeric portion is the primary information; the unit is secondary context. Separating them creates visual weight hierarchy matching the prototype where numbers are prominent and units are subordinate.

### D2 — ParameterEditor: unit as styled Surface badge

**Before:** unit rendered as plain `bodySmall` text after the input field.  
**After:** column layout (label above, input + unit-badge side-by-side). Label becomes `labelSmall Medium` in `onSurfaceVariant`. Unit becomes a `Surface(primaryContainer)` pill with `primary` colored bold text and 8/6 dp padding.

**Why:** Plain gray text for units is easy to miss. A colored chip communicates "this is a unit dimension, not a value", matches engineering tool conventions (like MATLAB/Keysight UI), and uses the existing color token system.

**Alternative considered:** Inline label inside OutlinedTextField — rejected because it competes with user-typed values.

### D3 — Inspector QuickSummaryBanner: larger primary value

**Before:** numeric portion in `titleMedium Bold`.  
**After:** numeric in `headlineSmall Bold`; label rendered above (uppercase, `labelSmall 0.6sp letterSpacing`); layout switches from horizontal Row to vertical Column.

**Why:** The banner's sole job is "what is this node's primary output right now?" A headline-sized number communicates urgency and is readable at a glance. The label-above layout gives the number more visual breathing room.

### D4 — Inspector ResultCard: colored left accent stripe

**Before:** colored dot on the left, no structural accent.  
**After:** 3dp left accent stripe clipped to card shape, `valueColor.copy(alpha=0.7f)`.

**Why:** The stripe pattern is already used in WorkflowCard and ChainNodeRow. Extending it to ResultCard creates visual consistency across all "value card" components. The stripe also indicates value sign at a glance before reading the number.

### D5 — SignalChainChart: rounded bar tops

**Before:** `drawRect` — flat-top rectangular bars.  
**After:** `drawRoundRect` with corner radius = `barW/3` (capped at 6dp) for top corners; a plain rect covers the lower portion to keep bars flat-bottom at the baseline.

**Why:** Rounded-top bars are a modern engineering dashboard convention (see Grafana, InfluxDB charts). Flat bottoms sit cleanly on the axis baseline. The technique is: draw RoundRect that extends below baseline by `cornerR`, then cover with a plain rect to flatten.

**Risk:** Canvas draw order — flat rect must be drawn after (on top of) the RoundRect. Order is correct in implementation.

### D6 — AnalysisScreen CategoryBarRow: rounded pill bars

**Before:** `drawRect` for both background track and fill bar.  
**After:** `drawRoundRect` with `cornerRadius = CornerRadius(trackH/2f)` for track; same for fill bar clamped to minimum fill width of `fillH` so the pill doesn't collapse.

**Why:** Pill-shaped bars are friendlier and match the modern data viz convention. The minimum fill width prevents narrow fractions from producing visible artifacts.

### D7 — AnalysisScreen StatCard: color accent top stripe

**Before:** flat card with no visual differentiation between the three stat cards.  
**After:** 3dp top stripe using the card's semantic color, clipped to top corners of the card shape.

**Why:** Three stat cards in a row are visually identical otherwise. The top stripe allows instant category identification (blue=Workflows, green=Nodes, purple=Types) without cluttering the number display area.

### D8 — ChainListView empty state: matches canvas empty state

**Before:** plain text "No nodes yet / Tap + to add a node".  
**After:** 64dp circular icon container (primaryContainer 40% alpha) + AccountTree icon + `titleSmall SemiBold` heading + body hint, same design as canvas empty state.

**Why:** The two views (Canvas and ChainList) toggle between each other. An empty state that looks different between views is confusing. Visual consistency prevents "did my nodes disappear?" confusion.

### D9 — ChainListHeader: status indicator + final value coloring

**Before:** "Out: X.X unit" badge always in primaryContainer color.  
**After:** badge color adapts to value sign (primary if positive, error if negative). A green 5dp dot appears next to the stage count when all nodes have computed outputs.

**Why:** A negative final output (e.g. link budget deficit) is a warning condition and should appear in error color, not the neutral primary blue. The green dot provides a visual confirmation that the workflow ran completely.

---

## Files Changed

| File | Change |
|------|--------|
| `ui/canvas/NodeCardView.kt` | Split outputSummary into num/unit |
| `ui/parameter/ParameterEditor.kt` | Column layout + unit Surface badge |
| `ui/inspector/Inspector.kt` | QuickSummaryBanner headline num; ResultCard left stripe; `clip` import |
| `apps/prorf-android/ui/WorkflowEditorScreen.kt` | Rounded bars; ChainListHeader sign coloring + dot; ChainListView empty state; `CornerRadius` import |
| `apps/prorf-android/ui/AnalysisScreen.kt` | StatCard top stripe; rounded pill bars; `CornerRadius` import |

---

## Risks

- `drawRoundRect` + flat-bottom technique relies on draw order correctness. Tested visually by inspection of drawing logic.
- `headlineSmall` in QuickSummaryBanner may be too large on narrow Inspector panel (280dp). Acceptable at that width — `headlineSmall` is 22sp.
- Unit badge in ParameterEditor adds height to each row, increasing total Inspector scroll length for nodes with many parameters. Acceptable trade-off.
