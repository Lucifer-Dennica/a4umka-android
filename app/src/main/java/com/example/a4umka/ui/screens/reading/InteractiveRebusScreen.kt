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
import com.example.a4umka.data.RebusData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveRebusScreen(
    word: String,
    onBack: () -> Unit
) {
    val rebuses = remember(word) { RebusData.getRebusWordsForWord(word) }
    val answers = remember(word) { mutableStateListOf(*Array(word.length) { "" }) }
    var isSolved by remember { mutableStateOf(false) }

    fun check() {
        var allCorrect = true
        for (i in word.indices) {
            val char = word[i]
            val input = answers[i].uppercase()
            if (char == ' ') continue
            
            val rebus = rebuses[i]
            val expected = if (rebus != null) rebus.word.first().uppercaseChar().toString() else char.toString()
            
            if (input != expected) {
                allCorrect = false
            }
        }
        if (allCorrect) isSolved = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Решаем ребус") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        for (i in answers.indices) answers[i] = ""
                        isSolved = false
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
                text = "Назови картинку и запиши первую букву:",
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Карточки с эмодзи и полями ввода
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in word.indices) {
                    val char = word[i]
                    if (char == ' ') continue
                    val rebus = rebuses[i]

                    Card(
                        modifier = Modifier.width(90.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (rebus != null) {
                                Text(rebus.emoji, fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = answers[i],
                                    onValueChange = { 
                                        answers[i] = it.take(1).uppercase()
                                        isSolved = false
                                    },
                                    modifier = Modifier.width(50.dp).height(50.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                )
                            } else {
                                // Готовая буква
                                Text(
                                    text = char.toString(),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("(готовая)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isSolved) {
                Text(
                    text = "🎉 Молодец! Слово разгадано: $word",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                Button(onClick = { check() }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Проверить")
                }
            }
        }
    }
}
