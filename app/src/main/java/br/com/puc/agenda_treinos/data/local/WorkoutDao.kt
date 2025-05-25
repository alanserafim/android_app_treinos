package br.com.puc.agenda_treinos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.puc.agenda_treinos.data.model.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("SELECT * FROM workouts ORDER BY name ASC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkoutById(id: String): Flow<Workout?>

    // Used for synchronous-like access within ViewModel, though still suspend
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutByIdSuspend(id: String): Workout?

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun getWorkoutsCount(): Int
}