# SDK consumer guide

The reusable artifacts are the shared wire-contract module and sdui-sdk Compose renderer module.

A host supplies a ScreenSource, API client, navigation, URL handler, policy, reporting, resources, and component registry. The composeApp module is an offline reference host, not a required runtime dependency.

Validate remote documents with SduiDocumentCodec before caching or rendering.
