package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.SettleUpRepository
import com.example.ui.SettleUpMainApp
import com.example.ui.SettleUpViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SettleUp Room persistence and repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SettleUpRepository(database.settleUpDao)

        // Instantiate our SettleUpViewModel using a Factory
        val viewModel: SettleUpViewModel by viewModels {
            SettleUpViewModel.Factory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.ui.theme.BrandDarkBackground
                ) {
                    SettleUpMainApp(viewModel = viewModel)
                }
            }
        }
    }
}
