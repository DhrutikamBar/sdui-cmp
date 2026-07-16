# Implementation Plan - Fix Build Error

The project currently fails to build with the error:
`Unable to load class 'org.gradle.api.internal.plugins.DefaultArtifactPublicationSet'`

This is caused by a version mismatch between Gradle (9.6.1) and the Android Gradle Plugin (8.7.0). Gradle 9.x removed internal APIs that AGP 8.7.0 still relies on.

## Proposed Changes

I have two options to fix this:

### Option 1: Downgrade Gradle (Recommended for quick fix)
Downgrade Gradle to 8.11.1 to match AGP 8.7.0. This is a simple change that maintains the current project structure.

### Option 2: Upgrade to AGP 9.x (Modern approach for 2026)
Upgrade to AGP 9.3.0 and Kotlin 2.4.10. This requires:
- Splitting `composeApp` into a dedicated `androidApp` module and a `shared-ui` KMP library.
- Migrating to the new `com.android.kotlin.multiplatform.library` plugin.
- Updating DSLs to the AGP 9 standards.

I will proceed with **Option 1** first as it's the most direct fix for the current error, unless you prefer the full AGP 9 migration.

#### [MODIFY] [gradle-wrapper.properties](file:///D:/chikul/sdui-demo/sdui-demo/gradle/wrapper/gradle-wrapper.properties)
- Downgrade `distributionUrl` to Gradle 8.11.1.

## Verification Plan

### Automated Tests
- Run `./gradlew :composeApp:assembleDebug` to verify the build completes successfully.
- Run `./gradlew :server:run` to ensure the backend still builds and starts.

### Manual Verification
- Verify that the IDE syncs successfully after the change.
