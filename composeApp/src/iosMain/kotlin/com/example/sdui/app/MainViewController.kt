package com.example.sdui.app

import androidx.compose.ui.window.ComposeUIViewController

// The iOS simulator shares the host machine's network directly, so plain "localhost" works
// here — unlike Android, which needs the 10.0.2.2 alias (see MainActivity.kt).
fun MainViewController() = ComposeUIViewController {
    App(
        supabaseUrl = "https://lqxcmudbwynnqqkmkhby.supabase.co", 
        supabaseKey = "sb_publishable_JCA5nNbwkBSgVsnONOa8Sg_miTMDhf7",
        driverFactory = DatabaseDriverFactory()
    )
}
