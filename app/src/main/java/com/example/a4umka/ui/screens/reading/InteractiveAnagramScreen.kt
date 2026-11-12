package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.a4umka.data.AnagramConfig
import com.example.a4umka.data.AnagramData
import com.example.a4umka.data.AnagramTask

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InteractiveAnagramScreen(
    config: AnagramConfig,
    onBack: () -> Unit
) {
    var currentTask by remember { mutableStateOf(AnagramData.generateTask(config)) }
    var selectedParts by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSolved by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    fun checkSolution() {
        val target = if (config.type == com.example.a4umka.data.AnagramType.SYLLABLES) currentTask.word.syllables else currentTask.word.letters
        if (selectedParts == target) {
            isSolved = true
            showError = false
        } else {
            showError = true
        }
    }

    fun loadNewTask() {
        currentTask = AnagramData.generateTask(config)
        selectedParts = emptyList()
        isSolved = false
        showError = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Собери слово") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { loadNewTask() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Пропустить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(currentTask.word.emoji, fontSize = 80.sp)
            Spacer(modifier = Modifier.height(32.dp))

            // Место для сборки слова
            Text("Твое слово:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (selectedParts.isEmpty()) {
                    Text("Нажимай на слоги внизу", color = Color.Gray, modifier = Modifier.align(Alignment.CenterVertically))
                } else {
                    selectedParts.forEachIndexed { index, part ->
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable(enabled = !isSolved) {
                                    val newList = selectedParts.toMutableList()
                                    newList.removeAt(index)
                                    selectedParts = newList
                                    showError = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSolved) Color(0xFF81C784) else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = part,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (isSolved) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showError) {
                Text("Пока не то, попробуй еще раз!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            if (isSolved) {
                Text("🎉 Молодец! Это ${currentTask.word.word}", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { loadNewTask() }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Следующее слово")
                }
            } else {
                Button(
                    onClick = { checkSolution() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = selectedParts.size == if (config.type == com.example.a4umka.data.AnagramType.SYLLABLES) currentTask.word.syllables.size else currentTask.word.letters.size
                ) {
                    Text("Проверить")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Доступные слоги/буквы
            Text("Доступные части:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.Center
            ) {
                val availableParts = currentTask.shuffledParts.filter { part -> !selectedParts.contains(part) }
                // Из-за того что части могут повторяться (например, буква "О" в слове МОЛОКО), нужно аккуратно считать
                val currentSelectedCounts = selectedParts.groupingBy { it }.eachCount()
                val usedCounts = mutableMapOf<String, Int>()

                val uniqueAvailable = mutableListOf<String>()
                for (part in currentTask.shuffledParts) {
                    val maxCount = currentTask.shuffledParts.count { it == part }
                    val selectedCount = currentSelectedCounts[part] ?: 0
                    val alreadyAdded = usedCounts[part] ?: 0
                    if (alreadyAdded < maxCount - selectedCount) {
                        uniqueAvailable.add(part)
                        usedCounts[part] = alreadyAdded + 1
                    }
                }

                uniqueAvailable.forEach { part ->
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                if (!isSolved) {
                                    selectedParts = selectedParts + part
                                    showError = false
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = part,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
