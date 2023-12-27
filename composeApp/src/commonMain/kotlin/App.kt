import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        // Initialize the greeting message as a string
        val greetingMessage = remember { Greeting().greet() }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "My Fitness Pal - Premium", style = MaterialTheme.typography.h6) },
                    actions = {
                        IconButton(onClick = { /* Handle settings action */ }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
            bottomBar = { AppBottomNavigation() }
        ) {
            // Pass the greeting message to the HomeScreen
            HomeScreen(greetingMessage = greetingMessage)
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
fun AppBottomNavigation() {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = false,
            onClick = { /* Handle navigate to Dashboard */ }
        )
        // Add more BottomNavigationItems for Diary, Newsfeed, Plans, etc.
    }
}

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
