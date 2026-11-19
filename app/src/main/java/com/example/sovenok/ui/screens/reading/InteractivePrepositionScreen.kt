package com.example.sovenok.ui.screens.reading

import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sovenok.data.PrepositionConfig
import com.example.sovenok.data.PrepositionData
import com.example.sovenok.data.PrepositionTask
import com.example.sovenok.ui.components.ZoomableImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractivePrepositionScreen(
    config: PrepositionConfig,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    // Генерируем задания один раз при входе в режим
    val tasks = remember(config) { PrepositionData.generateTasks(config) }
    
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf(false) }
    var isWrong by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    val currentTask = tasks.getOrNull(currentIndex)

    // Загрузка картинки из assets
    val bitmap = remember(currentTask) {
        if (currentTask != null) {
            try {
                val assetPath = "prepositions/${currentTask.scene.folderName}/${currentTask.preposition.fileName}.png"
                context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Игра: Предлоги") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        currentIndex = 0
                        selectedOption = null
                        isCorrect = false
                        isWrong = false
                        isFinished = false
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Заново")
                    }
                }
            )
        }
    ) { padding ->
        if (isFinished) {
            // Экран завершения
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎉", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Отлично справился!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ты прошел все задания", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Вернуться")
                }
                OutlinedButton(
                    onClick = {
                        currentIndex = 0
                        selectedOption = null
                        isCorrect = false
                        isWrong = false
                        isFinished = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Пройти ещё раз")
                }
            }
        } else if (currentTask != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Прогресс
                Text(
                    text = "Задание ${currentIndex + 1} из ${tasks.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Картинка (с зумом)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    if (bitmap != null) {
                        ZoomableImage(
                            bitmap = bitmap,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Заглушка
                        Text(
                            "Картинка не найдена:\n${currentTask.scene.folderName}/${currentTask.preposition.fileName}",
                            textAlign = TextAlign.Center,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Вопрос
                Text(
                    text = currentTask.question,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Варианты ответов
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentTask.options.forEach { option ->
                        val isThisCorrect = option == currentTask.correctAnswer
                        val isSelected = selectedOption == option
                        
                        // Анимация цвета кнопки
                        val buttonColor by animateColorAsState(
                            targetValue = when {
                                isSelected && isThisCorrect -> Color(0xFF81C784) // Зеленый при успехе
                                isSelected && isWrong -> Color(0xFFEF9A9A) // Красный при ошибке
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            label = "color"
                        )

                        Button(
                            onClick = {
                                if (!isCorrect) {
                                    selectedOption = option
                                    if (isThisCorrect) {
                                        isCorrect = true
                                        isWrong = false
                                    } else {
                                        isWrong = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(option, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Логика перехода к следующему заданию
                LaunchedEffect(isCorrect) {
                    if (isCorrect) {
                        delay(1000) // Ждем 1 секунду, чтобы ребенок увидел зеленую кнопку
                        if (currentIndex < tasks.size - 1) {
                            currentIndex++
                            selectedOption = null
                            isCorrect = false
                            isWrong = false
                        } else {
                            isFinished = true
                        }
                    }
                }
            }
        }
    }
}
