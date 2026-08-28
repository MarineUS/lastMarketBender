package com.marineus.lastmarketbender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.GoogleMap
import com.marineus.lastmarketbender.ui.screens.MapScreen
import com.marineus.lastmarketbender.ui.theme.Track101Theme
import com.marineus.lastmarketbender.ui.viewmodels.MapViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MapViewModel = viewModel()
            Track101Theme(darkTheme = viewModel.isDarkMode) {
                MapScreen(viewModel = viewModel)
            }
        }
    }
}

