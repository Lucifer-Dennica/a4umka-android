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

private val Green = Color(0xFF4C8C2A)
private val GreenDark = Color(0xFF33691E)
private val Cream = Color(0xFFF6F1E3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensionGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(ComprehensionConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var htmlMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (config.selectedStory == null) {
            config = config.copy(
                selectedStory = ReadingComprehensionData.getRandomStory(config.grade),
                fontSize = if (config.grade == 1) 20f else 18f
            )
        }
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
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = { Text("📖 Чтение с вопросами", color = GreenDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = GreenDark) }
                },
                actions = {
                    IconButton(onClick = { showInfoSheet = true }) { Icon(Icons.Default.Info, "Справка", tint = GreenDark) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // баннер
            item {
                OutlinedCard(border = BorderStroke(1.dp, Green.copy(0.35f)), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                        Text("🦊", fontSize = 28.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Каждый день выкладываю новый лист заданий", modifier = Modifier.weight(1f), fontSize = 13.sp, color = GreenDark, fontWeight = FontWeight.SemiBold)
                        OutlinedButton(onClick = { scope.launch { snackbarHostState.showSnackbar("Спасибо! Мы в соцсетях.") } },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenDark), border = BorderStroke(1.dp, Green)) { Text("Подписаться", fontSize = 12.sp) }
                        Spacer(Modifier.width(6.dp))
                        OutlinedButton(onClick = { scope.launch { snackbarHostState.showSnackbar("Поделиться") } },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenDark), border = BorderStroke(1.dp, Green)) { Text("Поделиться", fontSize = 12.sp) }
                    }
                }
            }

            item {
                Text("100 авторских рассказов — по 25 для каждого из 1–4 классов: прочитай, найди ответ в тексте, объясни поступки героев. На листе — текст и вопросы, для взрослого — отдельные ответы и подсчёт слов.",
                    fontSize = 13.sp, color = Color(0xFF555555))
            }

            // КЛАСС
            item {
                OutlinedCard(border = BorderStroke(1.dp, Green.copy(0.35f)), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Класс", color = GreenDark, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    },
                                    label = { Text(grade.toString()) },
                                    border = BorderStroke(1.dp, Green.copy(0.5f)),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Green, selectedLabelColor = Color.White, containerColor = Color.White, labelColor = GreenDark)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Это ориентир по сложности: выбирайте по навыку ребёнка.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // РАССКАЗ
            item {
                OutlinedCard(border = BorderStroke(1.dp, Green.copy(0.35f)), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Выберите текст", color = GreenDark, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }) {
                            OutlinedTextField(
                                value = config.selectedStory?.title ?: "Случайный рассказ",
                                onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green, unfocusedBorderColor = Green.copy(0.5f))
                            )
                            ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.heightIn(max = 350.dp)) {
                                gradeStories.forEach { s ->
                                    DropdownMenuItem(text = { Column { Text(s.title, fontWeight = FontWeight.Bold); Text("${s.totalWords} слов", fontSize = 12.sp, color = Color.Gray) } },
                                        onClick = { config = config.copy(selectedStory = s); dropdownExpanded = false })
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // НАСТРОЙКИ (чекбоксы как на сайте)
            item {
                Column(Modifier.padding(horizontal = 4.dp)) {
                    CheckRow("Количество слов справа от каждой строки", config.showWordCount) { config = config.copy(showWordCount = it) }
                    CheckRow("Подсказки для проверки техники чтения на листе взрослого", config.showAdultHints) { config = config.copy(showAdultHints = it) }
                    CheckRow("Добавить отдельный лист ответов", config.showAnswerSheet) { config = config.copy(showAnswerSheet = it) }
                    CheckRow("Чёрно-белая печать", config.isBlackAndWhite) { config = config.copy(isBlackAndWhite = it) }
                }
            }

            // ФОРМАТ
            item {
                OutlinedCard(border = BorderStroke(1.dp, Green.copy(0.35f)), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Формат", color = GreenDark, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !htmlMode, onClick = { htmlMode = false }, colors = RadioButtonDefaults.colors(selectedColor = Green))
                            Text("PDF / печать — открыть лист в новой вкладке", fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = htmlMode, onClick = { htmlMode = true }, colors = RadioButtonDefaults.colors(selectedColor = Green))
                            Text("HTML-файл — сохранить на компьютер", fontSize = 13.sp)
                        }
                    }
                }
            }

            // БОЛЬШАЯ КНОПКА
            item {
                Button(
                    onClick = {
                        if (htmlMode) { scope.launch { snackbarHostState.showSnackbar("HTML-экспорт появится позже — выберите PDF.") } }
                        else {
                            val res = PdfExporter.exportComprehension(context, config)
                            scope.launch { snackbarHostState.showSnackbar(if (res != null) "Лист составлен: $res" else "Ошибка сохранения") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                    shape = MaterialTheme.shapes.medium
                ) { Text("Составить лист", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            }

            // ПРЕДПРОСМОТР
            item {
                Text("Предпросмотр листа A4:", color = GreenDark, fontWeight = FontWeight.Bold)
                if (previewPages.size > 1) Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = Green, fontSize = 12.sp)
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

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Checkbox(checked = checked, onCheckedChange = onChange, colors = CheckboxDefaults.colors(checkedColor = Green, checkmarkColor = Color.White))
        Text(label, fontSize = 14.sp, color = Color(0xFF444444))
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenDark)
    Spacer(Modifier.height(4.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))
}

private fun questionForm(n: Int): String = when (n % 100) {
    1 -> "вопрос"; in 2..4 -> if (n % 100 in 12..14) "вопросов" else when (n % 10) { in 2..4 -> "вопроса"; else -> "вопросов" }
    else -> "вопросов"
}
