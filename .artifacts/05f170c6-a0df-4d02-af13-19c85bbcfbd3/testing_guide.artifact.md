# SDUI SDK Testing Guide

Since this is a Server-Driven UI system, testing requires a mix of automated logic checks and manual UX verification.

## 1. Automated Logic Testing
These tests verify that the "Brain" of the SDK works correctly without needing a screen.
- **Rules & Scripts**: Run `ConditionTest` to ensure `ageField > 18` and other logic gates evaluate correctly.
- **Interpolation**: Run `InterpolationTest` to verify that `{{user_name}}` in an API body is swapped for real values.

## 2. Manual UX Verification (The "Human" Touch)

### A. Performance & Rendering
- **Flat Tree Check**: Open the "Wallet" or "Checkout" screen. Scroll rapidly. There should be no "jank" or nested scrolling bugs.
- **Sticky Headers**: Scroll the "Home" screen. The "Sign up" title should stay at the top while other fields move underneath it.

### B. Resilience & Observability
- **Crash Test**: Temporarily break a widget's code (e.g., throw an exception in `Widgets.kt`). The app should show a red error box for just that widget, and the console should print `🚨 SDUI CRASH REPORTED`.
- **Analytics Check**: Click a button or open a screen. Watch the IDE console for `📊 SDUI EVENT`.

### C. Advanced Features
- **Haptics**: Click the "Submit" button on the Home screen. On a physical Android device, you should feel a distinct "heavy" vibration.
- **State Survival**: Type into the `ageField`. Exit the app, clear it from recent tasks (simulate process death), and reopen. Your typed age should still be there.
- **Accessibility**: Enable **TalkBack** (Android) or **VoiceOver** (iOS). Tap on widgets and verify the descriptions are read correctly as configured in the JSON.

## 3. Network & Speed
- **Instant Nav**: Open the Home screen. Wait 2 seconds. Disable your internet. Click "Submit" (if it navigates). The next screen should load instantly because it was already **prefetched** in the background.

---

> [!TIP]
> **Pro-Tip**: You can test different server configurations instantly by modifying `LocalScreens.kt` and re-running the app. No need to update any other Kotlin files!
