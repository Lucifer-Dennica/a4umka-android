package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.a4umka.R
import com.example.a4umka.data.*
import com.example.a4umka.util.BookletPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookletGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(BookletConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showFoldInstructions by remember { mutableStateOf(false) }
    
    // Состояние для увеличения картинки
    var zoomImageRes by remember { mutableStateOf<Int?>(null) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = BookletPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Книжка-малышка") },
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
                        val result = PdfExporter.exportBooklet(context, config)
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
                Text("Что внутри:", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        BookletType.STORY to "Своя сказка",
                        BookletType.ABOUT_ME to "Про меня",
                        BookletType.FAMILY to "Моя семья",
                        BookletType.GRANDMA to "Бабушке",
                        BookletType.RIDDLES to "Загадки",
                        BookletType.BLANK to "Чистая"
                    ).forEach { (type, name) ->
                        FilterChip(
                            selected = config.type == type,
                            onClick = { config = config.copy(type = type) },
                            label = { Text(name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = config.title,
                        onValueChange = { config = config.copy(title = it) },
                        label = { Text("Название на обложке") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = config.author,
                        onValueChange = { config = config.copy(author = it) },
                        label = { Text("Автор") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                var charExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = charExpanded, onExpandedChange = { charExpanded = it }) {
                    OutlinedTextField(
                        value = when(config.character) {
                            BookletCharacter.NONE -> "Без героя"
                            BookletCharacter.BOY -> "Мальчик"
                            BookletCharacter.GIRL -> "Девочка"
                        }, onValueChange = {}, readOnly = true,
                        label = { Text("Герой обложки") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = charExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = charExpanded, onDismissRequest = { charExpanded = false }) {
                        listOf(
                            BookletCharacter.NONE to "Без героя",
                            BookletCharacter.BOY to "Мальчик",
                            BookletCharacter.GIRL to "Девочка"
                        ).forEach { (c, name) ->
                            DropdownMenuItem(text = { Text(name) }, onClick = {
                                config = config.copy(character = c)
                                charExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Оформление:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.design == BookletDesign.LEAVES, onClick = { config = config.copy(design = BookletDesign.LEAVES) }, label = { Text("Листики") })
                    FilterChip(selected = config.design == BookletDesign.STARS, onClick = { config = config.copy(design = BookletDesign.STARS) }, label = { Text("Звёзды") })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Разлиновка страниц:", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.ruling == BookletRuling.LINE_8MM, onClick = { config = config.copy(ruling = BookletRuling.LINE_8MM) }, label = { Text("Линия 8 мм") })
                    FilterChip(selected = config.ruling == BookletRuling.WIDE_10MM, onClick = { config = config.copy(ruling = BookletRuling.WIDE_10MM) }, label = { Text("Широкая 10 мм") })
                    FilterChip(selected = config.ruling == BookletRuling.GRID_5MM, onClick = { config = config.copy(ruling = BookletRuling.GRID_5MM) }, label = { Text("Клетка 5 мм") })
                    FilterChip(selected = config.ruling == BookletRuling.NONE, onClick = { config = config.copy(ruling = BookletRuling.NONE) }, label = { Text("Без линий") })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showFoldLines, onCheckedChange = { config = config.copy(showFoldLines = it) })
                    Column {
                        Text("Линии сгиба и надреза", fontWeight = FontWeight.Bold)
                        Text("Подсказывают, где складывать и резать", fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.isBlackAndWhite, onCheckedChange = { config = config.copy(isBlackAndWhite = it) })
                    Column {
                        Text("Чёрно-белый режим", fontWeight = FontWeight.Bold)
                        Text("Экономит картридж", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { showFoldInstructions = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Как сложить книжку?")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр листа A4 (альбомный):", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(BookletPdfDrawer.PAGE_WIDTH / BookletPdfDrawer.PAGE_HEIGHT)
                    ) { page ->
                        if (page < previewPages.size) {
                            Image(
                                bitmap = previewPages[page].asImageBitmap(),
                                contentDescription = "Предпросмотр",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // МОДАЛЬНОЕ ОКНО С ИНСТРУКЦИЕЙ
        if (showFoldInstructions) {
            ModalBottomSheet(onDismissRequest = { showFoldInstructions = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Как сложить книжку", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Текст + Картинка 1
                    Text("1. Отрежьте нижнюю полоску с инструкцией.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.booklet_step1),
                        contentDescription = "Шаг 1",
                        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { zoomImageRes = R.drawable.booklet_step1 },
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Текст + Картинка 2
                    Text("2. Сложите лист пополам поперёк, потом ещё пополам и ещё раз — выйдет восемь клеток.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.booklet_step2),
                        contentDescription = "Шаг 2",
                        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { zoomImageRes = R.drawable.booklet_step2 },
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Текст + Картинка 3
                    Text("3. Разверните до первого сгиба и надрежьте по жирной линии в середине.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.booklet_step3),
                        contentDescription = "Шаг 3",
                        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { zoomImageRes = R.drawable.booklet_step3 },
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Текст + Картинка 4
                    Text("4. Разверните лист, сложите вдоль и сдвиньте края друг к другу — надрез раскроется. Сложите книжку и прогладьте сгибы.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.booklet_step4),
                        contentDescription = "Шаг 4",
                        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { zoomImageRes = R.drawable.booklet_step4 },
                        contentScale = ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "💡 Совет: страницы нижнего ряда напечатаны перевёрнутыми — так и задумано! После складывания они встанут правильной стороной.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // ДИАЛОГ ДЛЯ УВЕЛИЧЕНИЯ КАРТИНКИ С ЗУМОМ
        if (zoomImageRes != null) {
            Dialog(onDismissRequest = { zoomImageRes = null }) {
                Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                        
                        var scale by remember { mutableFloatStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 5f)
                                        if (scale > 1f) {
                                            offset += pan
                                        } else {
                                            offset = Offset.Zero
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = zoomImageRes!!),
                                contentDescription = "Увеличенная инструкция",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Кнопка закрытия
                        IconButton(
                            onClick = { zoomImageRes = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Text("✕", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        // ИНФО (С ПОЛНЫМ ТЕКСТОМ С САЙТА)
        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Книжка-малышка своими руками", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Генератор собирает лист A4, который складывается в книжку-малышку на восемь страниц — без клея, скотча и степлера. Нужны только принтер и ножницы: сделать сгибы по разметке, надрезать по отмеченной линии, сложить ещё раз — и в руках настоящая маленькая книжка с обложкой, страницами и словом «Конец» в конце.\n\n" +
                        "Внутри может быть своя сказка, книжка про себя или загадки — вид выбираете вы. На обложке печатаются название и имя автора, так что книжка сразу получается именной.\n\n" +
                        "Зачем делать книжку с ребёнком:\n" +
                        "• связная речь: придумать историю и уложить её в шесть страниц;\n" +
                        "• мелкая моторика — складывание по сгибу и работа ножницами;\n" +
                        "• понимание, как устроена книга: обложка, страницы, порядок, конец;\n" +
                        "• рисование по замыслу, а не по образцу;\n" +
                        "• письмо: подписать страницу своими словами — законный повод писать.\n\n" +
                        "Для какого возраста:\n" +
                        "• 5 лет. Книжка с рисунками: ребёнок рисует, взрослый записывает под диктовку. Складывает лист тоже взрослый — сгибы получаются аккуратнее.\n" +
                        "• 6 лет. «Книжка про меня»: на каждой странице подсказка — «Меня зовут», «Моя семья», «Мой день». Отвечать на такие вопросы можно сразу, не задумываясь.\n" +
                        "• 7 лет и старше. Своя сказка от начала до конца и книжка с загадками — её приятно давать разгадывать другим: загадка на странице, отгадка на следующей.\n\n" +
                        "Как пользоваться генератором:\n" +
                        "Выберите, что внутри, впишите название и имя автора, задайте разлиновку. Нажмите «Сохранить в PDF» и печатайте альбомно, масштаб 100%, без «вписать в страницу».\n\n" +
                        "Что можно настроить:\n" +
                        "• Своя сказка — рамка под рисунок и строки для текста;\n" +
                        "• Книжка про меня — шесть подсказок;\n" +
                        "• Моя семья — страницы с подсказками про близких;\n" +
                        "• Подарок бабушке — рисунки, воспоминания и пожелания;\n" +
                        "• Загадки — готовый банк загадок;\n" +
                        "• Чистая — только рамки и строки, без подписей;\n" +
                        "• разлиновка: линия 8 мм, широкая 10 мм, клетка 5 мм или без линий;\n" +
                        "• чёрно-белый режим и линии сгиба с надрезом.\n\n" +
                        "Как сложить:\n" +
                        "1. отрежьте нижнюю полоску с инструкцией;\n" +
                        "2. сложите лист пополам поперёк, потом ещё пополам и ещё раз — выйдет восемь клеток;\n" +
                        "3. разверните до первого сгиба и надрежьте по жирной линии в середине;\n" +
                        "4. разверните лист, сложите вдоль и сдвиньте края друг к другу — надрез раскроется;\n" +
                        "5. сложите книжку и прогладьте сгибы: страницы встанут по порядку сами.\n\n" +
                        "Какой навык развивает:\n" +
                        "Главное здесь — доведение замысла до конца. Придумать начало легко, а вот дотянуть историю до восьмой страницы, где написано «Конец», уже работа. Заодно ребёнок на собственных руках понимает, почему в книге страницы идут по порядку.\n\n" +
                        "Советы родителям:\n" +
                        "• первую книжку сложите сами, пока ребёнок смотрит;\n" +
                        "• не исправляйте орфографию в его книжке: она авторская;\n" +
                        "• печатайте на обычной бумаге — плотная плохо держит тройной сгиб;\n" +
                        "• делайте серию: «книжка про лето», «книжка про кота»;\n" +
                        "• если книжка делается в сад, впишите имя автора на обложке сразу.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
