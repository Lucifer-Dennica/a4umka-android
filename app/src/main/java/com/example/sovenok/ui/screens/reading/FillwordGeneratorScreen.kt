package com.example.sovenok.ui.screens.reading

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sovenok.data.*
import com.example.sovenok.ui.components.ZoomableImage
import com.example.sovenok.util.FillwordPdfDrawer
import com.example.sovenok.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillwordGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: (FillwordConfig) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(FillwordConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = FillwordPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })
    val words = config.getParsedWords()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Филворд из своих слов") },
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
                        val result = PdfExporter.exportFillword(context, config)
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // БЛОК: Ваши слова
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Ваши слова", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = config.wordsInput,
                            onValueChange = { config = config.copy(wordsInput = it) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            placeholder = { Text("МЕДВЕДЬ, ЛИСА, ВОЛК...") }
                        )
                        Text("Слов: ${words.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // БЛОК: Размер сетки
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Размер сетки", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = config.sizeMode == FillwordSizeMode.AUTO, onClick = { config = config.copy(sizeMode = FillwordSizeMode.AUTO) })
                            Text("Автоматически")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = config.sizeMode == FillwordSizeMode.MANUAL, onClick = { config = config.copy(sizeMode = FillwordSizeMode.MANUAL) })
                            Text("Вручную:")
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = config.manualSize.toString(),
                                onValueChange = {
                                    val num = it.toIntOrNull() ?: 11
                                    config = config.copy(manualSize = num.coerceIn(7, 20))
                                },
                                modifier = Modifier.width(80.dp),
                                singleLine = true
                            )
                            Text(" x ${config.manualSize}", modifier = Modifier.padding(start = 4.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.allowBackwards, onCheckedChange = { config = config.copy(allowBackwards = it) })
                            Text("Разрешить ← и ↑ (сложнее)", fontSize = 14.sp)
                        }
                    }
                }
            }

            // БЛОК: Настройки листа
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Настройки листа", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = config.title,
                            onValueChange = { config = config.copy(title = it) },
                            label = { Text("Заголовок листа") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.showNameField, onCheckedChange = { config = config.copy(showNameField = it) })
                            Text("Строка «Имя ____»")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.showWordList, onCheckedChange = { config = config.copy(showWordList = it) })
                            Text("Печатать список слов")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.addAnswers, onCheckedChange = { config = config.copy(addAnswers = it) })
                            Text("Добавить лист ответов")
                        }
                    }
                }
            }

            // БЛОК: Сколько вариантов
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Сколько вариантов", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = config.variants == FillwordVariants.ONE, onClick = { config = config.copy(variants = FillwordVariants.ONE) }, label = { Text("1") })
                            FilterChip(selected = config.variants == FillwordVariants.TWO, onClick = { config = config.copy(variants = FillwordVariants.TWO) }, label = { Text("2") })
                            FilterChip(selected = config.variants == FillwordVariants.FOUR, onClick = { config = config.copy(variants = FillwordVariants.FOUR) }, label = { Text("4") })
                        }
                    }
                }
            }

            // КНОПКА ИНТЕРАКТИВНОГО РЕЖИМА
            item {
                Button(
                    onClick = { onPlayInteractive(config) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Решать на экране")
                }
            }

            // ПРЕДПРОСМОТР
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр (4K):", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(FillwordPdfDrawer.PAGE_WIDTH / FillwordPdfDrawer.PAGE_HEIGHT)
                    ) { page ->
                        if (page < previewPages.size) {
                            ZoomableImage(
                                bitmap = previewPages[page],
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Филворд из своих слов", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "На этой странице можно создать филворд из своих слов и сразу распечатать его на A4. Вы вписываете любые слова, а генератор сам прячет их в сетку букв.\n\n" +
                        "Что можно настроить:\n" +
                        "• Размер сетки;\n" +
                        "• направления (← и ↑);\n" +
                        "• заголовок листа;\n" +
                        "• список слов на листе;\n" +
                        "• лист с ответами;\n" +
                        "• 1, 2 или 4 варианта.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
