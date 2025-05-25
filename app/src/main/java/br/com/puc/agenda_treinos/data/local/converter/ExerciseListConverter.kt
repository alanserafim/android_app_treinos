package br.com.puc.agenda_treinos.data.local.converter

import androidx.room.TypeConverter
import br.com.puc.agenda_treinos.data.model.Exercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExerciseListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromExerciseList(exercises: MutableList<Exercise>?): String? {
        return exercises?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toExerciseList(exercisesJson: String?): MutableList<Exercise>? {
        return exercisesJson?.let {
            val type = object : TypeToken<MutableList<Exercise>>() {}.type
            gson.fromJson(it, type)
        } ?: mutableListOf()
    }
}
