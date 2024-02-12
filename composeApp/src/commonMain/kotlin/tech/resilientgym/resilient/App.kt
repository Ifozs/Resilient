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
    Settings
    //screens
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App(userSessionManager: UserSessionManager, barcodeScanner: BarcodeScanner) {
    //probably the worst way to control screens, but cant use view models because of IOS implementation

    val platformDate = PlatformDate()
    MaterialTheme {
        var selectedScreen by remember { mutableStateOf(ScreenType.Login) } // Start with Login screen
        var isLoggedIn by remember { mutableStateOf(userSessionManager.isLoggedIn()) }
        var selectedExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
        val createRoutine by remember { mutableStateOf<List<SessionExercise>>(emptyList()) }
        var selectedFood by remember { mutableStateOf<FoodItemRecord?>(null) }
        var selectedWorkoutSession by remember { mutableStateOf<WorkoutSession?>(null) }
        var selectedDatesss by remember { mutableStateOf(platformDate.todayAsString()) }


        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (!isLoggedIn) {
                            // Center the title when not logged in
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Resilient", style = MaterialTheme.typography.h6, modifier = Modifier.align(Alignment.Center))
                            }
                        } else {
                            // Default alignment when logged in (settings icon visible)
                            Text(text = "Resilient", style = MaterialTheme.typography.h6)
                        }
                    },
                    navigationIcon = if (isLoggedIn) {
                        {
                            IconButton(onClick = { selectedScreen = ScreenType.Settings }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                    } else null,
                    actions = {
                        if (isLoggedIn) {
                            // Display the formatted date string
                            Text(
                                text = platformDate.formatDateString(selectedDatesss),
                                style = MaterialTheme.typography.subtitle1,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            // Inside your TopAppBar composable
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
                if (isLoggedIn) {
                    AppBottomNavigation(selectedScreen) { screen ->

                            selectedScreen = screen

                    }
                }
            }
        ) {
            if (!isLoggedIn) {
                when (selectedScreen) {

                    ScreenType.Login -> LoginScreen(
                        userSessionManager,
                        onLoginSuccess = {
                            isLoggedIn = true
                            selectedScreen = ScreenType.Dashboard
                        },
                        onNavigateToRegister = { selectedScreen = ScreenType.Registration }
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
                    ScreenType.Settings -> SettingsScreen(userSessionManager)
                    ScreenType.Dashboard -> HomeScreen(userSessionManager, selectedDatesss, unitforScreen = {ScreenType.Dashboard})
                    ScreenType.Food -> FoodsScreen(
                        userSessionManager = userSessionManager,
                        onSelectAddFood = {
                            selectedScreen = ScreenType.AddFood
                        },
                        selectedDatesss
                    )
                    ScreenType.AddFood -> AddFoodScreen(barcodeScanner){ selectedFoodItem ->
                        selectedFood = selectedFoodItem
                        selectedScreen = ScreenType.AddSelectedFoodScreen
                    }
                    ScreenType.AddSelectedFoodScreen -> AddSelectedFoodScreen(
                        selectedFood = selectedFood, // Make sure this is the selected food item to add to the meal.
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
                                    ) // Pass the Meal object and foodId to the insert function.
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
                            selectedScreen = ScreenType.StartRoutine // Change the screen
                        }
                    )
                    ScreenType.StartRoutine -> {
                        // Define what should happen when the workout session is finished
                        val onFinishSession: (WorkoutSession, Map<String, Double>) -> Unit = { session, updatedWeights ->
                            val exerciseWeightUpdates = updatedWeights.map { (exerciseId, maxWeight) ->
                                ExerciseWeightUpdate(exerciseId, maxWeight)
                            }

                            val dbHandler = DatabaseHandler()
                            // Call insertWorkoutProgress with the necessary parameters
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
                            selectedScreen = ScreenType.WorkoutSessions // Go back to the previous screen, for example
                        },
                        onSave = {
                            // Define what should happen when save is clicked

                            val dbHandler = DatabaseHandler()

                            CoroutineScope(Dispatchers.Default).launch { // Using Dispatchers.Default for background work
                                Repository(dbHandler).insertSessionForUser(userId = userSessionManager.userId!!, workoutSession = it)
                            }

                            selectedScreen = ScreenType.WorkoutSessions
                        },
                        onAddExercise = {
                            selectedScreen = ScreenType.ExerciseSelectionScreen
                        },
                        selectedExercises = selectedExercises,
                        createRoutine = createRoutine.toMutableList(),
                        selectedDatesss
                    )
                    ScreenType.ExerciseSelectionScreen -> AddExerciseScreen(
                        onCancel = {
                            // Define what should happen when the cancel action is triggered
                            // For example, navigate back to the previous screen
                            selectedScreen = ScreenType.RoutineCreation // Replace with actual screen type
                        },
                        onCreate = {
                            // Define what should happen when the create action is triggered
                            // For example, create a new exercise or navigate to the creation screen
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
fun SettingsScreen(userSessionManager: UserSessionManager) {
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
            modifier = Modifier.fillMaxSize() // Fills the parent to center the progress indicator
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Now that you have the user settings, create the UI for them
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
    var gender by remember { mutableStateOf(settings.gender ?: "Not Specified") }
    val genders = listOf("Male", "Female", "Other", "Not Specified")
    var expanded by remember { mutableStateOf(false) }
    var fitnessGoal by remember { mutableStateOf(settings.fitnessGoal ?: "Maintain") }
    val fitnessGoals = listOf("Bulk", "Cut", "Maintain")
    var fitnessExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            // Additional padding at the bottom to account for the navigation bar
            .padding(bottom = 68.dp), // Adjust this value based on the height of your nav bar
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Settings", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        // Enhanced UI with grouped fields and improved aesthetics
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
            readOnly = true // Makes the text field not editable, turning it into a dropdown
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

        //fuck main thread
        CoroutineScope(Dispatchers.Default).launch {
            repository.insertUserInfo(userId, weightFloat, ageInt, bodyFatFloat, heightFloat, selectedGender)
            //cant give wrong input if they do .... we're fucked
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword == password) {
                    val dbHandler = DatabaseHandler()

                    // Using CoroutineScope to handle asynchronous operations
                    CoroutineScope(Dispatchers.Main).launch { // Use Main dispatcher for UI updates
                        val userId = withContext(Dispatchers.Default) {
                            // Execute the registration in the IO context and wait for the result
                            Repository(dbHandler).registerUser(username, email, password)
                        }

                        // Call onRegistrationSuccess after getting the user ID
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

private fun validateRegistration(username: String, email: String, password: String, confirmPassword: String): Boolean {
    return username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && password == confirmPassword
}

@Composable
fun StartRoutineScreen(
    selectedWorkoutSession: WorkoutSession?,
    onFinishSession: (WorkoutSession, Map<String, Double>) -> Unit
) {
    var elapsedTime by remember { mutableStateOf(0L) } // Elapsed time in seconds
    val coroutineScope = rememberCoroutineScope()
    val updatedWeights = remember { mutableStateMapOf<String, Double>() }

    // Start the timer when the composable is first launched
    LaunchedEffect(key1 = "timer") {
        coroutineScope.launch {
            while (true) {
                delay(1000) // Wait for 1 second
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
            SearchBar() // You'll define this Composable for the search functionality
            FilterButtons() // This is for the "All Equipment" and "All Muscles" toggle buttons
            ExerciseList(onSelectExercise) // Pass a list of exercises and a function to handle selection
        }
    }
}

@Composable
fun ExerciseList(onSelectExercise: (Exercise) -> Unit) {

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
            // Assuming you have an image resource id or URL for each exercise
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
fun FilterButtons() {
    // Use a Row to align buttons horizontally, or LazyRow if you have more filters
    Row(modifier = Modifier.padding(8.dp)) {
        Button(onClick = { /* Handle All Equipment filter */ }) {
            Text("All Equipment")
        }
        Spacer(modifier = Modifier.width(8.dp)) // Add space between buttons
        Button(onClick = { /* Handle All Muscles filter */ }) {
            Text("All Muscles")
        }
    }
}

@Composable
fun RoutineCreationScreen(
    onCancel: () -> Unit,
    onSave: (WorkoutSession) -> Unit,
    onAddExercise: () -> Unit,
    selectedExercises: List<Exercise>,
    createRoutine: MutableList<SessionExercise>,
    selectedDatesss: String
) {

    var routine by remember { mutableStateOf(createRoutine) }
    var routineTitle by remember { mutableStateOf("") }

    val handleSave = {
        // Since the routine list is already updated with the latest sets,
        // you can directly use it to create the WorkoutSession
        val workoutSession = WorkoutSession(
            sessionId = 4, // or generate appropriately
            userId = 4,  // Set the user ID appropriately
            sessionDate = selectedDatesss, // Leave blank or set appropriately
            sessionTitle = routineTitle,
            exercises = routine.toList() // routine already contains the latest sets
        )

        onSave(workoutSession) // Call the onSave function with the WorkoutSession object
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
                        routine = (routine + it).toMutableList() // Append to the routine
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
            // Assuming the repository has a method to fetch sessions for a specific user
            val userSessions = repository.getAllSessionsForUser(userId!!)
            sessions = userSessions
        } catch (e: Exception) {
            // Handle exceptions, e.g., show an error message
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
                // You might want to style the FAB to match your theme
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Session")
            }
        }
    }
}

@Composable
fun SessionCard(session: WorkoutSession,
                onSessionClick: (WorkoutSession) -> Unit ) {

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
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit // Add this parameter for navigation to the registration screen
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
        var loginErrorMessage by remember { mutableStateOf("") }

        Button(
            onClick = {
                coroutineScope.launch {
                    if (userId.isNotBlank()) {
                        val isAuthenticated = Repository(dbHandler).authenticateUser(email = userId, password = password)
                        // Assuming userId can be converted to Int
                        userSessionManager.createSession(isAuthenticated!!)
                        onLoginSuccess()
                    } else {
                        loginErrorMessage = "Please enter both user ID and password"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = userId.isNotBlank() && password.isNotBlank()
        ) {
            Text("Log In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add a text element for navigation to the registration screen
        Text(
            text = "Don't have an account? Register here.",
            modifier = Modifier
                .clickable(onClick = onNavigateToRegister)
                .padding(8.dp), // Add padding for better touch area
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.body2
        )
    }
}

data class ExerciseProgress(val date: String, val maxWeight: Double)
@Composable
fun HomeScreen(userSessionManager: UserSessionManager, selectedDates: String, unitforScreen: () -> Unit) {
    val userId = userSessionManager.userId ?: return // Safely handle null userId
    val coroutineScope = rememberCoroutineScope()
    var allExercisesProgress by remember { mutableStateOf<Map<String, List<Pair<String, Double>>>>(emptyMap()) }
    val scrollState = rememberScrollState()
    var caloriesAdded by remember { mutableStateOf(false) }
    val dbHandler = DatabaseHandler()
    var oldDate by remember { mutableStateOf("0") }

    // React to changes in selectedDates
    LaunchedEffect(selectedDates) {
        if (selectedDates != oldDate) {
            // Assume unitforScreen() updates UI or performs necessary logic for new date
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
                        val userId = uid!! // Replace with the actual user ID
                        val date = selectedDatesss

                        val dbHandler = DatabaseHandler()
                        // Call insertWorkoutProgress with the necessary parameters
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
        // ... add other items as needed
    }
}

@Composable
fun FoodsScreen(userSessionManager: UserSessionManager, onSelectAddFood: () -> Unit, selectedDatesss: String) {
    val userId = userSessionManager.userId
    var caloriesConsumed by remember { mutableStateOf(0) }
    var caloriesBurned by remember { mutableStateOf(0) }
    var calorieGoal by remember { mutableStateOf(0) }
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    // Define the state for meals
    var meals by remember { mutableStateOf<List<Meal>>(listOf()) }
    val dbHandler = DatabaseHandler()

    if(userId != null) {
        // Using Dispatchers.Default for background work
        CoroutineScope(Dispatchers.Default).launch {
            meals = Repository(dbHandler).fetchMealsForDay(
                userId,
                date = selectedDatesss
            ) // Assuming default userId is 12
            caloriesConsumed = dbHandler.calculateDailyCaloriesConsumed(
                userId,
                selectedDatesss
            ) // Replace with correct userId
            caloriesBurned = dbHandler.calculateDailyExerciseCalories(
                userId,
                selectedDatesss
            ) // Replace with correct userId
            calorieGoal = dbHandler.calculateDailyCalorieGoal(userId) // Replace with correct userId
        }
    }

    val groupedMeals = meals.groupBy { it.mealType }

    LazyColumn {
        item {
            // Calories Remaining Header
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
fun AddFoodScreen(barcodeScanner: BarcodeScanner, onSelectAddFood: (FoodItemRecord) -> Unit) {
    val dbHandler = DatabaseHandler()
    val repository = Repository(dbHandler)

    // Use rememberCoroutineScope to get the scope associated with this Composable
    val coroutineScope = rememberCoroutineScope()

    // Manage search results and loading state
    var searchResults by remember { mutableStateOf<List<FoodItemRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Define the handleSearch function
    val handleSearch = { query: String ->
        // Launch the coroutine in the Composable's scope
        coroutineScope.launch {
            isLoading = true
            searchResults = repository.searchFood(query)
            isLoading = false
        }
        Unit
    }

    Column {
        TopAppBar(title = { Text("Add Food") })
        SearchBarFood(onSearch = handleSearch, barcodeScanner = barcodeScanner)
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

            // ... other UI components ...

            // "Save" button
            Button(
                onClick = {
                    // Assuming you want to save the current date
                    //val currentDate = LocalDate.now().toString()

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

    // The rest of your Canvas drawing logic goes here, using percentCarbs, percentProtein, and percentFat

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
                    color = Color(0xFF64B5F6), // Light blue
                    startAngle = startAngleCarbs,
                    sweepAngle = (percentCarbs / 100) * 360,
                    useCenter = false,
                    style = Stroke(width = 30f, cap = StrokeCap.Round)
                )

                // Draw the arc for protein
                drawArc(
                    color = Color(0xFF81C784), // Light green
                    startAngle = startAngleProtein,
                    sweepAngle = (percentProtein / 100) * 360,
                    useCenter = false,
                    style = Stroke(width = 30f, cap = StrokeCap.Round)
                )

                // Draw the arc for fat
                drawArc(
                    color = Color(0xFFFFF176), // Light yellow
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
            .background(Color(0xFFEDE7F6)), // Light grey background
            contentAlignment = Alignment.Center
        ) {
            Text("No macros to display", style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun SearchBarFood(onSearch: (String) -> Unit, barcodeScanner: BarcodeScanner) {
    var searchText by remember { mutableStateOf("") }
    // State for barcode result
    var barcodeResult by remember { mutableStateOf("") }

    // This will only be called once when the composable enters the Composition
    DisposableEffect(Unit) {
        // Set up the listener
        barcodeScanner.setBarcodeScannedListener { result ->
            barcodeResult = result
        }

        // Clean up the listener when the composable leaves the Composition
        onDispose {
            barcodeScanner.stopCamera()
        }
    }

    // Side-effect to handle barcode result updates
    LaunchedEffect(barcodeResult) {
        if (barcodeResult.isNotEmpty()) {
            onSearch(barcodeResult)
            // Optionally reset barcode result if needed
            barcodeResult = ""
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
                onClick = { barcodeScanner.startCamera() },
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Scan Barcode")
                Text("Scan a Barcode")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onSearch(searchText) },
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Text("Search")
            }
        }
    }
}

@Composable
fun CalorieCard(caloriesAdded: Boolean, userSessionManager: UserSessionManager, selectedDates: String) {

    val userId = userSessionManager.userId ?: return // Safely handle null userId
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
