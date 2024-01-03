package tech.resilientgym.resilient

import android.content.Context

actual class UserSessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    actual fun createSession(userId: Int) {
        sharedPreferences.edit().putInt("userId", userId).apply()
    }

    actual val userId: Int?
        get() = sharedPreferences.getInt("userId", -1).takeIf { it != -1 }

    actual fun isLoggedIn(): Boolean {
        return userId != null
    }

    actual fun logout() {
        sharedPreferences.edit().remove("userId").apply()
    }
}