# Host data bindings

The SDK never fetches or owns business data. The main app passes it to
`SduiReferenceScreenHost(dataContext = ...)` or directly to `SduiRenderer`.

```kotlin
val data = SduiDataContext(
    mapOf(
        "user" to SduiValue.ObjectValue(mapOf("name" to SduiValue.StringValue("Asha"))),
        "balance" to SduiValue.NumberValue(1250.0),
        "transactions" to SduiValue.ListValue(/* typed transaction objects */)
    )
)
```

Use `{{user.name}}` and `{{balance}}` in a document. A whole-value binding
keeps its type, while an embedded binding becomes display text.

For a list, use a `repeater` node with `"items": "{{transactions}}"` and a
single child template. Its template may use `{{item}}`, `{{item.title}}`,
and `{{index}}`. The SDK expands it into ordinary column children before
rendering.

For API button feedback, set an action metadata `stateKey` and the button
props `loadingKey` to the same value. While the host API request is running,
the button disables itself and displays `loadingLabel` (or `Loading…`).

Production hosts should use `HostAllowlistActionPolicy` with exact routes,
HTTPS hosts, API methods, and endpoint paths. The demo policy remains
intentionally permissive enough for the UAT sample.
