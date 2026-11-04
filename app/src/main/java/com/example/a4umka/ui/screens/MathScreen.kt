// app/src/main/java/com/example/a4umka/ui/screens/MathScreen.kt
package com.example.a4umka.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a4umka.data.TaskGenerator

@Composable
fun MathScreen() {
    val examples = TaskGenerator.generateMathExamples()
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Реши примеры:", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(examples) { example ->
                Text(text = example, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
