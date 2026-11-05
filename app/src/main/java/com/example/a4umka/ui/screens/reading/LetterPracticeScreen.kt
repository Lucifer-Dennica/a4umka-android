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

    // Ключ сброса холста: меняем — Canvas пересоздаётся, штрихи пропадают
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
                        val result = PdfExporter.exportLetter(context, letter, currentPaths.toList())
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
            Text(
                text = "Обведи букву по пунктиру пальцем:",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            key(resetKey) {
                LetterCanvas(
                    letter = letter,
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
