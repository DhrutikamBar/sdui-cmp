# Implementation Plan - SDUI Testing Strategy

This plan establishes a comprehensive testing suite to verify all the "Elite" and production-grade features developed so far.

## User Review Required

> [!IMPORTANT]
> **Test Dependencies**: I will add `kotlin-test` and Compose testing libraries to the project. This will slightly increase the build configuration complexity but is required for verification.

## Proposed Changes

### 1. Test Infrastructure

#### [MODIFY] [shared/build.gradle.kts](file:///D:/chikul/sdui-demo/sdui-demo/shared/build.gradle.kts)
- Add `kotlin.test` dependency to `commonTest`.

#### [MODIFY] [composeApp/build.gradle.kts](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/build.gradle.kts)
- Add `compose.uiTestJUnit4` and `kotlin.test` dependencies.

### 2. Logic Verification (Unit Tests)

#### [NEW] [ConditionTest.kt](file:///D:/chikul/sdui-demo/sdui-demo/shared/src/commonTest/kotlin/com/example/sdui/shared/ConditionTest.kt)
- Test all condition types: `Equals`, `NotEmpty`, `IsTrue`, `Matches`, `And`, `Or`, `Not`.
- Test the new `Script` evaluator with arithmetic operations.

#### [NEW] [InterpolationTest.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonTest/kotlin/com/example/sdui/app/InterpolationTest.kt)
- Verify `{{fieldId}}` interpolation works correctly with `FormState`.

### 3. Rendering & Interaction (UI Tests)

#### [NEW] [SduiUiTest.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/androidMain/kotlin/com/example/sdui/app/SduiUiTest.kt)
- Verify that components with `visibleWhen` appear/disappear based on `FormState`.
- Verify `onAppear` lifecycle hooks are triggered.
- Verify `AnalyticsInterceptor` captures events.

### 4. Integration Verification

#### [NEW] [PrefetchTest.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonTest/kotlin/com/example/sdui/app/PrefetchTest.kt)
- Verify `UiScanner` correctly identifies all navigation paths in a tree.
- Verify `UiRepository` populates the cache during `prefetch`.

---

## Verification Plan

### Automated Tests
- Run `./gradlew :shared:allTests`
- Run `./gradlew :composeApp:connectedAndroidTest` (requires emulator) or use JVM host tests.

### Manual Verification
- A set of "Testing Instructions" will be provided in a new artifact to guide you through manual verification of Haptics and Accessibility.
