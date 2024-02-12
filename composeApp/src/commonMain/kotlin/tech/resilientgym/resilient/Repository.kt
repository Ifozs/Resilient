package tech.resilientgym.resilient

import DatabaseHandler

class Repository(private val databaseHandler: DatabaseHandler) {

    suspend fun fetchMealsForDay(userId: Int, date: String): List<Meal> {
        return  databaseHandler.fetchMealsForDay(userId, date)
    }

    suspend fun getAllSessionsForUser(userId: Int): List<WorkoutSession>{
        return databaseHandler.getAllSessionsForUser(userId)
    }

    suspend fun getExercisesWithMuscleGroups(): List<Exercise>{
        return databaseHandler.getExercisesWithMuscleGroups()
    }

    suspend fun insertSessionForUser(userId: Int, workoutSession: WorkoutSession){
        databaseHandler.insertSessionForUser(userId, workoutSession)
    }

    suspend fun searchFood(food: String): List<FoodItemRecord>{
        return databaseHandler.searchFood(food)
    }

    suspend fun insertMeal(meal: Meal, userId: Int, foodId: Int){
        databaseHandler.insertMeal(meal, userId, foodId)
    }

    suspend fun insertWorkoutProgress(userId: Int, sessionId: Int, exerciseWeightUpdates: List<ExerciseWeightUpdate>){
        databaseHandler.insertWorkoutProgress(userId, sessionId, exerciseWeightUpdates)
    }

    suspend fun onFinishSession(session: WorkoutSession, updatedWeights: Map<String, Double>){
        databaseHandler.onFinishSession(session, updatedWeights)
    }

    suspend fun registerUser(name: String, email: String, password: String): Int{
        return databaseHandler.registerUser(name, email, password)
    }

    suspend fun trackDailyCalories(userId: Int, date: String, caloriesBurned: Int){
        databaseHandler.trackDailyCalories(userId, date, caloriesBurned)
    }

    suspend fun insertUserInfo(userId: Int, weight: Float, age: Int, bodyFat: Float, height: Float, gender: String) {
        databaseHandler.insertUserInfo(userId, weight, age, bodyFat, height, gender)
    }

    suspend fun getUserSettings(userId: Int): UserSettings?{
        return databaseHandler.getUserSettings(userId)
    }

    suspend fun updateUserSettings(settings: UserSettings){
        databaseHandler.updateUserSettings(settings)
    }

    suspend fun authenticateUser(email: String, password: String): Int?{
        return databaseHandler.authenticateUser(email, password)
    }

    suspend fun getAllUserExerciseProgress(userId: Int): Map<String, List<Pair<String, Double>>>{
        return databaseHandler.getAllUserExerciseProgress(userId);
    }
}
