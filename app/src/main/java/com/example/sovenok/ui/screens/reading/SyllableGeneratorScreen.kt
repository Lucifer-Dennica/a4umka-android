package com.example.sovenok.ui.screens.reading

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sovenok.data.*
import com.example.sovenok.ui.components.ZoomableImage
import com.example.sovenok.util.PdfExporter
import com.example.sovenok.util.SyllablePdfDrawer
import kotlinx.coroutines.delay
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
    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    
    // Для редактирования вопросов
    var editableQuestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var questionsChanged by remember { mutableStateOf(false) }
    
    LaunchedEffect(config) {
        delay(300)
        previewPages = SyllablePdfDrawer.generatePreviewPages(config, context)
    }
    
    // Обновляем редактируемые вопросы при смене текста
    LaunchedEffect(config.selectedTexts) {
        if (config.selectedTexts.size == 1) {
            editableQuestions = config.selectedTexts.first().questions.toMutableList()
            questionsChanged = false
        }
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
                        // Если вопросы были изменены, используем отредактированную версию
                        val finalConfig = if (questionsChanged && config.selectedTexts.size == 1) {
                            val updatedText = config.selectedTexts.first().copy(questions = editableQuestions)
                            config.copy(selectedTexts = listOf(updatedText))
                        } else config
                        
                        val result = PdfExporter.exportSyllableText(context, finalConfig)
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // БЛОК: Источник текста
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Источник текста", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = config.source == SyllableSource.READY,
                                onClick = { config = config.copy(source = SyllableSource.READY) },
                                label = { Text("Готовый текст") }
                            )
                            FilterChip(
                                selected = config.source == SyllableSource.CUSTOM,
                                onClick = { config = config.copy(source = SyllableSource.CUSTOM) },
                                label = { Text("Свой текст") }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
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
                            
                            // СЕТКА В 2 КОЛОНКИ
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.heightIn(max = 400.dp)
                            ) {
                                items(selectedCategory.texts) { textObj ->
                                    val isSelected = config.selectedTexts.contains(textObj)
                                    ElevatedCard(
                                        onClick = {
                                            if (config.isMultiSelectMode) {
                                                val newList = config.selectedTexts.toMutableList()
                                                if (isSelected) newList.remove(textObj) else newList.add(textObj)
                                                config = config.copy(selectedTexts = newList)
                                            } else {
                                                config = config.copy(selectedTexts = listOf(textObj))
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (isSelected) Modifier.border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    CardDefaults.elevatedShape
                                                ) else Modifier
                                            )
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    textObj.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (textObj.questions.isNotEmpty()) {
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            "? вопросы",
                                                            fontSize = 9.sp,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                textObj.text.take(50).replace("\n", " ") + "...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
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
                    }
                }
            }
            
            // БЛОК: Редактирование вопросов (только для одного выбранного текста)
            if (config.source == SyllableSource.READY && config.selectedTexts.size == 1 && config.selectedTexts.first().questions.isNotEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Вопросы по тексту", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text("Можно отредактировать под свой класс", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            editableQuestions.forEachIndexed { index, question ->
                                OutlinedTextField(
                                    value = question,
                                    onValueChange = { newQuestion ->
                                        editableQuestions = editableQuestions.toMutableList().apply {
                                            set(index, newQuestion)
                                        }
                                        questionsChanged = true
                                    },
                                    label = { Text("Вопрос ${index + 1}") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        editableQuestions = editableQuestions.toMutableList().apply {
                                            add("")
                                        }
                                        questionsChanged = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+ Добавить вопрос")
                                }
                                if (editableQuestions.size > 1) {
                                    OutlinedButton(
                                        onClick = {
                                            editableQuestions = editableQuestions.toMutableList().apply {
                                                removeAt(editableQuestions.size - 1)
                                            }
                                            questionsChanged = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("− Удалить последний")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // БЛОК: Настройки печати
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Настройки печати", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (config.source == SyllableSource.READY) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Выбрать несколько вариантов")
                                    Text("Печать нескольких текстов подряд", fontSize = 12.sp, color = Color.Gray)
                                }
                                Switch(
                                    checked = config.isMultiSelectMode,
                                    onCheckedChange = { isChecked ->
                                        config = config.copy(
                                            isMultiSelectMode = isChecked,
                                            selectedTexts = if (!isChecked && config.selectedTexts.size > 1)
                                                listOf(config.selectedTexts.first())
                                            else config.selectedTexts
                                        )
                                    }
                                )
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
                            Switch(
                                checked = config.showQuestions,
                                onCheckedChange = { config = config.copy(showQuestions = it) }
                            )
                        }
                    }
                }
            }
            
            // ПРЕДПРОСМОТР
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Text("Двумя пальцами можно приблизить лист", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(SyllablePdfDrawer.PAGE_WIDTH / SyllablePdfDrawer.PAGE_HEIGHT)
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
                    Text("Чтение по слогам", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Этот раздел помогает детям 5–7 лет учиться читать слитно. Под каждым слогом рисуется дуга, а под одиночными буквами — точка.\n\n" +
                        "В генераторе 100 готовых текстов от столбиков слов до рассказов для 1 класса — или вставьте свой. Разметка проставляется автоматически.\n\n" +
                        "Если включить «Выбрать несколько вариантов», можно накликать сразу несколько текстов. При сохранении в PDF они выйдут по очереди, каждый на отдельном листе.\n\n" +
                        "Для рассказов 1 класса можно отредактировать вопросы под свой класс — просто выберите текст и измените формулировки.\n\n" +
                        "Для самых первых занятий и для детей, которым трудно удерживать строку, увеличьте размер шрифта ползунком.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
