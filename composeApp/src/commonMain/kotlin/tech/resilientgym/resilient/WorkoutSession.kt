package tech.resilientgym.resilient

data class WorkoutSession(
    val sessionId: Int, // Unique identifier for the session
    val userId: Int, // ID of the user the session belongs to
    val sessionDate: String, // Date when the session took place
    val sessionTitle: String, // Title or name of the workout session
    val exercises: List<SessionExercise> // List of exercises performed in the session
)