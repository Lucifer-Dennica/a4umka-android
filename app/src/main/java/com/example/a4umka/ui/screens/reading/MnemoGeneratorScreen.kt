package com.example.a4umka.ui.screens.reading

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
import com.example.a4umka.data.*
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.MnemoPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MnemoGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(MnemoConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var textDropdownExpanded by remember { mutableStateOf(false) }
    var schemeDropdownExpanded by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = MnemoPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мнемотаблицы") },
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
                        val result = PdfExporter.exportMnemo(context, config)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ====== Карточка 1: Режим ======
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Вид листа", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        ModeRadioButton(
                            title = "Готовая мнемотаблица",
                            desc = "картинки-подсказки в клеточках с номерами и текст под ними",
                            selected = config.mode == MnemoMode.READY_TABLE,
                            onClick = { config = config.copy(mode = MnemoMode.READY_TABLE) }
                        )
                        ModeRadioButton(
                            title = "Нарисуй сам",
                            desc = "пустые пронумерованные клеточки и текст: ребёнок сам рисует подсказки",
                            selected = config.mode == MnemoMode.DRAW_YOURSELF,
                            onClick = { config = config.copy(mode = MnemoMode.DRAW_YOURSELF) }
                        )
                        ModeRadioButton(
                            title = "Описательный рассказ",
                            desc = "картинка и шесть вопросов по плану — для овощей, животных, игрушек",
                            selected = config.mode == MnemoMode.DESCRIPTIVE_STORY,
                            onClick = { config = config.copy(mode = MnemoMode.DESCRIPTIVE_STORY) }
                        )
                    }
                }
            }

            // ====== Карточка 2: Тема и текст ======
            item {
                if (config.mode != MnemoMode.DESCRIPTIVE_STORY) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Тема", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MnemoTheme.values().forEach { theme ->
                                    FilterChip(
                                        selected = config.theme == theme,
                                        onClick = { config = config.copy(theme = theme, selectedText = null) },
                                        label = { Text(theme.title) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Стих или рассказ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))

                            val availableTexts = remember(config.theme) { MnemoData.getTextsByTheme(config.theme) }

                            ExposedDropdownMenuBox(
                                expanded = textDropdownExpanded,
                                onExpandedChange = { textDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = config.selectedText?.title ?: "Любой из темы",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = textDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    label = { Text("Текст") }
                                )
                                ExposedDropdownMenu(
                                    expanded = textDropdownExpanded,
                                    onDismissRequest = { textDropdownExpanded = false },
                                    modifier = Modifier.heightIn(max = 350.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Любой из темы (случайный)", fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            config = config.copy(selectedText = null)
                                            textDropdownExpanded = false
                                        }
                                    )
                                    availableTexts.forEach { text ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(text.title, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        "${text.age.title} · ${text.cellsCount} клеток · ${text.type.title}",
                                                        fontSize = 12.sp, color = Color.Gray
                                                    )
                                                }
                                            },
                                            onClick = {
                                                config = config.copy(selectedText = text)
                                                textDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = config.showText,
                                    onCheckedChange = { config = config.copy(showText = it) }
                                )
                                Column {
                                    Text("Печатать текст под таблицей", fontWeight = FontWeight.Bold)
                                    Text("для взрослого — с номерами строк", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                } else {
                    // Режим описательного рассказа
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Категория", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DescriptiveCategory.values().forEach { category ->
                                    FilterChip(
                                        selected = config.descriptiveCategory == category,
                                        onClick = { config = config.copy(descriptiveCategory = category, selectedSchemeId = null) },
                                        label = { Text("${category.emoji} ${category.title}") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Конкретный предмет", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))

                            val availableSchemes = remember(config.descriptiveCategory) {
                                MnemoData.getSchemesByCategory(config.descriptiveCategory)
                            }

                            ExposedDropdownMenuBox(
                                expanded = schemeDropdownExpanded,
                                onExpandedChange = { schemeDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = config.selectedSchemeId?.let { MnemoData.getSchemeById(it)?.title } ?: "Любой из категории",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = schemeDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    label = { Text("Предмет") }
                                )
                                ExposedDropdownMenu(
                                    expanded = schemeDropdownExpanded,
                                    onDismissRequest = { schemeDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Любой из категории (случайный)", fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            config = config.copy(selectedSchemeId = null)
                                            schemeDropdownExpanded = false
                                        }
                                    )
                                    availableSchemes.forEach { scheme ->
                                        DropdownMenuItem(
                                            text = { Text("${scheme.emoji} ${scheme.title}") },
                                            onClick = {
                                                config = config.copy(selectedSchemeId = scheme.id)
                                                schemeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            // НОВОЕ: выбор количества листов (как на сайте-эталоне)
                            Text("Сколько листов", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = config.descriptiveSheetsCount == 1,
                                    onClick = { config = config.copy(descriptiveSheetsCount = 1) },
                                    label = { Text("1 лист — 2 картинки") }
                                )
                                FilterChip(
                                    selected = config.descriptiveSheetsCount == 2,
                                    onClick = { config = config.copy(descriptiveSheetsCount = 2) },
                                    label = { Text("2 листа — 4 картинки") }
                                )
                                FilterChip(
                                    selected = config.descriptiveSheetsCount == 3,
                                    onClick = { config = config.copy(descriptiveSheetsCount = 3) },
                                    label = { Text("3 листа — 6 картинок") }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "На каждом листе 2 описательные схемы с 6 вопросами по плану",
                                fontSize = 12.sp, color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            // НОВОЕ: кнопка «Обновить» — меняет randomSeed, превью пересчитается автоматически
                            OutlinedButton(
                                onClick = { config = config.copy(randomSeed = System.currentTimeMillis()) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = config.selectedSchemeId == null  // активна только в режиме «любая из категории»
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Обновить — другой случайный набор")
                            }
                        }
                    }
                }
            }

            // ====== Превью ======
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Стр. ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Text("Двумя пальцами можно приблизить лист", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(MnemoPdfDrawer.PAGE_WIDTH / MnemoPdfDrawer.PAGE_HEIGHT)
                    ) { page ->
                        if (page < previewPages.size) {
                            ZoomableImage(
                                bitmap = previewPages[page],
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Нет текста для выбранной темы",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // ====== Инфо-лист ======
        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Мнемотаблицы для дошкольников", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Мнемотаблица — это стихотворение, сказка или короткий рассказ, разложенный по клеточкам. " +
                        "В каждой клетке — картинка-подсказка к одной строке: листья кружатся, дождинка стучит в окно, " +
                        "ёжик спит под кустом. Ребёнок смотрит на картинки по порядку и сам пересказывает текст, " +
                        "не заучивая его «с голоса».\n\n" +
                        "Генератор собирает лист A4 под принтер — про осень, зиму, весну и лето, по народным потешкам " +
                        "и сказкам («Репка», «Колобок», «Теремок» и другим), про животных. Сразу бесплатно, без регистрации.\n\n" +
                        "Помимо готовых таблиц есть лист «Нарисуй сам» с пустыми клеточками и схемы описательного " +
                        "рассказа — по ним ребёнок учится рассказывать об овоще, животном или игрушке по плану.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("В чём польза", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Дошкольнику тяжело держать длинный текст в голове на слух, а картинку он схватывает легко. " +
                        "Мнемотаблица превращает слова в ряд образов — у рассказа появляется видимый глазом «план». " +
                        "Поэтому мнемотаблицы так любят в детском саду на занятиях по развитию речи.\n\n" +
                        "• связная речь — пересказ и рассказ по порядку, без пропусков;\n" +
                        "• память — стихи учатся быстрее и держатся дольше;\n" +
                        "• словарь — новые слова закрепляются вместе с картинкой;\n" +
                        "• образное мышление — на листе «Нарисуй сам» ребёнок придумывает свои подсказки;\n" +
                        "• умение рассказывать по плану — основа школьных пересказов.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Какой возраст — столько клеток", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "• 4–5 лет — 4 клетки: короткие потешки «Как у нашего кота», «Солнышко-вёдрышко»;\n" +
                        "• 5–6 лет — 6 клеток: рассказы о временах года, сказки «Репка» и «Теремок», схемы описательного рассказа;\n" +
                        "• 6–7 лет — 9 клеток («Ладушки», «Колобок») и лист «Нарисуй сам», где подсказки рисует сам ребёнок.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Три режима заданий", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "1. Готовая мнемотаблица — картинки-подсказки в клеточках с номерами и текст под ними.\n" +
                        "2. Нарисуй сам — пустые пронумерованные клеточки и текст: ребёнок сам рисует подсказку к каждой строке.\n" +
                        "3. Описательный рассказ — картинка и шесть вопросов по порядку: что это, какого цвета, какой формы, " +
                        "где растёт, что из него готовят. По две схемы на лист.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Советы взрослому", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "• Сначала прочитайте текст сами, показывая пальцем на клеточки по порядку.\n" +
                        "• Потом попросите ребёнка рассказать вместе с вами, затем самостоятельно.\n" +
                        "• Начинайте с таблиц на 4 клетки, к 9 переходите, когда станет легко.\n" +
                        "• Не требуйте дословного пересказа — свои слова тут только приветствуются.\n" +
                        "• Одна таблица за раз, 5–10 минут. К знакомой таблице полезно вернуться через пару дней.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ModeRadioButton(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (desc.isNotEmpty()) {
                Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 14.sp)
            }
        }
    }
}
