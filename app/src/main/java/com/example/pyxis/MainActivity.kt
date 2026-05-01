package com.example.pyxis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pyxis.data.AppDatabase
import com.example.pyxis.data.repository.InventoryRepository
import com.example.pyxis.ui.nav.AppNavHost
import com.example.pyxis.ui.theme.PyxisTheme
import com.example.pyxis.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: InventoryViewModel by viewModels {
        val db = AppDatabase.getInstance(applicationContext)
        InventoryViewModel.Factory(InventoryRepository(db.inventoryDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PyxisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}