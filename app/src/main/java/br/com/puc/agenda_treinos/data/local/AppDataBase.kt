package br.com.puc.agenda_treinos.data.local

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.puc.agenda_treinos.data.local.converter.ExerciseListConverter
import br.com.puc.agenda_treinos.data.model.Workout

@Database(entities = [Workout::class], version = 1, exportSchema = false)
@TypeConverters(ExerciseListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(application: Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application.applicationContext,
                    AppDatabase::class.java,
                    "gym_workout_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}