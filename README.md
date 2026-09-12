# SDUI Elite SDK — KMP + Compose Multiplatform + Supabase

A production-grade Server-Driven UI (SDUI) framework with high-performance rendering, type-safe logic, and multi-layered caching.

## Architecture

- **shared** — The wire contract. Defines the `UiNode` tree, `SduiValue system, and `Condition` logic.
- **sdui-sdk** — The reusable Compose renderer, widgets, action primitives, and host contracts. It has no Supabase or SQLDelight dependency.
- **composeApp** — The reference host/demo (Android + iOS): remote and bundled screen sources, action client, SQLDelight cache, configuration, `DemoApp` assembly, navigation wiring, and local screens.

## Key Features

- **Flat-Tree Rendering**: Automatically flattens nested layout JSON into a single high-performance `LazyColumn`.
- **Elite Scripting**: Device-side expression evaluation supporting arithmetic and multi-variable logic (e.g., `price * qty > 100`).
- **Multi-Tier Cache**: Memory -> Persistent SQLDelight Disk Cache -> Remote Fetch (with smart `updated_at` invalidation).
- **Transport Efficiency**: Dual support for JSON and binary Protocol Buffers.
- **Protocol Evolution**: Versioned `SduiDocument` payloads with legacy `UiNode` compatibility and bounded structural validation at the trust boundary.
- **Observability**: Pluggable `ReportingService` for automated screen tracking, action analytics, and component-level crash guards.

## Running the Project

### Reference demo mode

The Home document is fetched from the configured Mocki endpoint. It must return a versioned `SduiDocument` or legacy `UiNode` JSON payload. The bundled Home screen is used automatically if remote loading or validation fails; the remaining demo screens are bundled.

### Android
- Open in Android Studio.
- Run the `composeApp` configuration on an emulator or device.

### iOS
- Requires a Mac with Xcode.
- The repository currently produces iOS frameworks (`shared`, `sdui-sdk`, and `composeApp`) but does not include an `iosApp` Xcode host. Integrate the generated framework into a host app before running on iOS.

## Release readiness

CI runs Android unit tests for all modules. See [the consumer guide](docs/consumer-guide.md) and [changelog](CHANGELOG.md).

## Build & Test

- **Build all modules**: `./gradlew assemble`
- **Run logic tests**: `./gradlew :shared:allTests` and `./gradlew :composeApp:allTests`
