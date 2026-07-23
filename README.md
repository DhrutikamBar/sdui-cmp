# SDUI Elite SDK — KMP + Compose Multiplatform + Supabase

A production-grade Server-Driven UI (SDUI) framework with high-performance rendering, type-safe logic, and multi-layered caching.

## Architecture

- **shared** — The wire contract. Defines the `UiNode` tree, `SduiValue` system, and `Condition` logic.
- **composeApp** — The mobile client (Android + iOS). Fetches screens from Supabase, flattens the UI tree for 60fps performance, and handles local logic/state.
- **server** — A reference Ktor backend. **Note**: The mobile app currently fetches live screens from Supabase directly; this module serves as an example of how to build a custom backend if needed.

## Key Features

- **Flat-Tree Rendering**: Automatically flattens nested layout JSON into a single high-performance `LazyColumn`.
- **Elite Scripting**: Device-side expression evaluation supporting arithmetic and multi-variable logic (e.g., `price * qty > 100`).
- **Multi-Tier Cache**: Memory -> Persistent SQLDelight Disk Cache -> Remote Fetch (with smart `updated_at` invalidation).
- **Transport Efficiency**: Dual support for JSON and binary Protocol Buffers.
- **Observability**: Pluggable `ReportingService` for automated screen tracking, action analytics, and component-level crash guards.

## Running the Project

### Supabase Setup
1. Create a table named `screens` in Supabase with columns: `path` (TEXT, PK), `content` (JSONB), and `updated_at` (TIMESTAMPTZ).
2. Configure your `SUPABASE_URL` and `SUPABASE_KEY` in `gradle.properties`.

### Android
- Open in Android Studio.
- Run the `composeApp` configuration on an emulator or device.

### iOS
- Requires a Mac with Xcode.
- Run the `iosApp` via the Compose Multiplatform plugin or open the `iosApp` folder in Xcode.

### Server (Reference)
```bash
./gradlew :server:run
```
Serves a sample home screen at `localhost:8080/api/ui/home`.

## Build & Test

- **Build all modules**: `./gradlew assemble`
- **Run logic tests**: `./gradlew :shared:allTests` and `./gradlew :composeApp:allTests`
