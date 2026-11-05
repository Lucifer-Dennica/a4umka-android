package com.example.a4umka.ui.screens.reading

import android.graphics.Path
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.ui.components.LetterCanvas
import com.example.a4umka.ui.components.LetterStyle
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterPracticeScreen(
    letter: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var style by remember { mutableStateOf(LetterStyle.PRINT) }
    var resetKey by remember { mutableIntStateOf(0) }
    val currentPaths = remember { mutableStateListOf<Path>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Пропись буквы «$letter»") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        currentPaths.clear()
                        resetKey++
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Очистить")
                    }
                    IconButton(onClick = {
                        val result = PdfExporter.exportLetter(
                            context = context,
                            letter = letter,
                            userPaths = currentPaths.toList(),
                            style = style
                        )
                        scope.launch {
                            if (result != null) {
                                snackbarHostState.showSnackbar("Сохранено: $result")
                            } else {
                                snackbarHostState.showSnackbar("Не удалось сохранить PDF")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить в PDF")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Переключатель «Печатная / Пропись»
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = style == LetterStyle.PRINT,
                    onClick = {
                        if (style != LetterStyle.PRINT) {
                            style = LetterStyle.PRINT
                            currentPaths.clear()
                            resetKey++
                        }
                    },
                    label = { Text("Печатная") }
                )
                FilterChip(
                    selected = style == LetterStyle.CURSIVE,
                    onClick = {
                        if (style != LetterStyle.CURSIVE) {
                            style = LetterStyle.CURSIVE
                            currentPaths.clear()
                            resetKey++
                        }
                    },
                    label = { Text("Пропись") }
                )
            }

            Text(
                text = "Обведи букву по пунктиру пальцем:",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            key(resetKey, style) {
                LetterCanvas(
                    letter = letter,
                    style = style,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onPathsChanged = { list ->
                        currentPaths.clear()
                        currentPaths.addAll(list)
                    }
                )
            }
        }
    }
}
