# SDUI Demo — KMP + Compose Multiplatform + Ktor

A minimal, working version of everything from our conversation: a pluggable widget registry,
a rules engine, server-side data binding, and one shared UI running on Android and iOS from
the same Kotlin code.

## Modules

- **shared** — the wire contract. `UiNode` / `UiAction` / `Rule`, plain `@Serializable` data
  classes with no UI dependencies. Used by both `composeApp` and `server`, so client and
  backend can never drift out of sync on the schema.
- **composeApp** — the mobile client (Android + iOS via Compose Multiplatform). This is where
  the actual "SDK" lives: `ComponentRegistry` (pluggable, open for new widget types),
  `FormState` + `Rule.isSatisfied` (the rules engine), and `Widgets.kt` (the built-in
  column/text/textInput/button/card set).
- **server** — a Ktor backend serving one screen at `/api/ui/home`. `Application.kt` is where
  the data-binding pattern lives: `fakeProductApi.map { ... }` turning "your API data" into
  `UiNode.Card` before the JSON ever reaches a phone.

## What one screen demonstrates

- A text header
- An age text field
- A submit button that's **disabled until the age field is non-empty** — the rules-engine
  example from PhonePe's LiquidUI, minus the general expression parser
- Two product cards, built from a fake API call on the server (`fakeProductApi`) — swap that
  for your real integration and nothing on the client needs to change

## Running it

**Server:**
```
./gradlew :server:run
```
Serves `http://localhost:8080/api/ui/home`.

**Android:** open the project root in Android Studio, run the `composeApp` configuration on
an emulator. It points at `http://10.0.2.2:8080` — the emulator's alias for your host
machine's localhost, where the server above is listening.

**iOS:** requires a Mac with Xcode. Open the project in Android Studio (which drives the KMP
iOS run configuration) or open `iosApp` in Xcode directly once you've generated it via the
Compose Multiplatform wizard/plugin. It points at `http://localhost:8080` directly, since the
simulator shares your Mac's network.

## Deliberately not included

Everything below was discussed earlier in this conversation but left out here to keep the
demo focused — each is a natural next step, not a gap in the design:

- **NativeSlot** — the escape hatch for live/interactive widgets (a price ticker, search-as-
  you-type) that shouldn't be resolved server-side at all.
- **Protobuf / Wire** — this demo uses plain JSON via `kotlinx.serialization`, per the earlier
  recommendation to start there and move to Wire only once JSON's limits actually bite.
  Adding it here means writing a `.proto` schema, dropping the `props: JsonObject` field for
  typed fields, and swapping `ContentNegotiation`'s json() for Wire's generated adapters.
- **The multi-app split** — `ComponentRegistry` here already uses the pluggable pattern from
  the SDK conversation, but `composeApp` and `shared` are still Gradle subprojects in one
  repo. Turning this into something two separate app repos consume means pulling
  `ComponentRegistry` / `FormState` / `Widgets.kt` into their own module and following the
  composite-build or `mavenLocal()` setup from a couple of messages back.
- **A Web Console** — still very much optional; hand-editing the `buildHomeScreen()` function
  in `Application.kt` is your console until a non-engineer actually needs to touch it.

## One honest caveat

This was written and carefully reviewed in a sandboxed environment without network access to
Maven Central or Google's Maven repo, so it hasn't actually been compiled here — there's no
Android SDK or Xcode in this container to build against. Treat it as a correct, ready-to-open
starting point rather than a verified green build; the first thing to do is open it in Android
Studio and let it sync, which will surface anything environment-specific (SDK paths, Xcode
version, etc.) that no amount of static review catches.
