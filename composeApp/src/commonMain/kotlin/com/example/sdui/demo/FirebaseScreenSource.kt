package com.example.sdui.demo

import com.dhruti.sdui.sdk.ScreenSource

/**
 * Creates the reference host's Firestore-backed source on supported platforms.
 *
 * The SDK itself does not depend on Firebase. iOS returns bundled screens until
 * a Firebase iOS host configuration is supplied.
 */
expect fun createFirebaseScreenSource(): ScreenSource
