package com.example.sdui.app

import androidx.compose.runtime.compositionLocalOf

/**
 * Pluggable interface for crash reporting and analytics.
 * Swap [ConsoleReportingService] with Firebase/Sentry in production.
 */
interface ReportingService {
    fun reportCrash(throwable: Throwable, context: Map<String, String> = emptyMap())
    fun reportEvent(name: String, metadata: Map<String, String> = emptyMap())
}

class ConsoleReportingService : ReportingService {
    override fun reportCrash(throwable: Throwable, context: Map<String, String>) {
        println("🚨 SDUI CRASH REPORTED")
        println("Message: ${throwable.message}")
        if (context.isNotEmpty()) println("Context: $context")
        throwable.printStackTrace()
    }

    override fun reportEvent(name: String, metadata: Map<String, String>) {
        println("📊 SDUI EVENT: $name | Metadata: $metadata")
    }
}

val LocalReportingService = compositionLocalOf<ReportingService> { ConsoleReportingService() }
