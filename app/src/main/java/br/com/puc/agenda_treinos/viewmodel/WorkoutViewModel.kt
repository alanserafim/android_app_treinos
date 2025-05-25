package br.com.puc.agenda_treinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.puc.agenda_treinos.data.local.AppDatabase // Importação ajustada
import br.com.puc.agenda_treinos.data.model.Exercise // Importação ajustada
import br.com.puc.agenda_treinos.data.model.Workout // Importação ajustada
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val workoutDao = AppDatabase.getDatabase(application).workoutDao()

    val workouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()

    init {
        viewModelScope.launch {
            if (workoutDao.getWorkoutsCount() == 0) {
                addInitialData()
            }
        }
    }

    private suspend fun addInitialData() {
        val workoutA = Workout(name = "Treino A - Peito e Tríceps", exercises = mutableListOf(
            Exercise(name = "Supino Reto", sets = "4", reps = "8-12"),
            Exercise(name = "Crucifixo Inclinado", sets = "3", reps = "10-15")
        ))
        val workoutB = Workout(name = "Treino B - Costas e Bíceps")
        val workoutC = Workout(name = "Treino C - Pernas e Ombros")

        workoutDao.insertWorkout(workoutA)
        workoutDao.insertWorkout(workoutB)
        workoutDao.insertWorkout(workoutC)
    }

    fun addWorkout(name: String) {
        viewModelScope.launch {
            val newWorkout = Workout(name = name)
            workoutDao.insertWorkout(newWorkout)
        }
    }

    fun getWorkoutById(id: String): Flow<Workout?> {
        return workoutDao.getWorkoutById(id)
    }

    fun addExerciseToWorkout(workoutId: String, exercise: Exercise) {
        viewModelScope.launch {
            val workout = workoutDao.getWorkoutByIdSuspend(workoutId)
            workout?.let {
                it.exercises.add(exercise)
                workoutDao.updateWorkout(it)
            }
        }
    }

    fun updateExerciseInWorkout(workoutId: String, exerciseId: String, updatedExercise: Exercise) {
        viewModelScope.launch {
            val workout = workoutDao.getWorkoutByIdSuspend(workoutId)
            workout?.let {
                val index = it.exercises.indexOfFirst { ex -> ex.id == exerciseId }
                if (index != -1) {
                    it.exercises[index] = updatedExercise
                    workoutDao.updateWorkout(it)
                }
            }
        }
    }

    fun deleteExerciseFromWorkout(workoutId: String, exerciseId: String) {
        viewModelScope.launch {
            val workout = workoutDao.getWorkoutByIdSuspend(workoutId)
            workout?.let {
                it.exercises.removeAll { ex -> ex.id == exerciseId }
                workoutDao.updateWorkout(it)
            }
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutDao.deleteWorkout(workout)
        }
    }
}