package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.CrosswordMode
import com.example.a4umka.data.CustomCrosswordConfig
import com.example.a4umka.data.CustomCrosswordData
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.CustomCrosswordPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCrosswordGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: (CustomCrosswordConfig) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(CustomCrosswordConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    
    // Вычисляем сетку, чтобы показать ошибки
    val pairs = remember(config.wordsInput) { CustomCrosswordData.parseInput(config.wordsInput) }
    val grid = remember(config.seed, pairs) { CustomCrosswordData.generateGrid(pairs, config.seed) }
    val failedWords = grid.failedWords

    LaunchedEffect(config) {
        delay(300)
        previewPages = CustomCrosswordPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кроссворд из своих слов") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Справка")
                    }
                    IconButton(onClick = {
                        val result = PdfExporter.exportCustomCrossword(context, config)
                        scope.launch {
                            if (result != null) snackbarHostState.showSnackbar("Сохранено: $result")
                            else snackbarHostState.showSnackbar("Ошибка сохранения")
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить в PDF")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            val newTitle = if (config.title == "Мой сканворд") "Мой кроссворд" else config.title
                            config = config.copy(mode = CrosswordMode.STANDARD, title = newTitle) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (config.mode == CrosswordMode.STANDARD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (config.mode == CrosswordMode.STANDARD) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) { Text("Кроссворд") }
                    
                    Button(
                        onClick = { 
                            val newTitle = if (config.title == "Мой кроссворд") "Мой сканворд" else config.title
                            config = config.copy(mode = CrosswordMode.SCANWORD, title = newTitle) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (config.mode == CrosswordMode.SCANWORD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (config.mode == CrosswordMode.SCANWORD) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) { Text("Сканворд") }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Заголовок листа:", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = config.title,
                    onValueChange = { config = config.copy(title = it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Слова и вопросы (СЛОВО — Вопрос, по одной паре на строку):", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = config.wordsInput,
                    onValueChange = { config = config.copy(wordsInput = it) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("ЛИСА — Рыжая плутовка\nКОСМОС — Где летают ракеты\nБАБУШКА — Мама моей мамы") }
                )

                if (failedWords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Не удалось вставить слова (нет общих букв): ${failedWords.joinToString(", ")}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = themeExpanded, onExpandedChange = { themeExpanded = it }) {
                    OutlinedTextField(
                        value = "Взять готовую тему...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                        CustomCrosswordData.themes.forEach { (name, text) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    config = config.copy(wordsInput = text)
                                    themeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showAnswers, onCheckedChange = { config = config.copy(showAnswers = it) })
                    Text("Добавить лист с ответами")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Если сетка кажется неудобной, нажмите «Перемешать сетку».", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { config = config.copy(seed = System.currentTimeMillis()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Перемешать сетку")
                    }
                    Button(
                        onClick = { onPlayInteractive(config) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        enabled = pairs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Решать на телефоне")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Стр. ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Text("Двумя пальцами можно приблизить лист", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(CustomCrosswordPdfDrawer.PAGE_WIDTH / CustomCrosswordPdfDrawer.PAGE_HEIGHT)
                    ) { page ->
                        if (page < previewPages.size) {
                            ZoomableImage(
                                bitmap = previewPages[page],
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    Text("Введите слова и вопросы, чтобы увидеть предпросмотр", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Кроссворд из своих слов", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Этот раздел позволяет вам создать уникальный кроссворд, вписав свои слова и вопросы к ним.\n\n" +
                        "Режимы:\n" +
                        "• Кроссворд — классический вид, вопросы списком под сеткой.\n" +
                        "• Сканворд — вопросы и стрелки направления находятся прямо внутри клеток сетки.\n\n" +
                        "Важно: Слова могут не влезть в сетку, если у них нет общих букв с другими словами. Добавьте более длинные слова с частыми буквами (О, А, Е, И).",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
