import tech.resilientgym.resilient.Exercise
import tech.resilientgym.resilient.FoodItemRecord
import tech.resilientgym.resilient.Meal
import tech.resilientgym.resilient.WorkoutSession

expect class DatabaseHandler() {
    suspend fun fetchMealsForDay(userId: Int, date: String): List<Meal>
    suspend fun getAllSessionsForUser(userId: Int): List<WorkoutSession>
    suspend fun getExercisesWithMuscleGroups(): List<Exercise>
    suspend fun insertSessionForUser(userId: Int, workoutSession: WorkoutSession)
    suspend fun searchFood(food: String): List<FoodItemRecord>
}