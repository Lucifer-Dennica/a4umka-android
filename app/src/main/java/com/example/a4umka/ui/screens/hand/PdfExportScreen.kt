package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.a4umka.data.LetterCase
import com.example.a4umka.data.PdfExportConfig
import com.example.a4umka.util.PdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportScreen(
    letter: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(PdfExportConfig()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройка листа") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val result = PdfExporter.exportWorksheet(context, letter, config)
                        scope.launch {
                            if (result != null) {
                                snackbarHostState.showSnackbar("Сохранено: $result")
                            } else {
                                snackbarHostState.showSnackbar("Ошибка сохранения")
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
                .verticalScroll(rememberScrollState())
        ) {
            // Настройки
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Какие буквы показывать:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = config.letterCase == LetterCase.UPPERCASE,
                        onClick = { config = config.copy(letterCase = LetterCase.UPPERCASE) },
                        label = { Text("А Б В") }
                    )
                    FilterChip(
                        selected = config.letterCase == LetterCase.LOWERCASE,
                        onClick = { config = config.copy(letterCase = LetterCase.LOWERCASE) },
                        label = { Text("а б в") }
                    )
                    FilterChip(
                        selected = config.letterCase == LetterCase.BOTH,
                        onClick = { config = config.copy(letterCase = LetterCase.BOTH) },
                        label = { Text("Аа Бб") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Количество строк: ${config.rowsCount}", modifier = Modifier.width(140.dp))
                    Slider(
                        value = config.rowsCount.toFloat(),
                        onValueChange = { config = config.copy(rowsCount = it.toInt()) },
                        valueRange = 1f..6f,
                        steps = 4,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Шрифт прописью", modifier = Modifier.weight(1f))
                    Switch(
                        checked = config.isCursive,
                        onCheckedChange = { config = config.copy(isCursive = it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Вспомогательные линии", modifier = Modifier.weight(1f))
                    Switch(
                        checked = config.showGuides,
                        onCheckedChange = { config = config.copy(showGuides = it) }
                    )
                }
            }

            HorizontalDivider()

            // Превью
            Text(
                "Предпросмотр листа A4:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ограничиваем пропорциями A4
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(PdfDrawer.PAGE_WIDTH / PdfDrawer.PAGE_HEIGHT)
                ) {
                    val scale = minOf(size.width / PdfDrawer.PAGE_WIDTH, size.height / PdfDrawer.PAGE_HEIGHT)
                    val offsetX = (size.width - PdfDrawer.PAGE_WIDTH * scale) / 2f
                    val offsetY = (size.height - PdfDrawer.PAGE_HEIGHT * scale) / 2f

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.translate(offsetX, offsetY)
                        canvas.nativeCanvas.scale(scale, scale)

                        PdfDrawer.drawWorksheet(
                            canvas = canvas.nativeCanvas,
                            letter = letter,
                            config = config,
                            context = context
                        )
                        canvas.nativeCanvas.restore()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
