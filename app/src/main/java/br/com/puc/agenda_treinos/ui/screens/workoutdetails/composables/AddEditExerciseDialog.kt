package br.com.puc.agenda_treinos.ui.screens.workoutdetails.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import br.com.puc.agenda_treinos.data.model.Exercise
import br.com.puc.agenda_treinos.viewmodel.WorkoutViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExerciseDialog(
    workoutId: String,
    existingExercise: Exercise?,
    viewModel: WorkoutViewModel,
    onDismiss: () -> Unit
) {
    var exerciseName by remember { mutableStateOf(existingExercise?.name ?: "") }
    var sets by remember { mutableStateOf(existingExercise?.sets ?: "") }
    var reps by remember { mutableStateOf(existingExercise?.reps ?: "") }
    var notes by remember { mutableStateOf(existingExercise?.notes ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var setsError by remember { mutableStateOf<String?>(null) }
    var repsError by remember { mutableStateOf<String?>(null) }


    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp).heightIn(max=600.dp)) {
                Text(
                    if (existingExercise == null) "Adicionar Novo Exercício" else "Editar Exercício",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it; nameError = null },
                    label = { Text("Nome do Exercício") },
                    singleLine = true,
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError != null) {
                    Text(nameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it; setsError = null },
                    label = { Text("Séries (ex: 3 ou 3-4)") },
                    singleLine = true,
                    isError = setsError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (setsError != null) {
                    Text(setsError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it; repsError = null },
                    label = { Text("Repetições (ex: 10 ou 8-12)") },
                    singleLine = true,
                    isError = repsError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (repsError != null) {
                    Text(repsError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        var valid = true
                        if (exerciseName.isBlank()) {
                            nameError = "Nome é obrigatório."
                            valid = false
                        }
                        if (sets.isBlank()) {
                            setsError = "Séries são obrigatórias."
                            valid = false
                        }
                        if (reps.isBlank()) {
                            repsError = "Repetições são obrigatórias."
                            valid = false
                        }

                        if (valid) {
                            val exercise = Exercise(
                                id = existingExercise?.id ?: UUID.randomUUID().toString(),
                                name = exerciseName,
                                sets = sets,
                                reps = reps,
                                notes = notes.ifBlank { null }
                            )
                            if (existingExercise == null) {
                                viewModel.addExerciseToWorkout(workoutId, exercise)
                            } else {
                                viewModel.updateExerciseInWorkout(workoutId, existingExercise.id, exercise)
                            }
                            onDismiss()
                        }
                    }) {
                        Text(if (existingExercise == null) "Adicionar" else "Salvar")
                    }
                }
            }
        }
    }
}