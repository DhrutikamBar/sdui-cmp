# SDK consumer guide

The reusable artifacts are the `shared` wire-contract module and `sdui-sdk` Compose renderer module. The `composeApp` module is a reference host, not a required runtime dependency.

## Host responsibilities

A host supplies:

- a `ScreenSource` that retrieves decoded `UiNode` documents;
- an API client, navigation implementation, URL handler, action policy, reporting service, resources, design tokens, and component registry;
- a list of action types it implements for semantic validation;
- optionally, an `SduiDataContext` for typed host-owned live values.

Validate externally supplied documents with `SduiDocumentCodec` before caching or rendering them. Keep the action policy restrictive: it is the host's authorization boundary.

## Live API data binding

Firestore or another `ScreenSource` supplies layout documents. Your authenticated business API supplies live values through a host-owned `SduiScreenDataProvider`. The host maps each response model into neutral `SduiValue` data; the SDK never receives backend model classes, credentials, or endpoints.

A provider exposes `Loading`, `Empty`, `Failure`, or `Content(SduiDataContext)` for the active route. The reference host renders loading, empty, and retry states automatically. On content, bindings are resolved before rendering.

Use scalar bindings in JSON:

```json
{ "type": "text", "props": { "value": "Hello, {{user.firstName}}" } }
```

Use a repeater for API arrays:

```json
{
  "type": "repeater",
  "props": { "items": "{{transactions}}" },
  "children": [
    {
      "type": "row",
      "children": [
        { "type": "text", "props": { "value": "{{item.title}}" } },
        { "type": "text", "props": { "value": "{{item.amountDisplay}}" } }
      ]
    }
  ]
}
```

Whole-value bindings preserve type: `{{wallet.balance}}` remains a number, while embedded values such as `Balance: {{wallet.balance}}` are text. The demo's Wallet fallback shows this pattern and provides a bounded `refreshData` action. Production hosts should allow that action only where route refresh is permitted.

## Reference host

The Android reference host uses `FirebaseFirestoreScreenSource` to load `sduiScreens/{route}` documents from Cloud Firestore. It accepts a `content` map or JSON string and benefits from Firestore offline persistence. If a source is unavailable, the reference host uses its bundled local fallback screens.

The iOS reference host currently uses the bundled fallback source. Firebase configuration is deliberately an Android-demo concern, not an SDK dependency.

## Integration checklist

1. Register the widgets the host supports in `ComponentRegistry`.
2. Pass the same supported action types to `SduiReferenceScreenHost` that the host registers in `ActionRegistry`.
3. Supply a fallback widget for every remotely introduced widget type during rollout.
4. Report document-load and action failures through the host's `ReportingService`.
5. Call `ScreenSource.close()` when its owning host lifecycle ends.

## Verification

Run Android unit tests with:

```text
./gradlew :shared:testDebugUnitTest :sdui-sdk:testDebugUnitTest :composeApp:testDebugUnitTest
```

The SDK does not require Firestore. A production host can provide any `ScreenSource` and decide its own transport, authentication, cache, and authorization strategy.


## Production-ready SDK contracts

- Use `ComponentRegistry.capabilities(actionTypes)` to provide the SDK version plus supported widget/action types to a document service.
- Model host data with `SduiDataState` and return `SduiActionResult` from host workflows to keep loading, empty, failure, retryable, cancelled, and validation cases structured.
- Provide `HostAllowlistResourcePolicy` through `LocalResourcePolicy` to restrict remote image and Lottie hosts. The default remains permissive for compatibility; production hosts should opt into an explicit allowlist.
- The SDK publication coordinates are `com.dhruti.sdui:sdui-sdk:0.1.0-SNAPSHOT` by default. Publishing destinations and credentials are intentionally not configured.

Continuous integration verifies Android unit tests/assembly and compiles the iOS simulator SDK framework. This validates the iOS SDK surface without introducing an iOS application integration.

## Condition expressions and accessibility

Legacy `Condition.Script` rules use a bounded parser, not arbitrary code evaluation. Supported expressions contain values, multiplication, and one comparison, such as `amount * quantity >= 100` or `status == 'active'`. Unknown identifiers and function-like syntax are rejected.

Interactive custom containers enforce a minimum 48dp touch target. Documents can provide `Semantics.contentDescription`, `role`, `liveRegion`, and `stateDescription`; use these for stateful or dynamically updated content.

## Animation

For a node controlled by `visibleWhen`, put the following in `props.style`:

```json
{ "animation": "slide", "animationDurationMs": 300, "animationEasing": "linear" }
```

Supported transitions are `fade`, `slide`, `scale`, and `none`. Durations are bounded to 0–2000ms. Hosts can disable renderer and Lottie motion by providing `LocalSduiMotionPolicy` with `reduceMotion = true`. The reference application uses fade transitions between routes. Remote Lottie URLs remain subject to the host `SduiResourcePolicy`.

## Navigation chrome

Use `tabs` for an in-content tab strip. Use a root-level `bottomNavigation` for persistent app navigation: it is rendered outside the renderer's scroll container and reserves space so the final content is not obscured.

```json
{
  "id": "mainNavigation",
  "type": "bottomNavigation",
  "props": {
    "selectedIndex": 0,
    "items": [
      { "label": "Games", "icon": "🎮", "route": "home" },
      { "label": "Apps", "icon": "▦", "route": "apps", "badge": "2" },
      { "label": "Search", "icon": "⌕", "route": "search" },
      { "label": "You", "icon": "♙", "route": "profile" }
    ]
  }
}
```

Each selection updates the node's form state, and an item with a `route` dispatches the normal host-owned `navigate` action. The host remains responsible for deciding whether that route is allowed.
