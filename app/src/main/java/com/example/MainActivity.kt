package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CareeronixMainApp
import com.example.ui.theme.CareeronixTheme
import com.example.viewmodel.CareeronixViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Uncaught exception logger to prevent silent background crash issues
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        android.util.Log.e("MainActivity", "Uncaught exception on thread ${thread.name}", throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    setContent {
      val careerViewModel: CareeronixViewModel = viewModel()
      val dbError by careerViewModel.dbInitError.collectAsState()

      if (dbError != null) {
          CareeronixTheme {
              Box(
                  modifier = Modifier
                      .fillMaxSize()
                      .background(Color(0xFF1E1B4B))
                      .padding(24.dp),
                  contentAlignment = Alignment.Center
              ) {
                  Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                  ) {
                      Icon(
                          imageVector = Icons.Default.ErrorOutline,
                          contentDescription = "Error icon indicator",
                          tint = Color(0xFFEF4444),
                          modifier = Modifier.size(72.dp)
                      )
                      Spacer(modifier = Modifier.height(16.dp))
                      Text(
                          text = "Application Stopped Unexpectedly",
                          color = Color.White,
                          fontSize = 20.sp,
                          fontWeight = FontWeight.Bold
                      )
                      Spacer(modifier = Modifier.height(8.dp))
                      Text(
                          text = dbError?.localizedMessage ?: "Unknown initialization or database structure mismatch.",
                          color = Color.White.copy(alpha = 0.8f),
                          fontSize = 14.sp,
                          textAlign = TextAlign.Center
                      )
                      Spacer(modifier = Modifier.height(24.dp))
                      Button(
                          onClick = {
                              try {
                                  com.example.data.CareeronixDatabase.resetDatabase(this@MainActivity)
                                  finish()
                                  startActivity(intent)
                              } catch (e: Exception) {
                                  e.printStackTrace()
                              }
                          },
                          colors = ButtonDefaults.buttonColors(
                              containerColor = Color(0xFFEF4444)
                          )
                      ) {
                          Text("Reset & Recover App Database")
                      }
                  }
              }
          }
      } else {
          CareeronixTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
              val unused = innerPadding
              CareeronixMainApp(viewModel = careerViewModel)
            }
          }
      }
    }
  }
}
