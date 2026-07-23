# Implementation Plan - Extract SDUI SDK Module

This plan outlines the refactoring of the project to extract a reusable `:sdui-sdk` module from the existing `composeApp`.

## User Review Required

> [!IMPORTANT]
> **Package Renaming**: I will move reusable components to the `com.dhruti.sdui.sdk` package within the new module. This will require updating imports in `composeApp`.
>
> **Module Structure**:
> - `:shared`: Schema and Models.
> - `:sdui-sdk`: Core rendering engine, widgets, and platform abstractions.
> - `:composeApp`: Demo application, Supabase integration, and local screens.

## Proposed Changes

### 1. Project Configuration

#### [MODIFY] [settings.gradle.kts](file:///D:/chikul/sdui-demo/sdui-demo/settings.gradle.kts)
- Include `:sdui-sdk`.

#### [NEW] [:sdui-sdk/build.gradle.kts](file:///D:/chikul/sdui-demo/sdui-demo/sdui-sdk/build.gradle.kts)
- Configure KMP library for Android and iOS.
- Apply SQLDelight and Compose plugins.
- Add dependencies: `:shared`, Ktor, SQLDelight, Coil, Navigation (reusable parts).

### 2. SDK Extraction

#### [MOVE] Reusable Core Logic
Move the following files from `composeApp` to `sdui-sdk` and update their package to `com.dhruti.sdui.sdk`:
- `ComponentRegistry.kt`, `Widgets.kt`, `Style.kt`, `ActionRegistry.kt`, `Animations.kt`, `Icons.kt`, `UiFlattener.kt`, `UiScanner.kt`, `ReportingService.kt`.
- `ResourceResolver.kt`, `UrlOpener.kt`, `DatabaseDriverFactory.kt` (including platform-specific implementations).
- `CachedScreen.sq` (SQLDelight schema).

#### [NEW] Public SDK API
Create `SduiRenderer.kt` in `sdui-sdk` as the primary entry point:
```kotlin
@Composable
fun SduiRenderer(
    screen: UiNode,
    actionHandler: ActionHandler,
    modifier: Modifier = Modifier,
    formState: FormState = rememberSaveable(saver = FormState.Saver) { FormState() }
)
```

### 3. App Refactoring (`composeApp`)

#### [MODIFY] [build.gradle.kts](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/build.gradle.kts)
- Remove moved dependencies.
- Add `implementation(project(":sdui-sdk"))`.

#### [MODIFY] [App.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonMain/kotlin/com/example/sdui/app/App.kt)
- Update imports and use `SduiRenderer` from the SDK.
- Retain Supabase logic and `LocalScreens`.

## Verification Plan

### Automated Tests
- Run `./gradlew :sdui-sdk:allTests` (after moving tests).
- Run `./gradlew :composeApp:assembleDebug` to ensure integration works.

### Manual Verification
- Verify that the app still loads screens from Supabase and `LocalScreens` correctly.
- Verify that haptics and animations still function through the SDK.
