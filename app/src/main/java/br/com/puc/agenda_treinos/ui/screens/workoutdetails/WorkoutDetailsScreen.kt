package br.com.puc.agenda_treinos.ui.screens.workoutdetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.puc.agenda_treinos.data.model.Exercise
import br.com.puc.agenda_treinos.data.model.Workout
import br.com.puc.agenda_treinos.ui.screens.workoutdetails.composables.AddEditExerciseDialog
import br.com.puc.agenda_treinos.ui.screens.workoutdetails.composables.ExerciseListItem
import br.com.puc.agenda_treinos.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    workout: Workout,
    viewModel: WorkoutViewModel,
    onBack: () -> Unit
) {
    var showAddEditExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    val exercises = workout.exercises.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                exerciseToEdit = null
                showAddEditExerciseDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Exercício")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (exercises.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum exercício neste treino. Clique no '+' para adicionar.")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(exercises, key = { it.id }) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onEdit = {
                                exerciseToEdit = exercise
                                showAddEditExerciseDialog = true
                            },
                            onDelete = {
                                viewModel.deleteExerciseFromWorkout(workout.id, exercise.id)
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    if (showAddEditExerciseDialog) {
        AddEditExerciseDialog(
            workoutId = workout.id,
            existingExercise = exerciseToEdit,
            viewModel = viewModel,
            onDismiss = { showAddEditExerciseDialog = false }
        )
    }
}