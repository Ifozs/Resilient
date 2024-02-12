package tech.resilientgym.resilient

data class UserSettings( val userId: Int,
                         val name: String,
                         val email: String,
                         val gender: String?,
                         val weight: Double?,
                         val age: Int?,
                         val bodyfat: Double?,
                         val height: Double?,
                         val fitnessGoal: String?)
