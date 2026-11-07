package com.example.a4umka.ui.screens.reading

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

    var config by remember { mutableStateOf(SyllableConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    
    // Выбранная категория для отображения в UI
    var selectedCategory by remember { mutableStateOf(SyllableData.categories.first()) }

    // БИТМАП ПРЕДПРОСМОТРА
    val previewBitmap = remember(config) {
        val bmp = Bitmap.createBitmap(SyllablePdfDrawer.PAGE_WIDTH.toInt(), SyllablePdfDrawer.PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bmp)
        
        // Берем первый выбранный текст для превью, либо свой текст
        val textObj = if (config.source == SyllableSource.READY) {
            config.selectedTexts.firstOrNull() ?: SyllableText("Выберите текст", "", emptyList())
        } else {
            SyllableText(config.customTitle, config.customText, emptyList())
        }
        
        SyllablePdfDrawer.drawSinglePage(
            canvas = canvas,
            title = textObj.title,
            text = textObj.text,
            questions = textObj.questions,
            config = config,
            context = context
        )
        bmp
    }

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
                            scope.launch { snackbarHostState.showSnackbar("Сначала выберите текст") }
                            return@IconButton
                        }
                        val result = PdfExporter.exportSyllableText(context, config)
                        scope.launch {
                            if (result != null) snackbarHostState.showSnackbar("Сохранено: $result")
                            else snackbarHostState.showSnackbar("Ошибка: выберите текст")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Источник текста:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    // ГАЛОЧКА МУЛЬТИВЫБОРА
                    Text("Мультивыбор", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = config.isMultiSelectMode,
                        onCheckedChange = { isChecked ->
                            config = config.copy(
                                isMultiSelectMode = isChecked,
                                selectedTexts = if (!isChecked && config.selectedTexts.size > 1) {
                                    listOf(config.selectedTexts.first()) // оставляем только один если выключили
                                } else config.selectedTexts
                            )
                        }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.source == SyllableSource.READY, onClick = { config = config.copy(source = SyllableSource.READY) }, label = { Text("Готовый текст") })
                    FilterChip(selected = config.source == SyllableSource.CUSTOM, onClick = { config = config.copy(source = SyllableSource.CUSTOM) }, label = { Text("Свой текст") })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ИНТЕРФЕЙС ГОТОВЫХ ТЕКСТОВ
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
                            
                            ElevatedCard(
                                onClick = {
                                    if (config.isMultiSelectMode) {
                                        // Логика мультивыбора
                                        val newList = config.selectedTexts.toMutableList()
                                        if (isSelected) newList.remove(textObj) else newList.add(textObj)
                                        config = config.copy(selectedTexts = newList)
                                    } else {
                                        // Обычный выбор
                                        config = config.copy(selectedTexts = listOf(textObj))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CardDefaults.elevatedShape
                                )
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
                                    Text(
                                        textObj.text.take(40).replace("\n", " ") + if (textObj.text.length > 40) "..." else "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // СВОЙ ТЕКСТ
                    OutlinedTextField(
                        value = config.customTitle,
                        onValueChange = { config = config.copy(customTitle = it) },
                        label = { Text("Название рассказа") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = config.customText,
                        onValueChange = { config = config.copy(customText = it) },
                        label = { Text("Текст рассказа") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        maxLines = 8
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Настройки печати:", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Крупный шрифт", modifier = Modifier.weight(1f))
                    Switch(checked = config.isLargeFont, onCheckedChange = { config = config.copy(isLargeFont = it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Вопросы по тексту", modifier = Modifier.weight(1f))
                    Switch(checked = config.showQuestions, onCheckedChange = { config = config.copy(showQuestions = it) })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр (1-я страница):", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Предпросмотр",
                        modifier = Modifier.fillMaxWidth().aspectRatio(SyllablePdfDrawer.PAGE_WIDTH / SyllablePdfDrawer.PAGE_HEIGHT)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Чтение по слогам", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "• Этот раздел помогает детям 5–7 лет учиться читать слитно. Под каждым слогом рисуется дуга, а под одиночными буквами (например, предлогами «и», «в») — точка.\n\n" +
                        "• Вы можете выбрать готовый текст из нашей подборки или вставить любой свой (сказку, стихотворение, рассказ). Программа автоматически разобьёт слова на слоги.\n\n" +
                        "• Если включить «Мультивыбор», можно выбрать сразу несколько текстов и сохранить их в один PDF-файл (каждый текст будет на отдельной странице).\n\n" +
                        "• Для самых первых занятий и для детей, которым трудно удерживать строку, включайте «Крупный шрифт» — буквы становятся заметно больше, а строки просторнее.\n\n" +
                        "• Для рассказов 1 класса можно включить «Вопросы по тексту». Это проверяет не скорость чтения, а осмысленность: понял ли ребёнок, о чём прочитал.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
