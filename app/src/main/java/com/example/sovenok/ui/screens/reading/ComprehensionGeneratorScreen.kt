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
import kotlin.math.roundToInt

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
                    IconButton(onClick = {
                        val res = PdfExporter.exportComprehension(context, config)
                        scope.launch { snackbarHostState.showSnackbar(if (res != null) "Лист составлен: $res" else "Ошибка сохранения") }
                    }) { Icon(Icons.Default.Save, "Сохранить", tint = GreenDark) }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(subtitle, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                            TextButton(onClick = { config = config.copy(selectedStory = ReadingComprehensionData.getRandomStory(config.grade)) }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Случайный", color = Green)
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
                    CheckRow("Чёрно-белая печать", config.isBlackAndWhite) { config = config.copy(isBlackAndWhite = it) }
                    
                    Spacer(Modifier.height(8.dp))
                    Text("Размер шрифта: ${config.fontSize.toInt()}", fontSize = 14.sp, color = GreenDark, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = config.fontSize,
                        onValueChange = { config = config.copy(fontSize = it.roundToInt().toFloat()) }, // Округление до целого для точности
                        valueRange = 14f..24f,
                        steps = 10, // 10 шагов дают ровно 14, 15, 16... 24
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

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
                Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Чтение с вопросами — лист для распечатки (1–4 класс)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = GreenDark)
                    Spacer(Modifier.height(12.dp))
                    Text("Ребёнок прочитал рассказ. Сможет ли он объяснить поступок героя и чем всё закончилось? Генератор помогает перейти от чтения к разговору: на листе A4 — текст, вопросы и поля для ответов.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    InfoBlock("Что попадёт на лист", "• Лист ребёнка — рассказ и 4–5 вопросов. Отвечать можно устно или вписать под вопросом.\n• Лист взрослого — ответы и подсказки. Добавляется по желанию отдельной страницей.\n• Счётчик слов по строкам — разметка для замера техники чтения, можно выключить.\n• Цветная или чёрно-белая печать — оформление выбирается до создания листа.")
                    InfoBlock("Уровни по классам", "1 класс — короткие истории, 51–58 слов, 4 вопроса. Крупный шрифт помогает держать строку.\n2 класс — события и поступки, 82–97 слов, 4 вопроса. Ребёнок ищет подтверждение в тексте.\n3 класс — наблюдения и выводы, 114–135 слов, 4 вопроса.\n4 класс — причины и главная мысль, 154–175 слов, 5 вопросов.")
                    InfoBlock("Как обсуждать прочитанное", "• Начните с устного ответа, дайте время подумать.\n• Попросите показать предложение-опору.\n• Трудный фрагмент перечитайте вместе.\n• Ответы взрослого — ориентиры по смыслу, не шаблон.")
                    InfoBlock("Как читать счётчик слов", "Число справа — сумма слов от начала до конца строки (заголовок не в счёт, слово с дефисом = одно, отдельное тире не слово). Отметьте последнее прочитанное слово за минуту, отдельно запишите ошибки. Если закончил раньше — фиксируйте полное время, не пересчитывайте в минутный норматив. В следующий раз берите незнакомый текст.")
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
