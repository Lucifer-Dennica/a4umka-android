package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveFairyTaleScreen(
    config: FairyTaleConfig,
    onBack: () -> Unit
) {
    // Генерируем задания для игры
    val tasks = remember { 
        val baseTask = FairyTaleData.generateTask(config)
        // Для игры сделаем 5 вопросов (смешаем исправления и картинки)
        val questions = mutableListOf<Pair<String, String>>() // Question, Answer
        
        baseTask.fixNames.forEach { 
            questions.add(Pair("Как звучит правильно: «${it.brokenName}»?", it.correctName))
        }
        baseTask.guessPictures.forEach { 
            questions.add(Pair("Угадай сказку:\n${it.emojis}", it.title))
        }
        questions.shuffled().take(5)
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    val currentQ = tasks.getOrNull(currentQuestionIndex)
    
    // Генерируем варианты ответов
    val options = remember(currentQuestionIndex) {
        if (currentQ == null) emptyList()
        else {
            val correct = currentQ.second
            val allTales = if (config.age == FairyTaleAge.AGE_5_6) 
                listOf("Репка", "Колобок", "Теремок", "Курочка Ряба", "Красная Шапочка", "Кот в сапогах", "Маша и медведь", "Три медведя", "Гуси-лебеди", "Заюшкина избушка")
            else 
                listOf("Снежная королева", "Дюймовочка", "Русалочка", "Бременские музыканты", "Дикие лебеди", "Сказка о рыбаке и рыбке", "Золушка", "Буратино", "Карлик Нос", "Сказка о царе Салтане")
            
            val wrong = allTales.filter { it != correct }.shuffled().take(3)
            (wrong + correct).shuffled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Викторина по сказкам") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        currentQuestionIndex = 0
                        score = 0
                        isFinished = false
                        selectedOption = null
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Заново")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFinished) {
                Text("Викторина завершена!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Твой результат: $score из ${tasks.size}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Закончить")
                }
            } else if (currentQ != null) {
                Text("Вопрос ${currentQuestionIndex + 1} из ${tasks.size}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = currentQ.first, 
                    style = MaterialTheme.typography.headlineSmall, 
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(32.dp))

                options.forEach { option ->
                    val isCorrect = option == currentQ.second
                    val isSelected = selectedOption == option
                    
                    val buttonColor = when {
                        selectedOption == null -> MaterialTheme.colorScheme.surfaceVariant
                        isSelected && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                        !isSelected && isCorrect && selectedOption != null -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Button(
                        onClick = {
                            if (selectedOption == null) {
                                selectedOption = option
                                if (isCorrect) score++
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text(option, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                if (selectedOption != null) {
                    Button(
                        onClick = {
                            if (currentQuestionIndex < tasks.size - 1) {
                                currentQuestionIndex++
                                selectedOption = null
                            } else {
                                isFinished = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text(if (currentQuestionIndex < tasks.size - 1) "Следующий вопрос" else "Показать результат")
                    }
                }
            }
        }
    }
}
