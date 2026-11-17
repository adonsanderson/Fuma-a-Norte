package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fumacanorte.presentation.navigation.FumacaNorteNavGraph
import com.example.fumacanorte.presentation.theme.FumacaNorteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FumacaNorteTheme {
                FumacaNorteNavGraph()
            }
        }
    }
}
