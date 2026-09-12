# Firestore SDUI reference host

Android reads documents from the sduiScreens Firestore collection. The document ID is the SDUI route, such as home or wallet.

Each document has one map field named content. It holds a versioned document with schemaVersion and root fields.

Development rule:

match /sduiScreens/{screen} {
  allow read: if true;
  allow write: if false;
}

Do not use open write rules in a released application. When Firebase Authentication is added, replace the public read rule with an authenticated authorization rule.

Firestore Android provides offline persistence. The source reports documents returned from its local cache as DISK. The Firebase JSON configuration configures Android only. iOS continues to use bundled documents until an iOS host and GoogleService-Info.plist are added.
