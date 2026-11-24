package com.example.sovenok.ui.screens.reading

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import kotlin.math.roundToInt // ДОБАВЛЕН ИМПОРТ ДЛЯ roundToInt

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
    
    // Режим редактирования: список редактируемых текстов
    var editMode by remember { mutableStateOf(false) }
    var editedTexts by remember { mutableStateOf<List<SyllableText>>(emptyList()) }
    
    // Синхронизируем черновик с выбором, пока НЕ в режиме редактирования
    LaunchedEffect(config.selectedTexts) {
        if (!editMode) editedTexts = config.selectedTexts
    }
    
    // Конфиг, реально используемый для предпросмотра и экспорта
    val effectiveConfig =
        if (editMode && editedTexts.isNotEmpty() && config.source == SyllableSource.READY)
            config.copy(selectedTexts = editedTexts)
        else config
        
    LaunchedEffect(effectiveConfig) {
        delay(300)
        previewPages = SyllablePdfDrawer.generatePreviewPages(effectiveConfig, context)
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
                        if (effectiveConfig.source == SyllableSource.READY && effectiveConfig.selectedTexts.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Сначала выберите хотя бы один текст") }
                            return@IconButton
                        }
                        val result = PdfExporter.exportSyllableText(context, effectiveConfig)
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
                                onClick = { config = config.copy(source = SyllableSource.READY); editMode = false },
                                label = { Text("Готовый текст") }
                            )
                            FilterChip(
                                selected = config.source == SyllableSource.CUSTOM,
                                onClick = { config = config.copy(source = SyllableSource.CUSTOM); editMode = false },
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
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedCategory.texts.forEach { textObj ->
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
                            // Переключатель режима
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = !editMode,
                                    onClick = { editMode = false },
                                    label = { Text("Просмотр") }
                                )
                                FilterChip(
                                    selected = editMode,
                                    onClick = {
                                        if (config.selectedTexts.isNotEmpty()) {
                                            editMode = true
                                            editedTexts = config.selectedTexts
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar("Сначала выберите текст") }
                                        }
                                    },
                                    label = { Text("Редактировать") }
                                )
                            }
                            // Редактирование ВСЕХ выбранных текстов
                            if (editMode && editedTexts.isNotEmpty()) {
                                editedTexts.forEachIndexed { idx, t ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Текст ${idx + 1}: ${t.title}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = t.title,
                                        onValueChange = { newTitle ->
                                            val list = editedTexts.toMutableList()
                                            list[idx] = t.copy(title = newTitle)
                                            editedTexts = list
                                        },
                                        label = { Text("Название") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = t.text,
                                        onValueChange = { newText ->
                                            val list = editedTexts.toMutableList()
                                            list[idx] = t.copy(text = newText)
                                            editedTexts = list
                                        },
                                        label = { Text("Текст") },
                                        modifier = Modifier.fillMaxWidth().height(120.dp)
                                    )
                                    if (t.questions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Вопросы:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                        t.questions.forEachIndexed { qi, q ->
                                            OutlinedTextField(
                                                value = q,
                                                onValueChange = { newQ ->
                                                    val list = editedTexts.toMutableList()
                                                    val newQuestions = list[idx].questions.toMutableList()
                                                    newQuestions[qi] = newQ
                                                    list[idx] = list[idx].copy(questions = newQuestions)
                                                    editedTexts = list
                                                },
                                                label = { Text("Вопрос ${qi + 1}") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
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
                                Switch(checked = config.isMultiSelectMode, onCheckedChange = { isChecked ->
                                    config = config.copy(
                                        isMultiSelectMode = isChecked,
                                        selectedTexts = if (!isChecked && config.selectedTexts.size > 1) listOf(config.selectedTexts.first()) else config.selectedTexts
                                    )
                                })
                            }
                        }
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text("Размер шрифта: ${config.fontSize.toInt()}", fontSize = 14.sp)
                            Slider(
                                value = config.fontSize,
                                onValueChange = { config = config.copy(fontSize = it.roundToInt().toFloat()) },
                                valueRange = 16f..40f,
                                steps = 23, // <-- ДОБАВЛЕНА ЗАПЯТАЯ ЗДЕСЬ
                                modifier = Modifier.fillMaxWidth()
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
                            Switch(checked = config.showQuestions, onCheckedChange = { config = config.copy(showQuestions = it) })
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
                            ZoomableImage(bitmap = previewPages[page], modifier = Modifier.fillMaxSize())
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        // СПРАВКА (INFO SHEET) — ПОЛНАЯ ВЕРСИЯ
        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Чтение по слогам для детей — распечатать бесплатно",
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Когда ребёнок только начинает читать, целое слово кажется ему пугающе длинным. Но стоит увидеть, что слово состоит из коротких знакомых кусочков-слогов, как чтение становится посильным. Возьмите готовый текст из подборки — 100 текстов от столбиков слов до историй для 1 класса — или вставьте любой свой. Разметка проставляется автоматически: дуги под слогами и точки под одиночными буквами. Готовый лист печатается на обычном принтере A4.",
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Text("Что такое разметка по слогам", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Под каждым слогом рисуется дуга снизу строки — как в букварях: ребёнок ведёт по ней пальцем и читает слог слитно, одним движением. Одиночные буквы, которые не входят в слог («и», «в», союз «а», буква на стыке), отмечаются точкой снизу — так малыш видит, что их нужно прочитать отдельно. Разметка выполняется по правилам слогоделения, вручную ничего рисовать не нужно.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Вот что даёт такая разметка ребёнку:\n" +
                        "• уверенность — слово больше не выглядит непреодолимой стеной из букв;\n" +
                        "• плавность — слоги читаются слитно, а не побуквенно;\n" +
                        "• понимание — освободившееся внимание идёт на смысл текста;\n" +
                        "• скорость — постепенно ребёнок начинает узнавать слоги с одного взгляда.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Готовые тексты — 100 штук по пяти ступеням", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("• Слоги и слова — 14 листов со столбиками коротких слов: животные, еда, транспорт, одежда, слова с мягким знаком. Самый первый шаг, когда предложение читать ещё рано.\n" +
                        "• Первые предложения — 22 текста по три-пять коротких строк. Ребёнок впервые читает не слово, а мысль целиком.\n" +
                        "• Короткие рассказы — 28 историй на пять-восемь предложений для детей 5–6 лет.\n" +
                        "• Рассказы для 1 класса — 22 текста по десять-пятнадцать предложений, у каждого вопросы по прочитанному.\n" +
                        "• Сказки — 14 знакомых сюжетов простыми короткими фразами: «Репка», «Теремок», «Маша и медведь», «Гуси-лебеди», «Каша из топора».",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Выбранный текст можно тут же поправить во вкладке «Редактировать»: сократить, заменить трудное слово или вписать имя вашего ребёнка вместо имени героя. Дети читают заметно охотнее, когда встречают себя в рассказе.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Тексты для 1 класса — с вопросами по прочитанному", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Первокласснику мало прочитать — нужно ещё понять. Поставьте галочку «Вопросы по тексту», и они напечатаются под рассказом на том же листе. Вопросы простые и по делу: кто главный герой, почему он так поступил, чем всё закончилось. Это ровно то, что проверяют в школе на технике чтения: не скорость сама по себе, а осмысленность. Если ответить не получилось — ребёнок читал механически, текст стоит перечитать вслух вместе.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Для самых первых занятий и для детей, которым трудно удерживать строку, включайте «Крупный шрифт» — буквы становятся заметно больше, а строки просторнее. Такой лист удобно читать и с расстояния, и группе детей сразу.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Для какого возраста подходит", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Тексты рассчитаны прежде всего на детей 5–7 лет. В 5 лет ребёнок обычно только знакомится со слиянием букв в слоги — подойдут совсем короткие тексты из простых слов. В 6 лет большинство уже читают короткие фразы, можно брать небольшие рассказы. В 7 лет, перед школой или в первом классе, размеченный текст помогает наращивать скорость и переходить к чтению целыми словами. Поскольку текст выбираете вы, генератор легко подстраивается под уровень конкретного ребёнка.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Как пользоваться генератором", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("• Выберите источник: «Готовый текст» — подборка из 100 текстов, или «Свой текст» — вставьте что угодно: сказку, стихотворение, страницу из книги.\n" +
                        "• При желании отметьте «Крупный шрифт», а для рассказов 1 класса — «Вопросы по тексту».\n" +
                        "• Разметка на слоги появляется автоматически: дуги под слогами, точки под одиночными буквами.\n" +
                        "• Нажмите «Сохранить в PDF» и распечатайте лист A4. Длинный текст сам разложится на несколько страниц.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Свобода выбора текста", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Главная возможность — свобода выбора. Подборка нужна, чтобы не искать материал самому, но вы ей не ограничены: вставьте именно тот текст, который интересен вашему ребёнку и подходит по сложности. Хотите легче — короткий текст из простых слов; хотите сложнее — больше предложений или более длинные слова. Разметка выполняется заново под любой введённый текст, так что можно готовить новый лист к каждому занятию и держать интерес ребёнка живым.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Как чтение по слогам помогает развитию навыка", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Сначала ребёнок учится сливать буквы в слог — дуга прямо показывает, какие буквы читать вместе. Затем он учится соединять слоги в слово, не теряя предыдущий: дуги делят слово на ритмичные кусочки. Постепенно происходит самое важное — ребёнок начинает узнавать частые слоги («ма», «ка», «но», «ли») целиком, не разбирая по буквам. Это и есть переход от медленного побуквенного чтения к беглому. Точки под одиночными буквами учат не «проглатывать» предлоги и короткие слова.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Подготовка к школе", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Умение читать — один из ключевых навыков для первого класса. Программа начальной школы построена так, что читающему ребёнку учиться заметно легче: он быстрее справляется с заданиями, лучше понимает условия задач и увереннее чувствует себя среди одноклассников. Чтение по слогам ведёт дошкольника за руку: сегодня две строчки, через неделю — небольшой рассказ. Распечатанные листы удобно собирать в папку, чтобы видеть, как растёт уровень.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Советы родителям и как часто заниматься", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("• Занимайтесь понемногу, но регулярно: 5–10 минут каждый день лучше, чем полчаса раз в неделю.\n" +
                        "• Подбирайте интересный текст — про динозавров, любимого мультгероя или домашнего питомца.\n" +
                        "• Не торопите и не исправляйте на каждом слоге — это отбивает желание читать.\n" +
                        "• Хвалите за усилие: прочитанную строчку, новое узнанное слово.\n" +
                        "• Читайте по очереди: строчку ребёнок, строчку вы — так держится темп и снимается напряжение.\n" +
                        "• Оптимальный режим для 5–7 лет — одно короткое занятие в день. Если ребёнок устал, лучше остановиться и вернуться позже.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("Где использовать размеченные тексты", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("• Дома — для ежедневных коротких занятий с родителями, спокойно и без спешки.\n" +
                        "• В детском саду — на занятиях по подготовке к школе.\n" +
                        "• У логопеда — в работе над звукопроизношением, слоговой структурой слова и техникой чтения.\n" +
                        "• В дороге — лист удобно взять в поездку, к бабушке или в очередь к врачу.\n" +
                        "Лист обычный, бумажный: им можно пользоваться где угодно без планшета и интернета — это бережёт зрение и не отвлекает картинками.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Text("С какими заданиями сочетать", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Чтобы ребёнок быстрее запоминал слоги, начните занятие со слоговых таблиц — они тренируют узнавание слогов с одного взгляда. Закрепить навык поможет упражнение «собери слово из слогов». Когда читает увереннее, добавьте игровые задания со словами: филворды и «найди слово в слове». А чтобы параллельно готовить руку к письму, чередуйте чтение с прописями.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
