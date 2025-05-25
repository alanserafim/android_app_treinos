package br.com.puc.agenda_treinos.ui.screens.workoutdetails.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.puc.agenda_treinos.data.model.Exercise

@Composable
fun ExerciseListItem(exercise: Exercise, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                Text("Séries: ${exercise.sets}, Repetições: ${exercise.reps}", style = MaterialTheme.typography.bodyMedium)
                exercise.notes?.let {
                    if (it.isNotBlank()) {
                        Text("Notas: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar Exercício")
            }
            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir Exercício", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Excluir Exercício") },
            text = { Text("Tem certeza que deseja excluir o exercício '${exercise.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}