import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.resilientgym.resilient.Exercise
import tech.resilientgym.resilient.ExerciseSet
import tech.resilientgym.resilient.FoodItemRecord
import tech.resilientgym.resilient.Meal
import tech.resilientgym.resilient.SessionExercise
import tech.resilientgym.resilient.WorkoutSession
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

actual class DatabaseHandler {

    private val jdbcUrl = "jdbc:postgresql://10.0.2.2:5432/postgres"
    private val username = "postgres"
    private val password = "root"

    private fun initConnection(): Connection {
        return DriverManager.getConnection(jdbcUrl, username, password)
    }
    actual suspend fun fetchMealsForDay(userId: Int, date: String): List<Meal> {
        val meals = mutableListOf<Meal>()
        val sql = """
            SELECT m.meal_id, m.meal_type, m.date, f.name, f.calories
            FROM meals m
            JOIN food f ON m.food_id = f.food_id
            WHERE m.user_id = ? AND m.date = ?
        """

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                val preparedStatement = connection.prepareStatement(sql).apply {
                    setInt(1, userId)
                    setDate(2, java.sql.Date.valueOf(date))
                }

                val resultSet = preparedStatement.executeQuery()
                while (resultSet.next()) {
                    val mealId = resultSet.getInt("meal_id")
                    val mealType = resultSet.getString("meal_type")
                    val mealDate = resultSet.getDate("date").toString()
                    val foodName = resultSet.getString("name")
                    val calories = resultSet.getInt("calories")

                    meals.add(
                        Meal(mealId, mealType, mealDate, foodName, calories)
                    )
                }
                meals
            }
        }
    }


    actual suspend fun getAllSessionsForUser(userId: Int): List<WorkoutSession> {
        val sessions = mutableMapOf<Int, WorkoutSession>()

        val sql = """
            SELECT 
                ws.session_id, ws.user_id, ws.session_date, ws.session_title, 
                se.session_exercise_id, se.exercise_id, 
                es.set_id, es.set_number, es.weight, es.reps
            FROM workout_sessions ws
            LEFT JOIN session_exercises se ON ws.session_id = se.session_id
            LEFT JOIN exercise_sets es ON se.session_exercise_id = es.session_exercise_id
            WHERE ws.user_id = ?
            ORDER BY ws.session_date DESC, se.session_exercise_id, es.set_number
        """
        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                val preparedStatement = connection.prepareStatement(sql).apply {
                    setInt(1, userId)
                }

                val resultSet = preparedStatement.executeQuery()
                while (resultSet.next()) {
                    val sessionId = resultSet.getInt("session_id")
                    val sessionDate = resultSet.getDate("session_date").toString()
                    val sessionTitle = resultSet.getString("session_title")

                    // Get the existing session or create a new one
                    val workoutSession = sessions[sessionId] ?: WorkoutSession(
                        sessionId = sessionId,
                        userId = userId,
                        sessionDate = sessionDate,
                        sessionTitle = sessionTitle,
                        exercises = listOf()
                    )

                    val sessionExerciseId = resultSet.getInt("session_exercise_id")
                    val exerciseId = resultSet.getString("exercise_id")

                    if (sessionExerciseId != 0) {
                        val foundSessionExercise = workoutSession.exercises.find { it.sessionExerciseId == sessionExerciseId }
                        val sessionExercise = foundSessionExercise ?: SessionExercise(
                            sessionExerciseId = sessionExerciseId,
                            sessionId = sessionId,
                            exerciseId = exerciseId,
                            sets = mutableListOf()
                        )

                        val setNumber = resultSet.getInt("set_number")
                        val weight = resultSet.getDouble("weight")
                        val reps = resultSet.getInt("reps")

                        val exerciseSet = ExerciseSet(
                            setId = resultSet.getInt("set_id"),
                            sessionExerciseId = sessionExerciseId,
                            setNumber = setNumber,
                            weight = weight,
                            reps = reps
                        )

                        if (foundSessionExercise == null || foundSessionExercise.sets.none { it.setId == exerciseSet.setId }) {
                            val updatedSets = sessionExercise.sets.toMutableList().apply { add(exerciseSet) }
                            val updatedSessionExercise = sessionExercise.copy(sets = updatedSets)

                            val updatedExercises = workoutSession.exercises.toMutableList().apply {
                                val index = indexOfFirst { it.sessionExerciseId == sessionExerciseId }
                                if (index != -1) this[index] = updatedSessionExercise else add(updatedSessionExercise)
                            }

                            // Update the session in the sessions map
                            sessions[sessionId] = workoutSession.copy(exercises = updatedExercises)
                        }
                    }
                }

                sessions.values.toList()
            }
        }

    }

    actual suspend fun getExercisesWithMuscleGroups(): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        val sql = """
        SELECT e.exercise, e.calories, emg.muscle_group_name
        FROM exercises e
        JOIN exercise_muscle_groups emg ON e.exercise = emg.exercise_name
    """

        initConnection().use { connection ->
            val preparedStatement = connection.prepareStatement(sql)
            val resultSet = preparedStatement.executeQuery()
            while (resultSet.next()) {
                val exerciseName = resultSet.getString("exercise")
                val muscleGroupName = resultSet.getString("muscle_group_name")

                val existingExercise = exercises.find { it.name == exerciseName }
                if (existingExercise != null) {
                    // If the exercise already exists, add the muscle group to it
                    if (muscleGroupName !in existingExercise.muscleGroups) {
                        val updatedMuscleGroups = existingExercise.muscleGroups.toMutableList().apply { add(muscleGroupName) }
                        val updatedExercise = existingExercise.copy(muscleGroups = updatedMuscleGroups)
                        exercises[exercises.indexOf(existingExercise)] = updatedExercise
                    }
                } else {
                    // If the exercise does not exist, create a new one
                    val newExercise = Exercise(name = exerciseName, muscleGroups = listOf(muscleGroupName))
                    exercises.add(newExercise)
                }
            }
        }
        return exercises
    }

    actual suspend fun insertSessionForUser(userId: Int, workoutSession: WorkoutSession) {
        initConnection().use { connection ->
            val sqlInsertSession = """
                INSERT INTO workout_sessions (user_id, session_date, session_title)
                VALUES (?, ?, ?)
                RETURNING session_id;
            """

            val sqlInsertExercise = """
                INSERT INTO session_exercises (session_id, exercise_id)
                VALUES (?, ?)
                RETURNING session_exercise_id;
            """

            val sqlInsertSet = """
                INSERT INTO exercise_sets (session_exercise_id, set_number, weight, reps)
                VALUES (?, ?, ?, ?);
            """

            connection.autoCommit = false  // Start transaction

            try {
                withContext(Dispatchers.IO) {
                    // Inserting into workout_sessions
                    connection.prepareStatement(sqlInsertSession).use { sessionPreparedStatement ->
                        sessionPreparedStatement.setInt(1, userId)
                        sessionPreparedStatement.setDate(2, java.sql.Date.valueOf(workoutSession.sessionDate))
                        sessionPreparedStatement.setString(3, workoutSession.sessionTitle)
                        val rs = sessionPreparedStatement.executeQuery()
                        if (rs.next()) {
                            val sessionId = rs.getInt(1)

                            workoutSession.exercises.forEach { exercise ->
                                // Inserting into session_exercises
                                connection.prepareStatement(sqlInsertExercise).use { exercisePreparedStatement ->
                                    exercisePreparedStatement.setInt(1, sessionId)
                                    exercisePreparedStatement.setString(2, exercise.exerciseId)
                                    val exerciseRs = exercisePreparedStatement.executeQuery()
                                    if (exerciseRs.next()) {
                                        val sessionExerciseId = exerciseRs.getInt(1)

                                        exercise.sets.forEach { set ->
                                            // Inserting into exercise_sets
                                            connection.prepareStatement(sqlInsertSet).use { setPreparedStatement ->
                                                setPreparedStatement.setInt(1, sessionExerciseId)
                                                setPreparedStatement.setInt(2, set.setNumber)
                                                setPreparedStatement.setDouble(3, set.weight)
                                                setPreparedStatement.setInt(4, set.reps)
                                                setPreparedStatement.executeUpdate()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    connection.commit() // Commit transaction
                }
            } catch (e: Exception) {
                connection.rollback() // Rollback in case of an error
                throw e
            } finally {
                connection.autoCommit = true  // Reset auto-commit
            }
        }
    }

    actual suspend fun searchFood(food: String): List<FoodItemRecord> {
        val foods = mutableListOf<FoodItemRecord>()
        val sql = """
        SELECT food_id, barcode, name, calories, carbs, protein, fat
        FROM food
        WHERE name ILIKE ? OR barcode ILIKE ?  -- ILIKE for case-insensitive search
    """

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                val preparedStatement = connection.prepareStatement(sql).apply {
                    setString(1, "%$food%")
                    setString(2, "%$food%")
                }

                val resultSet = preparedStatement.executeQuery()
                while (resultSet.next()) {
                    val foodId = resultSet.getInt("food_id")
                    val barcode = resultSet.getString("barcode")
                    val name = resultSet.getString("name")
                    val calories = resultSet.getInt("calories")
                    val carbs = resultSet.getInt("carbs")
                    val protein = resultSet.getInt("protein")
                    val fat = resultSet.getInt("fat")

                    foods.add(
                        FoodItemRecord(
                            foodId = foodId,
                            barcode = barcode,
                            name = name,
                            calories = calories,
                            carbs = carbs,
                            protein = protein,
                            fat = fat
                        )
                    )
                }
                foods
            }
        }
    }


}