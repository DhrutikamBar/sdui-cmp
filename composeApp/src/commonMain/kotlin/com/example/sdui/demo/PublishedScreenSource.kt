package com.example.sdui.demo

import com.dhruti.sdui.sdk.ScreenSource

/**
 * Creates the reference host's published-screen source on supported platforms.
 *
 * The SDK itself does not depend on the delivery service. iOS returns bundled
 * screens until a remote iOS source is supplied.
 */
expect fun createPublishedScreenSource(): ScreenSource

