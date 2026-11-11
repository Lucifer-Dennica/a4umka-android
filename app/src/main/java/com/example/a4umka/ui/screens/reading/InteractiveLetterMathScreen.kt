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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.LetterMathConfig
import com.example.a4umka.data.LetterMathData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveLetterMathScreen(
    config: LetterMathConfig,
    onBack: () -> Unit
) {
    val problems = remember { LetterMathData.generateProblems(config, 8) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val problem = problems[currentIndex]
    
    val answers = remember(currentIndex) { mutableStateListOf(*Array(problem.targetWord.length) { "" }) }
    var isSolved by remember(currentIndex) { mutableStateOf(false) }
    var showError by remember(currentIndex) { mutableStateOf(false) }

    fun check() {
        var allCorrect = true
        for (i in problem.targetWord.indices) {
            if (answers[i].uppercase() != problem.targetWord[i].toString()) {
                allCorrect = false
            }
        }
        if (allCorrect) {
            isSolved = true
            showError = false
        } else {
            showError = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сложи слово") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        for (i in answers.indices) answers[i] = ""
                        isSolved = false
                        showError = false
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
            Text(
                text = "Задание ${currentIndex + 1} из ${problems.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = problem.formula + " =",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (config.hint == com.example.a4umka.data.LetterMathHint.PICTURE) {
                Text(problem.emoji, fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Клеточки для ответа
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in problem.targetWord.indices) {
                    OutlinedTextField(
                        value = answers[i],
                        onValueChange = { 
                            answers[i] = it.take(1).uppercase()
                            isSolved = false
                            showError = false
                        },
                        modifier = Modifier.width(45.dp).height(55.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showError) {
                Text("Есть ошибка, попробуй еще раз!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isSolved) {
                Text(
                    text = "🎉 Молодец! Слово: ${problem.targetWord}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (currentIndex < problems.size - 1) {
                    Button(
                        onClick = { currentIndex++ }, 
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Далее")
                    }
                } else {
                    Button(
                        onClick = onBack, 
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Закончить")
                    }
                }
            } else {
                Button(onClick = { check() }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Проверить")
                }
            }
        }
    }
}
