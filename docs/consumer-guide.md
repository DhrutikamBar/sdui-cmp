# SDK consumer guide

The reusable artifacts are the `shared` wire-contract module and `sdui-sdk` Compose renderer module. The `composeApp` module is a reference host, not a required runtime dependency.

## Host responsibilities

A host supplies:

- a `ScreenSource` that retrieves decoded `UiNode` documents;
- an API client, navigation implementation, URL handler, action policy, reporting service, resources, design tokens, and component registry;
- a list of action types it implements for semantic validation;
- optionally, an `SduiDataContext` for typed host-owned live values.

Validate externally supplied documents with `SduiDocumentCodec` before caching or rendering them. Keep the action policy restrictive: it is the host's authorization boundary.

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
