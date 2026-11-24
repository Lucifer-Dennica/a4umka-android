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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensionGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(ComprehensionConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    // ПУНКТ 8: Восстановлен режим редактирования
    var editMode by remember { mutableStateOf(false) }
    var editedStory by remember { mutableStateOf<ComprehensionStory?>(null) }

    LaunchedEffect(Unit) {
        if (config.selectedStory == null) {
            config = config.copy(
                selectedStory = ReadingComprehensionData.getRandomStory(config.grade),
                fontSize = if (config.grade == 1) 20f else 18f
            )
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

    val gradeStories = ReadingComprehensionData.getStoriesByGrade(config.grade)
    val wMin = gradeStories.minOfOrNull { it.totalWords } ?: 0
    val wMax = gradeStories.maxOfOrNull { it.totalWords } ?: 0
    val qNum = gradeStories.firstOrNull()?.questions?.size ?: 4
    val subtitle = "${gradeStories.size} рассказов · $wMin–$wMax слов · $qNum ${questionForm(qNum)}."

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Чтение с вопросами", color = Color.Black, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.Black) }
                },
                actions = {
                    IconButton(onClick = { showInfoSheet = true }) { Icon(Icons.Default.Info, "Справка", tint = Color.Black) }
                    IconButton(onClick = {
                        val finalConfig = if (editMode && editedStory != null) {
                            config.copy(selectedStory = editedStory)
                        } else {
                            config
                        }
                        val res = PdfExporter.exportComprehension(context, finalConfig)
                        scope.launch { snackbarHostState.showSnackbar(if (res != null) "Лист составлен: $res" else "Ошибка сохранения") }
                    }) { Icon(Icons.Default.Save, "Сохранить", tint = Color.Black) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
                OutlinedCard(border = BorderStroke(1.dp, Color.LightGray), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(12.dp)) {
                        // ПУНКТ 2: Кнопки классов на одном уровне с надписью
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Класс:", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                            listOf(1, 2, 3, 4).forEach { grade ->
                                FilterChip(
                                    selected = config.grade == grade,
                                    onClick = {
                                        config = config.copy(
                                            grade = grade,
                                            fontSize = if (grade == 1) 20f else 18f,
                                            selectedStory = ReadingComprehensionData.getRandomStory(grade)
                                        )
                                        dropdownExpanded = false
                                        editMode = false
                                    },
                                    label = { Text(grade.toString()) },
                                    border = BorderStroke(1.dp, Color.Gray),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.Black, selectedLabelColor = Color.White, containerColor = Color.White, labelColor = Color.Black)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Это ориентир по сложности: выбирайте по навыку ребёнка.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            item {
                OutlinedCard(border = BorderStroke(1.dp, Color.LightGray), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Выберите текст", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            // ПУНКТ 8: Переключатель режима
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = !editMode, onClick = { editMode = false }, label = { Text("Просмотр") })
                                FilterChip(selected = editMode, onClick = { if (config.selectedStory != null) { editMode = true; editedStory = config.selectedStory } }, label = { Text("Редактировать") })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }) {
                            OutlinedTextField(
                                value = config.selectedStory?.title ?: "Случайный рассказ",
                                onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.heightIn(max = 350.dp)) {
                                gradeStories.forEach { s ->
                                    DropdownMenuItem(text = { Column { Text(s.title, fontWeight = FontWeight.Bold); Text("${s.totalWords} слов", fontSize = 12.sp, color = Color.Gray) } },
                                        onClick = { config = config.copy(selectedStory = s); dropdownExpanded = false; editMode = false })
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(subtitle, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                            TextButton(onClick = { config = config.copy(selectedStory = ReadingComprehensionData.getRandomStory(config.grade)); editMode = false }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Случайный", color = Color.Black)
                            }
                        }

                        // ПУНКТ 8: Блок редактирования
                        if (editMode && editedStory != null) {
                            val story = editedStory!!
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(value = story.title, onValueChange = { editedStory = story.copy(title = it); config = config.copy(selectedStory = editedStory) }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = story.text, onValueChange = { editedStory = story.copy(text = it); config = config.copy(selectedStory = editedStory) }, label = { Text("Текст рассказа") }, modifier = Modifier.fillMaxWidth().height(150.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Вопросы и ответы:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            
                            story.questions.forEachIndexed { index, q ->
                                OutlinedTextField(value = q, onValueChange = { newQ -> 
                                    val newQs = story.questions.toMutableList(); newQs[index] = newQ
                                    editedStory = story.copy(questions = newQs); config = config.copy(selectedStory = editedStory)
                                }, label = { Text("Вопрос ${index + 1}") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                
                                val ansList = story.answers
                                val currentAnswer = if (index < ansList.size) ansList[index] else ""
                                OutlinedTextField(value = currentAnswer, onValueChange = { newAns -> 
                                    val newA = ansList.toMutableList()
                                    while (newA.size <= index) newA.add("")
                                    newA[index] = newAns
                                    editedStory = story.copy(answers = newA); config = config.copy(selectedStory = editedStory)
                                }, label = { Text("Ответ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 4.dp)) {
                    CheckRow("Количество слов справа от каждой строки", config.showWordCount) { config = config.copy(showWordCount = it) }
                    CheckRow("Подсказки для проверки техники чтения на листе взрослого", config.showAdultHints) { config = config.copy(showAdultHints = it) }
                    CheckRow("Добавить отдельный лист ответов", config.showAnswerSheet) { config = config.copy(showAnswerSheet = it) }
                    // ПУНКТ 4: Новая функция
                    CheckRow("Вопросы по тексту на отдельном листе", config.showQuestionsOnSeparateSheet) { config = config.copy(showQuestionsOnSeparateSheet = it) }
                    // ПУНКТ 6: Галочка "Чёрно-белая печать" удалена
                    
                    Spacer(Modifier.height(8.dp))
                    Text("Размер шрифта: ${config.fontSize.toInt()}", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = config.fontSize,
                        onValueChange = { config = config.copy(fontSize = it.roundToInt().toFloat()) },
                        valueRange = 14f..24f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Text("Предпросмотр листа A4:", color = Color.Black, fontWeight = FontWeight.Bold)
                if (previewPages.size > 1) Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = Color.DarkGray, fontSize = 12.sp)
                Text("Двумя пальцами можно приблизить лист", fontSize = 10.sp, color = Color.Gray)
            }
            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().aspectRatio(ComprehensionPdfDrawer.PAGE_WIDTH / ComprehensionPdfDrawer.PAGE_HEIGHT)) { page ->
                        if (page < previewPages.size) ZoomableImage(bitmap = previewPages[page], modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Тексты для чтения с вопросами — распечатать для 1–4 класса", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(12.dp))
                    Text("Ребёнок прочитал рассказ. А может ли он объяснить, почему герой так поступил и чем закончилась история? Генератор помогает перейти от чтения к обсуждению: на листе A4 — рассказ, вопросы и место для ответов.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("После обсуждения рассказа предложите отдельные упражнения на подлежащее и сказуемое, чтобы потренировать разбор коротких предложений. А карточки с синонимами и антонимами помогут обсудить близкие и противоположные значения слов с опорой на контекст.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("В подборке 100 авторских рассказов — по 25 для каждого класса. Выберите конкретную историю или случайный вариант. Материалы доступны бесплатно, без регистрации.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    InfoBlock("Что будет на распечатке", "• Лист ребёнка — рассказ и четыре или пять вопросов. Можно отвечать устно или записывать ответы под вопросами.\n• Лист взрослого — ответы и подсказки для обсуждения. Он добавляется по желанию и печатается отдельной страницей.\n• Количество слов по строкам — дополнительная разметка для проверки чтения. Числа можно отключить.\n• Оформление строго черно-белое для экономии краски при печати.")
                    InfoBlock("Тексты для чтения по классам", "Истории посвящены знакомым детям ситуациям: прогулкам, семье, школьным занятиям, открытиям и совместным делам. На каждом уровне — 25 разных рассказов.\n\n1 класс — первые короткие истории. 51–5 a8 слов и четыре вопроса. Короткие предложения и крупный шрифт помогают удерживать строку.\n\n2 класс — события и поступки. 82–97 слов и четыре вопроса. Ребёнок объясняет поступки героев и находит подтверждение своему ответу в тексте.\n\n3 класс — наблюдения и выводы. 114–135 слов и четыре вопроса.\n\n4 класс — причины и главная мысль. 154–175 слов и пять вопросов.")
                    InfoBlock("Ориентир сложности", "Класс — ориентир сложности. Он не означает соответствие конкретному учебнику, четверти или обязательному нормативу. Выбирайте по навыку ребёнка: при необходимости возьмите предыдущий уровень.")
                    InfoBlock("Как обсуждать прочитанное", "• Начните с устного ответа. Дайте ребёнку время обдумать содержание.\n• Найдите подтверждение. Попросите показать предложение, на которое он опирается.\n• Перечитайте трудный фрагмент вместе, если ответить пока не получается.\n• Ответы на листе взрослого — смысловые ориентиры. Необязательно повторять их слово в слово.")
                    InfoBlock("Как пользоваться подсчётом слов", "Число справа показывает сумму слов от начала рассказа до конца строки. Заголовок не учитывается. Слово с дефисом считается одним словом, отдельное тире — не словом.\n\n• Для проверки за минуту отметьте последнее полностью прочитанное слово и отдельно запишите ошибки.\n• Если рассказ закончился раньше минуты, запишите полное время чтения. Не пересчитывайте его автоматически в минутный норматив.\n• Для следующей проверки выберите незнакомую историю.")
                    InfoBlock("Как составить и распечатать лист", "• Выберите класс и рассказ.\n• Отметьте нужные настройки: подсчёт слов, ответы для взрослого, вынос вопросов на отдельный лист.\n• Нажмите «Сохранить в PDF».\n• При печати выберите A4 и масштаб 100%, отключите колонтитулы.")
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Checkbox(checked = checked, onCheckedChange = onChange, colors = CheckboxDefaults.colors(checkedColor = Color.Black, checkmarkColor = Color.White))
        Text(label, fontSize = 14.sp, color = Color(0xFF333333))
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
    Spacer(Modifier.height(4.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))
}

private fun questionForm(n: Int): String = when {
    n % 100 in 11..19 -> "вопросов"
    n % 10 == 1 -> "вопрос"
    n % 10 in 2..4 -> "вопроса"
    else -> "вопросов"
}
