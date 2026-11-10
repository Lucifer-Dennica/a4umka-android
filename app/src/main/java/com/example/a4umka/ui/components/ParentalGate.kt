package com.example.a4umka.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun ParentalGate(
    onPassed: () -> Unit,
    onCancel: () -> Unit
) {
    val a = remember { Random.nextInt(2, 10) }
    val b = remember { Random.nextInt(2, 10) }
    val answer = a + b
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Вход для родителей") },
        text = {
            Column {
                Text("Решите пример, чтобы продолжить:")
                Spacer(modifier = Modifier.height(12.dp))
                Text("$a + $b = ?", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { newValue ->
                        input = newValue.filter { c -> c.isDigit() }.take(3)
                        error = false
                    },
                    isError = error,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (error) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Неверно, попробуйте ещё раз", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (input.toIntOrNull() == answer) onPassed() else error = true
            }) { Text("ОК") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Отмена") }
        }
    )
}
