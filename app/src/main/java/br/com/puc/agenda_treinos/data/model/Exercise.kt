package br.com.puc.agenda_treinos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var name: String,
    var sets: String,
    var reps: String,
    var notes: String? = null
)