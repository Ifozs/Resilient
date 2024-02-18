package tech.resilientgym.resilient

import DatabaseHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

enum class ScreenType {
    Dashboard,
    Food,
    WorkoutSessions,
    RoutineCreation,
    ExerciseSelectionScreen,
    Login,
    AddFood,
    AddSelectedFoodScreen,
    StartRoutine,
    Registration,
    UserInfo,
    Settings,
    AdminDashboard
    //screens
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App(userSessionManager: UserSessionManager, barcodeScanner: BarcodeScanner, barcodeResult: String) {
    //probably the worst way to control screens, but cant use view models because of IOS implementation

    val platformDate = PlatformDate()
    MaterialTheme {
        var selectedScreen by remember { mutableStateOf(ScreenType.Dashboard) }
        var isLoggedIn by remember { mutableStateOf(userSessionManager.isLoggedIn()) }
        var selectedExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
        val createRoutine by remember { mutableStateOf<List<SessionExercise>>(emptyList()) }
        var selectedFood by remember { mutableStateOf<FoodItemRecord?>(null) }
        var selectedWorkoutSession by remember { mutableStateOf<WorkoutSession?>(null) }
        var selectedDatesss by remember { mutableStateOf(platformDate.todayAsString()) }

        val handleNavigate: (ScreenType) -> Unit = { screen ->
            selectedScreen = screen
            isLoggedIn = true // Assuming login was successful if this callback is invoked
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (!isLoggedIn) {
                            // Center the title when not logged in
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Resilient", style = MaterialTheme.typography.h6, modifier = Modifier.align(Alignment.Center))
                            }
                        } else if (isLoggedIn && selectedScreen != ScreenType.AdminDashboard && selectedScreen != ScreenType.Registration) {
                            // Center the title when not logged in
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Resilient", style = MaterialTheme.typography.h6, modifier = Modifier.align(Alignment.Center))
                            }
                        }else{
                            // Default alignment when logged in
                            Text(text = "Resilient", style = MaterialTheme.typography.h6)
                        }
                    },
                    navigationIcon = if (isLoggedIn && selectedScreen != ScreenType.AdminDashboard && selectedScreen != ScreenType.Registration) {
                        {
                            //settings icon
                            IconButton(onClick = { selectedScreen = ScreenType.Settings }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                    } else null,
                    actions = {
                        if (isLoggedIn && selectedScreen != ScreenType.AdminDashboard && selectedScreen != ScreenType.Registration) {
                            // Display the formatted date string
                            Text(
                                text = platformDate.formatDateString(selectedDatesss),
                                style = MaterialTheme.typography.subtitle1,
                                // Adjust the minWidth to suit the longest possible date string you expect
                                modifier = Modifier.padding(end = 5.dp).widthIn(min = 85.dp) // Example minWidth
                            )
                            // Previous Day Button
                            IconButton(onClick = {
                                selectedDatesss = platformDate.previousDayAsString(selectedDatesss)
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Day")
                            }
                            // Next Day Button
                            IconButton(onClick = {
                                selectedDatesss = platformDate.nextDayAsString(selectedDatesss)
                            }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next Day")
                            }

                        }
                    }
                )
            },
            bottomBar = {
                if (isLoggedIn && selectedScreen != ScreenType.AdminDashboard && selectedScreen != ScreenType.Registration) {
                    //navigation menu at bottom
                    AppBottomNavigation(selectedScreen) { screen ->
                            selectedScreen = screen
                    }
                }
            }
        ) {
            if (!isLoggedIn) {
                //if not logged in
                selectedScreen = ScreenType.Login
                when (selectedScreen) {

                    ScreenType.Login -> LoginScreen(
                        userSessionManager = userSessionManager,
                        onNavigate = handleNavigate, // Pass the navigation handler
                        onNavigateToRegister = {isLoggedIn = true
                            selectedScreen = ScreenType.Registration}
                    )

                    ScreenType.Registration -> RegistrationScreen(onRegistrationSuccess = { userId ->
                        userSessionManager.createSession(userId)
                        selectedScreen = ScreenType.UserInfo
                    })

                    ScreenType.UserInfo -> UserInfoScreen(userSessionManager,
                        onSaveUserInfo = {
                            // After saving user info, set isLoggedIn to true and navigate to Dashboard
                            isLoggedIn = true
                            selectedScreen = ScreenType.Dashboard
                        }
                    )

                    else -> {}
                }
            } else {

                when (selectedScreen) {
                    ScreenType.Registration -> RegistrationScreen(onRegistrationSuccess = { userId ->
                        userSessionManager.createSession(userId)
                        selectedScreen = ScreenType.UserInfo
                    })

                    ScreenType.UserInfo -> UserInfoScreen(userSessionManager,
                        onSaveUserInfo = {
                            // After saving user info, set isLoggedIn to true and navigate to Dashboard
                            selectedScreen = ScreenType.Dashboard
                        }
                    )

                    ScreenType.AdminDashboard -> AdminDashboardScreen()
                    ScreenType.Settings -> SettingsScreen(userSessionManager)
                    ScreenType.Dashboard -> HomeScreen(userSessionManager, selectedDatesss, unitforScreen = {ScreenType.Dashboard})
                    ScreenType.Food -> FoodsScreen(
                        userSessionManager = userSessionManager,
                        onSelectAddFood = {
                            selectedScreen = ScreenType.AddFood
                        },
                        selectedDatesss
                    )
                    ScreenType.AddFood -> AddFoodScreen(barcodeScanner, barcodeResult){ selectedFoodItem ->
                        selectedFood = selectedFoodItem
                        selectedScreen = ScreenType.AddSelectedFoodScreen
                    }
                    ScreenType.AddSelectedFoodScreen -> AddSelectedFoodScreen(
                        selectedFood = selectedFood,
                        onSave = { meal, foodId ->
                            val dbHandler = DatabaseHandler()
                            val repository = Repository(dbHandler)
                            val userid =  userSessionManager.userId

                            CoroutineScope(Dispatchers.Default).launch {
                                if(userid != null) {
                                    repository.insertMeal(
                                        meal,
                                        userid,
                                        foodId
                                    )
                                }
                            }
                            selectedScreen = ScreenType.Food
                        },
                        selectedDatesss
                    )

                    ScreenType.WorkoutSessions -> SessionsScreen(
                        userSessionManager = userSessionManager,
                        onSelectAddSessions = {
                            selectedScreen = ScreenType.RoutineCreation
                        },
                        onSessionClick = {
                                session ->
                            selectedWorkoutSession = session // Save the selected session
                            selectedScreen = ScreenType.StartRoutine
                        }
                    )
                    ScreenType.StartRoutine -> {

                        val onFinishSession: (WorkoutSession, Map<String, Double>) -> Unit = { session, updatedWeights ->
                            //when a workout is finished ->
                            val exerciseWeightUpdates = updatedWeights.map { (exerciseId, maxWeight) ->
                                ExerciseWeightUpdate(exerciseId, maxWeight)
                            }

                            val dbHandler = DatabaseHandler()
                            // Call insertWorkoutProgress
                            CoroutineScope(Dispatchers.Default).launch {
                                Repository(dbHandler).insertWorkoutProgress(session.userId, session.sessionId, exerciseWeightUpdates)
                                //Repository(dbHandler).insertWorkoutProgress(session.userId, session.sessionId, exerciseWeightUpdates)
                            }

                            selectedScreen = ScreenType.WorkoutSessions
                        }

                        // Call the StartRoutineScreen composable function with the necessary parameters
                        StartRoutineScreen(
                            selectedWorkoutSession = selectedWorkoutSession,
                            onFinishSession = onFinishSession
                        )
                    }

                    ScreenType.RoutineCreation -> RoutineCreationScreen(
                        onCancel = {
                            // Define what should happen when cancel is clicked
                            selectedExercises = emptyList()
                            selectedScreen = ScreenType.WorkoutSessions
                        },
                        onSave = {

                            val dbHandler = DatabaseHandler()

                            CoroutineScope(Dispatchers.Default).launch {
                                Repository(dbHandler).insertSessionForUser(userId = userSessionManager.userId!!, workoutSession = it)
                            }

                            selectedScreen = ScreenType.WorkoutSessions
                        },
                        onAddExercise = {
                            selectedScreen = ScreenType.ExerciseSelectionScreen
                        },
                        selectedExercises = selectedExercises,
                        createRoutine = createRoutine.toMutableList(),
                        selectedDatesss,
                        userId = userSessionManager
                    )
                    ScreenType.ExerciseSelectionScreen -> AddExerciseScreen(
                        onCancel = {
                            selectedScreen = ScreenType.RoutineCreation
                        },
                        onCreate = {
                            //TODO: create a new exercise or navigate to the creation screen
                        },
                        onSelectExercise = { exercise ->
                            selectedExercises = selectedExercises + exercise
                            selectedScreen = ScreenType.RoutineCreation
                        }
                    )
                    else -> {}
                }

            }
        }
    }
}

@Composable
fun AdminDashboardScreen() {
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var exerciseName by remember { mutableStateOf("") }
    var muscleGroupName by remember { mutableStateOf("") }
    var exerciseToMuscle by remember { mutableStateOf("") }
    var associatedMuscleGroupName by remember { mutableStateOf("") }

    val dbHandler = DatabaseHandler()
    val repository = Repository(dbHandler)

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Admin Dashboard", style = MaterialTheme.typography.h5)
        }

        item {
            // Add Food Section
            Text("Add Food", style = MaterialTheme.typography.h6)
            InputField(value = foodName, onValueChange = { foodName = it }, label = "Food Name")
            InputField(value = calories, onValueChange = { calories = it }, label = "Calories")
            InputField(value = carbs, onValueChange = { carbs = it }, label = "Carbs (g)")
            InputField(value = protein, onValueChange = { protein = it }, label = "Protein (g)")
            InputField(value = fat, onValueChange = { fat = it }, label = "Fat (g)")
            Button(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    repository.insertFood(
                        null,
                        foodName,
                        calories.toInt(),
                        carbs.toIntOrNull(),
                        protein.toIntOrNull(),
                        fat.toIntOrNull(),
                        100f // Assuming default serving size is 100g for simplicity
                    )
                    // Reset fields after insertion
                    foodName = ""
                    calories = ""
                    carbs = ""
                    protein = ""
                    fat = ""
                }
            }) {
                Text("Add Food")
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            // Add Exercise Section
            Text("Add Exercise", style = MaterialTheme.typography.h6)
            InputField(value = exerciseName, onValueChange = { exerciseName = it }, label = "Exercise Name")
            Button(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    repository.insertExercise(exerciseName)
                    exerciseName = "" // Reset field after insertion
                }
            }) {
                Text("Add Exercise")
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            // Add Muscle Group Section
            Text("Add Muscle Group", style = MaterialTheme.typography.h6)
            InputField(value = muscleGroupName, onValueChange = { muscleGroupName = it }, label = "Muscle Group Name")
            Button(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    repository.insertMuscleGroup(muscleGroupName)
                    muscleGroupName = "" // Reset field after insertion
                }
            }) {
                Text("Add Muscle Group")
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            // Associate Exercise with Muscle Group Section
            Text("Associate Exercise with Muscle Group", style = MaterialTheme.typography.h6)
            InputField(value = exerciseToMuscle, onValueChange = { exerciseToMuscle = it }, label = "Exercise Name")
            InputField(value = associatedMuscleGroupName, onValueChange = { associatedMuscleGroupName = it }, label = "Muscle Group Name")
            Button(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    repository.insertExerciseMuscleGroup(exerciseToMuscle, associatedMuscleGroupName)
                    exerciseToMuscle = "" // Reset field after insertion
                    associatedMuscleGroupName = ""
                }
            }) {
                Text("Associate Exercise")
            }
        }
    }
}


@Composable
fun InputField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SettingsScreen(userSessionManager: UserSessionManager) {
    //Settings
    val dbHandler = remember { DatabaseHandler() }
    val repository = remember { Repository(dbHandler) }
    var userSettings by remember { mutableStateOf<UserSettings?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = userSessionManager.userId) {
        isLoading = true
        userSettings = repository.getUserSettings(userSessionManager.userId!!)
        isLoading = false
    }

    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize() //center the progress indicator
        ) {
            CircularProgressIndicator()
        }
    } else {
        userSettings?.let { settings ->
            SettingsForm(settings = settings, onUpdateSettings = { updatedSettings -> CoroutineScope(Dispatchers.Default).launch {Repository(dbHandler).updateUserSettings(updatedSettings)}})
        } ?: run {
            // If the settings are null, show an error or a message indicating no settings were found
            Text("No settings found.")
        }
    }
}

@Composable
fun SettingsForm(settings: UserSettings, onUpdateSettings: (UserSettings) -> Unit) {
    var name by remember { mutableStateOf(settings.name) }
    var email by remember { mutableStateOf(settings.email) }
    var weight by remember { mutableStateOf(settings.weight?.toString() ?: "") }
    var age by remember { mutableStateOf(settings.age?.toString() ?: "") }
    var bodyFat by remember { mutableStateOf(settings.bodyfat?.toString() ?: "") }
    var height by remember { mutableStateOf(settings.height?.toString() ?: "") }
    var gender by remember { mutableStateOf(settings.gender ?: "") }
    val genders = listOf("Male", "Female")
    var fitnessGoal by remember { mutableStateOf(settings.fitnessGoal ?: "Maintain") }
    val fitnessGoals = listOf("Bulk", "Cut", "Maintain")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 68.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Settings", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        GroupedTextField(label = "Personal Information", icon = Icons.Default.Person) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        }

        GroupedTextField(label = "Physical Attributes", icon = Icons.Default.Face) {
            OutlinedNumberField(value = weight, onValueChange = { weight = it }, label = "Weight (kg)")
            OutlinedNumberField(value = height, onValueChange = { height = it }, label = "Height (cm)")
            OutlinedNumberField(value = age, onValueChange = { age = it }, label = "Age")
            OutlinedNumberField(value = bodyFat, onValueChange = { bodyFat = it }, label = "Body Fat (%)")
        }

        GenderDropdown(gender, genders, onGenderSelect = { gender = it })
        FitnessGoalDropdown(fitnessGoal, fitnessGoals, onGoalSelect = { fitnessGoal = it })

        Button(onClick = {
            val updatedSettings = settings.copy(
                name = name, email = email, weight = weight.toDoubleOrNull(), age = age.toIntOrNull(),
                bodyfat = bodyFat.toDoubleOrNull(), height = height.toDoubleOrNull(), gender = gender,
                fitnessGoal = fitnessGoal
            )
            onUpdateSettings(updatedSettings)
        }) {
            Text("Save Changes")
        }
    }
}
@Composable
fun GenderDropdown(selectedGender: String, genders: List<String>, onGenderSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = selectedGender,
            onValueChange = { },
            label = { Text("Gender") },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, "Dropdown", Modifier.clickable { expanded = true })
            },
            readOnly = true
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            genders.forEach { gender ->
                DropdownMenuItem(onClick = {
                    onGenderSelect(gender)
                    expanded = false
                }) {
                    Text(text = gender)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun FitnessGoalDropdown(selectedGoal: String, goals: List<String>, onGoalSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = selectedGoal,
            onValueChange = { },
            label = { Text("Fitness Goal") },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown", Modifier.clickable { expanded = true })
            },
            readOnly = true // Makes the text field not editable, turning it into a dropdown
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            goals.forEach { goal ->
                DropdownMenuItem(onClick = {
                    onGoalSelect(goal)
                    expanded = false
                }) {
                    Text(text = goal)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}


// Example implementations for custom components used above
@Composable
fun GroupedTextField(label: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(elevation = 4.dp, modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = label)
            content()
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun OutlinedNumberField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Spacer(modifier = Modifier.height(8.dp))
}

// Implement `GenderDropdown` and `FitnessGoalDropdown` similar to the original dropdown logic,
// potentially refining UI/UX based on the above suggestions.


//save the user_info
@Composable
fun UserInfoScreen(userSessionManager: UserSessionManager, onSaveUserInfo: () -> Unit) {

    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("Male") } // Default selection
    var showDropdownMenu by remember { mutableStateOf(false) }
    val genderOptions = listOf("male", "female")


    //Save func
    fun handleSave() {
        val dbHandler = DatabaseHandler()
        val repository = Repository(dbHandler)

        //convert and check
        val userId = userSessionManager.userId!!
        val weightFloat = weight.toFloatOrNull() ?: return
        val ageInt = age.toIntOrNull() ?: return
        val bodyFatFloat = bodyFat.toFloatOrNull() ?: return
        val heightFloat = height.toFloatOrNull() ?: return

        if (weightFloat <= 0f || weight.toString().count { it == '.' } > 1 || weight.split(".")[0].length > 3) {
            // Show error message or return
            return
        }

        if (ageInt <= 0) {
            // Show error message or return
            return
        }

        if (bodyFatFloat <= 0f || bodyFat.toString().count { it == '.' } > 1 || bodyFat.split(".")[0].length > 3) {
            // Show error message or return
            return
        }

        if (heightFloat <= 0f || height.toString().count { it == '.' } > 1 || height.split(".")[0].length > 3) {
            // Show error message or return
            return
        }

        //fuck main thread
        CoroutineScope(Dispatchers.Default).launch {
            repository.insertUserInfo(userId, weightFloat, ageInt, bodyFatFloat, heightFloat, selectedGender)
        }
        onSaveUserInfo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = bodyFat,
            onValueChange = { bodyFat = it },
            label = { Text("Body Fat (%)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = selectedGender,
            onValueChange = { },
            label = { Text("Gender") },
            readOnly = true, // Make the field read-only
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, "dropdown")
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDropdownMenu = true }
        )
        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            genderOptions.forEach { gender ->
                DropdownMenuItem(onClick = {
                    selectedGender = gender
                    showDropdownMenu = false
                }) {
                    Text(gender)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Save button
        Button(onClick = { handleSave() }, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }
    }
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    return email.matches(emailRegex.toRegex())
}

@Composable
fun RegistrationScreen(
    onRegistrationSuccess: (Int) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var registrationErrorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Register", style = MaterialTheme.typography.h5)

        if (registrationErrorMessage != null) {
            Text(
                text = registrationErrorMessage ?: "",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.body2
            )
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )


        // Validation Functions

        fun isValidPassword(password: String): Boolean {

            return password.length >= 8
        }

        fun isFormValid(): Boolean {
            when {
                username.isBlank() -> {
                    registrationErrorMessage = "Username cannot be empty"
                    return false
                }
                !isValidEmail(email) -> {
                    registrationErrorMessage = "Invalid email format"
                    return false
                }
                !isValidPassword(password) -> {
                    registrationErrorMessage = "Password does not meet criteria"
                    return false
                }
                password != confirmPassword -> {
                    registrationErrorMessage = "Passwords do not match"
                    return false
                }
                else -> {
                    registrationErrorMessage = null
                    return true
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isFormValid()) {
                    val dbHandler = DatabaseHandler()

                    CoroutineScope(Dispatchers.Main).launch {
                        val userId = withContext(Dispatchers.Default) {
                            Repository(dbHandler).registerUser(username, email, password)
                        }

                        if (userId > 0) {
                            onRegistrationSuccess(userId)
                        } else {
                            registrationErrorMessage = "Registration failed, please try again."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword == password
        ) {
            Text("Register")
        }
    }
}

@Composable
fun StartRoutineScreen(
    selectedWorkoutSession: WorkoutSession?,
    onFinishSession: (WorkoutSession, Map<String, Double>) -> Unit
) {
    var elapsedTime by remember { mutableStateOf(0L) } // Elapsed time in seconds
    val coroutineScope = rememberCoroutineScope()
    val updatedWeights = remember { mutableStateMapOf<String, Double>() }

    LaunchedEffect(key1 = "timer") {
        coroutineScope.launch {
            while (true) {
                delay(1000) // 1 sec
                elapsedTime += 1
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedWorkoutSession?.sessionTitle ?: "Unknown Session", style = MaterialTheme.typography.h6) },
                actions = {
                    // Timer Display
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        Text(text = formatElapsedTime(elapsedTime), style = MaterialTheme.typography.body1)
                    }
                    // Finish button
                    if (selectedWorkoutSession != null) {
                        TextButton(onClick = { onFinishSession(selectedWorkoutSession, updatedWeights) }) {
                            Text("Finish", style = MaterialTheme.typography.button.copy(color = MaterialTheme.colors.onPrimary))
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.primarySurface,
                contentColor = MaterialTheme.colors.onPrimary
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (selectedWorkoutSession != null) {
                    WorkoutSessionContent(
                        session = selectedWorkoutSession,
                        updatedWeights = updatedWeights,
                        padding = PaddingValues(16.dp) // Provide some internal padding for content
                    )
                } else {
                    Text(
                        "No session selected",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
        }
    )
}
fun formatElapsedTime(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val formattedHours = if (hours < 10) "0$hours" else "$hours"
    val formattedMinutes = if (minutes < 10) "0$minutes" else "$minutes"
    val formattedSeconds = if (seconds < 10) "0$seconds" else "$seconds"

    return "$formattedHours:$formattedMinutes:$formattedSeconds"
}


@Composable
fun WorkoutSessionContent(
    session: WorkoutSession,
    updatedWeights: SnapshotStateMap<String, Double>,
    padding: PaddingValues
) {
    Column(modifier = Modifier.padding(padding)) {

        // Display session date with an icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Routine creation date",
                tint = MaterialTheme.colors.secondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Routine created on: ${session.sessionDate}",
                style = MaterialTheme.typography.subtitle1
            )
        }

        // Separation line for aesthetic purposes
        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f), thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // List of exercises
        LazyColumn {
            items(session.exercises) { exercise ->
                ExerciseItem(exercise = exercise, updatedWeights = updatedWeights)
                Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: SessionExercise,
    updatedWeights: SnapshotStateMap<String, Double>
) {
    //show exercise
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.exerciseId,
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))

            exercise.sets.forEachIndexed { index, set ->
                var weight by remember { mutableStateOf(set.weight) }

                // Update the shared state map when the weight changes
                LaunchedEffect(weight) {
                    val key = "${exercise.exerciseId}"
                    updatedWeights[key] = weight
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Set ${index + 1}: ${set.reps} reps", style = MaterialTheme.typography.body1)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = weight.toString(),
                        onValueChange = { weightStr ->
                            val newWeight = weightStr.toDoubleOrNull() ?: set.weight
                            weight = newWeight
                        },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AddExerciseScreen(onCancel: () -> Unit, onCreate: () -> Unit, onSelectExercise: (Exercise) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Exercise") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                },
                actions = {
                    TextButton(onClick = onCreate) {
                        Text("Create")
                    }
                }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            //TODO: SearchBar()
            ExerciseList(onSelectExercise)
        }
    }
}

@Composable
fun ExerciseList(onSelectExercise: (Exercise) -> Unit) {
    //shows exercises

    // Define the state for meals
    var exercises by remember { mutableStateOf<List<Exercise>>(listOf()) }
    val dbHandler = DatabaseHandler()

    LaunchedEffect(Unit) {
        exercises = withContext(Dispatchers.Default) {
            Repository(dbHandler).getExercisesWithMuscleGroups()
        }
    }

    LazyColumn {
        items(exercises) { exercise ->
            ExerciseItem(exercise, onSelectExercise)
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: Exercise,
    onSelectExercise: (Exercise) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onSelectExercise(exercise) },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, fontWeight = FontWeight.ExtraBold)
                Text(text = exercise.muscleGroups.joinToString())
            }
            IconButton(
                onClick = { onSelectExercise(exercise) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Select")
            }
        }
    }
}

@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = { /* Handle text changes */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text("Search exercise") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") }
    )
}

@Composable
fun RoutineCreationScreen(
    onCancel: () -> Unit,
    onSave: (WorkoutSession) -> Unit,
    onAddExercise: () -> Unit,
    selectedExercises: List<Exercise>,
    createRoutine: MutableList<SessionExercise>,
    selectedDatesss: String,
    userId: UserSessionManager
) {

    var routine by remember { mutableStateOf(createRoutine) }
    var routineTitle by remember { mutableStateOf("") }

    val usrId = userId.userId

    val handleSave = {
        val workoutSession = WorkoutSession(
            sessionId = 4, // will be generated properly form the db
            userId = usrId!!,
            sessionDate = selectedDatesss,
            sessionTitle = routineTitle,
            exercises = routine.toList()
        )

        onSave(workoutSession) // Call the onSave func
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Routine") },
                navigationIcon = {
                    // Cancel button as an IconButton with text
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = MaterialTheme.colors.onPrimary)
                    }
                },
                actions = {
                    // Save button as a TextButton
                    TextButton(onClick = handleSave) {
                        Text("Save", color = MaterialTheme.colors.onPrimary)
                    }
                },
                backgroundColor = MaterialTheme.colors.primarySurface,
                contentColor = MaterialTheme.colors.onPrimary
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = routineTitle,
                onValueChange = { routineTitle = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            // Iterate through selected exercises to display them
            selectedExercises.forEach { exercise ->
                // Find or create the SessionExercise
                val sessionExerciseIndex = routine.indexOfFirst { it.exerciseId == exercise.name }
                val sessionExercise = if (sessionExerciseIndex != -1) {
                    routine[sessionExerciseIndex]
                } else {
                    SessionExercise(
                        sessionExerciseId = 3, // Placeholder for generating a temporary ID
                        sessionId = 2, // Placeholder for generating a temporary ID
                        exerciseId = exercise.name,
                        sets = mutableListOf()
                    ).also {
                        routine = (routine + it).toMutableList()
                    }
                }

                // Display the exercise name
                Text(exercise.name, style = MaterialTheme.typography.h6)

                // Iterate through the sets of the sessionExercise
                sessionExercise.sets.forEachIndexed { index, set ->
                    // UI for each set
                    SetInputUI(set, onWeightChanged = { newWeight ->
                        // Update the weight in the set
                        val updatedSet = set.copy(weight = newWeight)
                        val updatedSets = sessionExercise.sets.toMutableList().apply {
                            set(index, updatedSet)
                        }
                        // Update the exercise in the routine
                        routine = routine.toMutableList().apply {
                            set(sessionExerciseIndex, sessionExercise.copy(sets = updatedSets))
                        }
                    }, onRepsChanged = { newReps ->
                        // Update the reps in the set
                        val updatedSet = set.copy(reps = newReps)
                        val updatedSets = sessionExercise.sets.toMutableList().apply {
                            set(index, updatedSet)
                        }
                        // Update the exercise in the routine
                        routine = routine.toMutableList().apply {
                            set(sessionExerciseIndex, sessionExercise.copy(sets = updatedSets))
                        }
                    }, onRemove = {
                        // Remove the set from the routine
                        val updatedSets = sessionExercise.sets.toMutableList().apply {
                            removeAt(index)
                        }
                        routine = routine.toMutableList().apply {
                            set(sessionExerciseIndex, sessionExercise.copy(sets = updatedSets))
                        }
                    })
                }

                // Button to add a new set
                Button(onClick = {
                    // Add a new set directly to the sessionExercise in the routine
                    val newSet = ExerciseSet(1, sessionExercise.sessionExerciseId, sessionExercise.sets.size + 1, 0.0, 0)
                    val updatedSets = sessionExercise.sets.toMutableList().apply {
                        add(newSet)
                    }
                    routine = routine.toMutableList().apply {
                        set(sessionExerciseIndex, sessionExercise.copy(sets = updatedSets))
                    }
                }) {
                    Text("Add Set")
                }
            }

            if(selectedExercises.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Get started by adding an exercise to your routine.",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Add exercise")
            }
        }
    }
}

@Composable
fun SetInputUI(
    set: ExerciseSet,
    onWeightChanged: (Double) -> Unit,
    onRepsChanged: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row {
        OutlinedTextField(
            value = set.weight.toString(),
            onValueChange = { newWeight -> onWeightChanged(newWeight.toDoubleOrNull() ?: 0.0) },
            label = { Text("Weight") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = set.reps.toString(),
            onValueChange = { newReps -> onRepsChanged(newReps.toIntOrNull() ?: 0) },
            label = { Text("Reps") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        IconButton(onClick = onRemove) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Set")
        }
    }
}

@Composable
fun SessionsScreen(userSessionManager: UserSessionManager,
                   onSelectAddSessions: () -> Unit,
                   onSessionClick: (WorkoutSession) -> Unit ){
    val databaseHandler = remember { DatabaseHandler() }
    val repository = remember { Repository(databaseHandler) }
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val userId = userSessionManager.userId

    LaunchedEffect(userId) {
        isLoading = true
        try {
            val userSessions = repository.getAllSessionsForUser(userId!!)
            sessions = userSessions
        } catch (e: Exception) {
        } finally {
            isLoading = false
        }
    }


    Scaffold(
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 68.dp)
                ) {
                    items(sessions) { session ->
                        SessionCard(session = session, onSessionClick = { onSessionClick(session) })
                    }
                }

            }
            // Place the 'Add Session' Button in the bottom right corner of the screen
            FloatingActionButton(
                onClick = onSelectAddSessions,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 68.dp, end = 18.dp),
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Session")
            }
        }
    }
}

@Composable
fun SessionCard(session: WorkoutSession,
                onSessionClick: (WorkoutSession) -> Unit ) {
    //show created routines

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = session.sessionTitle,
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Exercises: ${session.exercises.joinToString { it.exerciseId }}",
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSessionClick(session) }, // Use the onSessionClick parameter
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Routine")
            }
        }
    }
}

@Composable
fun LoginScreen(
    userSessionManager: UserSessionManager,
    onNavigate: (ScreenType) -> Unit, // Callback for navigation
    onNavigateToRegister: (ScreenType) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", style = MaterialTheme.typography.h5)

        if (loginErrorMessage != null) {
            Text(
                text = loginErrorMessage ?: "",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.body2
            )
        }

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val dbHandler = DatabaseHandler()
        val coroutineScope = rememberCoroutineScope()

        Button(onClick = {
            if(!isValidEmail(userId)){
                loginErrorMessage = "Please enter a valid email address."
            }else if(password.isBlank()){
                loginErrorMessage = "Password cannot be empty."
            }else {
                coroutineScope.launch {
                    val authResult =
                        Repository(dbHandler).authenticateUser(email = userId, password = password)
                    if (authResult != null) {
                        val (authenticatedUserId, role) = authResult
                        userSessionManager.createSession(authenticatedUserId)
                        if (role == "admin") {
                            onNavigate(ScreenType.AdminDashboard)
                        } else {
                            onNavigate(ScreenType.Dashboard)
                        }
                    } else {
                        loginErrorMessage = "Authentication failed. Please check your credentials."
                    }
                }
            }
        }){
            Text("Log In")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Don't have an account? Register here.",
            modifier = Modifier
                .clickable(onClick = { onNavigateToRegister(ScreenType.Registration) })
                .padding(8.dp),
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.body2
        )
    }
}

data class ExerciseProgress(val date: String, val maxWeight: Double)
@Composable
fun HomeScreen(userSessionManager: UserSessionManager, selectedDates: String, unitforScreen: () -> Unit) {
    val userId = userSessionManager.userId ?: return
    val coroutineScope = rememberCoroutineScope()
    var allExercisesProgress by remember { mutableStateOf<Map<String, List<Pair<String, Double>>>>(emptyMap()) }
    val scrollState = rememberScrollState()
    var caloriesAdded by remember { mutableStateOf(false) }
    val dbHandler = DatabaseHandler()
    var oldDate by remember { mutableStateOf("0") }

    // React to changes in selectedDates
    LaunchedEffect(selectedDates) {
        if (selectedDates != oldDate) {
            unitforScreen()
            oldDate = selectedDates
        }
    }

    LaunchedEffect(key1 = userId) {
        coroutineScope.launch {
            // Fetch all exercises progress
            val repository = Repository(dbHandler)
            val allProgress = repository.getAllUserExerciseProgress(userId)
            allExercisesProgress = allProgress
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(bottom = 68.dp) // Adjust padding as needed
    ) {
        GreetingHeader(selectedDates)
        CalorieCard(caloriesAdded, userSessionManager, selectedDates)
        QuickAdd({
            caloriesAdded = !caloriesAdded
        }, selectedDates, userSessionManager)

        if (allExercisesProgress.isNotEmpty()) {
            AllExercisesProgressCards(allProgressData = allExercisesProgress)
        }
    }
}



@Composable
fun AllExercisesProgressCards(allProgressData: Map<String, List<Pair<String, Double>>>) {
    allProgressData.forEach { (exercise, progressPairs) ->
        val progressData = progressPairs.map { ExerciseProgress(it.first, it.second) }
        if (progressData.isNotEmpty()) {
            ExerciseProgressCard(exercise = exercise, progressData = progressData)
        }
    }
}

@Composable
fun ExerciseProgressCard(exercise: String, progressData: List<ExerciseProgress>) {
    val latestProgress = progressData.maxByOrNull { it.date }
    val previousProgress = progressData.minus(latestProgress).maxByOrNull { it!!.date }
    val improvement = latestProgress?.maxWeight?.minus(previousProgress?.maxWeight ?: 0.0) ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Progress for $exercise",
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (latestProgress != null) {
                Text("Latest: ${latestProgress.date} - ${latestProgress.maxWeight}kg")
            }
            if (previousProgress != null) {
                Text("Previous: ${previousProgress.date} - ${previousProgress.maxWeight}kg")
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (improvement / (latestProgress?.maxWeight ?: 1.0)).toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Improvement $improvement kg",
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
fun QuickAdd(onCaloriesAdded: () -> Unit,
             selectedDatesss: String,
             userSessionManager: UserSessionManager) {
    var text by remember { mutableStateOf("") }

    //quick add for burned calories
    var uid = userSessionManager.userId
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("Quick Add", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = text,
                onValueChange = { newValue ->
                    text = newValue.filter { it.isDigit() }
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                label = { Text("Enter Calories") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (text.isNotEmpty()) {
                        val caloriesBurned = text.toInt()
                        val userId = uid!!
                        val date = selectedDatesss

                        val dbHandler = DatabaseHandler()
                        CoroutineScope(Dispatchers.Default).launch {
                            Repository(dbHandler).trackDailyCalories(userId, date, caloriesBurned)

                        }
                        onCaloriesAdded()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun GreetingHeader(r: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome back!",
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.padding(8.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colors.primary
        ) {
            Text(
                text = "Let's achieve your goals today!",
                style = MaterialTheme.typography.subtitle1.copy(color = Color.White),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun AppBottomNavigation(
    selectedScreen: ScreenType,
    onScreenSelected: (ScreenType) -> Unit
) {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = selectedScreen == ScreenType.Dashboard,
            onClick = { onScreenSelected(ScreenType.Dashboard) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Outlined.Check, contentDescription = "Foods") },
            label = { Text("Foods") },
            selected = selectedScreen == ScreenType.Food,
            onClick = { onScreenSelected(ScreenType.Food) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Rounded.Person, contentDescription = "Workouts") },
            label = { Text("Workouts") },
            selected = selectedScreen == ScreenType.WorkoutSessions,
            onClick = { onScreenSelected(ScreenType.WorkoutSessions) }
        )
    }
}

@Composable
fun FoodsScreen(userSessionManager: UserSessionManager, onSelectAddFood: () -> Unit, selectedDatesss: String) {
    //display info about food
    val userId = userSessionManager.userId
    var caloriesConsumed by remember { mutableStateOf(0) }
    var caloriesBurned by remember { mutableStateOf(0) }
    var calorieGoal by remember { mutableStateOf(0) }
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    // Define the state for meals
    var meals by remember { mutableStateOf<List<Meal>>(listOf()) }
    val dbHandler = DatabaseHandler()

    if(userId != null) {
        CoroutineScope(Dispatchers.Default).launch {
            meals = Repository(dbHandler).fetchMealsForDay(
                userId,
                date = selectedDatesss
            )
            caloriesConsumed = dbHandler.calculateDailyCaloriesConsumed(
                userId,
                selectedDatesss
            )
            caloriesBurned = dbHandler.calculateDailyExerciseCalories(
                userId,
                selectedDatesss
            )
            calorieGoal = dbHandler.calculateDailyCalorieGoal(userId)
        }
    }

    val groupedMeals = meals.groupBy { it.mealType }

    LazyColumn {
        item {
            CaloriesRemainingHeader(
                calorieGoal,
                caloriesConsumed,
                caloriesBurned
            )
        }

        mealTypes.forEach { mealType ->
            val mealsForType = groupedMeals[mealType] ?: listOf()
            item {
                MealGroupSection(mealType, mealsForType, onSelectAddFood)
            }
        }
    }
}


@Composable
fun CaloriesRemainingHeader(
    calorieGoal: Int,
    caloriesConsumed: Int,
    caloriesBurned: Int
) {

    val caloriesRemaining = calorieGoal - (caloriesConsumed - caloriesBurned)
    val progress = caloriesConsumed.toFloat() / calorieGoal.toFloat()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$calorieGoal Goal", style = MaterialTheme.typography.body2)
            Text("$caloriesConsumed Food", style = MaterialTheme.typography.body2)
            Text("$caloriesBurned Exercise", style = MaterialTheme.typography.body2)
            Text("$caloriesRemaining Remaining", style = MaterialTheme.typography.body2)
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}
@Composable
fun MealGroupSection(mealType: String, meals: List<Meal>, onAddFoodClick: () -> Unit ) {
    //shows foods eaten so far
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(mealType, style = MaterialTheme.typography.h6)
        Divider(Modifier.padding(vertical = 4.dp))

        meals.forEach { meal ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Food: ${meal.foodName}", style = MaterialTheme.typography.body1)
                }
                Text("${meal.calories} cal", style = MaterialTheme.typography.body2)
            }
        }

        Button(onClick = onAddFoodClick) {
            Text("ADD FOOD")
        }
    }
}

@Composable
fun AddFoodScreen(barcodeScanner: BarcodeScanner, barcodeResult: String,onSelectAddFood: (FoodItemRecord) -> Unit) {
    val dbHandler = DatabaseHandler()
    val repository = Repository(dbHandler)
    val coroutineScope = rememberCoroutineScope()

    // Manage search results and loading state
    var searchResults by remember { mutableStateOf<List<FoodItemRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Define the handleSearch function
    val handleSearch = { query: String ->
        coroutineScope.launch {
            isLoading = true
            searchResults = repository.searchFood(query)
            isLoading = false
        }
        Unit
    }

    Column {
        TopAppBar(title = { Text("Add Food") })
        SearchBarFood(onSearch = handleSearch, barcodeScanner = barcodeScanner, barcodeResult)
        // Loading indicator
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        // Display the search results
        LazyColumn {
            items(searchResults) { foodItem ->
                // Define a composable that represents a row in your search results
                FoodItemRow(foodItem = foodItem) { selectedItem ->
                    onSelectAddFood(selectedItem)
                }
            }
        }
    }
}

@Composable
fun FoodItemRow(foodItem: FoodItemRecord, onSelectFood: (FoodItemRecord) -> Unit) {
    // shows food cards in the add food screen
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onSelectFood(foodItem) },
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = foodItem.name, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Calories: ${foodItem.calories}")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Carbs: ${foodItem.carbs}g, Protein: ${foodItem.protein}g, Fat: ${foodItem.fat}g")
            Spacer(modifier = Modifier.height(8.dp))
            MacroProgressIndicator(foodItem.carbs.toFloat(), foodItem.protein.toFloat(), foodItem.fat.toFloat())
        }
    }
}

@Composable
fun AddSelectedFoodScreen(selectedFood: FoodItemRecord?,
                          onSave: (Meal, Int) -> Unit,
                          selectedDatesss: String) {

    val mealOptions = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    var selectedMealType by remember { mutableStateOf(mealOptions.first()) }
    var expanded by remember { mutableStateOf(false) }

    selectedFood?.let { food ->
        var numberOfServings by remember { mutableStateOf("1") }
        var servingSize by remember { mutableStateOf("${food.servingSize}") }

        // Recalculate nutritional information
        val servings = numberOfServings.toIntOrNull() ?: 1
        val size = servingSize.toIntOrNull() ?: 100
        val totalCalories = food.calories * servings * size / 100
        val totalCarbs = food.carbs * servings * size / 100
        val totalProtein = food.protein * servings * size / 100
        val totalFat = food.fat * servings * size / 100

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

            Text(
                text = food.name,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedMealType,
                    onValueChange = { /* No op, as the TextField is read-only */ },
                    label = { Text("Meal Type") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Drop-down arrow")
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    mealOptions.forEach { meal ->
                        DropdownMenuItem(onClick = {
                            selectedMealType = meal
                            expanded = false
                        }) {
                            Text(text = meal)
                        }
                    }
                }
            }

            // Number of servings
            OutlinedTextField(
                value = numberOfServings,
                onValueChange = { newValue ->
                    numberOfServings = newValue.filter { it.isDigit() }
                },
                label = { Text("Number of Servings") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Serving size
            OutlinedTextField(
                value = servingSize,
                onValueChange = { newValue ->
                    servingSize = newValue.filter { it.isDigit() }
                },
                label = { Text("Serving Size (g)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Macronutrient progress indicator
            MacronutrientProgressIndicator(
                calories = totalCalories,
                carbs = totalCarbs,
                protein = totalProtein,
                fat = totalFat
            )

            // "Save" button
            Button(
                onClick = {

                    // Create a new Meal with the collected data
                    val newMeal = Meal(
                        mealId = 0, // Set to 0 or generate a unique ID if necessary
                        mealType = selectedMealType,
                        date = selectedDatesss,
                        foodName = selectedFood.name,
                        calories = totalCalories,
                        numberOfServings = numberOfServings.toInt(),
                        serving_size_used = servingSize.toDouble()
                    )

                    // Call the onSave callback with the new Meal object
                    onSave(newMeal, food.foodId)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    } ?: run {
        Text("No food selected", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun MacronutrientProgressIndicator(calories: Int, carbs: Int, protein: Int, fat: Int) {
    val totalMacros = (carbs + protein + fat).toFloat()
    val percentCarbs = (carbs / totalMacros) * 100
    val percentProtein = (protein / totalMacros) * 100
    val percentFat = (fat / totalMacros) * 100

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
            Canvas(modifier = Modifier.size(100.dp)) {
                // Draw the background circle
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    radius = size.minDimension / 2
                )

                // Calculate start angles for each macronutrient
                val startAngleCarbs = 270f // Starting at the top (12 o'clock)
                val startAngleProtein = startAngleCarbs + (percentCarbs / 100) * 360
                val startAngleFat = startAngleProtein + (percentProtein / 100) * 360

                // Draw the arc for carbs
                drawArc(
                    color = Color(0xFF64B5F6),
                    startAngle = startAngleCarbs,
                    sweepAngle = (percentCarbs / 100) * 360,
                    useCenter = false,
                    style = Stroke(width = 30f, cap = StrokeCap.Round)
                )

                // Draw the arc for protein
                drawArc(
                    color = Color(0xFF81C784),
                    startAngle = startAngleProtein,
                    sweepAngle = (percentProtein / 100) * 360,
                    useCenter = false,
                    style = Stroke(width = 30f, cap = StrokeCap.Round)
                )

                // Draw the arc for fat
                drawArc(
                    color = Color(0xFFFFF176),
                    startAngle = startAngleFat,
                    sweepAngle = (percentFat / 100) * 360,
                    useCenter = false,
                    style = Stroke(width = 30f, cap = StrokeCap.Round)
                )
            }
            // Center text for calories
            Text(
                text = "$calories Cal",
                style = MaterialTheme.typography.h6
            )
        }

        // Column for the macronutrient percentages
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = "Carbs: ${((percentCarbs * 10).toInt() / 10.0)}%",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "Protein: ${((percentProtein * 10).toInt() / 10.0)}%",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "Fat: ${((percentFat * 10).toInt() / 10.0)}%",
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
fun MacroProgressIndicator(carbs: Float, protein: Float, fat: Float) {
    val totalMacros = carbs + protein + fat

    if (totalMacros > 0) {
        Row(modifier = Modifier
            .height(20.dp)
            .fillMaxWidth()
            .background(Color(0xFFEDE7F6))
        ) {
            if (carbs > 0) {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .weight(carbs / totalMacros)
                    .background(Color(0xFFD1C4E9))
                )
            }
            if (protein > 0) {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .weight(protein / totalMacros)
                    .background(Color(0xFF9575CD))
                )
            }
            if (fat > 0) {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .weight(fat / totalMacros)
                    .background(Color(0xFF673AB7))
                )
            }
        }
    } else {
        // Handle the case where total macros is zero
        Box(modifier = Modifier
            .height(20.dp)
            .fillMaxWidth()
            .background(Color(0xFFEDE7F6)),
            contentAlignment = Alignment.Center
        ) {
            Text("No macros to display", style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun SearchBarFood(onSearch: (String) -> Unit, barcodeScanner: BarcodeScanner, initialBarcodeResult: String) {
    var searchText by remember { mutableStateOf(initialBarcodeResult) } // Initialize searchText with the passed barcode result

    // No need to declare another barcodeResult variable here. Use initialBarcodeResult for the initial state.

    // This will only be called once when the composable enters the Composition
    DisposableEffect(Unit) {
        // Assuming your BarcodeScanner has some mechanism to notify this composable when a new barcode is scanned.
        // This setup implies the external trigger (from MainActivity) is responsible for updating the composable state.
        // Thus, the barcodeScanner's listener inside this composable might not be necessary unless it serves another purpose.

        onDispose {
            barcodeScanner.stopCamera()
        }
    }

    // If you expect the initialBarcodeResult to change during the lifetime of this composable, consider using a LaunchedEffect
    // to react to changes. Otherwise, if initialBarcodeResult is only set once (e.g., at creation time), this might not be necessary.
    LaunchedEffect(initialBarcodeResult) {
        if (initialBarcodeResult.isNotEmpty()) {
            searchText = initialBarcodeResult // Update searchText with the new barcode result
            onSearch(initialBarcodeResult)
            // Note: Resetting initialBarcodeResult here isn't straightforward since it's a val. Consider how you manage state outside this composable.
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search for a food") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(
                onClick = {
                    // If barcodeScanner.startCamera() is intended to open a camera for a new scan,
                    // ensure that the mechanism to update the composable state after scanning is handled outside (e.g., in MainActivity).
                    barcodeScanner.startCamera()
                },
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Scan Barcode")
                Text("Scan a Barcode")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onSearch(searchText) // Uses the searchText, which is updated with the barcode result
                },
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Text("Search")
            }
        }
    }
}

@Composable
fun CalorieCard(caloriesAdded: Boolean, userSessionManager: UserSessionManager, selectedDates: String) {

    val userId = userSessionManager.userId ?: return
    val coroutineScope = rememberCoroutineScope()
    val dbHandler = DatabaseHandler()
    var caloriesConsumed by remember { mutableStateOf(0) }
    var exerciseCalories by remember { mutableStateOf(0) }
    var calorieGoal by remember { mutableStateOf(0) }

    // Listen for changes in caloriesAdded or selectedDates
    LaunchedEffect(caloriesAdded, selectedDates) {
        coroutineScope.launch {
            caloriesConsumed = dbHandler.calculateDailyCaloriesConsumed(userId, selectedDates)
            exerciseCalories = dbHandler.calculateDailyExerciseCalories(userId, selectedDates)
            calorieGoal = dbHandler.calculateDailyCalorieGoal(userId)
        }
    }
    // Calculate the remaining calories
    val caloriesRemaining = calorieGoal - (caloriesConsumed - exerciseCalories)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Calories",
                style = MaterialTheme.typography.h6,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calorie goal
                Column {
                    Text("Base Goal", style = MaterialTheme.typography.body2)
                    Text("$calorieGoal", style = MaterialTheme.typography.body1)
                }
                // Calories consumed
                Column {
                    Text("Food", style = MaterialTheme.typography.body2)
                    Text("$caloriesConsumed", style = MaterialTheme.typography.body1)
                }
                // Calories burned through exercise
                Column {
                    Text("Exercise", style = MaterialTheme.typography.body2)
                    Text("$exerciseCalories", style = MaterialTheme.typography.body1)
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = calculateProgress(caloriesConsumed, calorieGoal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$caloriesRemaining Remaining",
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

// Helper function to calculate progress for the progress indicator
fun calculateProgress(consumed: Int, goal: Int): Float {
    return (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
}
