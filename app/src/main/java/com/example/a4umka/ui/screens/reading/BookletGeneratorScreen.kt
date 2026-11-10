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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Дебаунс для предотвращения лагов
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
                        BookletType.ABOUT_ME to "Книжка про меня",
                        BookletType.FAMILY to "Моя семья",
                        BookletType.GRANDMA to "Подарок бабушке",
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
                
                // КНОПКА ИНСТРУКЦИИ
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

        // ИНФО
        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Книжка-малышка", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Генератор собирает лист A4, который складывается в книжку-малышку на восемь страниц — без клея, скотча и степлера. Нужны только принтер и ножницы.\n\n" +
                        "• Своя сказка — рамка под рисунок и строки для текста на каждой странице;\n" +
                        "• Книжка про меня — шесть подсказок, по одной на страницу;\n" +
                        "• Загадки — готовый банк загадок, отгадка печатается на следующей странице;\n" +
                        "• Чистая — только рамки и строки, без подписей.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // ИНСТРУКЦИЯ ПО СКЛАДЫВАНИЮ
        if (showFoldInstructions) {
            ModalBottomSheet(onDismissRequest = { showFoldInstructions = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Как сложить книжку", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("1. Отрежьте нижнюю полоску с инструкцией.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("2. Сложите лист пополам поперёк, потом ещё пополам и ещё раз — выйдет восемь клеток.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("3. Разверните до первого сгиба и надрежьте по жирной линии в середине.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("4. Разверните лист, сложите вдоль и сдвиньте края друг к другу — надрез раскроется.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("5. Сложите книжку и прогладьте сгибы: страницы встанут по порядку сами.", style = MaterialTheme.typography.bodyLarge)
                    
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
    }
}
