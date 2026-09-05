package com.marineus.lastmarketbender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.marineus.lastmarketbender.ui.screens.MapScreen
import com.marineus.lastmarketbender.ui.theme.Track101Theme
import com.marineus.lastmarketbender.ui.viewmodels.MapViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MapViewModel = hiltViewModel()
            Track101Theme(darkTheme = viewModel.isDarkMode) {
                MapScreen(viewModel = viewModel)
            }
        }
    }
}