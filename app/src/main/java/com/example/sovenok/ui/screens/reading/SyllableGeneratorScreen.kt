package com.example.sovenok.ui.screens.reading

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    
    // Состояние для режима редактирования
    var editMode by remember { mutableStateOf(false) }
    var editedTexts by remember { mutableStateOf<List<SyllableText>>(emptyList()) }
    
    LaunchedEffect(config) {
        delay(300)
        previewPages = SyllablePdfDrawer.generatePreviewPages(config, context)
    }
    
    // При изменении selectedTexts обновляем editedTexts
    LaunchedEffect(config.selectedTexts) {
        editedTexts = config.selectedTexts.toList()
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
                        val finalConfig = if (editMode && editedTexts.isNotEmpty()) {
                            config.copy(selectedTexts = editedTexts)
                        } else {
                            config
                        }
                        
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
            // БЛОК: Источник текста + Редактирование
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
                                selected = config.source == SyllableSource.READY && !editMode,
                                onClick = { 
                                    config = config.copy(source = SyllableSource.READY)
                                    editMode = false
                                },
                                label = { Text("Готовый текст") }
                            )
                            FilterChip(
                                selected = config.source == SyllableSource.CUSTOM,
                                onClick = { 
                                    config = config.copy(source = SyllableSource.CUSTOM)
                                    editMode = false
                                },
                                label = { Text("Свой текст") }
                            )
                            FilterChip(
                                selected = editMode,
                                onClick = { 
                                    if (config.selectedTexts.isNotEmpty()) {
                                        editMode = true
                                        editedTexts = config.selectedTexts.toList()
                                    } else {
                                        scope.launch { 
                                            snackbarHostState.showSnackbar("Сначала выберите тексты во вкладке «Готовый текст»") 
                                        }
                                    }
                                },
                                label = { 
                                    Text(
                                        "Редактировать (${config.selectedTexts.size})",
                                         maxLines = 1,
                                         softWrap = false,
                                         fontSize = 12.sp
                                    )   
                                }    
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // РЕЖИМ РЕДАКТИРОВАНИЯ
                        if (editMode && config.source == SyllableSource.READY) {
                            if (editedTexts.isEmpty()) {
                                Text("Нет выбранных текстов. Вернитесь во вкладку «Готовый текст» и выберите тексты.", 
                                    color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text("Редактирование выбранных текстов", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                Text("Изменения сразу отображаются в предпросмотре", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                editedTexts.forEachIndexed { index, editedText ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(editedText.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            OutlinedTextField(
                                                value = editedText.title,
                                                onValueChange = { newTitle ->
                                                    val newText = editedText.copy(title = newTitle)
                                                    editedTexts = editedTexts.toMutableList().apply { set(index, newText) }
                                                    config = config.copy(selectedTexts = editedTexts)
                                                },
                                                label = { Text("Название") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            OutlinedTextField(
                                                value = editedText.text,
                                                onValueChange = { newText ->
                                                    val updatedText = editedText.copy(text = newText)
                                                    editedTexts = editedTexts.toMutableList().apply { set(index, updatedText) }
                                                    config = config.copy(selectedTexts = editedTexts)
                                                },
                                                label = { Text("Текст") },
                                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                                maxLines = 6
                                            )
                                            
                                            if (editedText.questions.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Вопросы:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                editedText.questions.forEachIndexed { qIndex, question ->
                                                    OutlinedTextField(
                                                        value = question,
                                                        onValueChange = { newQuestion ->
                                                            val newQuestions = editedText.questions.toMutableList()
                                                            newQuestions[qIndex] = newQuestion
                                                            val updatedText = editedText.copy(questions = newQuestions)
                                                            editedTexts = editedTexts.toMutableList().apply { set(index, updatedText) }
                                                            config = config.copy(selectedTexts = editedTexts)
                                                        },
                                                        label = { Text("Вопрос ${qIndex + 1}") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        singleLine = true
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            val newQuestions = editedText.questions.toMutableList()
                                                            newQuestions.add("")
                                                            val updatedText = editedText.copy(questions = newQuestions)
                                                            editedTexts = editedTexts.toMutableList().apply { set(index, updatedText) }
                                                            config = config.copy(selectedTexts = editedTexts)
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("+ Вопрос")
                                                    }
                                                    if (editedText.questions.size > 1) {
                                                        OutlinedButton(
                                                            onClick = {
                                                                val newQuestions = editedText.questions.toMutableList()
                                                                newQuestions.removeAt(newQuestions.size - 1)
                                                                val updatedText = editedText.copy(questions = newQuestions)
                                                                editedTexts = editedTexts.toMutableList().apply { set(index, updatedText) }
                                                                config = config.copy(selectedTexts = editedTexts)
                                                            },
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("− Удалить")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                        // РЕЖИМ ГОТОВЫХ ТЕКСТОВ (двухрядный список)
                        else if (config.source == SyllableSource.READY && !editMode) {
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
                            
                            // ДВУХРЯДНЫЙ СПИСОК
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
                        }
                        // РЕЖИМ СВОЕГО ТЕКСТА
                        else if (config.source == SyllableSource.CUSTOM) {
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
                        "Этот раздел помогает детям 5–7 лет учиться читать слитно. Под каждым слогом рисуется дуга, а под одиночными гласными — точка.\n\n" +
                        "В генераторе более 100 готовых текстов от столбиков слов до рассказов для 1 класса. Или вставьте свой текст — разметка проставится автоматически.\n\n" +
                        "Как пользоваться:\n" +
                        "• Выберите «Готовый текст» и накликните нужные рассказы из списка\n" +
                        "• Или выберите «Свой текст» и вставьте любой рассказ\n" +
                        "• Включите «Выбрать несколько вариантов», чтобы напечатать сразу несколько текстов\n" +
                        "• Для рассказов 1 класса можно добавить вопросы по тексту\n" +
                        "• Нажмите «Редактировать», чтобы изменить выбранные тексты перед печатью\n\n" +
                        "Для самых первых занятий увеличьте размер шрифта ползунком — буквы станут крупнее, строки просторнее.\n\n" +
                        "Сохраните в PDF и распечатайте на обычном принтере A4.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
