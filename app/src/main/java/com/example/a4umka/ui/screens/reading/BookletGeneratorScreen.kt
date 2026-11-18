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
import com.example.a4umka.ui.components.ZoomableImage
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
                            DropdownMenuItem(text = { Text(name) }, onClick = { config = config.copy(character = c); charExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр листа A4 (альбомный):", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(BookletPdfDrawer.PAGE_WIDTH / BookletPdfDrawer.PAGE_HEIGHT)
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

        if (showFoldInstructions) {
            ModalBottomSheet(onDismissRequest = { showFoldInstructions = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Как сложить книжку", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("1. Отрежьте нижнюю полоску с инструкцией.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.booklet_step1),
                        contentDescription = "Шаг 1",
                        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { zoomImageRes = R.drawable.booklet_step1 },
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("2. Сложите лист пополам поперёк, потом ещё пополам и ещё раз — выйдет восемь клеток.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.booklet_step2),
                        contentDescription = "Шаг 2",
                        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { zoomImageRes = R.drawable.booklet_step2 },
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("3. Разверните до первого сгиба и надрежьте по жирной линии в середине.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.booklet_step3),
                        contentDescription = "Шаг 3",
                        modifier = Modifier.fillMaxWidth().height(150.dp).clickable { zoomImageRes = R.drawable.booklet_step3 },
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))

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

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Книжка-малышка своими руками", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Генератор собирает лист A4, который складывается в книжку-малышку на восемь страниц — без клея, скотча и степлера. Нужны только принтер и ножницы.\n\n" +
                        "Внутри может быть своя сказка, книжка про себя или загадки — вид выбираете вы. На обложке печатаются название и имя автора, так что книжка сразу получается именной.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
