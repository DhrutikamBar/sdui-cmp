plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "composeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation("io.ktor:ktor-client-core:3.0.0")
            implementation("io.ktor:ktor-client-encoding:3.0.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
            implementation("io.ktor:ktor-client-logging:3.0.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
            implementation("io.ktor:ktor-serialization-kotlinx-protobuf:3.0.0")
            implementation(compose.materialIconsExtended)
            implementation("io.coil-kt.coil3:coil-compose:3.0.4")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.4")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")
            implementation("io.github.jan-tennert.supabase:postgrest-kt:3.1.0")
            implementation("app.cash.sqldelight:runtime:2.3.2")
            implementation("app.cash.sqldelight:coroutines-extensions:2.3.2")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

        }
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.9.3")
            implementation("io.ktor:ktor-client-okhttp:3.0.0")
            implementation("app.cash.sqldelight:android-driver:2.3.2")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.0.0")
            implementation("app.cash.sqldelight:native-driver:2.3.2")
        }
    }
}

android {
    namespace = "com.example.sdui.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.sdui.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

sqldelight {
    databases {
        create("SduiDatabase") {
            packageName.set("com.example.sdui.app.db")
        }
    }
}
