# Implementation Plan - Fix Build Error and Robustify Caching

The current build is failing due to unresolved references to `System` in `SupaBaseUiRepository.kt`, likely caused by ambiguity or incomplete resolution of `Clock.System` in the Kotlin Multiplatform common code. Additionally, I will address the empty responses from Supabase seen in the logcat.

## Proposed Changes

### 1. Fix Build Errors in SupaBaseUiRepository

#### [MODIFY] [SupaBaseUiRepository.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonMain/kotlin/com/example/sdui/app/SupaBaseUiRepository.kt)
- Replace `Clock.System.now()` with `kotlinx.datetime.Clock.System.now()` to eliminate any ambiguity with `java.lang.System`.
- Ensure `kotlinx.datetime.Clock` is correctly utilized.

### 2. Robustify Empty Response Handling

#### [MODIFY] [SupaBaseUiRepository.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonMain/kotlin/com/example/sdui/app/SupaBaseUiRepository.kt)
- Add a check for empty results in `fetchUpdatedAt` and `fetchFullRow`. If Supabase returns an empty list (which happens when a row is missing), the app should throw a descriptive `NoSuchElementException` so the local fallback logic in `App.kt` can take over gracefully.

## Verification Plan

### Automated Tests
- Run `./gradlew :composeApp:assembleDebug` to verify the fix for the build error.

### Manual Verification
- **Logcat Monitoring**: Verify that if a screen (like `topup`) is missing in Supabase, the app logs "Remote fetch failed" and switches to the local fallback instead of crashing or showing a blank screen.
- **Cache Verification**: Relaunch the app and confirm `KTOR: [CACHE]` logs show successful disk hits for existing screens.
