// app/src/main/java/com/example/a4umka/ui/screens/HandwritingScreen.kt
package com.example.a4umka.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HandwritingScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Прописи", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Здесь будет генератор прописей с возможностью сохранения в PDF.")
    }
}
