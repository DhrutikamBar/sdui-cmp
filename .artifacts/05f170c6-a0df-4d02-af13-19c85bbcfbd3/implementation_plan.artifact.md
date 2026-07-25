# Implementation Plan - Fix JitPack Java 21 Build Failure

This plan addresses the "invalid source release: 21" error during JitPack builds by correctly scoping the build and enabling automatic JDK toolchain resolution.

## User Review Required

> [!IMPORTANT]
> **New Tag Required**: After these changes are pushed, you **MUST** create and push a **NEW Git tag** (e.g., `1.0.1`) to JitPack. JitPack caches build results for existing tags, so re-triggering the failed tag may not pick up these fixes.

## Proposed Changes

### 1. Build Scoping (`jitpack.yml`)

#### [NEW] [jitpack.yml](file:///D:/chikul/sdui-demo/sdui-demo/jitpack.yml)
- Create a `jitpack.yml` file at the repository root.
- Set the JDK to `openjdk17` (the baseline for the build environment).
- Override the `install` command to target **only** the `:sdui-sdk` module: `./gradlew :sdui-sdk:publishToMavenLocal`.
- This ensures that `composeApp` (the application) is not compiled during the JitPack library build process, significantly reducing build time and avoiding application-only toolchain issues.

### 2. Automatic Toolchain Resolution

#### [MODIFY] [settings.gradle.kts](file:///D:/chikul/sdui-demo/sdui-demo/settings.gradle.kts)
- Add a top-level `plugins` block.
- Install `org.gradle.toolchains.foojay-resolver-convention` version `0.9.0`. This plugin allows Gradle to automatically download and install the required JDK 21 if it's missing from the build environment.

### 3. Toolchain Audit Results

I have audited all modules for Java/Kotlin version requirements:
- **`:shared`**: Requires **Java 21** (`jvmTarget`, `sourceCompatibility`, `targetCompatibility`).
- **`:sdui-sdk`**: Requires **Java 21** (`jvmTarget`, `sourceCompatibility`, `targetCompatibility`).
- **`:composeApp`**: Requires **Java 21** (`jvmTarget`, `sourceCompatibility`, `targetCompatibility`).

The project is **internally consistent** (all modules use 21), but **externally inconsistent** with JitPack's default environment (Java 17).

## Verification Plan

### Automated Tests
- Run `./gradlew :sdui-sdk:publishToMavenLocal` locally to ensure the build and publication logic remains sound.

### Manual Verification
- Verify `jitpack.yml` is correctly placed at the repo root.
- Verify `settings.gradle.kts` syncs correctly with the new plugin.
