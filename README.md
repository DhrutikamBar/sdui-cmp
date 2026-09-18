# SDUI Elite SDK — KMP + Compose Multiplatform + Firebase

A production-grade Server-Driven UI framework with high-performance rendering, type-safe logic, and resilient document delivery.

## Architecture

- shared — The wire contract. Defines the UI tree, SDUI values, and conditions.
- sdui-sdk — The reusable Compose renderer, widgets, action primitives, and host contracts. It has no Firebase dependency.
- composeApp — The reference host. Android loads SDUI documents from Cloud Firestore; iOS uses bundled documents until its Firebase host configuration is added.

## Firestore-controlled Android demo

Android reads published screens from the FlexFlow UI service by route, for example `home`. Publish a screen in Studio before running the Android app. See docs/published-screens.md.

## Build and test

- Build all modules: ./gradlew assemble
- Run logic tests: ./gradlew :shared:allTests and ./gradlew :composeApp:allTests

