package com.example.a4umka.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun ParentalGate(
    onPassed: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val a = remember { Random.nextInt(2, 10) }
    val b = remember { Random.nextInt(2, 10) }
    val answer = a + b
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    // Состояния для пасхалки
    var showEasterEgg by remember { mutableStateOf(false) }
    var easterEggBitmap by remember { mutableStateOf<Bitmap?>(null) }

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
                        input = newValue.filter { c -> c.isDigit() }.take(10)
                        error = false
                        
                        // --- СЕКРЕТНЫЙ КОД ---
                        if (input == "9968772") {
                            showEasterEgg = true
                            input = "" // Очищаем поле
                        }
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

    // --- ОКНО ПАСХАЛКИ ---
    if (showEasterEgg) {
        // Асинхронно загружаем картинку, чтобы не подвешивать интерфейс
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val stream = context.assets.open("booklet/3.jpg")
                    easterEggBitmap = BitmapFactory.decodeStream(stream)
                } catch (e: Exception) {
                    // Если файл не найден или ошибка чтения — ничего не делаем
                }
            }
        }
        
        Dialog(onDismissRequest = { 
            showEasterEgg = false 
            easterEggBitmap = null
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (easterEggBitmap != null) {
                    Image(
                        bitmap = easterEggBitmap!!.asImageBitmap(),
                        contentDescription = "Секрет",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}
