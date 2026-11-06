package com.example.a4umka.ui.screens.hand // <-- ИЗМЕНЕНО

import android.graphics.Path
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
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
    onExportClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var resetKey by remember { mutableIntStateOf(0) }

    val printPaths = remember { mutableStateListOf<Path>() }
    val cursivePaths = remember { mutableStateListOf<Path>() }
    var printSize by remember { mutableStateOf(IntSize.Zero) }
    var cursiveSize by remember { mutableStateOf(IntSize.Zero) }

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
                        printPaths.clear()
                        cursivePaths.clear()
                        resetKey++
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Очистить")
                    }
                    IconButton(onClick = {
                        // (Оставил старую логику сохранения рисунка)
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить рисунок в PDF")
                    }
                    IconButton(onClick = onExportClick) {
                        Icon(Icons.Default.Print, contentDescription = "Сгенерировать лист прописей")
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Обведи обе буквы по пунктиру:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(text = "Печатная", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().weight(1f).onSizeChanged { printSize = it }) {
                key(resetKey, "print") {
                    LetterCanvas(
                        letter = letter,
                        letterStyle = LetterStyle.PRINT,
                        modifier = Modifier.fillMaxSize(),
                        onPathsChanged = { list ->
                            printPaths.clear()
                            printPaths.addAll(list)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(text = "Пропись", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().weight(1f).onSizeChanged { cursiveSize = it }) {
                key(resetKey, "cursive") {
                    LetterCanvas(
                        letter = letter,
                        letterStyle = LetterStyle.CURSIVE,
                        modifier = Modifier.fillMaxSize(),
                        onPathsChanged = { list ->
                            cursivePaths.clear()
                            cursivePaths.addAll(list)
                        }
                    )
                }
            }
        }
    }
}
