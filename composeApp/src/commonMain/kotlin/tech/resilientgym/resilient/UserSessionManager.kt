package tech.resilientgym.resilient

expect class UserSessionManager {
    fun createSession(userId: Int)

    val userId: Int?
    fun isLoggedIn(): Boolean
    fun logout()
}