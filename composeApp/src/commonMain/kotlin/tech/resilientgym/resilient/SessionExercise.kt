package tech.resilientgym.resilient

data class SessionExercise(
    val sessionExerciseId: Int, // Unique identifier for the session exercise
    val sessionId: Int, // ID of the session this exercise belongs to
    val exerciseId: String, // ID or name of the exercise
    val sets: List<ExerciseSet> // List of sets performed for this exercise
)