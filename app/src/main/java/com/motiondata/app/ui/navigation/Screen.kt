package com.motiondata.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Setup : Screen("setup")

    data object LiveRecording : Screen("live_recording/{sessionId}/{name}/{label}/{hz}/{limit}") {
        fun createRoute(
            sessionId: String,
            name: String,
            label: String,
            hz: Int,
            limit: Int
        ): String {
            val encName = java.net.URLEncoder.encode(name, "UTF-8")
            val encLabel = java.net.URLEncoder.encode(label, "UTF-8")
            return "live_recording/$sessionId/$encName/$encLabel/$hz/$limit"
        }
    }

    data object Summary : Screen("summary/{sessionId}") {
        fun createRoute(sessionId: String) = "summary/$sessionId"
    }

    data object Visualization : Screen("visualization/{sessionId}") {
        fun createRoute(sessionId: String) = "visualization/$sessionId"
    }

    data object History : Screen("history")
    data object Settings : Screen("settings")
}
