
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import tech.resilientgym.resilient.Exercise
import tech.resilientgym.resilient.ExerciseSet
import tech.resilientgym.resilient.ExerciseWeightUpdate
import tech.resilientgym.resilient.FoodItemRecord
import tech.resilientgym.resilient.Meal
import tech.resilientgym.resilient.SessionExercise
import tech.resilientgym.resilient.UserSettings
import tech.resilientgym.resilient.WorkoutSession
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.time.LocalDate
import java.util.Properties

actual class DatabaseHandler {
    // I miss hibernate :(((
    // java >>> kotlin

    //db conn
    private val jdbcUrl = "jdbc:postgresql://resilient-gym-do-user-15687110-0.c.db.ondigitalocean.com:25060/defaultdb"
    private val username = "doadmin"
    private val password = "AVNS_Y_T4UqSpppCpwsesTQZ"

    // Initialize connection properties within an init block or function
    private fun initConnectionProperties(): Properties {
        val props = Properties()
        props.setProperty("user", username)
        props.setProperty("password", password)
        props.setProperty("ssl", "true")
        props.setProperty("sslmode", "require")
        props.setProperty("sslrootcert", "/Resilient/composeApp/src/commonMain/resources/ca-certificate.crt")
        return props
    }
    private fun initConnection(): Connection {
        val props = initConnectionProperties()
        return DriverManager.getConnection(jdbcUrl, props)
    }

    actual suspend fun fetchMealsForDay(userId: Int, date: String): List<Meal> {

        // get the users meals for that day
        val meals = mutableListOf<Meal>()
        val sql = """
    SELECT m.meal_id, m.meal_type, m.date, f.name as food_name,
           (f.calories / f.default_serving_size * m.serving_size_used) * m.number_of_servings as total_calories,
           m.number_of_servings, m.serving_size_used
    FROM meals m
    JOIN food f ON m.food_id = f.food_id
    WHERE m.user_id = ? AND m.date = ?
    """

        val localDate = LocalDate.parse(date)
        val sqlDate = java.sql.Date.valueOf(localDate.toString())
        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                val preparedStatement = connection.prepareStatement(sql).apply {
                    setInt(1, userId)
                    setDate(2, sqlDate)
                }

                val resultSet = preparedStatement.executeQuery()
                while (resultSet.next()) {
                    val mealId = resultSet.getInt("meal_id")
                    val mealType = resultSet.getString("meal_type")
                    val mealDate = resultSet.getDate("date").toString()
                    val foodName = resultSet.getString("food_name")
                    val totalCalories = resultSet.getDouble("total_calories") // Now calculated correctly
                    val numberOfServings = resultSet.getInt("number_of_servings")
                    val servingSizeUsed = resultSet.getDouble("serving_size_used")

                    meals.add(
                        Meal(mealId, mealType, mealDate, foodName, totalCalories.toInt(), numberOfServings, servingSizeUsed)
                    )
                }
                meals
            }
        }
    }

    actual suspend fun getAllSessionsForUser(userId: Int): List<WorkoutSession> {
        //get the users workout sessions
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
        //cant bother to explain this convoluted shit

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

                            sessions[sessionId] = workoutSession.copy(exercises = updatedExercises)
                        }
                    }
                }

                sessions.values.toList()
            }
        }

    }

    actual suspend fun getExercisesWithMuscleGroups(): List<Exercise> {
        //get the session with the muscle groups
        val exercises = mutableListOf<Exercise>()
        val sql = """
            SELECT e.exercise, emg.muscle_group_name
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
                    if (muscleGroupName !in existingExercise.muscleGroups) {
                        val updatedMuscleGroups = existingExercise.muscleGroups.toMutableList().apply { add(muscleGroupName) }
                        val updatedExercise = existingExercise.copy(muscleGroups = updatedMuscleGroups)
                        exercises[exercises.indexOf(existingExercise)] = updatedExercise
                    }
                } else {
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
            //only way to make it work

            connection.autoCommit = false

            try {
                withContext(Dispatchers.IO) {
                    // workout_sessions
                    connection.prepareStatement(sqlInsertSession).use { sessionPreparedStatement ->
                        sessionPreparedStatement.setInt(1, userId)
                        sessionPreparedStatement.setDate(2, java.sql.Date.valueOf(workoutSession.sessionDate))
                        sessionPreparedStatement.setString(3, workoutSession.sessionTitle)
                        val rs = sessionPreparedStatement.executeQuery()
                        if (rs.next()) {
                            val sessionId = rs.getInt(1)

                            workoutSession.exercises.forEach { exercise ->
                                // session_exercises
                                connection.prepareStatement(sqlInsertExercise).use { exercisePreparedStatement ->
                                    exercisePreparedStatement.setInt(1, sessionId)
                                    exercisePreparedStatement.setString(2, exercise.exerciseId)
                                    val exerciseRs = exercisePreparedStatement.executeQuery()
                                    if (exerciseRs.next()) {
                                        val sessionExerciseId = exerciseRs.getInt(1)

                                        exercise.sets.forEach { set ->
                                            // exercise_sets
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

                    connection.commit()
                }
            } catch (e: Exception) {
                connection.rollback() // Should never fail, but if it does we are fucked
                throw e
            } finally {
                connection.autoCommit = true
            }
        }
    }

    actual suspend fun searchFood(food: String): List<FoodItemRecord> {
        //search food by barcode and name
        val foods = mutableListOf<FoodItemRecord>()
        val sql = """
        SELECT food_id, barcode, name, calories, carbs, protein, fat, default_serving_size
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
                    val servingSize = resultSet.getFloat("default_serving_size")

                    foods.add(
                        FoodItemRecord(
                            foodId = foodId,
                            barcode = barcode,
                            name = name,
                            calories = calories,
                            carbs = carbs,
                            protein = protein,
                            fat = fat,
                            servingSize = servingSize
                        )
                    )
                }
                foods
            }
        }
    }

    actual suspend fun insertMeal(meal: Meal, userId: Int, foodId: Int) {
        //insert meal for date
        val sql = """
        INSERT INTO meals (user_id, meal_type, date, food_id, number_of_servings, serving_size_used)
        VALUES (?, ?, ?, ?, ?, ?)
    """

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(sql).use { preparedStatement ->
                    preparedStatement.setInt(1, userId)
                    preparedStatement.setString(2, meal.mealType)
                    preparedStatement.setDate(3, java.sql.Date.valueOf(meal.date))
                    preparedStatement.setInt(4, foodId)
                    preparedStatement.setInt(5, meal.numberOfServings)
                    preparedStatement.setDouble(6, meal.serving_size_used)

                    preparedStatement.executeUpdate() > 0
                }
            }
        }
    }

    actual suspend fun insertWorkoutProgress(userId: Int, sessionId: Int, exerciseWeightUpdates: List<ExerciseWeightUpdate>) {
        initConnection().use { connection ->
            val sqlInsertProgressSession = """
            INSERT INTO workout_progress_sessions (session_id, user_id, session_date)
            VALUES (?, ?, CURRENT_DATE)
            RETURNING progress_session_id;
        """

            val sqlUpsertWeightProgress = """
            INSERT INTO exercise_weight_progress (progress_session_id, exercise_id, max_weight)
            VALUES (?, ?, ?)
            ON CONFLICT (progress_session_id, exercise_id)
            DO UPDATE SET max_weight = EXCLUDED.max_weight;
        """

            connection.autoCommit = false

            try {
                withContext(Dispatchers.IO) {
                    //workout_progress_sessions
                    var progressSessionId: Int? = null
                    connection.prepareStatement(sqlInsertProgressSession).use { progressSessionStmt ->
                        progressSessionStmt.setInt(1, sessionId)
                        progressSessionStmt.setInt(2, userId)
                        val rs = progressSessionStmt.executeQuery()
                        if (rs.next()) {
                            progressSessionId = rs.getInt(1)
                        }
                    }

                    // Check if progressSessionId is retrieved
                    progressSessionId?.let { sessionId ->
                        // Inserting or updating exercise_weight_progress
                        exerciseWeightUpdates.forEach { update ->
                            connection.prepareStatement(sqlUpsertWeightProgress).use { weightProgressStmt ->
                                weightProgressStmt.setInt(1, sessionId)
                                weightProgressStmt.setString(2, update.exerciseId)
                                weightProgressStmt.setDouble(3, update.maxWeight)
                                weightProgressStmt.executeUpdate()
                            }
                        }
                    }

                    connection.commit()
                }
            } catch (e: Exception) {
                connection.rollback()
                throw e
            } finally {
                connection.autoCommit = true
            }
        }
    }

    actual suspend fun onFinishSession(session: WorkoutSession, updatedWeights: Map<String, Double>) {
        // save sessions things
        val insertSessionSql = """
        INSERT INTO workout_progress_sessions (session_id, user_id, session_date)
        VALUES (?, ?, ?)
        RETURNING progress_session_id
    """

        val insertWeightProgressSql = """
        INSERT INTO exercise_weight_progress (progress_session_id, exercise_id, max_weight)
        VALUES (?, ?, ?)
    """

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->

                val progressSessionId = connection.prepareStatement(insertSessionSql).use { preparedStatement ->
                    preparedStatement.setInt(1, session.sessionId)
                    preparedStatement.setInt(2, session.userId)
                    preparedStatement.setDate(3, java.sql.Date.valueOf(session.sessionDate))

                    val resultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) resultSet.getInt(1) else null
                }

                // Check if the progress_session_id was successfully retrieved
                progressSessionId?.let { id ->

                    updatedWeights.forEach { (exerciseId, maxWeight) ->
                        connection.prepareStatement(insertWeightProgressSql).use { preparedStatement ->
                            preparedStatement.setInt(1, id)
                            preparedStatement.setString(2, exerciseId)
                            preparedStatement.setDouble(3, maxWeight)

                            preparedStatement.executeUpdate()
                        }
                    }
                } ?: throw Exception("Failed to get id lol")
            }
        }
    }

    actual suspend fun registerUser(name: String, email: String, password: String): Int {
        //regirtration and returning their new ID
        val insertUserSql = """
    INSERT INTO users (name, email, password_hash)
    VALUES (?, ?, ?);
    """

        val passwordHash = hashPassword(password)

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS).use { preparedStatement ->
                    preparedStatement.setString(1, name)
                    preparedStatement.setString(2, email)
                    preparedStatement.setString(3, passwordHash)

                    val affectedRows = preparedStatement.executeUpdate()
                    if (affectedRows == 0) {
                        throw SQLException("Creating user failed, no rows affected.")
                    }

                    // get ID
                    preparedStatement.generatedKeys.use { generatedKeys ->
                        if (generatedKeys.next()) {
                            generatedKeys.getInt(1)
                        } else {
                            throw SQLException("Creating user failed, no ID obtained.")
                        }
                    }
                }
            }
        }
    }


    fun hashPassword(password: String): String {
        //cant steal passwords :((
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    actual suspend fun trackDailyCalories(userId: Int, date: String, caloriesBurned: Int) {
        // QUICK ADD CALORIES NO CLUE WHY I NAMED IT TRACK DAILY CALORIES
        val insertCalorieTrackingSql = """
        INSERT INTO daily_calorie_tracking (user_id, date, calories_burned)
        VALUES (?, ?, ?);
    """

        withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(insertCalorieTrackingSql).use { preparedStatement ->
                    preparedStatement.setInt(1, userId)
                    preparedStatement.setDate(2, java.sql.Date.valueOf(date))
                    preparedStatement.setInt(3, caloriesBurned)

                    // Execute the update. Since we're not returning anything, we don't need to process the ResultSet.
                    val affectedRows = preparedStatement.executeUpdate()
                    if (affectedRows == 0) {
                        throw SQLException("Tracking daily calories failed, no rows affected.")
                    }
                }
            }
        }
    }

    actual suspend fun calculateDailyCaloriesConsumed(userId: Int, date: String): Int {
        //how much they eat yk
        val queryTotalCaloriesSql = """
        SELECT SUM(f.calories * m.number_of_servings * (m.serving_size_used / 100.0)) AS total_calories
        FROM meals m
        JOIN food f ON m.food_id = f.food_id
        WHERE m.user_id = ?
        AND m.date = ?;
    """

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(queryTotalCaloriesSql).use { preparedStatement ->
                    preparedStatement.setInt(1, userId)
                    preparedStatement.setDate(2, java.sql.Date.valueOf(date))

                    // Execute the query and process the ResultSet
                    val resultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        resultSet.getInt("total_calories")
                    } else {
                        0 // If no data is returned, it could mean no meals were logged for that day.
                    }
                }
            }
        }
    }

    actual suspend fun calculateDailyExerciseCalories(userId: Int, date: String): Int {
        val queryTotalExerciseCaloriesSql = """
        SELECT SUM(calories_burned) AS total_exercise_calories
        FROM daily_calorie_tracking
        WHERE user_id = ?
        AND date = ?;
    """

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(queryTotalExerciseCaloriesSql).use { preparedStatement ->
                    preparedStatement.setInt(1, userId)
                    preparedStatement.setDate(2, java.sql.Date.valueOf(date))

                    val resultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        resultSet.getInt("total_exercise_calories")
                    } else {
                        0 // if nun, maybe no exercise for the day
                    }
                }
            }
        }
    }

    actual suspend fun calculateDailyCalorieGoal(userId: Int): Int {
        // Query to get user info including the fitness goal
        val queryUserInfoSql = """
    SELECT weight, height, age, gender, fitness_goal
    FROM user_info
    WHERE user_id = ?;
    """

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(queryUserInfoSql).use { preparedStatement ->
                    preparedStatement.setInt(1, userId)

                    val resultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        val weight = resultSet.getDouble("weight")
                        val height = resultSet.getDouble("height")
                        val age = resultSet.getInt("age")
                        val gender = resultSet.getString("gender")
                        val fitnessGoal = resultSet.getString("fitness_goal")

                        calculateCalorieGoal(weight, height, age, gender, fitnessGoal)
                    } else {
                        throw SQLException("User info not found for user_id $userId")
                    }
                }
            }
        }
    }

    fun calculateCalorieGoal(weight: Double, height: Double, age: Int, gender: String, fitnessGoal: String): Int {
        val bmr = if (gender.lowercase() == "male") {
            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
        } else {
            447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
        }

        // Determine calorie adjustment based on the fitness goal
        val adjustment = when (fitnessGoal.lowercase()) {
            "cut" -> 0.80 // Example: 20% calorie deficit for cutting
            "bulk" -> 1.20 // Example: 20% calorie surplus for bulking
            else -> 1.0 // Maintain
        }

        val maintenanceCalories = bmr * 1.375 // Adjust this multiplier based on actual activity level
        return (maintenanceCalories * adjustment).toInt()
    }


    actual suspend fun insertUserInfo(userId: Int, weight: Float, age: Int, bodyFat: Float, height: Float, gender: String) {
        //after register users measurements
        val insertUserInfoSql = """
    INSERT INTO user_info (user_id, weight, age, bodyfat, height, gender)
    VALUES (?, ?, ?, ?, ?, ?);
    """

        withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(insertUserInfoSql).use { preparedStatement ->
                    preparedStatement.setInt(1, userId)
                    preparedStatement.setFloat(2, weight)
                    preparedStatement.setInt(3, age)
                    preparedStatement.setFloat(4, bodyFat)
                    preparedStatement.setFloat(5, height)
                    preparedStatement.setString(6, gender)

                    val affectedRows = preparedStatement.executeUpdate()
                    if (affectedRows == 0) {
                        throw SQLException("Inserting user info failed, no rows affected.")
                    }
                }
            }
        }
    }

    actual suspend fun getUserSettings(userId: Int): UserSettings? {
        val queryUserSettingsSql = """
        SELECT u.user_id, u.name, u.email, ui.weight, ui.age, ui.gender, ui.bodyfat, ui.height, ui.fitness_goal
        FROM users u
        LEFT JOIN user_info ui ON u.user_id = ui.user_id
        WHERE u.user_id = ?;
        """


        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(queryUserSettingsSql).use { preparedStatement ->
                    preparedStatement.setInt(1, userId)

                    val resultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        UserSettings(
                            userId = resultSet.getInt("user_id"),
                            name = resultSet.getString("name"),
                            email = resultSet.getString("email"),
                            gender = resultSet.getString("gender"), // Retrieve gender from the result set
                            weight = resultSet.getBigDecimal("weight")?.toDouble(),
                            age = resultSet.getInt("age"),
                            bodyfat = resultSet.getBigDecimal("bodyfat")?.toDouble(),
                            height = resultSet.getBigDecimal("height")?.toDouble(),
                            fitnessGoal = resultSet.getString("fitness_goal")
                        )
                    } else {
                        null // if null, user settings not found
                    }
                }
            }
        }
    }

    actual suspend fun updateUserSettings(settings: UserSettings) {
        // SQL query to update the users table
        val updateUserSql = """
    UPDATE users
    SET name = ?, email = ?
    WHERE user_id = ?;
    """

        // SQL query to update the user_info table
        val updateUserInfoSql = """
    UPDATE user_info
    SET weight = ?, age = ?, bodyfat = ?, height = ?, gender = ?, fitness_goal = ?
    WHERE user_id = ?;
    """

        withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.autoCommit = false

                try {
                    // Update users table
                    connection.prepareStatement(updateUserSql).use { updateUserStmt ->
                        updateUserStmt.setString(1, settings.name)
                        updateUserStmt.setString(2, settings.email)
                        updateUserStmt.setInt(3, settings.userId)
                        updateUserStmt.executeUpdate()
                    }

                    // Update user_info table
                    connection.prepareStatement(updateUserInfoSql).use { updateUserInfoStmt ->
                        updateUserInfoStmt.setObject(1, settings.weight, java.sql.Types.DOUBLE) // Null safe
                        updateUserInfoStmt.setObject(2, settings.age, java.sql.Types.INTEGER)
                        updateUserInfoStmt.setObject(3, settings.bodyfat, java.sql.Types.DOUBLE)
                        updateUserInfoStmt.setObject(4, settings.height, java.sql.Types.DOUBLE)
                        updateUserInfoStmt.setString(5, settings.gender) // Assuming gender can be NULL
                        updateUserInfoStmt.setString(6, settings.fitnessGoal) // Set the fitness goal
                        updateUserInfoStmt.setInt(7, settings.userId) // Adjusted to be the seventh parameter
                        updateUserInfoStmt.executeUpdate()
                    }

                    // Commit the transaction
                    connection.commit()
                } catch (e: Exception) {
                    // If there is an exception, roll back the transaction
                    connection.rollback()
                    throw e // Re-throw the exception to be handled elsewhere
                } finally {
                    // Reset auto-commit to its default state
                    connection.autoCommit = true
                }
            }
        }
    }

    actual suspend fun authenticateUser(email: String, password: String): Int? {
        val queryUserSql = """
        SELECT user_id, password_hash FROM users WHERE email = ?;
    """

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(queryUserSql).use { preparedStatement ->
                    preparedStatement.setString(1, email)

                    val resultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        val storedPasswordHash = resultSet.getString("password_hash")
                        if (BCrypt.checkpw(password, storedPasswordHash)) {
                            resultSet.getInt("user_id")
                        } else {
                            null // Password does not match
                        }
                    } else {
                        null // User not found
                    }
                }
            }
        }
    }

    actual suspend fun getAllUserExerciseProgress(userId: Int): Map<String, List<Pair<String, Double>>> {
        val queryAllExerciseProgressSql = """
            SELECT e.exercise, wps.session_date, ewp.max_weight
            FROM workout_progress_sessions wps
            JOIN exercise_weight_progress ewp ON wps.progress_session_id = ewp.progress_session_id
            JOIN exercises e ON ewp.exercise_id = e.exercise
            WHERE wps.user_id = ?
            ORDER BY e.exercise, wps.session_date DESC;
        """

        return withContext(Dispatchers.IO) {
            initConnection().use { connection ->
                connection.prepareStatement(queryAllExerciseProgressSql).use { preparedStatement ->
                    preparedStatement.setInt(1, userId)
                    val resultSet = preparedStatement.executeQuery()
                    val results = mutableMapOf<String, MutableList<Pair<String, Double>>>()
                    while (resultSet.next()) {
                        // Corrected to use "exercise" as per the SELECT statement in the SQL query
                        val exerciseName = resultSet.getString("exercise")
                        val sessionDate = resultSet.getDate("session_date").toString()
                        val maxWeight = resultSet.getDouble("max_weight")
                        results.getOrPut(exerciseName) { mutableListOf() }.add(sessionDate to maxWeight)
                    }
                    results
                }
            }
        }
    }

}