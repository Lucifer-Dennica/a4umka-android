package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.ComprehensionConfig
import com.example.a4umka.data.ReadingComprehensionData
import com.example.a4umka.util.ComprehensionPdfDrawer
import com.example.a4umka.util.PdfExporter
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

    LaunchedEffect(Unit) {
        if (config.selectedStory == null) {
            config = config.copy(selectedStory = ReadingComprehensionData.getRandomStory(config.grade))
        }
    }

    val previewPages = remember(config) {
        ComprehensionPdfDrawer.generatePreviewPages(config, context)
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
                        val result = PdfExporter.exportComprehension(context, config)
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
                Text("Класс:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 4).forEach { grade ->
                        FilterChip(
                            selected = config.grade == grade,
                            onClick = {
                                config = config.copy(
                                    grade = grade,
                                    selectedStory = ReadingComprehensionData.getRandomStory(grade)
                                )
                                dropdownExpanded = false
                            },
                            label = { Text(grade.toString()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Рассказ:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        config = config.copy(selectedStory = ReadingComprehensionData.getRandomStory(config.grade))
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Случайный")
                    }
                }
                
                val stories = ReadingComprehensionData.getStoriesByGrade(config.grade)
                
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = config.selectedStory?.title ?: "Выберите рассказ",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { 
                            if (config.selectedStory != null) {
                                Text("${config.selectedStory!!.text.split(" ").size} слов • ${config.selectedStory!!.questions.size} вопроса", fontSize = 12.sp)
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
                                        Text("${story.text.split(" ").size} слов", fontSize = 12.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    config = config.copy(selectedStory = story)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Настройки печати:", style = MaterialTheme.typography.labelLarge)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Количество слов справа", modifier = Modifier.weight(1f))
                    Switch(checked = config.showWordCount, onCheckedChange = { config = config.copy(showWordCount = it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Подсказки для взрослого", modifier = Modifier.weight(1f))
                    Switch(checked = config.showAdultHints, onCheckedChange = { config = config.copy(showAdultHints = it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Отдельный лист ответов", modifier = Modifier.weight(1f))
                    Switch(checked = config.showAnswerSheet, onCheckedChange = { config = config.copy(showAnswerSheet = it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Чёрно-белая печать", modifier = Modifier.weight(1f))
                    Switch(checked = config.isBlackAndWhite, onCheckedChange = { config = config.copy(isBlackAndWhite = it) })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(ComprehensionPdfDrawer.PAGE_WIDTH / ComprehensionPdfDrawer.PAGE_HEIGHT)
                    ) { page ->
                        Image(
                            bitmap = previewPages[page].asImageBitmap(),
                            contentDescription = "Предпросмотр",
                            modifier = Modifier.fillMaxSize()
                        )
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
                        "Ребёнок прочитал рассказ. А может ли он объяснить, почему герой так поступил и чем закончилась история? Этот раздел помогает перейти от чтения к обсуждению: на листе A4 — рассказ, вопросы и место для ответов.\n\n" +
                        "В подборке 100 рассказов — по 25 для каждого класса. Выберите конкретную историю или случайный вариант.\n\n" +
                        "• Количество слов по строкам — дополнительная разметка для проверки техники чтения. Число справа показывает сумму слов от начала рассказа. Числа можно отключить.\n\n" +
                        "• Отдельный лист ответов добавит вторую страницу с подсказками для взрослого и полями для записи ответов ребёнка.\n\n" +
                        "• Цветная или чёрно-белая печать — выберите подходящее оформление перед созданием листа.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Класс — ориентир сложности", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "1 класс — 51–58 слов. Первые короткие истории. Вопросы предлагают вспомнить героев и простую причину события.\n\n" +
                        "2 класс — 82–97 слов. События и поступки. Ребёнок объясняет поступки героев и находит подтверждение в тексте.\n\n" +
                        "3 класс — 114–135 слов. Наблюдения и выводы. Нужно связать детали рассказа и заметить, как изменилось отношение героя.\n\n" +
                        "4 класс — 154–175 слов. Причины и главная мысль. Важны точность деталей, причинные связи и смысл поступков.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Как обсуждать прочитанное", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Начните с устного ответа. Дайте ребёнку время обдумать содержание.\n" +
                        "• Попросите показать предложение, на которое он опирается.\n" +
                        "• Перечитайте трудный фрагмент вместе, если ответить пока не получается.\n" +
                        "• Запишите короткий ответ, если хотите потренировать письменную речь.\n\n" +
                        "Ответы на листе взрослого — смысловые ориентиры. Важны понимание истории и соответствие тексту, а не дословное повторение.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
