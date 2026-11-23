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
import com.example.sovenok.data.ComprehensionStory
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
    var editedStory by remember { mutableStateOf<ComprehensionStory?>(null) }

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
                                    val s = config.selectedStory
                                    if (s != null) {
                                        val w: Int = s.totalWords
                                        val q: Int = s.questions.size
                                        Text(text = "$w слов • $q вопроса", fontSize = 12.sp)
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
                            val story: ComprehensionStory = editedStory!!
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = story.title,
                                onValueChange = { newTitle -> 
                                    editedStory = story.copy(title = newTitle)
                                    config = config.copy(selectedStory = editedStory)
                                },
                                label = { Text("Название") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = story.text,
                                onValueChange = { newText -> 
                                    editedStory = story.copy(text = newText)
                                    config = config.copy(selectedStory = editedStory)
                                },
                                label = { Text("Текст рассказа") },
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Вопросы и ответы:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            
                            story.questions.forEachIndexed { index, q ->
                                OutlinedTextField(
                                    value = q,
                                    onValueChange = { newQ -> 
                                        val newQuestions: MutableList<String> = story.questions.toMutableList()
                                        newQuestions[index] = newQ
                                        editedStory = story.copy(questions = newQuestions)
                                        config = config.copy(selectedStory = editedStory)
                                    },
                                    label = { Text("Вопрос ${index + 1}") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                
                                val ansList: List<String> = story.answers
                                val currentAnswer: String = if (index < ansList.size) ansList[index] else ""
                                
                                OutlinedTextField(
                                    value = currentAnswer,
                                    onValueChange = { newAnswer: String -> 
                                        val newA: MutableList<String> = ansList.toMutableList()
                                        while (newA.size <= index) newA.add("")
                                        newA[index] = newAnswer
                                        editedStory = story.copy(answers = newA)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Тексты для чтения с вопросами — распечатать для 1–4 класса",
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Ребёнок прочитал рассказ. А может ли он объяснить, почему герой так поступил и чем закончилась история? Этот раздел помогает перейти от чтения к обсуждению: на листе A4 — рассказ, вопросы и место для ответов.",
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Text("Что будет на распечатке", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("• Лист ребёнка — рассказ и четыре или пять вопросов. Отвечать можно устно или записывать под вопросами.\n" +
                        "• Лист взрослого — ответы и подсказки для обсуждения. Добавляется по желанию и печатается отдельной страницей.\n" +
                        "• Количество слов по строкам — разметка для проверки техники чтения. Числа можно отключить.\n" +
                        "• Цветное или нейтральное оформление — выберите фон листа перед печатью.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Тексты по классам", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Истории посвящены знакомым детям ситуациям: прогулкам, семье, школьным занятиям, открытиям и совместным делам. В подборке 100 авторских рассказов — по 25 на каждый класс.\n\n" +
                        "1 класс — первые короткие истории. 51–58 слов и четыре вопроса. Короткие предложения и крупный шрифт помогают удерживать строку; вопросы просят вспомнить героев, предметы и простую причину события.\n\n" +
                        "2 класс — события и поступки. 82–97 слов и четыре вопроса. В рассказах несколько последовательных событий, ребёнок объясняет поступки и находит подтверждение в тексте.\n\n" +
                        "3 класс — наблюдения и выводы. 114–135 слов и четыре вопроса. Нужно связать детали, объяснить, что помогло герою, и заметить, как изменилось его отношение к ситуации.\n\n" +
                        "4 класс — причины и главная мысль. 154–175 слов и пять вопросов. Важны точность деталей, причинные связи и смысл поступков.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Класс — это ориентир сложности, а не привязка к конкретному учебнику, четверти или нормативу. Выбирайте по навыку ребёнка: при необходимости возьмите предыдущий уровень.",
                        style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text("Как обсуждать прочитанное", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("• Начните с устного ответа и дайте ребёнку время обдумать содержание.\n" +
                        "• Попросите показать предложение, на которое он опирается.\n" +
                        "• Перечитайте трудный фрагмент вместе, если ответить пока не получается.\n" +
                        "• Запишите короткий ответ, если хотите потренировать письменную речь.\n" +
                        "• Ответы на листе взрослого — смысловые ориентиры: повторять их слово в слово не обязательно, важны понимание истории и соответствие тексту.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Как пользоваться подсчётом слов", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Число справа показывает сумму слов от начала рассказа до конца строки. Заголовок не учитывается. Слово с дефисом считается одним словом, отдельное тире — не словом.\n\n" +
                        "• Для проверки за минуту отметьте последнее полностью прочитанное слово и отдельно запишите ошибки.\n" +
                        "• Для неполной строки возьмите число у предыдущей строки и прибавьте прочитанные слова текущей.\n" +
                        "• Если рассказ закончился раньше минуты, запишите полное время чтения и не пересчитывайте его автоматически в минутный норматив.\n" +
                        "• Для следующей проверки выберите незнакомую историю, чтобы результат не зависел от запоминания.\n" +
                        "• Скорость сама по себе не показывает, насколько понят рассказ. Для занятия без замера времени снимите галочку с подсчётом слов.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Как составить и распечатать лист", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("• Выберите класс и рассказ (при случайном выборе следующий в этой вкладке отличается от предыдущего).\n" +
                        "• Отметьте нужные настройки: подсчёт слов, лист ответов для взрослого, фон листа.\n" +
                        "• Нажмите «Сохранить в PDF».\n" +
                        "• При печати выберите A4 и масштаб 100%, отключите колонтитулы. Для цветного оформления включите печать фона.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
