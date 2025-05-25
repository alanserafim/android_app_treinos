package br.com.puc.agenda_treinos.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var name: String,
    @ColumnInfo(name = "exercises_list") // Room will use TypeConverter for this field
    var exercises: MutableList<Exercise> = mutableListOf()
)