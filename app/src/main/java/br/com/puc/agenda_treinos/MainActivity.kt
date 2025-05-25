package br.com.puc.agenda_treinos

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

// --- Room Database Setup ---

// TypeConverter for List<Exercise>
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

// Data Classes (Entities)
@Entity(tableName = "exercises") // Though not directly stored as a table, defined for clarity if ever needed separately
data class Exercise(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var name: String,
    var sets: String,
    var reps: String,
    var notes: String? = null
)

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var name: String,
    @ColumnInfo(name = "exercises_list") // Room will use TypeConverter for this field
    var exercises: MutableList<Exercise> = mutableListOf()
)

// DAO (Data Access Object)
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

// Database
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
                    .fallbackToDestructiveMigration() // Not for production, for simplicity
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- ViewModel ---
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

    fun deleteWorkout(workout: Workout) { // Changed to accept Workout object for DAO
        viewModelScope.launch {
            workoutDao.deleteWorkout(workout)
        }
    }
}

// --- Main Activity & UI ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymWorkoutAppTheme {
                // Obtain ViewModel using the factory for AndroidViewModel
                val viewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModelFactory(application)
                )
                WorkoutAppNavigator(viewModel = viewModel)
            }
        }
    }
}

// ViewModel Factory for AndroidViewModel
class WorkoutViewModelFactory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// Theme (Assuming Typography() and darkColorScheme() are defined in ui.theme package)
@Composable
fun GymWorkoutAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(), // Or lightColorScheme()
        typography = Typography(),
        content = content
    )
}

// Navigation
enum class Screen {
    WorkoutList,
    WorkoutDetails
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutAppNavigator(viewModel: WorkoutViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.WorkoutList) }
    var selectedWorkoutId by remember { mutableStateOf<String?>(null) }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.WorkoutList -> WorkoutListScreen(
                    viewModel = viewModel,
                    onWorkoutClick = { workoutId ->
                        selectedWorkoutId = workoutId
                        currentScreen = Screen.WorkoutDetails
                    }
                )
                Screen.WorkoutDetails -> {
                    selectedWorkoutId?.let { workoutId ->
                        // Observe the Flow for the selected workout
                        val workoutState = viewModel.getWorkoutById(workoutId).collectAsState(initial = null)
                        val workout = workoutState.value

                        if (workout != null) {
                            WorkoutDetailsScreen(
                                workout = workout,
                                viewModel = viewModel,
                                onBack = {
                                    currentScreen = Screen.WorkoutList
                                    selectedWorkoutId = null
                                }
                            )
                        } else {
                            // Show loading indicator or handle null workout state
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                // Text("Carregando treino...") // Or a CircularProgressIndicator
                                // If it remains null after a while, it means workout was deleted or ID is wrong
                            }
                        }
                    } ?: run {
                        // This case should ideally not be reached if navigation is managed correctly.
                        // If selectedWorkoutId is null, we should be on WorkoutList.
                        // For safety, navigate back to list.
                        LaunchedEffect(Unit) {
                            currentScreen = Screen.WorkoutList
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    viewModel: WorkoutViewModel,
    onWorkoutClick: (String) -> Unit
) {
    val workoutsState = viewModel.workouts.collectAsState(initial = emptyList())
    val workouts = workoutsState.value
    var showAddWorkoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meus Treinos") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddWorkoutDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Treino")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum treino cadastrado ainda. Clique no '+' para adicionar.")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(workouts, key = { it.id }) { workout ->
                        WorkoutListItem(
                            workout = workout,
                            onClick = { onWorkoutClick(workout.id) },
                            onDelete = { viewModel.deleteWorkout(workout) } // Pass workout object
                        )
                        Divider()
                    }
                }
            }
        }
    }

    if (showAddWorkoutDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onAddWorkout = { workoutName ->
                viewModel.addWorkout(workoutName)
                showAddWorkoutDialog = false
            }
        )
    }
}

@Composable
fun WorkoutListItem(workout: Workout, onClick: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = workout.name, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir Treino", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Excluir Treino") },
            text = { Text("Tem certeza que deseja excluir o treino '${workout.name}'? Esta ação não pode ser desfeita.") },
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
                    Button(onClick = {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    workout: Workout, // Workout is now directly passed after being collected
    viewModel: WorkoutViewModel,
    onBack: () -> Unit
) {
    var showAddEditExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }

    // The workout object itself is now the source of truth for its exercises,
    // and it's updated reactively from the Flow.
    val exercises = workout.exercises.toList() // Create a stable copy for LazyColumn

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
                                exerciseToEdit = exercise // Correctly assign the exercise to be edited
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
            Column(modifier = Modifier.padding(16.dp).heightIn(max=600.dp)) { // Increased max height
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
