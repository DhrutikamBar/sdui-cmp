# Firestore SDUI reference host

Android reads documents from the sduiScreens Firestore collection. The document ID is the SDUI route, such as home or wallet.

Each document has one field named content. It can be either:

- a Firestore map containing a versioned SDUI document; or
- a String containing the complete versioned SDUI JSON document.

The String form is convenient for pasting JSON from the Firebase console. Example content value:

{
  "schemaVersion": 1,
  "root": {
    "type": "column",
    "children": [
      {
        "type": "text",
        "props": { "value": "Hello from Firestore" }
      }
    ]
  }
}

Development rule:

match /sduiScreens/{screen} {
  allow read: if true;
  allow write: if false;
}

Do not use open write rules in a released application. When Firebase Authentication is added, replace the public read rule with an authenticated authorization rule.

Firestore Android provides offline persistence. The source reports documents returned from its local cache as DISK. The Firebase JSON configuration configures Android only. iOS continues to use bundled documents until an iOS host and GoogleService-Info.plist are added.
