// app/src/main/java/com/example/a4umka/ui/screens/HomeScreen.kt
package com.example.a4umka.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "А4Умка",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { /* Переход к прописям */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Прописи")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* Переход к математике */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Математика")
        }
    }
}
