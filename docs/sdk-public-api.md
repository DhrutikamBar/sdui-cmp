# SDUI SDK public API boundary

This document defines the Phase 1 public boundary for the reusable mobile SDK. Its reference-host integration preserves the current default runtime behaviour.

## Module ownership

| Module | Ownership |
| --- | --- |
| `shared` | Wire contract: `UiNode`, `UiAction`, `SduiValue`, conditions, semantics, and serialization. |
| `sdui-sdk` | Reusable Compose renderer, built-in widgets, form state, action dispatch primitives, host contracts, resources, and reporting. |
| `composeApp` | Reference host application. Supabase, SQLDelight cache, Android/iOS entry points, navigation wiring, and local screens remain demo-owned. |

The reusable SDK must not require Supabase, BuildConfig values, SQLDelight, or a particular navigation library.

## Supported integration contracts

### Rendering and widgets

- `SduiRenderer` renders a `UiNode` using an `ActionHandler`, `ComponentRegistry`, and `FormState`.
- `ComponentRegistry` registers host or SDK widget renderers by node `type`.
- `FormState` is the observable state used by widgets and conditions.

### Screen delivery

- `ScreenSource` supplies decoded `UiNode` screens and optional prefetching.
- `ScreenRequest` and `ScreenLoadResult` provide typed success/failure delivery while preserving coroutine cancellation.
- `ScreenLoadSource` represents memory, disk, network, or unknown provenance; sources with cache metadata may report a precise origin.
- `cancelPrefetch` and `close` provide lifecycle hooks for host-owned cleanup.
- The existing `SupabaseScreenSource` is a reference-host implementation only. It is not a dependency of `sdui-sdk`.

### Host capabilities

- `SduiNavigator` lets the host own navigation.
- `SduiUrlHandler` lets the host validate and open external links.
- `SduiActionPolicy` lets the host allow or deny server-defined actions. The reference host passes a documented allowlist to `ActionRegistry` by default.
- `SduiApiCallClient` lets the host execute an allowed `apiCall` using its own transport, credentials, and retry policy.
- `ReportingService` remains the host-provided analytics/crash-reporting boundary.
- `ResourceResolver` remains the host-provided string/image resource boundary.
- `SduiReferenceScreenHost` is the injectable reference-screen composition point: callers provide the path, source, API client, component registry, navigator, URL handler, policy, reporting service, resource resolver, and design tokens.

## Compatibility rules

1. Additive fields in the wire contract must have defaults and be accompanied by serialization fixtures.
2. Removing or renaming public SDK APIs is a breaking change.
3. Widget type strings and action type strings are protocol values; their support and fallback behaviour must be documented before release.
4. Server-provided side effects remain host-controlled. The SDK must not silently expand allowed navigation, URL, or network behaviour.
5. The demo app may evolve independently, provided it consumes the published SDK APIs rather than becoming a requirement of them.

## Deliberate non-goals of this phase

- No Supabase extraction or cache rewrite.
- No renderer or schema behaviour change.
- No navigation-library migration.
- No authentication-aware or tenant-aware policy yet; the reference host uses a static demo allowlist.

### Reference-host action policy

`DemoSduiActionPolicy` allows only the action types handled by the demo host:

- `navigate` to a non-empty local route;
- `back` without a target;
- `toggleState` for identifier-like state keys;
- `openUrl` to HTTPS URLs; and
- `apiCall` with a relative path and one of `GET`, `POST`, `PUT`, `PATCH`, or `DELETE`.

Everything else is denied before interceptors and handlers run. This is a baseline policy, not a substitute for server-side authorization, Supabase RLS, or a tenant-aware production policy.

## Phase 1 completion

The reference host now consumes `ScreenLoadResult`, closes its `ScreenSource` when the repository leaves composition, and preserves cancellation instead of treating it as a load failure. Existing `ScreenSource` implementations remain compatible through the default `loadScreen` adapter.

Phase 2 ownership increments: the Supabase source, SQLDelight cache/driver, cache clock, configuration, and `SupabaseApiCallClient` live under `com.example.sdui.demo`. `DemoApp` assembles those demo-only dependencies before calling the demo navigation shell. `SduiReferenceScreenHost` is the source- and navigation-agnostic composition point for host-provided capabilities. The renderer and wire schema remain unchanged.
