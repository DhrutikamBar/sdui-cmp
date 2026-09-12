# SDUI Elite SDK — KMP + Compose Multiplatform + Supabase

A production-grade Server-Driven UI (SDUI) framework with high-performance rendering, type-safe logic, and multi-layered caching.

## Architecture

- **shared** — The wire contract. Defines the `UiNode` tree, `SduiValue system, and `Condition` logic.
- **sdui-sdk** — The reusable Compose renderer, widgets, action primitives, and host contracts. It has no Supabase or SQLDelight dependency.
- **composeApp** — The reference host/demo (Android + iOS): Supabase screen source, action client, SQLDelight cache, configuration, `DemoApp` assembly, navigation wiring, and bundled fallback screens.

## Key Features

- **Flat-Tree Rendering**: Automatically flattens nested layout JSON into a single high-performance `LazyColumn`.
- **Elite Scripting**: Device-side expression evaluation supporting arithmetic and multi-variable logic (e.g., `price * qty > 100`).
- **Multi-Tier Cache**: Memory -> Persistent SQLDelight Disk Cache -> Remote Fetch (with smart `updated_at` invalidation).
- **Transport Efficiency**: Dual support for JSON and binary Protocol Buffers.
- **Protocol Evolution**: Versioned `SduiDocument` payloads with legacy `UiNode` compatibility and bounded structural validation at the trust boundary.
- **Observability**: Pluggable `ReportingService` for automated screen tracking, action analytics, and component-level crash guards.

## Running the Project

### Supabase-controlled reference demo

The app reads screen documents from the Supabase `screens` table using the `path`, `content`, and `updated_at` fields. The existing table format—where `content` is a JSONB SDUI tree—is supported.

For Android, copy `gradle.properties.example` to an untracked local `gradle.properties` file and set:

```text
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your-anon-key
```

Use the anonymous public key, never a service-role key. When those values are absent, the reference app uses bundled local screens so it remains runnable offline.

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
