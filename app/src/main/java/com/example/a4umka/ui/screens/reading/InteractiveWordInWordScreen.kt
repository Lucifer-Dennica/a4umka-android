package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.WordInWordData
import com.example.a4umka.data.WordPair

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveWordInWordScreen(onBack: () -> Unit) {
    var currentPair by remember { mutableStateOf(WordInWordData.getRandomPairs(1).first()) }
    var selectedIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isSolved by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    fun checkSolution() {
        if (selectedIndices.size != currentPair.shortWord.length) return
        
        val selectedLetters = selectedIndices.sorted().map { currentPair.longWord[it] }.joinToString("")
        if (selectedLetters == currentPair.shortWord) {
            isSolved = true
            showError = false
        } else {
            showError = true
        }
    }

    fun nextWord() {
        currentPair = WordInWordData.getRandomPairs(1).first()
        selectedIndices = emptySet()
        isSolved = false
        showError = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Найди слово") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { nextWord() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Следующее")
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
            Text(currentPair.emoji, fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Найди слово: ${currentPair.shortWord}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(32.dp))

            Text("Нажимай на буквы в слове, чтобы выделить спрятанное слово:", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in currentPair.longWord.indices) {
                    val char = currentPair.longWord[i]
                    val isSelected = selectedIndices.contains(i)
                    val bgColor = when {
                        isSolved && isSelected -> Color(0xFF81C784)
                        showError && isSelected -> Color(0xFFEF9A9A)
                        isSelected -> Color(0xFF64B5F6)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(bgColor, MaterialTheme.shapes.small)
                            .clickable(enabled = !isSolved) {
                                selectedIndices = if (isSelected) selectedIndices - i else selectedIndices + i
                                showError = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(char.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showError) {
                Text("Не совсем так, попробуй еще раз!", color = MaterialTheme.colorScheme.error)
            }

            if (isSolved) {
                Text("🎉 Молодец! Ты нашел слово!", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { nextWord() }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Следующее слово")
                }
            } else {
                Button(
                    onClick = { checkSolution() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = selectedIndices.isNotEmpty()
                ) {
                    Text("Проверить")
                }
            }
        }
    }
}
