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
}
