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
    val scheme = MaterialTheme.colorScheme

    var config by remember { mutableStateOf(ComprehensionConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

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
        containerColor = scheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Чтение с вопросами", color = scheme.onSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = scheme.onSurface) }
                },
                actions = {
                    IconButton(onClick = { showInfoSheet = true }) { Icon(Icons.Default.Info, "Справка", tint = scheme.onSurface) }
                    IconButton(onClick = {
                        val finalConfig = if (editMode && editedStory != null) config.copy(selectedStory = editedStory) else config
                        val res = PdfExporter.exportComprehension(context, finalConfig)
                        scope.launch { snackbarHostState.showSnackbar(if (res != null) "Лист составлен: $res" else "Ошибка сохранения") }
                    }) { Icon(Icons.Default.Save, "Сохранить", tint = scheme.primary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.surface)
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
                OutlinedCard(border = BorderStroke(1.dp, scheme.outlineVariant), colors = CardDefaults.cardColors(containerColor = scheme.surface)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Класс:", color = scheme.onSurface, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
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
                                    border = BorderStroke(1.dp, scheme.outlineVariant),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = scheme.primaryContainer,
                                        selectedLabelColor = scheme.onPrimaryContainer,
                                        containerColor = scheme.surface,
                                        labelColor = scheme.onSurface
                                    )
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Это ориентир по сложности: выбирайте по навыку ребёнка.", fontSize = 12.sp, color = scheme.onSurfaceVariant)
                    }
                }
            }

            item {
                OutlinedCard(border = BorderStroke(1.dp, scheme.outlineVariant), colors = CardDefaults.cardColors(containerColor = scheme.surface)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Выберите текст", color = scheme.onSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = !editMode, onClick = { editMode = false },
                                    label = { Text("Просмотр") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = scheme.primaryContainer, selectedLabelColor = scheme.onPrimaryContainer))
                                FilterChip(selected = editMode, onClick = { if (config.selectedStory != null) { editMode = true; editedStory = config.selectedStory } },
                                    label = { Text("Редактировать") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = scheme.primaryContainer, selectedLabelColor = scheme.onPrimaryContainer))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }) {
                            OutlinedTextField(
                                value = config.selectedStory?.title ?: "Случайный рассказ",
                                onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = scheme.primary, unfocusedBorderColor = scheme.outlineVariant)
                            )
                            ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.heightIn(max = 350.dp)) {
                                gradeStories.forEach { s ->
                                    DropdownMenuItem(text = { Column { Text(s.title, fontWeight = FontWeight.Bold); Text("${s.totalWords} слов", fontSize = 12.sp, color = scheme.onSurfaceVariant) } },
                                        onClick = { config = config.copy(selectedStory = s); dropdownExpanded = false; editMode = false })
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(subtitle, fontSize = 12.sp, color = scheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            TextButton(onClick = { config = config.copy(selectedStory = ReadingComprehensionData.getRandomStory(config.grade)); editMode = false }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Случайный", color = scheme.primary)
                            }
                        }

                        if (editMode && editedStory != null) {
                            val story = editedStory!!
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(value = story.title, onValueChange = { editedStory = story.copy(title = it); config = config.copy(selectedStory = editedStory) }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = story.text, onValueChange = { editedStory = story.copy(text = it); config = config.copy(selectedStory = editedStory) }, label = { Text("Текст рассказа") }, modifier = Modifier.fillMaxWidth().height(150.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Вопросы и ответы:", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
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
                    CheckRow("Вопросы по тексту на отдельном листе", config.showQuestionsOnSeparateSheet) { config = config.copy(showQuestionsOnSeparateSheet = it) }

                    Spacer(Modifier.height(8.dp))
                    Text("Размер шрифта: ${config.fontSize.toInt()}", fontSize = 14.sp, color = scheme.onSurface, fontWeight = FontWeight.SemiBold)
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
                Text("Предпросмотр листа A4:", color = scheme.onSurface, fontWeight = FontWeight.Bold)
                if (previewPages.size > 1) Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = scheme.primary, fontSize = 12.sp)
                Text("Двумя пальцами можно приблизить лист", fontSize = 10.sp, color = scheme.onSurfaceVariant)
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
                    Text("Тексты для чтения с вопросами — распечатать для 1–4 класса", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                    Spacer(Modifier.height(12.dp))
                    Text("Ребёнок прочитал рассказ. А может ли он объяснить, почему герой так поступил и чем закончилась история? Генератор помогает перейти от чтения к обсуждению: на листе A4 — рассказ, вопросы и место для ответов.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("После обсуждения рассказа предложите отдельные упражнения на подлежащее и сказуемое, чтобы потренировать разбор коротких предложений. А карточки с синонимами и антонимами помогут обсудить близкие и противоположные значения слов с опорой на контекст.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("В подборке 100 авторских рассказов — по 25 для каждого класса. Выберите конкретную историю или случайный вариант. Материалы доступны бесплатно, без регистрации.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    InfoBlock("Что будет на распечатке", "• Лист ребёнка — рассказ и четыре или пять вопросов. Можно отвечать устно или записывать ответы под вопросами.\n• Лист взрослого — ответы и подсказки для обсуждения. Он добавляется по желанию и печатается отдельной страницей.\n• Количество слов по строкам — дополнительная разметка для проверки чтения. Числа можно отключить.\n• Цветная или чёрно-белая печать — выберите подходящее оформление перед созданием листа.")
                    InfoBlock("Тексты для чтения по классам", "Истории посвящены знакомым детям ситуациям: прогулкам, семье, школьным занятиям, открытиям и совместным делам. На каждом уровне — 25 разных рассказов.\n\n1 класс — первые короткие истории. 51–58 слов и четыре вопроса. Короткие предложения и крупный шрифт помогают удерживать строку. Вопросы предлагают вспомнить героев, предметы и простую причину события. Уровень подходит ребёнку, который уже читает предложения целиком. Если пока нужна поддержка со слогами, начните с чтения по слогам.\n\n2 класс — события и поступки. 82–97 слов и четыре вопроса. В рассказах несколько последовательных событий. Ребёнок объясняет поступки героев и находит подтверждение своему ответу в тексте.\n\n3 класс — наблюдения и выводы. 114–135 слов и четыре вопроса. Нужно связать детали рассказа, объяснить, что помогло герою, и заметить, как изменилось его отношение к ситуации.\n\n4 класс — причины и главная мысль. 154–175 слов и пять вопросов. Важны точность деталей, причинные связи и смысл поступков. Среди тем — сотрудничество, наблюдения и проверка сведений.")
                    InfoBlock("Ориентир сложности", "Класс — ориентир сложности. Он не означает соответствие конкретному учебнику, четверти или обязательному нормативу. Выбирайте по навыку ребёнка: при необходимости возьмите предыдущий уровень.")
                    InfoBlock("Как обсуждать прочитанное", "• Начните с устного ответа. Дайте ребёнку время обдумать содержание.\n• Найдите подтверждение. Попросите показать предложение, на которое он опирается.\n• Перечитайте трудный фрагмент вместе, если ответить пока не получается.\n• Запишите короткий ответ, если хотите потренировать письменную речь.\n• Ответы на листе взрослого — смысловые ориентиры. Необязательно повторять их слово в слово: важны понимание истории и соответствие тексту.")
                    InfoBlock("Как пользоваться подсчётом слов", "Число справа показывает сумму слов от начала рассказа до конца строки. Заголовок не учитывается. Слово с дефисом считается одним словом, отдельное тире — не словом.\n\n• Для проверки за минуту отметьте последнее полностью прочитанное слово и отдельно запишите ошибки.\n• Для неполной строки возьмите число у предыдущей строки и прибавьте прочитанные слова текущей.\n• Если рассказ закончился раньше минуты, запишите полное время чтения. Не пересчитывайте его автоматически в минутный норматив.\n• Для следующей проверки выберите незнакомую историю, чтобы результат не зависел от запоминания.\n• После чтения обсудите содержание. Попросите ребёнка показать предложение, на которое он опирается, или объяснить ответ своими словами. Скорость сама по себе не показывает, насколько хорошо понят рассказ. Для занятия без замера времени снимите галочки с подсчётом слов и подсказками по технике чтения.")
                    InfoBlock("Как составить и распечатать лист", "• Выберите класс и рассказ. При случайном выборе следующий рассказ в этой вкладке отличается от предыдущего.\n• Отметьте нужные настройки: подсчёт слов, ответы для взрослого и чёрно-белую печать.\n• Нажмите «Сохранить в PDF».\n• При печати выберите A4 и масштаб 100%, отключите колонтитулы.")
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
