# Firestore SDUI reference host

Android reads documents from the sduiScreens Firestore collection. The document ID is the SDUI route, such as home or wallet.

Each document has these fields:

- content: a Firestore map containing an SDUI document, or a String containing complete SDUI JSON.
- revision: optional increasing integer, starting at 1.
- updatedAt: optional ISO-8601 timestamp, such as 2026-09-12T12:00:00Z.

The screen source validates content before rendering it. It uses Firestore offline persistence, keeps an in-memory entry fresh for five minutes, and times out an unavailable Firestore request after ten seconds. A retry from the error screen forces a fresh Firestore read.

Development rule:

match /sduiScreens/{screen} {
  allow read: if true;
  allow write: if false;
}

Do not use open write rules in a released application. When Firebase Authentication is added, replace the public read rule with an authenticated authorization rule. The Firebase JSON configuration configures Android only. iOS continues to use bundled documents until an iOS host and GoogleService-Info.plist are added.
