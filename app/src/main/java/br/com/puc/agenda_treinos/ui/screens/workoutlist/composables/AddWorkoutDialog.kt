package br.com.puc.agenda_treinos.ui.screens.workoutlist.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutDialog(onDismiss: () -> Unit, onAddWorkout: (String) -> Unit) {
    var workoutName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Adicionar Novo Treino", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it; nameError = null },
                    label = { Text("Nome do Treino") },
                    singleLine = true,
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError != null) {
                    Text(nameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonColors(
                            containerColor = Color(0xFFF2B872),
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.Gray
                        ),
                        onClick = {
                        if (workoutName.isNotBlank()) {
                            onAddWorkout(workoutName)
                        } else {
                            nameError = "O nome do treino não pode estar vazio."
                        }
                    }) {
                        Text("Adicionar")
                    }
                }
            }
        }
    }
}