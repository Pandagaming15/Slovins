package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SlovinMainScreen
import com.example.ui.SlovinViewModel
import com.example.ui.theme.SlovinCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SlovinCalcTheme {
                val viewModel: SlovinViewModel = viewModel(
                    factory = SlovinViewModel.provideFactory(application)
                )
                SlovinMainScreen(viewModel = viewModel)
            }
        }
    }
}
