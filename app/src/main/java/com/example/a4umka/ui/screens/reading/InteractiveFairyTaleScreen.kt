package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveFairyTaleScreen(
    config: FairyTaleConfig,
    onBack: () -> Unit
) {
    // Список вопросов
    val tasks = remember {
        val baseTask = FairyTaleData.generateTask(config)
        val questions = mutableListOf<Pair<String, String>>()
        baseTask.fixNames.forEach {
            questions.add(Pair("Как звучит правильно: «${it.brokenName}»?", it.correctName))
        }
        baseTask.guessPictures.forEach {
            questions.add(Pair("Угадай сказку:\n${it.emojis}", it.title))
        }
        questions.shuffled().take(5)
    }

    // Данные для отчета (Вопрос, Правильный ответ, Ответ пользователя)
    data class QuizResult(val question: String, val correct: String, val userAnswer: String)

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val userResults = remember { mutableStateListOf<QuizResult>() }

    val currentQ = tasks.getOrNull(currentQuestionIndex)

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
                        isFinished = false
                        selectedOption = null
                        userResults.clear()
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
                
                val correctCount = userResults.count { it.correct == it.userAnswer }
                Text("Твой результат: $correctCount из ${tasks.size}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))

                // Подробный разбор
                userResults.forEachIndexed { index, result ->
                    val isCorrect = result.correct == result.userAnswer
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${index + 1}. ${result.question}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Твой ответ: ${result.userAnswer}", color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828))
                            if (!isCorrect) {
                                Text("Правильно: ${result.correct}", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
                        isSelected && isCorrect -> Color(0xFFA5D6A7) // Зеленый при верном
                        isSelected && !isCorrect -> Color(0xFFEF9A9A) // Красный при неверном
                        !isSelected && isCorrect && selectedOption != null -> Color(0xFFC8E6C9) // Подсветить верный, если ошиблись
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Button(
                        onClick = {
                            if (selectedOption == null) {
                                selectedOption = option
                                userResults.add(QuizResult(currentQ.first, currentQ.second, option))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = Color.Black)
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
