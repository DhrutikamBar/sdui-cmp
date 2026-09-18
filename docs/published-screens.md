# Published screens in the Android reference app

The Android reference host requests `GET /api/published-screens/{route}` from the FlexFlow UI service. Studio serves the immutable published version for that route. Drafts and archived screens return 404. The reference app keeps a successful response in memory for five minutes, and Retry fetches again.

The default service URL is configured in `composeApp/src/androidMain/kotlin/com/example/sdui/demo/PublishedScreenSource.android.kt`. To connect another host, construct `PublishedScreenSource(baseUrl = "https://your-host/api/published-screens")`.

The renderer SDK does not depend on the delivery service. iOS still uses bundled documents until a remote source is configured.

