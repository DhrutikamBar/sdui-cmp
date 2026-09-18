# SDUI SDK public API boundary

## Module ownership

| Module | Ownership |
| --- | --- |
| `shared` | Wire contract: `UiNode`, `UiAction`, `SduiValue`, conditions, semantics, serialization, and document validation. |
| `sdui-sdk` | Reusable Compose renderer, built-in widgets, form state, action primitives, host contracts, resources, and reporting. It has no Firebase dependency. |
| `composeApp` | Reference host application. Its Android adapter reads Firestore documents and falls back to bundled screens; iOS currently uses bundled fallback screens. |

The reusable SDK does not require a particular transport, database, navigation library, or backend service.

## Rendering and data

- `SduiRenderer` renders a `UiNode` using an `ActionHandler`, `ComponentRegistry`, and `FormState`. Its public `modifier` is applied to the root rendering container.
- `ComponentRegistry` registers host or SDK widget renderers by node `type`.
- `SduiDataContext` supplies typed, host-owned data bindings to a document without granting documents access to the host data model.

## Screen delivery

- `ScreenSource` is host-owned and supplies decoded `UiNode` screens.
- `ScreenRequest`, `ScreenLoadResult`, and `ScreenLoadSource` provide typed delivery and cache provenance while preserving coroutine cancellation.
- `cancelPrefetch` and `close` provide lifecycle hooks for host-owned cleanup.
- The Android reference host uses `PublishedScreenSource`. `LocalDemoScreenSource` supplies bundled fallback screens. Neither is a dependency of `sdui-sdk`.

## Host extension points

- `SduiNavigator` owns navigation.
- `SduiUrlHandler` validates and opens external links.
- `SduiActionPolicy` authorizes server-defined actions.
- `SduiApiCallClient` executes allowed API calls using host transport and credentials.
- `ReportingService`, `ResourceResolver`, design tokens, custom widgets, and `SduiReferenceScreenHost` allow hosts to integrate platform and product-specific behavior.

## Compatibility rules

1. Additive wire-contract fields need defaults and serialization coverage.
2. Removing or renaming public SDK APIs is breaking.
3. Widget and action type strings are protocol values; hosts validate them before rendering.
4. Server-defined side effects remain host-controlled.
5. The reference host may change independently as long as it continues to consume the reusable SDK boundary.

