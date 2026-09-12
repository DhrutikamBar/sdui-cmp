# SDUI SDK public API boundary

This document defines the Phase 1 public boundary for the reusable mobile SDK. It does not alter the current demo application's runtime behaviour.

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
- The existing `SupaBaseUiRepository` is a reference-host implementation only. It is not a dependency of `sdui-sdk`.
- Result metadata, cancellation, cache freshness, and error modelling are intentionally deferred to a later compatibility-preserving phase.

### Host capabilities

- `SduiNavigator` lets the host own navigation.
- `SduiUrlHandler` lets the host validate and open external links.
- `SduiActionPolicy` lets the host allow or deny server-defined actions before a future dispatch integration.
- `ReportingService` remains the host-provided analytics/crash-reporting boundary.
- `ResourceResolver` remains the host-provided string/image resource boundary.

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
- No automatic action-policy enforcement yet.

The next Phase 1 increment should wire these host contracts into the reference host behind compatibility-preserving defaults, then test the policy boundary before moving demo infrastructure.
