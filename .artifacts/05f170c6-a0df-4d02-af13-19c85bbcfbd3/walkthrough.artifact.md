# Walkthrough - Supabase Internal Logging

I have enabled full network logging for the Supabase internal client. Previously, only manual `KTOR:` logs (from Edge Functions or API calls) were visible, while database queries were silent.

## Changes Made

### 1. Supabase Client Configuration
- **[MODIFY] [SupaBaseUiRepository.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonMain/kotlin/com/example/sdui/app/SupaBaseUiRepository.kt)**:
    - Updated the `createSupabaseClient` initialization.
    - Added an `httpConfig` block to install the Ktor `Logging` plugin directly into Supabase's internal engine.
    - Implemented a custom logger that prefixes output with **`SUPABASE:`**.

## Verification Results

### Console Output Example
When the app fetches a screen from the database, you will now see logs like this:

```text
SUPABASE: REQUEST: https://lqxcmudbwynnqqkmkhby.supabase.co/rest/v1/screens?path=eq.home&select=content%2Cupdated_at
SUPABASE: METHOD: HttpMethod(value=GET)
SUPABASE: COMMON HEADERS:
SUPABASE: -> Accept: application/json
SUPABASE: -> apikey: ***
SUPABASE: RESPONSE: 200 OK
SUPABASE: BODY: [{"content": {...}, "updated_at": "..."}]
```

---

> [!NOTE]
> You now have 100% visibility into every network request the SDK makes, whether it's a binary prefetch or a standard database query.
