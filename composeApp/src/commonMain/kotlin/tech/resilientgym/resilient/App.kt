package tech.resilientgym.resilient

import DatabaseHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlin.random.Random


enum class ScreenType {
    Dashboard,
    Food,
    WorkoutSessions,
    RoutineCreation,
    ExerciseSelectionScreen,
    Login,
    AddFood
    // ... define other screen types if necessary
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App(userSessionManager: UserSessionManager, barcodeScanner: BarcodeScanner) {
    MaterialTheme {
        var selectedScreen by remember { mutableStateOf(ScreenType.Dashboard) } // Start with Login screen
        val greetingMessage = remember { Greeting().greet() }
        var isLoggedIn by remember { mutableStateOf(userSessionManager.isLoggedIn()) }
        var selectedExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
        var createRoutine by remember { mutableStateOf<List<SessionExercise>>(emptyList()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Resilient", style = MaterialTheme.typography.h6) },
                    actions = {
                        IconButton(onClick = { /* Handle settings action */ }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                LoginScreen(userSessionManager) {
                    isLoggedIn = true
                    selectedScreen = ScreenType.Dashboard // Go to Dashboard after login
                }
            } else {

                when (selectedScreen) {
                    ScreenType.Dashboard -> HomeScreen(greetingMessage = greetingMessage)
                    ScreenType.Food -> FoodsScreen(
                        userSessionManager = userSessionManager,
                        onSelectAddFood = {
                            selectedScreen = ScreenType.AddFood
                        }
                    )
                    ScreenType.AddFood -> AddFoodScreen(barcodeScanner)
                    ScreenType.WorkoutSessions -> SessionsScreen(
                        userSessionManager = userSessionManager,
                        onSelectAddSessions = {
                            selectedScreen = ScreenType.RoutineCreation
                        },
                        onSessionClick = {
                            // Handle the session click here, for example:
                            // Show session details or start the session workout
                        }
                    )
                    ScreenType.RoutineCreation -> RoutineCreationScreen(
                        onCancel = {
                            // Define what should happen when cancel is clicked
                            selectedExercises = emptyList()
                            selectedScreen = ScreenType.WorkoutSessions // Go back to the previous screen, for example
                        },
                        onSave = {
                            // Define what should happen when save is clicked
                            // TODO: Implement saving logic

                            val dbHandler = DatabaseHandler()

                            CoroutineScope(Dispatchers.Default).launch { // Using Dispatchers.Default for background work
                                Repository(dbHandler).insertSessionForUser(userId = 3, workoutSession = it)
                            }

                            selectedScreen = ScreenType.WorkoutSessions
                        },
                        onAddExercise = {
                            selectedScreen = ScreenType.ExerciseSelectionScreen
                        },
                        selectedExercises = selectedExercises,
                        createRoutine = createRoutine.toMutableList()
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
    createRoutine: MutableList<SessionExercise>
) {

    var routine by remember { mutableStateOf(createRoutine) }
    var routineTitle by remember { mutableStateOf("") }

    val handleSave = {
        // Since the routine list is already updated with the latest sets,
        // you can directly use it to create the WorkoutSession
        val workoutSession = WorkoutSession(
            sessionId = 4, // or generate appropriately
            userId = 3,  // Set the user ID appropriately
            sessionDate = "2023-11-12", // Leave blank or set appropriately
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


//    LaunchedEffect(userId) {
//        isLoading = true
//        // Simulate a network or database call with a delay
//        delay(1000) // 1 second delay to simulate loading
//        // Assign some dummy data to the sessions variable
//        sessions = listOf(
//            WorkoutSession(
//                sessionId = 1,
//                userId = userId!!,
//                sessionDate = "2023-01-01",
//                sessionTitle = "Upper Body Strength",
//                exercises = listOf(
//                    SessionExercise(
//                        sessionExerciseId = 101,
//                        sessionId = 1,
//                        exerciseId = "Bench Press",
//                        sets = listOf(
//                            ExerciseSet(setId = 201, sessionExerciseId = 101, setNumber = 1, weight = 50.0, reps = 10),
//                            ExerciseSet(setId = 202, sessionExerciseId = 101, setNumber = 2, weight = 55.0, reps = 8),
//                            ExerciseSet(setId = 203, sessionExerciseId = 101, setNumber = 3, weight = 60.0, reps = 6)
//                        )
//                    ),
//                    SessionExercise(
//                        sessionExerciseId = 102,
//                        sessionId = 1,
//                        exerciseId = "Pull-ups",
//                        sets = listOf(
//                            ExerciseSet(setId = 204, sessionExerciseId = 102, setNumber = 1, weight = 0.0, reps = 10),
//                            ExerciseSet(setId = 205, sessionExerciseId = 102, setNumber = 2, weight = 0.0, reps = 10),
//                            ExerciseSet(setId = 206, sessionExerciseId = 102, setNumber = 3, weight = 0.0, reps = 10)
//                        )
//                    )
//                    // ... Add more session exercises as needed
//                )
//            )
//            // ... Add more sessions as needed
//        )
//        isLoading = false
//    }

    Scaffold(
        topBar = {
            // Your TopBar code here...
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Display the list of sessions in the style of the first image
                LazyColumn {
                    items(sessions) { session ->
                        SessionCard(session = session, onSessionClick = { onSessionClick(session) })
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    FloatingActionButton(
                        onClick = onSelectAddSessions,
                        modifier = Modifier
                            .align(Alignment.BottomEnd) // This will now work because it's within a Box
                            .padding(bottom = 68.dp, end = 18.dp),
                        backgroundColor = MaterialTheme.colors.primary
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Session")
                    }
                }
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
fun LoginScreen(userSessionManager: UserSessionManager, onLoginSuccess: () -> Unit) {
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
            label = { Text("User ID") },
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

        Button(
            onClick = {
                // Simulate login validation. Replace with actual logic as needed.
                if (userId.isNotBlank() && password.isNotBlank()) {
                    // Assuming we've validated the user's credentials
                    userSessionManager.createSession(userId.toInt())
                    onLoginSuccess()
                } else {
                    loginErrorMessage = "Please enter a valid user ID and password"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = userId.isNotBlank() && password.isNotBlank()
        ) {
            Text("Log In")
        }
    }
}


@Composable
fun HomeScreen(greetingMessage: String) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        GreetingHeader(greetingMessage)
        // Define and add other cards like CalorieCard, StepsCard, ExerciseCard, and NutrientCard here
        CalorieCard()
    }
}

@Composable
fun GreetingHeader(greeting: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Good morning,!", // assuming Greeting has a name property
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

// Define other composable functions like CalorieCard, StepsCard, ExerciseCard, and NutrientCard based on your design requirements.
// These will display the data in a similar fashion to the provided screenshot.

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
fun ExerciseSelectionScreen(onExerciseSelected: (String) -> Unit) {
    // Placeholder for a list of exercises
    val exercises = listOf("Barbell Bench Press", "Air Bicycles", "Dumbbell Bench Press")

    Column {
        Text("Select an Exercise", style = MaterialTheme.typography.h6, modifier = Modifier.align(Alignment.CenterHorizontally))
        LazyColumn {
            items(exercises) { exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onExerciseSelected(exercise) },
                    elevation = 2.dp
                ) {
                    Text(exercise, style = MaterialTheme.typography.subtitle1, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun WorkoutsScreen(exerciseName: String) {
    // Placeholder for workout data, replace with your actual data model or state management
    var sets by remember { mutableStateOf(3) }
    var currentSet by remember { mutableStateOf(1) }
    var weight by remember { mutableStateOf(225) }
    var reps by remember { mutableStateOf(5) }

    Column(modifier = Modifier.fillMaxSize()) {
        ExerciseHeader(exerciseName, sets, currentSet)
        WeightDisplay(weight) { newWeight -> weight = newWeight }
        RepsCounter(reps) { newReps -> reps = newReps }
        RestTimerButton()
        // Add LastWorkoutSection and PersonalRecordsSection if needed

    }
}

@Composable
fun ExerciseHeader(name: String, sets: Int, currentSet: Int) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = name, style = MaterialTheme.typography.h5)
        Text(text = "Set $currentSet/$sets", style = MaterialTheme.typography.subtitle1)
    }
}


@Composable
fun WeightDisplay(weight: Int, onWeightChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = { /* Handle decrease weight */ }) {
            Text("-")
        }
        Text(
            text = "$weight lbs",
            style = MaterialTheme.typography.h4,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .align(Alignment.CenterVertically)
        )
        Button(onClick = { /* Handle increase weight */ }) {
            Text("+")
        }
    }
}


@Composable
fun RepsCounter(reps: Int, onRepsChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = { /* Handle decrease reps */ }) {
            Text("-")
        }
        Text(
            text = "$reps reps",
            style = MaterialTheme.typography.h4,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .align(Alignment.CenterVertically)
        )
        Button(onClick = { /* Handle increase reps */ }) {
            Text("+")
        }
    }
}


@Composable
fun RestTimerButton() {
    Button(
        onClick = { /* Start rest timer */ },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Rest Timer")
    }
}


@Composable
fun LastWorkoutSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Last workout", style = MaterialTheme.typography.subtitle1)
        // Display the details of the last workout
    }
}


@Composable
fun PersonalRecordsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Personal records", style = MaterialTheme.typography.subtitle1)
        // Display the personal records
    }
}


@Composable
fun WorkoutBottomNavigation() {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = false,
            onClick = { /* Handle Home click */ }
        )
        // Add other BottomNavigationItems for Log, Goals, etc.
    }
}


// Replace this with your actual data model
data class ExerciseData(
    val name: String,
    val sets: Int,
    val currentSet: Int,
    val weight: Int,
    val reps: Int
)


@Composable
fun FoodsScreen(userSessionManager: UserSessionManager, onSelectAddFood: () -> Unit) {
    val scrollState = rememberScrollState()

    val userId = userSessionManager.userId

    // Define the state for meals
    var meals by remember { mutableStateOf<List<Meal>>(listOf()) }
    val dbHandler = DatabaseHandler()

//    val meals = listOf(
//        Meal(1, "Breakfast", "2023-01-01", "Oatmeal", 300),
//        Meal(2, "Breakfast", "2023-01-01", "Banana", 90),
//        Meal(3, "Lunch", "2023-01-01", "Chicken Salad", 450),
//        Meal(4, "Lunch", "2023-01-01", "Apple", 80),
//        Meal(5, "Dinner", "2023-01-01", "Steak", 600),
//        Meal(6, "Dinner", "2023-01-01", "Mashed Potatoes", 200)
//    )

    if (userId != null) {
        CoroutineScope(Dispatchers.Default).launch { // Using Dispatchers.Default for background work
            meals = Repository(dbHandler).fetchMealsForDay(userId, date = "2023-01-01")
        }
    }

    val groupedMeals = meals.groupBy { it.mealType + it.date }

    LazyColumn {
        item {
            // Calories Remaining Header
            CaloriesRemainingHeader(
                calorieGoal = 2000,
                caloriesConsumed = 910,
                caloriesBurned = 300
            )
        }

        groupedMeals.forEach { (groupKey, groupMeals) ->
            item {
                val firstMeal = groupMeals.first()
                MealGroupSection(mealType = firstMeal.mealType, meals = groupMeals, onAddFoodClick = onSelectAddFood)
            }
        }
    }
}



@Composable
fun CaloriesRemainingHeader(
    calorieGoal: Int, // Total calorie goal for the day
    caloriesConsumed: Int, // Calories consumed so far
    caloriesBurned: Int // Calories burned from exercise
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
fun AddFoodScreen(barcodeScanner: BarcodeScanner) {
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
            CircularProgressIndicator()
        }
        // Display the search results
        LazyColumn {
            items(searchResults) { foodItem ->
                // Define a composable that represents a row in your search results
                FoodItemRow(foodItem)
            }
        }
        // Placeholder for recent foods list
        RecentFoodsList(recentFoods = listOf(/* ... */))
    }
}

// Define the FoodItemRow Composable if not already defined
@Composable
fun FoodItemRow(foodItem: FoodItemRecord) {
    // Layout for displaying a single food item in the list
    Text(text = foodItem.name) // For example, just display the name
}

@Composable
fun SearchBarFood(onSearch: (String) -> Unit, barcodeScanner: BarcodeScanner) {
    var searchText by remember { mutableStateOf("") }

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
                modifier = Modifier.weight(1f).height(40.dp) // Match Material Design button height
            ) {
                Icon(Icons.Default.Search, contentDescription = "Scan Barcode")
                Text("Scan a Barcode")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onSearch(searchText) },
                modifier = Modifier.weight(1f).height(40.dp) // Match Material Design button height
            ) {
                Text("Search")
            }
        }
    }
}

@Composable
fun RecentFoodsList(recentFoods: List<FoodItem>) {
    // Placeholder implementation
    recentFoods.forEach { foodItem ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(foodItem.name, style = MaterialTheme.typography.subtitle1)
                Text("${foodItem.calories} cal, 100 gram")
            }
            IconButton(onClick = { /* TODO: Implement adding this food */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

data class FoodItem(val name: String, val calories: Int, val description: String)


@Composable
fun CalorieCard(
    caloriesConsumed: Int = 650, // Placeholder values
    exerciseCalories: Int = 400, // Placeholder values
    calorieGoal: Int = 1500 // Placeholder values
) {
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
                style = MaterialTheme.typography.h6
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
