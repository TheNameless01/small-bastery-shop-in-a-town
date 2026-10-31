package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.BakeryApp
import com.example.ui.theme.KhoubzaTheme
import com.example.ui.viewmodels.BakeryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KhoubzaTheme {
                val viewModel: BakeryViewModel = viewModel()
                BakeryApp(viewModel = viewModel)
            }
        }
    }
}
