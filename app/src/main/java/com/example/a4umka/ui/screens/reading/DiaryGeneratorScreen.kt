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
import com.example.a4umka.data.*
import com.example.a4umka.util.DiaryPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(DiaryConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    val previewPages = remember(config) {
        DiaryPdfDrawer.generatePreview(config, context)
    }
    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Читательский дневник") },
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
                        val result = PdfExporter.exportDiary(context, config)
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
                // ВИД СТРАНИЦЫ
                Text("Вид страницы книги:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = config.pageType == DiaryPageType.SCHOOL,
                        onClick = { config = config.copy(pageType = DiaryPageType.SCHOOL) },
                        label = { Text("Школьная") }
                    )
                    FilterChip(
                        selected = config.pageType == DiaryPageType.PICTURES,
                        onClick = { config = config.copy(pageType = DiaryPageType.PICTURES) },
                        label = { Text("В картинках") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ДАННЫЕ РЕБЁНКА
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = config.childName,
                        onValueChange = { config = config.copy(childName = it) },
                        label = { Text("Имя") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = config.childGrade,
                        onValueChange = { config = config.copy(childGrade = it) },
                        label = { Text("Класс") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.academicYear,
                    onValueChange = { config = config.copy(academicYear = it) },
                    label = { Text("Учебный год") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // НАСТРОЙКИ ЛИСТА
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var pagesExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = pagesExpanded, onExpandedChange = { pagesExpanded = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = "Стр: ${config.bookPagesCount}", onValueChange = {}, readOnly = true,
                            label = { Text("Страниц для книг", fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pagesExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = pagesExpanded, onDismissRequest = { pagesExpanded = false }) {
                            listOf(4, 8, 12, 16).forEach { count ->
                                DropdownMenuItem(text = { Text("$count") }, onClick = { config = config.copy(bookPagesCount = count); pagesExpanded = false })
                            }
                        }
                    }
                    
                    var rulingExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = rulingExpanded, onExpandedChange = { rulingExpanded = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = when(config.ruling) {
                                DiaryRuling.STANDARD -> "Обычная линейка"
                                DiaryRuling.WIDE -> "Широкая линейка"
                                DiaryRuling.GRID -> "Клетка"
                                DiaryRuling.NONE -> "Без линий"
                            }, onValueChange = {}, readOnly = true,
                            label = { Text("Разлиновка", fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rulingExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = rulingExpanded, onDismissRequest = { rulingExpanded = false }) {
                            listOf(
                                DiaryRuling.STANDARD to "Обычная",
                                DiaryRuling.WIDE to "Широкая (1 класс)",
                                DiaryRuling.GRID to "Клетка",
                                DiaryRuling.NONE to "Без линий"
                            ).forEach { (type, name) ->
                                DropdownMenuItem(text = { Text(name) }, onClick = { config = config.copy(ruling = type); rulingExpanded = false })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                var colorExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = colorExpanded, onExpandedChange = { colorExpanded = it }) {
                    OutlinedTextField(
                        value = when(config.color) {
                            DiaryColor.PINK -> "Розовый"
                            DiaryColor.GREEN -> "Зеленый"
                            DiaryColor.BLUE -> "Синий"
                            DiaryColor.ORANGE -> "Оранжевый"
                            DiaryColor.BLACK_WHITE -> "Черно-белый"
                        }, onValueChange = {}, readOnly = true,
                        label = { Text("Цвет листа") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colorExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = colorExpanded, onDismissRequest = { colorExpanded = false }) {
                        listOf(
                            DiaryColor.PINK to "Розовый",
                            DiaryColor.GREEN to "Зеленый",
                            DiaryColor.BLUE to "Синий",
                            DiaryColor.ORANGE to "Оранжевый",
                            DiaryColor.BLACK_WHITE to "Черно-белый"
                        ).forEach { (type, name) ->
                            DropdownMenuItem(text = { Text(name) }, onClick = { config = config.copy(color = type); colorExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // ЧТО ЕЩЕ ВЛОЖИТЬ
                Text("Что ещё вложить в дневник:", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.includeTitlePage, onCheckedChange = { config = config.copy(includeTitlePage = it) })
                    Text("Титульный лист", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.includeBookList, onCheckedChange = { config = config.copy(includeBookList = it) })
                    Text("Список прочитанного", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.includeSpeedTest, onCheckedChange = { config = config.copy(includeSpeedTest = it) })
                    Text("Лист техники чтения", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ЧТО ПЕЧАТАТЬ НА СТРАНИЦЕ
                if (config.pageType == DiaryPageType.SCHOOL) {
                    Text("Что печатать на странице книги:", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val fields = listOf(
                            "Автор" to config.showAuthor,
                            "Название" to config.showTitle,
                            "Жанр" to config.showGenre,
                            "Сколько страниц" to config.showPages,
                            "Начал/закончил" to config.showDates,
                            "Главные герои" to config.showCharacters,
                            "О чём книга" to config.showPlot,
                            "Что запомнилось" to config.showMemorable,
                            "Чему научила" to config.showLesson,
                            "Новые слова" to config.showNewWords,
                            "Рисунок" to config.showDrawing,
                            "Оценка" to config.showRating
                        )
                        fields.forEachIndexed { index, (name, checked) ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.widthIn(max = 160.dp)) {
                                Checkbox(checked = checked, onCheckedChange = { 
                                    config = when(index) {
                                        0 -> config.copy(showAuthor = it)
                                        1 -> config.copy(showTitle = it)
                                        2 -> config.copy(showGenre = it)
                                        3 -> config.copy(showPages = it)
                                        4 -> config.copy(showDates = it)
                                        5 -> config.copy(showCharacters = it)
                                        6 -> config.copy(showPlot = it)
                                        7 -> config.copy(showMemorable = it)
                                        8 -> config.copy(showLesson = it)
                                        9 -> config.copy(showNewWords = it)
                                        10 -> config.copy(showDrawing = it)
                                        11 -> config.copy(showRating = it)
                                        else -> config
                                    }
                                })
                                Text(name, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр листов A4:", style = MaterialTheme.typography.labelLarge)
                if (previewPages.size > 1) {
                    Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(DiaryPdfDrawer.PAGE_WIDTH / DiaryPdfDrawer.PAGE_HEIGHT)
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
                    Text("Читательский дневник", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Читательский дневник задают почти в каждой школе. Этот генератор устроен иначе — вы собираете дневник под своего ребёнка и распечатываете его целиком на A4: титульный лист, страницы для книг, список прочитанного и лист техники чтения.\n\n" +
                        "• Титульный лист — имя, класс, учебный год и рамка, чтобы нарисовать обложку любимой книги.\n\n" +
                        "• Школьная страница книги — до двенадцати полей на выбор, лишние снимаются галочкой, оставшиеся растягиваются на весь лист.\n\n" +
                        "• Страница в картинках — для тех, кто читает по слогам: рисунок на пол-листа, две строки крупно, оценка смайликом.\n\n" +
                        "• Список прочитанного — таблица на 8–18 строк: дата, автор и название, страниц, оценка, подпись взрослого.\n\n" +
                        "• Лист техники чтения — двенадцать замеров: дата, что читал, слов в минуту, отметка взрослого.\n\n" +
                        "Вы можете выбрать разлиновку (обычная, широкая для 1 класса, клетка, без линий) и цвет листа (розовый, зеленый, синий, оранжевый, ч/б).",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
