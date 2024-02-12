import tech.resilientgym.resilient.Exercise
import tech.resilientgym.resilient.ExerciseWeightUpdate
import tech.resilientgym.resilient.FoodItemRecord
import tech.resilientgym.resilient.Meal
import tech.resilientgym.resilient.UserSettings
import tech.resilientgym.resilient.WorkoutSession

expect class DatabaseHandler() {
    suspend fun fetchMealsForDay(userId: Int, date: String): List<Meal>
    suspend fun getAllSessionsForUser(userId: Int): List<WorkoutSession>
    suspend fun getExercisesWithMuscleGroups(): List<Exercise>
    suspend fun insertSessionForUser(userId: Int, workoutSession: WorkoutSession)
    suspend fun searchFood(food: String): List<FoodItemRecord>
    suspend fun insertMeal(meal: Meal, userId: Int, foodId: Int)
    suspend fun insertWorkoutProgress(userId: Int, sessionId: Int, exerciseWeightUpdates: List<ExerciseWeightUpdate>)
    suspend fun onFinishSession(session: WorkoutSession, updatedWeights: Map<String, Double>)
    suspend fun registerUser(name: String, email: String, password: String): Int
    suspend fun trackDailyCalories(userId: Int, date: String, caloriesBurned: Int)
    suspend fun calculateDailyCaloriesConsumed(userId: Int, date: String): Int
    suspend fun calculateDailyExerciseCalories(userId: Int, date: String): Int
    suspend fun calculateDailyCalorieGoal(userId: Int): Int
    suspend fun insertUserInfo(userId: Int, weight: Float, age: Int, bodyFat: Float, height: Float, gender: String)
    suspend fun getUserSettings(userId: Int): UserSettings?
    suspend fun updateUserSettings(settings: UserSettings)
    suspend fun authenticateUser(email: String, password: String): Int?
    suspend fun getAllUserExerciseProgress(userId: Int): Map<String, List<Pair<String, Double>>>
}