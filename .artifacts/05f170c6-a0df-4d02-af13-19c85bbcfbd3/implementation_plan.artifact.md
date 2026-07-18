# Implementation Plan - Observability & Reliability

This plan introduces a structured way to handle rendering crashes and report analytics in our SDUI system. Since the server controls the UI, the client must be highly resilient and provide clear feedback when things go wrong.

## User Review Required

> [!IMPORTANT]
> **Reporting Service**: I will introduce a `ReportingService` interface. You can later swap the `ConsoleReporter` for a real tool like Firebase Crashlytics or Sentry.
>
> **Rendering Guard**: If a specific widget crashes (e.g., bad logic in a custom renderer), only that widget will show an "Error Widget" instead of crashing the whole app.

## Proposed Changes

### 1. Structured Reporting (`composeApp`)

#### [NEW] [ReportingService.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonMain/kotlin/com/example/sdui/app/ReportingService.kt)
- Define an `Observable` reporting interface.
- Implement `reportCrash(throwable: Throwable, context: Map<String, Any>)`.
- Implement `reportEvent(name: String, metadata: Map<String, Any>)`.

### 2. Resilience: Component Guards (`composeApp`)

#### [MODIFY] [ComponentRegistry.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonMain/kotlin/com/example/sdui/app/ComponentRegistry.kt)
- Wrap the `renderer(...)` call in a `try-catch` block.
- If a crash occurs:
    1. Report the crash via `ReportingService` (including component `type` and `id`).
    2. Render a "Debug Error Box" in development mode or the `fallback` node in production.

### 3. Advanced Analytics Interceptor (`composeApp`)

#### [MODIFY] [App.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonMain/kotlin/com/example/sdui/app/App.kt)
- Enhance `AnalyticsInterceptor` to use the `metadata` field from `UiAction` and send it to `ReportingService`.
- Add a `LaunchedEffect` in `SduiScreenContent` to report a "Screen View" event whenever a new path is loaded.

### 4. Fetching Diagnostics (`composeApp`)

#### [MODIFY] [UiRepository.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonMain/kotlin/com/example/sdui/app/UiRepository.kt)
- Report network failures (4xx/5xx/Exceptions) to the `ReportingService` with the URL and response code.

---

## Verification Plan

### Automated Tests
- **Simulated Crash**: Create a widget that throws an exception and verify that the app remains stable and only that widget shows an error.
- **Event Verification**: Verify that navigating to a screen triggers the "screen_view" event in logs.

### Manual Verification
- Trigger a malformed JSON error from the server and verify the error is reported.
- Click a button with metadata and verify the metadata is captured in the analytics logs.
