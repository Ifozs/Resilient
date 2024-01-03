package tech.resilientgym.resilient

data class ExerciseSet(
    val setId: Int, // Unique identifier for the set
    val sessionExerciseId: Int, // ID of the session exercise this set belongs to
    val setNumber: Int, // Number of the set within the exercise
    val weight: Double, // Weight used in this set
    val reps: Int // Number of repetitions performed
)
