package br.com.puc.agenda_treinos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.puc.agenda_treinos.ui.screens.workoutdetails.WorkoutDetailsScreen
import br.com.puc.agenda_treinos.ui.screens.workoutlist.WorkoutListScreen
import br.com.puc.agenda_treinos.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutAppNavigator(viewModel: WorkoutViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.WorkoutList) }
    var selectedWorkoutId by remember { mutableStateOf<String?>(null) }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.WorkoutList -> WorkoutListScreen(
                    viewModel = viewModel,
                    onWorkoutClick = { workoutId ->
                        selectedWorkoutId = workoutId
                        currentScreen = Screen.WorkoutDetails
                    }
                )
                Screen.WorkoutDetails -> {
                    selectedWorkoutId?.let { workoutId ->
                        val workoutState = viewModel.getWorkoutById(workoutId).collectAsState(initial = null)
                        val workout = workoutState.value

                        if (workout != null) {
                            WorkoutDetailsScreen(
                                workout = workout,
                                viewModel = viewModel,
                                onBack = {
                                    currentScreen = Screen.WorkoutList
                                    selectedWorkoutId = null
                                }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                            }
                        }
                    } ?: run {
                        LaunchedEffect(Unit) {
                            currentScreen = Screen.WorkoutList
                        }
                    }
                }
            }
        }
    }
}