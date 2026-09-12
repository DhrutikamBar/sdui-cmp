# SDUI Elite SDK — KMP + Compose Multiplatform + Firebase

A production-grade Server-Driven UI framework with high-performance rendering, type-safe logic, and resilient document delivery.

## Architecture

- shared — The wire contract. Defines the UI tree, SDUI values, and conditions.
- sdui-sdk — The reusable Compose renderer, widgets, action primitives, and host contracts. It has no Firebase dependency.
- composeApp — The reference host. Android loads SDUI documents from Cloud Firestore; iOS uses bundled documents until its Firebase host configuration is added.

## Firestore-controlled Android demo

Android reads screens from Firestore collection sduiScreens. A document ID is the route name, for example home; its content field holds the SDUI document map. See docs/firebase-firestore.md.

The Firebase Android configuration is in composeApp/google-services.json. Enable Cloud Firestore and add a sduiScreens/home document before running the Android app.

## Build and test

- Build all modules: ./gradlew assemble
- Run logic tests: ./gradlew :shared:allTests and ./gradlew :composeApp:allTests
