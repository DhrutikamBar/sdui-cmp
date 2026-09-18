# UI certification fixture

The bundled `ui-certification` route is an offline visual/behavior test screen. It deliberately exercises:

- nested column, box, row, grid, and list layouts;
- finance-style card and transaction content;
- buttons and navigation actions;
- a stateful switch;
- live-region and state-description semantics;
- unsupported-widget fallback.

## Run it locally

To reach it from a UAT Firestore home document, temporarily add a button action:

```json
{
  "type": "button",
  "props": { "label": "UI certification" },
  "action": { "type": "navigate", "target": "ui-certification" }
}
```

With Firestore unavailable, the bundled local fallback route renders the same fixture.

## Visual approval checklist

Capture Android screenshots at phone and tablet widths, light/dark theme, and 1.0x/1.3x font scale. Confirm:

1. Card, grid, and transaction rows remain visible without overlap.
2. All quick-action buttons navigate or report analytics.
3. The switch is reachable and announced by a screen reader.
4. The unsupported widget shows its fallback message.
5. Long transaction labels do not crash or clip essential values.

The fixture is covered by `LocalDemoScreenSourceTest` and runs in the Android CI job. Screenshot capture remains a device/emulator visual approval step; it does not require Firestore.
