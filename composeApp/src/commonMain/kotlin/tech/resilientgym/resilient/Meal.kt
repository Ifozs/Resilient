package tech.resilientgym.resilient

data class Meal(val mealId: Int,
                val mealType: String,
                val date: String,
                val foodName: String,
                val calories: Int,
                val numberOfServings: Int,
                val serving_size_used: Double)
