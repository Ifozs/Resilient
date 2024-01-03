package tech.resilientgym.resilient

actual class UserSessionManager {
    actual fun createSession(userId: Int) {
    }

    actual val userId: Int?
        get() = TODO("Not yet implemented")

    actual fun isLoggedIn(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun logout() {
    }
}