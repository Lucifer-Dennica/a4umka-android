package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.FunnyStory
import com.example.a4umka.data.FunnyStoryConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveFunnyStoryScreen(
    story: FunnyStory,
    onBack: () -> Unit
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateListOf<String>() }
    var currentAnswer by remember { mutableStateOf("") }
    var isFinished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Весёлая история") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        currentQuestionIndex = 0
                        answers.clear()
                        currentAnswer = ""
                        isFinished = false
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
            if (!isFinished) {
                Text(
                    text = "Вопрос ${currentQuestionIndex + 1} из ${story.questions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = story.questions[currentQuestionIndex],
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = currentAnswer,
                    onValueChange = { currentAnswer = it },
                    label = { Text("Ваш ответ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (currentAnswer.isNotBlank()) {
                            answers.add(currentAnswer.trim())
                            currentAnswer = ""
                            if (currentQuestionIndex < story.questions.size - 1) {
                                currentQuestionIndex++
                            } else {
                                isFinished = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = currentAnswer.isNotBlank()
                ) {
                    Text(if (currentQuestionIndex < story.questions.size - 1) "Далее" else "Показать историю!")
                }
            } else {
                Text(
                    text = "История готова!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Заменяем {1}, {2} и т.д. на ответы
                var finalText = story.template
                answers.forEachIndexed { index, answer ->
                    finalText = finalText.replace("${index + 1}", answer)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = finalText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = 24.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Прочитайте историю вслух!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
