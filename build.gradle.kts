// Versions are pinned here for reference — check for newer patch releases before you build.
// Verified during this conversation: Compose Multiplatform 1.8.0 is the release that brought
// Compose for iOS to Stable (May 2025), so 1.8.x+ is the floor you want for the composeApp module.
plugins {
    kotlin("multiplatform") version "2.1.0" apply false
    kotlin("plugin.serialization") version "2.1.0" apply false
    id("com.android.application") version "8.7.0" apply false
    id("com.android.library") version "8.7.0" apply false
    id("org.jetbrains.compose") version "1.8.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("io.ktor.plugin") version "3.0.0" apply false
    id("app.cash.sqldelight") version "2.3.2" apply false
}
