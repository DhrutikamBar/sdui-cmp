# Implementation Plan - Remove Server Module

This plan outlines the complete removal of the unused Ktor `server` module to streamline the project and resolve build issues in environments like JitPack.

## Proposed Changes

### Root Project

#### [DELETE] [server directory](file:///D:/chikul/sdui-demo/sdui-demo/server)
- Delete the entire `server/` directory and its contents.

#### [MODIFY] [settings.gradle.kts](file:///D:/chikul/sdui-demo/sdui-demo/settings.gradle.kts)
- Remove `:server` from the `include` statement.

#### [MODIFY] [README.md](file:///D:/chikul/sdui-demo/sdui-demo/README.md)
- Remove the "server" architecture bullet point.
- Remove the "Server (Reference)" section from "Running the Project".

### Compose App

#### [MODIFY] [MainActivity.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/androidMain/kotlin/com/example/sdui/app/MainActivity.kt)
- Remove comments referring to `./gradlew :server:run`.

## Verification Plan

### Automated Tests
- Run `./gradlew :sdui-sdk:publishToMavenLocal` to ensure the core SDK library still builds and publishes locally without the server module.
- Run `./gradlew assemble` to ensure the entire project (shared, sdk, and app) still compiles correctly.

### Manual Verification
- Verify that the `server/` directory is physically removed from the disk.
