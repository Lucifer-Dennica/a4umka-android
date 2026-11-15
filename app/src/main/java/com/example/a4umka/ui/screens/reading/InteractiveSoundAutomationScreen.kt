package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.SoundAutomationConfig
import com.example.a4umka.data.SoundAutomationData
import com.example.a4umka.data.SpeechMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveSoundAutomationScreen(
    config: SoundAutomationConfig,
    onBack: () -> Unit
) {
    val data = remember(config) { SoundAutomationData.getData(config.sound) }
    
    // Собираем все элементы для интерактива (только слова и слоги)
    val items = remember(config) {
        when (config.mode) {
            SpeechMode.SYLLABLES -> data.syllables.map { it.text }
            SpeechMode.WORDS -> (data.wordsStart + data.wordsMiddle + data.wordsEnd).map { it.text }
            else -> data.syllables.map { it.text } // По умолчанию слоги
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var taps by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    val currentText = items.getOrNull(currentIndex) ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Игра: Звук ${config.sound.letter}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        currentIndex = 0
                        taps = 0
                        isFinished = false
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Заново")
                    }
                }
            )
        }
    ) { padding ->
        if (isFinished) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎉", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Отлично!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Вернуться")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Проговори 3 раза и нажми на кружки",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Карточка со словом
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentText,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Кружочки
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.clickable {
                        if (taps < 3) {
                            taps++
                            if (taps == 3) {
                                // Небольшая задержка перед следующим словом
                            }
                        }
                    }
                ) {
                    for (i in 0 until 3) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(if (i < taps) MaterialTheme.colorScheme.primary else Color.LightGray)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        if (currentIndex < items.size - 1) {
                            currentIndex++
                            taps = 0
                        } else {
                            isFinished = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    enabled = taps == 3
                ) {
                    Text("Дальше", fontSize = 18.sp)
                }
            }
        }
    }
}
