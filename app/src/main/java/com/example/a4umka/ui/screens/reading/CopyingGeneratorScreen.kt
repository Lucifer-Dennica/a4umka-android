package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.CopyingConfig
import com.example.a4umka.data.CopyingData
import com.example.a4umka.data.CopyingMode
import com.example.a4umka.util.CopyingPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.launch

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

    val allStories = CopyingData.getStories(config.grade)
    val filteredStories = allStories.filter { story ->
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

    val previewPages = remember(config) {
        CopyingPdfDrawer.generatePreview(config, context)
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
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text("Класс:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 4).forEach { grade ->
                        FilterChip(
                            selected = config.grade == grade,
                            onClick = { 
                                config = config.copy(grade = grade, selectedStories = emptyList())
                            },
                            label = { Text(grade.toString()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск по названию или тексту") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Подходит: ${filteredStories.size}. Выбрано: ${config.selectedStories.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(filteredStories) { story ->
                val isSelected = config.selectedStories.contains(story)
                Card(
                    onClick = {
                        val newList = config.selectedStories.toMutableList()
                        if (isSelected) newList.remove(story) else newList.add(story)
                        config = config.copy(selectedStories = newList)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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

            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Как работаем:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = config.mode == CopyingMode.COPYING,
                        onClick = { config = config.copy(mode = CopyingMode.COPYING) },
                        label = { Text("Списываем") }
                    )
                    FilterChip(
                        selected = config.mode == CopyingMode.DICTATION,
                        onClick = { config = config.copy(mode = CopyingMode.DICTATION) },
                        label = { Text("Диктант") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Настройки листа:", style = MaterialTheme.typography.labelLarge)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Не разделять листы")
                        Text("Печатать тексты подряд для экономии бумаги", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Switch(
                        checked = config.combineOnOnePage,
                        onCheckedChange = { config = config.copy(combineOnOnePage = it) }
                    )
                }

                if (!config.combineOnOnePage) {
                    Spacer(modifier = Modifier.height(8.dp))
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

                Spacer(modifier = Modifier.height(8.dp))
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

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.signature,
                    onValueChange = { config = config.copy(signature = it) },
                    label = { Text("Подпись на листе (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Добавить чистый лист для письма", modifier = Modifier.weight(1f))
                    Switch(checked = config.addBlankPage, onCheckedChange = { config = config.copy(addBlankPage = it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Чёрно-белый вариант", modifier = Modifier.weight(1f))
                    Switch(checked = config.isBlackAndWhite, onCheckedChange = { config = config.copy(isBlackAndWhite = it) })
                }
                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("Размер шрифта: ${config.fontSize.toInt()}", fontSize = 14.sp)
                    Slider(
                        value = config.fontSize,
                        onValueChange = { config = config.copy(fontSize = it) },
                        valueRange = 12f..24f,
                        steps = 6
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр листов A4:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Стр. ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(CopyingPdfDrawer.PAGE_WIDTH / CopyingPdfDrawer.PAGE_HEIGHT)
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
                    Text("Тексты для списывания и диктантов", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Для письменного занятия нужен понятный образец, подходящий объём и достаточно места для своего почерка. Здесь собраны 100 авторских текстов для списывания и диктантов: по 25 для каждого класса с первого по четвёртый. Рассказы содержат от 22 до 84 слов. Выберите нужные, настройте разлиновку и подготовьте комплект на A4.\n\n" +
                        "Класс здесь служит ориентиром при поиске. Он не означает соответствия конкретной школьной программе. Сначала прочитайте рассказ целиком: понятны ли слова, знакома ли ситуация, удобно ли ребёнку удерживать такие предложения.\n\n" +
                        "Объём выбирается отдельно. В каждом классе есть варианты до 50 слов и больше 50 слов.\n\n" +
                        "Доступны две темы: «Природа и наблюдения» и «Дом и общие дела». Поиск работает по названию и самому тексту.\n\n" +
                        "Списывание: на детском листе напечатан весь выбранный рассказ. Ребёнок читает его и переносит в строки, возвращаясь к образцу.\n\n" +
                        "Диктант: на детском листе нет рассказа для подсматривания. Взрослый читает полный текст с общего листа, затем диктует предложения.\n\n" +
                        "На выбор есть шаг строк 8 или 10 мм. При 10 мм расстояние между основными линиями больше; при 8 мм на той же высоте помещается больше строк. Выбирайте по привычному размеру письма.\n\n" +
                        "Если ребёнок пишет крупно или выбран длинный рассказ, включите дополнительный лист. Для каждого выбранного текста каждому ребёнку добавится ещё одна разлинованная страница.\n\n" +
                        "Включите «Не разделять листы», если хотите напечатать несколько коротких текстов на одной странице подряд для экономии бумаги.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
