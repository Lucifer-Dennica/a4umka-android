package com.example.sovenok.ui.screens.reading

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sovenok.data.CopyingConfig
import com.example.sovenok.data.CopyingData
import com.example.sovenok.data.CopyingMode
import com.example.sovenok.ui.components.ZoomableImage
import com.example.sovenok.util.CopyingPdfDrawer
import com.example.sovenok.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyingGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var config by remember { mutableStateOf(CopyingConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var volumeFilter by remember { mutableStateOf("Любой объём") }
    var themeFilter by remember { mutableStateOf("Все темы") }

    val filteredStories = remember(searchQuery, volumeFilter, themeFilter, config.grade) {
        val allStories = CopyingData.getStories(config.grade)
        allStories.filter { story ->
            val matchesSearch = story.title.contains(searchQuery, ignoreCase = true) || story.text.contains(searchQuery, ignoreCase = true)
            val matchesVolume = when (volumeFilter) {
                "До 50 слов" -> story.wordCount <= 50
                "Больше 50 слов" -> story.wordCount > 50
                else -> true
            }
            val matchesTheme = when (themeFilter) {
                "Природа" -> story.theme == "Природа"
                "Дом" -> story.theme == "Дом"
                else -> true
            }
            matchesSearch && matchesVolume && matchesTheme
        }
    }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = CopyingPdfDrawer.generatePreview(config, context)
    }
    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Списывание и диктанты") },
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
                        if (config.selectedStories.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Выберите хотя бы один текст") }
                            return@IconButton
                        }
                        val result = PdfExporter.exportCopying(context, config)
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
            // БЛОК: Класс и фильтры
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Класс:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = 8.dp))
                            listOf(1, 2, 3, 4).forEach { grade ->
                                FilterChip(
                                    selected = config.grade == grade,
                                    onClick = { config = config.copy(grade = grade, selectedStories = emptyList()) },
                                    label = { Text(grade.toString()) }
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var volExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = volExpanded, onExpandedChange = { volExpanded = it }, modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = volumeFilter, onValueChange = {}, readOnly = true,
                                    label = { Text("Объём", fontSize = 12.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = volExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = volExpanded, onDismissRequest = { volExpanded = false }) {
                                    listOf("Любой объём", "До 50 слов", "Больше 50 слов").forEach { opt ->
                                        DropdownMenuItem(text = { Text(opt) }, onClick = { volumeFilter = opt; volExpanded = false })
                                    }
                                }
                            }
                            var themeExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = themeExpanded, onExpandedChange = { themeExpanded = it }, modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = themeFilter, onValueChange = {}, readOnly = true,
                                    label = { Text("Тема", fontSize = 12.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                                    listOf("Все темы", "Природа", "Дом").forEach { opt ->
                                        DropdownMenuItem(text = { Text(opt) }, onClick = { themeFilter = opt; themeExpanded = false })
                                    }
                                }
                            }
                        }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Поиск по названию или тексту") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Подходит: ${filteredStories.size}. Выбрано: ${config.selectedStories.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            items(filteredStories, key = { it.id }) { story ->
                val isSelected = config.selectedStories.contains(story)
                Card(
                    onClick = {
                        val newList = config.selectedStories.toMutableList()
                        if (isSelected) newList.remove(story) else newList.add(story)
                        config = config.copy(selectedStories = newList)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isSelected, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(story.title, fontWeight = FontWeight.Bold)
                            Text("${story.wordCount} слов • ${story.theme}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // БЛОК: Как работаем и настройки листа
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Как работаем", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = config.mode == CopyingMode.COPYING, onClick = { config = config.copy(mode = CopyingMode.COPYING) }, label = { Text("Списываем") })
                            FilterChip(selected = config.mode == CopyingMode.DICTATION, onClick = { config = config.copy(mode = CopyingMode.DICTATION) }, label = { Text("Диктант") })
                        }
                        Text("Настройки листа", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Печатать несколько текстов на одном листе (только для учителя)")
                                Text("Для экономии бумаги (тексты идут подряд)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Switch(checked = config.combineOnOnePage, onCheckedChange = { config = config.copy(combineOnOnePage = it) })
                        }
                        if (!config.combineOnOnePage) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                var childrenExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(expanded = childrenExpanded, onExpandedChange = { childrenExpanded = it }, modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = "Детей: ${config.childrenCount}", onValueChange = {}, readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = childrenExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(expanded = childrenExpanded, onDismissRequest = { childrenExpanded = false }) {
                                        listOf(1, 2, 3, 4, 5).forEach { count ->
                                            DropdownMenuItem(text = { Text("$count") }, onClick = { config = config.copy(childrenCount = count); childrenExpanded = false })
                                        }
                                    }
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var stepExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = stepExpanded, onExpandedChange = { stepExpanded = it }, modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = "Шаг: ${config.lineStep} мм", onValueChange = {}, readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stepExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = stepExpanded, onDismissRequest = { stepExpanded = false }) {
                                    listOf(8, 10, 12, 15).forEach { step ->
                                        DropdownMenuItem(text = { Text("$step мм") }, onClick = { config = config.copy(lineStep = step); stepExpanded = false })
                                    }
                                }
                            }
                        }
                        OutlinedTextField(
                            value = config.signature,
                            onValueChange = { config = config.copy(signature = it) },
                            label = { Text("Подпись на листе (необязательно)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Добавить чистый лист для письма", modifier = Modifier.weight(1f))
                            Switch(checked = config.addBlankPage, onCheckedChange = { config = config.copy(addBlankPage = it) })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Лист подготовки и проверки", modifier = Modifier.weight(1f))
                            Switch(checked = config.showPrepSheet, onCheckedChange = { config = config.copy(showPrepSheet = it) })
                        }
                        Column {
                            Text("Размер шрифта: ${config.fontSize.toInt()}", fontSize = 14.sp)
                            Slider(
                                value = config.fontSize,
                                onValueChange = { config = config.copy(fontSize = it.roundToInt().toFloat()) },
                                valueRange = 12f..24f,
                                steps = 11,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ПРЕДПРОСМОТР
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр листов A4:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Стр. ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Text("Двумя пальцами можно приблизить лист", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(CopyingPdfDrawer.PAGE_WIDTH / CopyingPdfDrawer.PAGE_HEIGHT)
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

        // СПРАВКА (INFO SHEET)
        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Тексты для списывания и диктантов", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Для письменного занятия нужен понятный образец, подходящий объём и достаточно места для своего почерка. Здесь собраны 100 авторских текстов: по 25 для каждого класса с 1-го по 4-й (от 22 до 84 слов).",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    InfoBlock(
                        "Как выбрать текст",
                        "Класс — это ориентир, а не строгое соответствие программе. Сначала прочитайте рассказ целиком: понятны ли слова, знакома ли ситуация. Объём варьируется: даже в 1 классе есть варианты как до 50, так и больше 50 слов. Тема («Природа» или «Дом») помогает быстро найти знакомый сюжет."
                    )
                    InfoBlock(
                        "Списывание или диктант",
                        "• Списывание: на детском листе напечатан весь рассказ. Ребёнок читает и переносит текст в строки своим почерком.\n• Диктант: на детском листе нет текста для подсматривания. Взрослый читает полный текст, затем диктует предложения."
                    )
                    InfoBlock(
                        "Как собрать подборку",
                        "1. Задайте класс, режим, объём и тему.\n2. Отметьте галочками нужные рассказы (до 3 штук).\n3. Настройте листы: число детей, шаг строк, подпись.\n4. Нажмите «Сохранить в PDF». Если вы меняете фильтры, выбор текстов сбрасывается — проверьте его перед экспортом."
                    )
                    InfoBlock(
                        "Разлиновка и место для письма",
                        "Выберите шаг строк 8 или 10 мм (при 8 мм помещается больше строк). Между основными линиями проходит пунктирная вспомогательная. Если текст длинный или ребёнок пишет крупно, включите опцию «Добавить чистый лист для письма»."
                    )
                    InfoBlock(
                        "Расчёт бумаги",
                        "Каждый ребёнок получает свои страницы для каждого выбранного рассказа. В конце комплекта добавляется один общий лист взрослого (он не размножается). Пример: 2 рассказа для 3 детей = 6 детских листов + 1 общий = 7 листов А4."
                    )
                    InfoBlock(
                        "Как использовать готовый лист",
                        "• Перед списыванием прочитайте текст целиком, уточните непонятные слова.\n• Ребёнок читает предложение, пишет его, сверяется с образцом.\n• Для диктанта взрослый сначала читает весь текст, затем диктует по частям, а в конце читает всё ещё раз для самопроверки."
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(4.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))
}
