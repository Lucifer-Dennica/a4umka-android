package com.example.sovenok.ui.screens.reading

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import com.example.sovenok.data.ComprehensionConfig
import com.example.sovenok.data.ReadingComprehensionData
import com.example.sovenok.ui.components.ZoomableImage
import com.example.sovenok.util.ComprehensionPdfDrawer
import com.example.sovenok.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensionGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(ComprehensionConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    var editMode by remember { mutableStateOf(false) }
    var editedStory by remember { mutableStateOf<com.example.sovenok.data.ComprehensionStory?>(null) }

    LaunchedEffect(Unit) {
        if (config.selectedStory == null) {
            config = config.copy(selectedStory = ReadingComprehensionData.getRandomStory(config.grade))
        }
    }
    
    LaunchedEffect(config.selectedStory) {
        editedStory = config.selectedStory
    }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = ComprehensionPdfDrawer.generatePreviewPages(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чтение с вопросами") },
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
                        val finalConfig = if (editMode && editedStory != null) {
                            config.copy(selectedStory = editedStory)
                        } else {
                            config
                        }
                        val result = PdfExporter.exportComprehension(context, finalConfig)
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
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Класс", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 4).forEach { grade ->
                                FilterChip(
                                    selected = config.grade == grade,
                                    onClick = {
                                        config = config.copy(grade = grade, selectedStory = ReadingComprehensionData.getRandomStory(grade))
                                        dropdownExpanded = false
                                        editMode = false
                                    },
                                    label = { Text(grade.toString()) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Рассказ", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                config = config.copy(selectedStory = ReadingComprehensionData.getRandomStory(config.grade))
                                editMode = false
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Случайный")
                            }
                        }

                        val stories = ReadingComprehensionData.getStoriesByGrade(config.grade)

                        ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }) {
                            OutlinedTextField(
                                value = config.selectedStory?.title ?: "Выберите рассказ",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                label = {
                                    if (config.selectedStory != null) {
                                        Text("${config.selectedStory!!.totalWords} слов • ${config.selectedStory!!.questions.size} вопроса", fontSize = 12.sp)
                                    }
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 350.dp)
                            ) {
                                stories.forEach { story ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(story.title, fontWeight = FontWeight.Bold)
                                                Text("${story.totalWords} слов", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            config = config.copy(selectedStory = story)
                                            dropdownExpanded = false
                                            editMode = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !editMode,
                                onClick = { editMode = false },
                                label = { Text("Просмотр") }
                            )
                            FilterChip(
                                selected = editMode,
                                onClick = { 
                                    if (config.selectedStory != null) {
                                        editMode = true
                                        editedStory = config.selectedStory
                                    }
                                },
                                label = { Text("Редактировать") }
                            )
                        }
                        
                        if (editMode && editedStory != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = editedStory!!.title,
                                onValueChange = { editedStory = editedStory!!.copy(title = it); config = config.copy(selectedStory = editedStory) },
                                label = { Text("Название") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editedStory!!.text,
                                onValueChange = { editedStory = editedStory!!.copy(text = it); config = config.copy(selectedStory = editedStory) },
                                label = { Text("Текст рассказа") },
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Вопросы и ответы:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            editedStory!!.questions.forEachIndexed { index, q ->
                                OutlinedTextField(
                                    value = q,
                                    onValueChange = { 
                                        val newQ = editedStory!!.questions.toMutableList().apply { this[index] = it }
                                        editedStory = editedStory!!.copy(questions = newQ)
                                        config = config.copy(selectedStory = editedStory)
                                    },
                                    label = { Text("Вопрос ${index + 1}") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = editedStory!!.answers[index],
                                    onValueChange = { 
                                        val newA = editedStory!!.answers.toMutableList().apply { this[index] = it }
                                        editedStory = editedStory!!.copy(answers = newA)
                                        config = config.copy(selectedStory = editedStory)
                                    },
                                    label = { Text("Ответ") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Настройки печати", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Счётчик слов справа", modifier = Modifier.weight(1f))
                            Switch(checked = config.showWordCount, onCheckedChange = { config = config.copy(showWordCount = it) })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Отдельный лист ответов", modifier = Modifier.weight(1f))
                            Switch(checked = config.showAnswerSheet, onCheckedChange = { config = config.copy(showAnswerSheet = it) })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Персиковый фон листа", modifier = Modifier.weight(1f))
                            Switch(checked = config.isPeachBackground, onCheckedChange = { config = config.copy(isPeachBackground = it) })
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
                    }
                }
            }

            item {
                Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                if (previewPages.size > 1) {
                    Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
                Text("Двумя пальцами можно приблизить лист", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(ComprehensionPdfDrawer.PAGE_WIDTH / ComprehensionPdfDrawer.PAGE_HEIGHT)
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
                    Text("Тексты для чтения с вопросами", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Этот раздел помогает перейти от чтения к обсуждению. На листе A4 — рассказ, вопросы и место для ответов.\n\n" +
                        "В подборке 100 авторских рассказов — по 25 для каждого класса. Выберите конкретную историю или случайный вариант.\n\n" +
                        "• Счётчик слов справа помогает проверять технику чтения.\n" +
                        "• Вкладка «Редактировать» позволяет изменить текст, вопросы и ответы под ваш класс.\n" +
                        "• «Отдельный лист ответов» добавит вторую страницу с подсказками для взрослого.\n" +
                        "• По умолчанию фон белый для экономии краски, но его можно сделать персиковым.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
