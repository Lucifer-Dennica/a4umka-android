package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.a4umka.data.ProverbConfig
import com.example.a4umka.data.ProverbData
import com.example.a4umka.data.ProverbTask

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InteractiveProverbScreen(
    config: ProverbConfig,
    onBack: () -> Unit
) {
    var currentTask by remember { mutableStateOf<ProverbTask?>(null) }
    var selectedCards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isSolved by remember { mutableStateOf(false) }

    fun loadNewTask() {
        currentTask = ProverbData.generateTask(config)
        selectedCards = emptyList()
        isSolved = false
    }

    LaunchedEffect(Unit) {
        loadNewTask()
    }

    fun checkSolution() {
        val task = currentTask ?: return
        val isCorrect = selectedCards.map { it.first } == task.correctSequence
        if (isCorrect) {
            isSolved = true
        } else {
            selectedCards = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Собери пословицу") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { loadNewTask() }) {
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
            val task = currentTask
            if (task != null) {
                Text(
                    text = task.instruction,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Твой порядок (нажми, чтобы убрать):", style = MaterialTheme.typography.labelLarge)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp)
                        .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    if (selectedCards.isEmpty()) {
                        Text("Нажимай на карточки внизу в правильном порядке", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            selectedCards.forEach { card ->
                                Card(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .clickable(enabled = !isSolved) {
                                            // Удаляем карточку из выбранных
                                            selectedCards = selectedCards - card
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF81C784))
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                                        Text(card.first, fontSize = 10.sp, color = Color.White)
                                        Text(card.second, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedCards.isNotEmpty() && !isSolved) {
                    Button(
                        onClick = { checkSolution() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = selectedCards.size == task.cards.size
                    ) {
                        Text("Проверить")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isSolved) {
                    Text(
                        text = "🎉 Молодец! Фраза: ${task.phrase}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Закончить")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Доступные карточки:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val availableCards = task.cards.filter { card -> !selectedCards.contains(card) }
                    availableCards.forEach { card ->
                        Card(
                            modifier = Modifier
                                .width(60.dp)
                                .clickable {
                                    if (!isSolved) {
                                        selectedCards = selectedCards + card
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                                Text(card.first, fontSize = 10.sp, color = Color.Gray)
                                Text(card.second, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
