# ADR-010: Android Resources Scaffolding and UI Bug Fixes

**Status:** Accepted  
**Date:** 2026-06-13  
**Scope:** apps/prorf-android, ui/

---

## Context

The `apps/prorf-android` module referenced Android resources (`@string/app_name`,
`@style/Theme.ProRF`, `@mipmap/ic_launcher`) that did not exist — the `res/`
directory was never created. This blocks all builds.

Several additional UI bugs were also identified:

| # | Bug | File | Impact |
|---|-----|------|--------|
| 1 | Missing `res/` directory | apps/prorf-android | **Build fails** |
| 2 | Missing `proguard-rules.pro` | apps/prorf-android | Build warning / release fail |
| 3 | Drag delta in px mixed with dp node positions | WorkflowCanvas | Node drags 2-3× too fast on hdpi |
| 4 | Edge bezier endpoints computed in raw floats ignoring density | WorkflowCanvas | Edges miss node ports on hdpi |
| 5 | `StatusDot` Canvas has no size modifier | NodeCardView | Status indicator invisible |
| 6 | Inspector `Column` not scrollable | Inspector | Parameters cut off on small screens |
| 7 | ParameterEditor shows no unit labels | ParameterEditor | Users cannot tell which unit they edit |
| 8 | Error overlay uses `contentAlignment` not `align(BottomCenter)` | WorkflowEditorScreen | Errors appear at top, not bottom |

---

## Decision

### 1. Android Resources

Create the minimum required resource files:
- `res/values/strings.xml` — `app_name`
- `res/values/themes.xml` — `Theme.ProRF` (window-level only; Compose handles actual theming)
- `res/values/colors.xml` — launcher icon background color
- `res/drawable/ic_launcher_foreground.xml` — vector RF waveform icon
- `res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` — adaptive icons

The window theme (`Theme.ProRF`) uses `android:Theme.Material.Light.NoActionBar` as its parent
(available API 21+, safe since minSdk=26). Status/navigation bar colors are set to transparent
so `enableEdgeToEdge()` can take full control.

### 2. Drag Delta Units

`detectDragGestures` returns delta in **pixels**. Node positions are stored and rendered in **dp**
(via `Modifier.offset(x.dp, y.dp)`). Fix: convert delta with `Density.toDp()` before
accumulating into node position.

### 3. Edge Bezier Coordinates

`Canvas` coordinates are in **px**. Node card positions (`UiNodeCard.x/y`) are in **dp**.
Fix: capture `LocalDensity.current` once per composition, convert all dp positions to px
(`with(density) { value.dp.toPx() }`), and use the px values inside `Canvas { }`.

### 4. StatusDot Visibility

`Canvas(Modifier)` with no size modifier allocates zero space and draws nothing.
Fix: `Canvas(Modifier.size(10.dp))` with `drawCircle(radius = 4.dp.toPx())`.

### 5. Inspector Scrollability

Wrap the Inspector's root `Column` with `verticalScroll(rememberScrollState())` so all
parameters remain reachable regardless of screen height.

### 6. Unit Labels in ParameterEditor

For `quantity`-typed parameters, display the unit symbol (e.g., "dBm", "dB", "km") to the
right of the text field. The symbol is taken from `Quantity.unit.symbol` of the current value,
so it always reflects the actual unit without any parsing.

### 7. Error Overlay

The Scaffold content is `BoxScope`. Use `Modifier.align(Alignment.BottomCenter)` to pin the
error overlay to the bottom. Use `MaterialTheme.colorScheme.errorContainer` background for
accessible contrast instead of raw `Color.Red`.

---

## Alternatives Considered

- **Use AppCompat theme** as base: rejected — AppCompat is not a dependency; adding it for a
  single XML theme attribute would be wasteful.
- **Store node positions in px**: would fix drag/canvas coherence, but any `Dp` API call
  (like `Modifier.offset()`) still needs conversion. Storing in dp is idiomatic Compose.
- **Store positions as `Dp`**: rejected — `NodePosition` is in L0 platform (no Compose
  dependency allowed). `Float` dp-valued fields are the correct cross-layer representation.

---

## Consequences

- The app now compiles and installs from this module.
- Drag feels correct on all pixel densities.
- Edges connect accurately to node port positions.
- Inspector is usable on small-screen phones.
- Unit-aware parameter editing prevents confusion (e.g., entering "38000" in a km field).
