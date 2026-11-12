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
import com.example.a4umka.data.CustomCrosswordConfig
import com.example.a4umka.data.CustomCrosswordData
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveCustomCrosswordScreen(
    config: CustomCrosswordConfig,
    onBack: () -> Unit
) {
    val pairs = remember { CustomCrosswordData.parseInput(config.wordsInput) }
    val grid = remember(config.seed) { CustomCrosswordData.generateGrid(pairs, config.seed) }
    
    // Состояние введенных букв: Pair(x, y) -> Char
    val userInputs = remember { mutableStateMapOf<Pair<Int, Int>, String>() }
    var isSolved by remember { mutableStateOf(false) }
    
    fun checkSolution() {
        var allCorrect = true
        for ((pos, char) in grid.cells) {
            if (userInputs[pos]?.uppercase() != char.toString()) {
                allCorrect = false
                break
            }
        }
        if (allCorrect) isSolved = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(config.title.ifEmpty { "Кроссворд" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { userInputs.clear(); isSolved = false }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Очистить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (grid.cells.isEmpty()) {
                Text("Нет слов для отображения. Проверьте ввод.")
                return@Column
            }

            val minX = grid.cells.keys.minOfOrNull { it.first } ?: 0
            val maxX = grid.cells.keys.maxOfOrNull { it.first } ?: 0
            val minY = grid.cells.keys.minOfOrNull { it.second } ?: 0
            val maxY = grid.cells.keys.maxOfOrNull { it.second } ?: 0

            val cols = maxX - minX + 1
            val rows = maxY - minY + 1

            // Отрисовка сетки (упрощенно через Column/Row для интерактива)
            Column(modifier = Modifier.border(1.dp, Color.Gray)) {
                for (y in minY..maxY) {
                    Row {
                        for (x in minX..maxX) {
                            val pos = Pair(x, y)
                            val isCell = grid.cells.containsKey(pos)
                            
                            if (isCell) {
                                val wordNum = grid.words.find { it.x == x && it.y == y }?.num
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(0.5.dp, Color.Gray)
                                        .background(if (userInputs[pos]?.uppercase() == grid.cells[pos].toString()) Color(0xFFC8E6C9) else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (wordNum != null) {
                                        Text(
                                            text = wordNum.toString(),
                                            fontSize = 8.sp,
                                            color = Color.Red,
                                            modifier = Modifier.align(Alignment.TopStart).padding(2.dp)
                                        )
                                    }
                                    BasicTextField(
                                        value = userInputs[pos] ?: "",
                                        onValueChange = { 
                                            if (it.length <= 1) {
                                                userInputs[pos] = it.uppercase()
                                                isSolved = false
                                            }
                                        },
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isSolved) {
                Text("🎉 Молодец! Кроссворд решен!", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Button(onClick = { checkSolution() }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Проверить")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text("Вопросы:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            
            grid.words.forEach { word ->
                val question = pairs.find { it.word == word.word }?.question ?: ""
                Text("${word.num} ${if (word.isAcross) "→" else "↓"} $question", fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp).align(Alignment.Start))
            }
        }
    }
}
