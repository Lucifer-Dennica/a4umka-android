package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.*
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.SyllablePdfDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SyllableGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { 
        mutableStateOf(
            SyllableConfig(selectedTexts = listOf(SyllableData.categories.first().texts.first()))
        ) 
    }
    var showInfoSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(SyllableData.categories.first()) }

    val previewPages = remember(config) {
        SyllablePdfDrawer.generatePreviewPages(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чтение по слогам") },
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
                        if (config.source == SyllableSource.READY && config.selectedTexts.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Сначала выберите хотя бы один текст") }
                            return@IconButton
                        }
                        val result = PdfExporter.exportSyllableText(context, config)
                        scope.launch {
                            if (result != null) snackbarHostState.showSnackbar("Сохранено: $result")
                            else snackbarHostState.showSnackbar("Ошибка при сохранении")
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Источник текста:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = config.source == SyllableSource.READY, onClick = { config = config.copy(source = SyllableSource.READY) }, label = { Text("Готовый текст") })
                        FilterChip(selected = config.source == SyllableSource.CUSTOM, onClick = { config = config.copy(source = SyllableSource.CUSTOM) }, label = { Text("Свой текст") })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (config.source == SyllableSource.READY) {
                        Text("← Листайте вкладки →", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(SyllableData.categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.name) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            selectedCategory.texts.forEach { textObj ->
                                val isSelected = config.selectedTexts.contains(textObj)
                                
                                // ИСПРАВЛЕНО: ElevatedCard заменен на Card (поддерживает border)
                                Card(
                                    onClick = {
                                        if (config.isMultiSelectMode) {
                                            val newList = config.selectedTexts.toMutableList()
                                            if (isSelected) newList.remove(textObj) else newList.add(textObj)
                                            config = config.copy(selectedTexts = newList)
                                        } else {
                                            config = config.copy(selectedTexts = listOf(textObj))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(textObj.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                            if (textObj.questions.isNotEmpty()) {
                                                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(4.dp)) {
                                                    Text("? вопросы", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                        Text(textObj.text.take(40).replace("\n", " ") + "...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(value = config.customTitle, onValueChange = { config = config.copy(customTitle = it) }, label = { Text("Название рассказа") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = config.customText, onValueChange = { config = config.copy(customText = it) }, label = { Text("Текст рассказа") }, modifier = Modifier.fillMaxWidth().height(150.dp), maxLines = 8)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Настройки печати:", style = MaterialTheme.typography.labelLarge)
                    
                    if (config.source == SyllableSource.READY) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Выбрать несколько вариантов")
                                Text("Печать нескольких текстов подряд", fontSize = 12.sp, color = Color.Gray)
                            }
                            Switch(checked = config.isMultiSelectMode, onCheckedChange = { isChecked ->
                                config = config.copy(isMultiSelectMode = isChecked, selectedTexts = if (!isChecked && config.selectedTexts.size > 1) listOf(config.selectedTexts.first()) else config.selectedTexts)
                            })
                        }
                    }

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Размер шрифта: ${config.fontSize.toInt()}", fontSize = 14.sp)
                        Slider(
                            value = config.fontSize,
                            onValueChange = { config = config.copy(fontSize = it) },
                            valueRange = 16f..40f,
                            steps = 6
                        )
                    }

                    if (config.isMultiSelectMode && config.selectedTexts.size > 1) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text("Отступ между текстами: ${config.textSpacing.toInt()}", fontSize = 14.sp)
                            Slider(
                                value = config.textSpacing,
                                onValueChange = { config = config.copy(textSpacing = it) },
                                valueRange = 20f..150f
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Вопросы по тексту", modifier = Modifier.weight(1f))
                        Switch(checked = config.showQuestions, onCheckedChange = { config = config.copy(showQuestions = it) })
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        if (previewPages.size > 1) {
                            Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (previewPages.isNotEmpty()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().aspectRatio(SyllablePdfDrawer.PAGE_WIDTH / SyllablePdfDrawer.PAGE_HEIGHT)
                        ) { page ->
                            Image(
                                bitmap = previewPages[page].asImageBitmap(),
                                contentDescription = "Предпросмотр страницы ${page + 1}",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // ИСПРАВЛЕНО: Текст обернут в Box, чтобы работал align
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("← Свайпните для просмотра других страниц →", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        Text("Выберите текст для предпросмотра", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Чтение по слогам", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Этот раздел помогает детям 5–7 лет учиться читать слитно. Под каждым слогом рисуется дуга, а под одиночными буквами — точка.\n\n" +
                        "• Включите «Выбрать несколько вариантов», чтобы распечатать сразу несколько текстов. Программа автоматически разобьёт их на страницы, если они не поместятся на один лист.\n\n" +
                        "• Слайдеры в настройках помогут изменить размер шрифта и отступ между рассказами для удобства чтения.",
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
