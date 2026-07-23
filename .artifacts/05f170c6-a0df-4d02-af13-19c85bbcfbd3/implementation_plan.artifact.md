# Implementation Plan - Enable Supabase Internal Logging

This plan ensures that Supabase's internal network requests (database queries) are logged to the IDE console, providing full visibility into all SDUI network traffic.

## Proposed Changes

### 1. Configure Supabase Internal Logging

#### [MODIFY] [SupaBaseUiRepository.kt](file:///D:/chikul/sdui-demo/sdui-demo/composeApp/src/commonMain/kotlin/com/example/sdui/app/SupaBaseUiRepository.kt)
- Update the `createSupabaseClient` block to include `httpConfig`.
- Inside `httpConfig`, install the Ktor `Logging` plugin.
- Use a custom logger with a `SUPABASE:` prefix to distinguish these logs from manual `KTOR:` logs.

## Verification Plan

### Manual Verification
1. **Check Logs**: Run the app and observe the IDE console.
2. **Database Queries**: Verify that logs starting with `SUPABASE:` appear when a screen is fetched from the database.
3. **Headers & Body**: Confirm that the log contains the full request URL, headers, and the returned JSON body.
