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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sovenok.data.*
import com.example.sovenok.ui.components.ZoomableImage
import com.example.sovenok.util.DiaryPdfDrawer
import com.example.sovenok.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var config by remember { mutableStateOf(DiaryConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    val expandedBooks = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(config) {
        delay(300)
        previewPages = DiaryPdfDrawer.generatePreview(config, context)
    }
    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    fun updateBook(index: Int, update: (DiaryBook) -> DiaryBook) {
        val newList = config.books.toMutableList()
        newList[index] = update(newList[index])
        config = config.copy(books = newList)
    }

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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // БЛОК: Вид страницы и данные ученика
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Вид страницы книги", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = config.pageType == DiaryPageType.SCHOOL, onClick = { config = config.copy(pageType = DiaryPageType.SCHOOL) }, label = { Text("Школьная") })
                            FilterChip(selected = config.pageType == DiaryPageType.PICTURES, onClick = { config = config.copy(pageType = DiaryPageType.PICTURES) }, label = { Text("В картинках") })
                        }
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
                        OutlinedTextField(
                            value = config.academicYear,
                            onValueChange = { config = config.copy(academicYear = it) },
                            label = { Text("Учебный год") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // БЛОК: Страницы и оформление
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Страницы и оформление", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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
                                    (1..16).toList().forEach { count ->
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
                                        DiaryRuling.NONE to "Без линий"
                                    ).forEach { (type, name) ->
                                        DropdownMenuItem(text = { Text(name) }, onClick = { config = config.copy(ruling = type); rulingExpanded = false })
                                    }
                                }
                            }
                        }
                        var colorExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = colorExpanded, onExpandedChange = { colorExpanded = it }) {
                            OutlinedTextField(
                                value = when(config.color) {
                                    DiaryColor.GREEN -> "Зеленый"
                                    DiaryColor.ORANGE -> "Оранжевый"
                                    DiaryColor.BLUE -> "Синий"
                                    DiaryColor.PINK -> "Розовый"
                                    DiaryColor.BLACK_WHITE -> "Черно-белый"
                                }, onValueChange = {}, readOnly = true,
                                label = { Text("Цвет листа") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colorExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = colorExpanded, onDismissRequest = { colorExpanded = false }) {
                                listOf(
                                    DiaryColor.GREEN to "Зеленый",
                                    DiaryColor.ORANGE to "Оранжевый",
                                    DiaryColor.BLUE to "Синий",
                                    DiaryColor.PINK to "Розовый",
                                    DiaryColor.BLACK_WHITE to "Черно-белый"
                                ).forEach { (type, name) ->
                                    DropdownMenuItem(text = { Text(name) }, onClick = { config = config.copy(color = type); colorExpanded = false })
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var listExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = listExpanded, onExpandedChange = { listExpanded = it }, modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = "Строк: ${config.listRowsCount}", onValueChange = {}, readOnly = true,
                                    label = { Text("Строк в списке", fontSize = 12.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = listExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = listExpanded, onDismissRequest = { listExpanded = false }) {
                                    (8..18).toList().forEach { count ->
                                        DropdownMenuItem(text = { Text("$count") }, onClick = { config = config.copy(listRowsCount = count); listExpanded = false })
                                    }
                                }
                            }
                        }
                        Text("Шрифт заголовков", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = config.titleFont == DiaryFont.SYSTEM, onClick = { config = config.copy(titleFont = DiaryFont.SYSTEM) }, label = { Text("Обычный") })
                            FilterChip(selected = config.titleFont == DiaryFont.CAVEAT, onClick = { config = config.copy(titleFont = DiaryFont.CAVEAT) }, label = { Text("Рукописный") })
                        }
                        Text("Шрифт подписей полей", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = config.labelFont == DiaryFont.SYSTEM, onClick = { config = config.copy(labelFont = DiaryFont.SYSTEM) }, label = { Text("Обычный") })
                            FilterChip(selected = config.labelFont == DiaryFont.CAVEAT, onClick = { config = config.copy(labelFont = DiaryFont.CAVEAT) }, label = { Text("Рукописный") })
                        }
                        Text("Шрифт заполненных значений", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = config.valueFont == DiaryFont.SYSTEM, onClick = { config = config.copy(valueFont = DiaryFont.SYSTEM) }, label = { Text("Обычный") })
                            FilterChip(selected = config.valueFont == DiaryFont.CAVEAT, onClick = { config = config.copy(valueFont = DiaryFont.CAVEAT) }, label = { Text("Рукописный") })
                        }
                    }
                }
            }

            // БЛОК: Что ещё вложить в дневник
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Что ещё вложить в дневник", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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
                    }
                }
            }

            // БЛОК: Что печатать на странице книги (ТОЛЬКО для школьного типа)
            if (config.pageType == DiaryPageType.SCHOOL) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Что печатать на странице книги", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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
                    }
                }
            }

            // БЛОК: Книги (для обоих режимов)
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Книги", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                config = config.copy(books = List(16) { DiaryBook() })
                                expandedBooks.clear()
                                scope.launch { snackbarHostState.showSnackbar("Все книги очищены") }
                            }) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Очистить")
                            }
                        }
                        Text(
                            "Можно ничего не заполнять — тогда напечатается чистый бланк. А можно вписать книги, которые уже прочитаны: они попадут и на страницы, и в список прочитанного.",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        for (i in 0 until config.bookPagesCount) {
                            val book = config.books[i]
                            val expanded = expandedBooks[i] ?: false
                            OutlinedCard(
                                onClick = { expandedBooks[i] = !expanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            modifier = Modifier.rotate(if (expanded) 90f else 0f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Книга ${i + 1}", fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(8.dp))
                                        Text(book.statusText(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                    if (expanded) {
                                        Spacer(Modifier.height(12.dp))
                                        BookForm(
                                            book = book,
                                            config = config,
                                            isPicturesMode = (config.pageType == DiaryPageType.PICTURES),
                                            onUpdate = { newBook -> updateBook(i) { newBook } }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ПРЕДПРОСМОТР
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр (4K):", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(DiaryPdfDrawer.PAGE_WIDTH / DiaryPdfDrawer.PAGE_HEIGHT)
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
                    Text("Читательский дневник — генератор для печати", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Читательский дневник задают почти в каждой школе: летом — обязательно, в течение года — как привычку. Готовые бланки в интернете есть, но они чужие: то полей слишком много для первоклассника, то линейка мелкая, то на странице спрашивают «главную мысль произведения» у ребёнка, который только-только читает сам. Это приложение устроено иначе — вы собираете дневник под своего ребёнка и распечатываете его целиком на A4: титульный лист, страницы для книг, список прочитанного и лист техники чтения. Всё бесплатно и без регистрации.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Что это такое и в чём польза", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Читательский дневник — это тетрадь, где после каждой книги ребёнок записывает: что читал, про что книга, кто главные герои, что запомнилось. Смысл не в отчётности, а в том, что прочитанное проговаривается ещё раз и остаётся. Пересказать своими словами и записать хотя бы строчку — это и есть работа с текстом, ради которой дневник и придумали.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• понимание прочитанного — ребёнок вспоминает и формулирует сам", style = MaterialTheme.typography.bodySmall)
                    Text("• связная речь: назвать героев, сказать, про что книга", style = MaterialTheme.typography.bodySmall)
                    Text("• навык письма и аккуратность — строки на листе разлинованы", style = MaterialTheme.typography.bodySmall)
                    Text("• привычка читать регулярно: заполненные страницы видно, и это радует", style = MaterialTheme.typography.bodySmall)
                    Text("• внимание к деталям — что именно запомнилось больше всего", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Для какого возраста", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("5 лет. Читает взрослый, ребёнок отвечает и рисует. Берите вид «Страница в картинках»: большая рамка для рисунка, две крупные строки и оценка — три лица, одно надо обвести. Писать почти не нужно, а дневник уже ведётся.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("6–7 лет и первый класс. Тот же вид, но добавьте название книги и широкую линейку. Ребёнок пишет пару слов сам, остальное — рисунком. Здесь важно не требовать много: две строки, написанные самостоятельно, ценнее страницы под диктовку.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2–4 класс. Школьная страница со всеми полями: автор, название, жанр, сколько страниц, даты, главные герои, о чём книга, что запомнилось, чему научила, новые слова. Линейку ставьте обычную, число страниц — по количеству книг в списке на лето.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Как пользоваться приложением", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Всё делается на одном экране. Выберите вид страницы книги, укажите, сколько таких страниц нужно (от 1 до 16), отметьте, что ещё вложить в дневник, — и нажмите «Сохранить в PDF». Комплект откроется одним документом: печатайте целиком или сохраняйте файл. Ничего не заполняя, вы получите чистый бланк — ребёнок впишет всё от руки сам.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Из чего собирается комплект", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Титульный лист — имя, класс, учебный год и рамка, чтобы нарисовать обложку любимой книги", style = MaterialTheme.typography.bodySmall)
                    Text("• Школьная страница книги — до двенадцати полей на выбор, лишние снимаются галочкой, оставшиеся растягиваются на весь лист", style = MaterialTheme.typography.bodySmall)
                    Text("• Страница в картинках — для тех, кто читает по слогам: рисунок на пол-листа, строки крупно, оценка смайликом", style = MaterialTheme.typography.bodySmall)
                    Text("• Список прочитанного — таблица на 8–18 строк: дата, автор и название, страниц, оценка, подпись взрослого", style = MaterialTheme.typography.bodySmall)
                    Text("• Лист техники чтения — двенадцать замеров: дата, что читал, слов в минуту, отметка взрослого", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Что можно настроить", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• поля страницы — печатать только то, что нужно вашему ребёнку", style = MaterialTheme.typography.bodySmall)
                    Text("• разлиновку — обычная линейка, широкая для первого класса или без линий", style = MaterialTheme.typography.bodySmall)
                    Text("• цвет листа — четыре цветных варианта и чёрно-белый для экономии тонера", style = MaterialTheme.typography.bodySmall)
                    Text("• количество страниц — от одного листа до дневника на шестнадцать книг", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Какой навык развивает", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Главный — умение говорить о прочитанном. Ребёнок, который может назвать героев и пересказать сюжет в трёх фразах, понял книгу; тот, кто прочитал вслух без запинки, но не может сказать про что, — ещё нет. Дневник каждый раз задаёт одни и те же вопросы, и постепенно ребёнок начинает задавать их себе сам, ещё пока читает. Это и есть осознанное чтение.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Подготовка к школе", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("В первом классе читательский дневник заводят почти сразу, и для ребёнка это первая работа «в свободной форме»: никто не подсказывает, что писать. Если дневник в картинках вёлся ещё до школы, задание перестаёт быть страшным — форма уже знакома, меняется только объём письма. Заодно тренируется то, что в школе проверяют отдельно: техника чтения. Лист замеров показывает рост в цифрах, и это мотивирует лучше уговоров.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Советы родителям", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• заполняйте страницу сразу после книги, пока впечатление свежее", style = MaterialTheme.typography.bodySmall)
                    Text("• не требуйте всех полей — две-три строки от ребёнка лучше, чем целая страница под вашу диктовку", style = MaterialTheme.typography.bodySmall)
                    Text("• рисунок — полноправная часть дневника, а не украшение: он показывает, что запомнилось", style = MaterialTheme.typography.bodySmall)
                    Text("• для дошкольника пишите вы, а ребёнок диктует — это тоже ведение дневника", style = MaterialTheme.typography.bodySmall)
                    Text("• печатайте комплект сразу на несколько книг и сшивайте скрепкой: дневник, который лежит целиком, заполняется охотнее", style = MaterialTheme.typography.bodySmall)
                    Text("• замеры техники чтения делайте раз в неделю, не чаще — иначе роста не видно и становится обидно", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Где использовать", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• летом — по списку литературы, заданному на каникулы", style = MaterialTheme.typography.bodySmall)
                    Text("• в течение учебного года как домашнюю привычку", style = MaterialTheme.typography.bodySmall)
                    Text("• в детском саду и подготовительной группе — вид в картинках", style = MaterialTheme.typography.bodySmall)
                    Text("• в дороге и на даче: комплект листов не требует ни интернета, ни устройства", style = MaterialTheme.typography.bodySmall)
                    Text("• в библиотеке и кружке чтения — как общий бланк на группу", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BookForm(
    book: DiaryBook,
    config: DiaryConfig,
    isPicturesMode: Boolean,
    onUpdate: (DiaryBook) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isPicturesMode) {
            // Режим "В картинках" — только два поля, без оценки
            OutlinedTextField(
                value = book.title,
                onValueChange = { onUpdate(book.copy(title = it)) },
                label = { Text("Книга называется") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = book.plot,
                onValueChange = { onUpdate(book.copy(plot = it)) },
                label = { Text("Про что книга") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Режим "Школьная" — полная форма по галочкам
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (config.showAuthor) {
                        OutlinedTextField(value = book.author, onValueChange = { onUpdate(book.copy(author = it)) }, label = { Text("Автор") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    if (config.showGenre) {
                        OutlinedTextField(value = book.genre, onValueChange = { onUpdate(book.copy(genre = it)) }, label = { Text("Жанр") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    if (config.showDates) {
                        OutlinedTextField(value = book.dates, onValueChange = { onUpdate(book.copy(dates = it)) }, label = { Text("Начал(а) / закончил(а)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    if (config.showCharacters) {
                        OutlinedTextField(value = book.characters, onValueChange = { onUpdate(book.copy(characters = it)) }, label = { Text("Главные герои") }, modifier = Modifier.fillMaxWidth())
                    }
                    if (config.showPlot) {
                        OutlinedTextField(value = book.plot, onValueChange = { onUpdate(book.copy(plot = it)) }, label = { Text("О чём эта книга") }, modifier = Modifier.fillMaxWidth())
                    }
                    if (config.showLesson) {
                        OutlinedTextField(value = book.lesson, onValueChange = { onUpdate(book.copy(lesson = it)) }, label = { Text("Чему научила эта книга") }, modifier = Modifier.fillMaxWidth())
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (config.showTitle) {
                        OutlinedTextField(value = book.title, onValueChange = { onUpdate(book.copy(title = it)) }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    if (config.showPages) {
                        OutlinedTextField(value = book.pages, onValueChange = { onUpdate(book.copy(pages = it)) }, label = { Text("Сколько страниц") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    if (config.showMemorable) {
                        OutlinedTextField(value = book.memorable, onValueChange = { onUpdate(book.copy(memorable = it)) }, label = { Text("Что запомнилось больше всего") }, modifier = Modifier.fillMaxWidth())
                    }
                    if (config.showNewWords) {
                        OutlinedTextField(value = book.newWords, onValueChange = { onUpdate(book.copy(newWords = it)) }, label = { Text("Новые слова") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                }
            }
            if (config.showRating) {
                var ratingExpanded by remember { mutableStateOf(false) }
                val ratingLabels = listOf("не ставим", "1 из 5", "2 из 5", "3 из 5", "4 из 5", "5 из 5")
                ExposedDropdownMenuBox(expanded = ratingExpanded, onExpandedChange = { ratingExpanded = it }) {
                    OutlinedTextField(
                        value = ratingLabels[book.rating],
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Оценка (звёзд из пяти)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ratingExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = ratingExpanded, onDismissRequest = { ratingExpanded = false }) {
                        ratingLabels.forEachIndexed { index, label ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { onUpdate(book.copy(rating = index)); ratingExpanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoBlock(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(4.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))
}
