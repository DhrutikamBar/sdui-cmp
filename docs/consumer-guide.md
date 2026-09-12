# SDK consumer guide

The reusable artifacts are the `shared` wire-contract module and `sdui-sdk` Compose renderer module. The `composeApp` module is an offline reference host, not a required runtime dependency.

## Host responsibilities

A production host supplies:

- a `ScreenSource` that retrieves a `UiNode`;
- an API client, navigation implementation, URL handler, action policy, reporting service, resources, and component registry;
- a list of action types it implements for semantic validation.

Validate externally supplied documents with `SduiDocumentCodec` before caching or rendering them. Keep the action policy restrictive: it is the host's authorization boundary.

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

The reference host is deliberately offline. Add remote transport, authentication-aware caching, and signature verification only when a remote document service is selected.
