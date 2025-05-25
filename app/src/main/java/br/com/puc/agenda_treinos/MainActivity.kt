package br.com.puc.agenda_treinos

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.puc.agenda_treinos.ui.navigation.WorkoutAppNavigator
import br.com.puc.agenda_treinos.ui.theme.Agenda_treinosTheme
import br.com.puc.agenda_treinos.viewmodel.WorkoutViewModel
import br.com.puc.agenda_treinos.viewmodel.WorkoutViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Agenda_treinosTheme {
                val viewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModelFactory(application)
                )
                WorkoutAppNavigator(viewModel = viewModel)
            }
        }
    }
}
