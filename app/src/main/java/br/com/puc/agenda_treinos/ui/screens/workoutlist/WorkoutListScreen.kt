package br.com.puc.agenda_treinos.ui.screens.workoutlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.puc.agenda_treinos.ui.screens.workoutlist.composables.AddWorkoutDialog
import br.com.puc.agenda_treinos.ui.screens.workoutlist.composables.WorkoutListItem
import br.com.puc.agenda_treinos.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    viewModel: WorkoutViewModel,
    onWorkoutClick: (String) -> Unit
) {
    val workoutsState = viewModel.workouts.collectAsState(initial = emptyList())
    val workouts = workoutsState.value
    var showAddWorkoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meus Treinos") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddWorkoutDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Treino")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum treino cadastrado ainda. Clique no '+' para adicionar.")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(workouts, key = { it.id }) { workout ->
                        WorkoutListItem(
                            workout = workout,
                            onClick = { onWorkoutClick(workout.id) },
                            onDelete = { viewModel.deleteWorkout(workout) }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    if (showAddWorkoutDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onAddWorkout = { workoutName ->
                viewModel.addWorkout(workoutName)
                showAddWorkoutDialog = false
            }
        )
    }
}