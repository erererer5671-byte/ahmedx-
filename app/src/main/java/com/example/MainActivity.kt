package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.SoccerAppMainView
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CareerViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Instantiate our core Career ViewModel
    val viewModel = ViewModelProvider(this)[CareerViewModel::class.java]

    setContent {
      MyApplicationTheme {
        SoccerAppMainView(viewModel = viewModel)
      }
    }
  }
}
