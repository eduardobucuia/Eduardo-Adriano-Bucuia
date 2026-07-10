package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.PharmacyRepository
import com.example.ui.PharmacyApp
import com.example.ui.PharmacyViewModel
import com.example.ui.PharmacyViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Firebase programmatically to prevent crash if google-services.json is absent
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:486588265012:android:5fa7f0a8d438bbfa0f8c3c")
                    .setProjectId("farmacia-la-reference")
                    .setApiKey("AIzaSyB9_mockApiKeyForOfflineIntegrationSoAppDoesNotCrash")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(this, options)
                android.util.Log.d("FirebaseInit", "Firebase programmatically initialized.")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseInit", "Firebase programmatically initialization failed: ${e.message}")
        }

        val database = AppDatabase.getDatabase(this)
        val repository = PharmacyRepository(database)
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: PharmacyViewModel = viewModel(
                        factory = PharmacyViewModelFactory(repository)
                    )
                    PharmacyApp(viewModel = viewModel)
                }
            }
        }
    }
}

