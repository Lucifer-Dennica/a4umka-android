package com.example.sovenok.ui.screens.reading

import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sovenok.data.*
import com.example.sovenok.ui.components.ZoomableImage
import com.example.sovenok.util.PdfExporter
import com.example.sovenok.util.RibbonPdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RibbonGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(RibbonConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = RibbonPdfDrawer.getPreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Лента букв") },
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
                        val result = PdfExporter.exportRibbon(context, config)
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
            // БЛОК: Что печатаем
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Что печатаем", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = config.mode == RibbonMode.DESK, onClick = { config = config.copy(mode = RibbonMode.DESK) })
                                Column {
                                    Text("Лента на парту")
                                    Text("вся лента на одном листе с подсказками", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = config.mode == RibbonMode.BOARD, onClick = { config = config.copy(mode = RibbonMode.BOARD) })
                                Column {
                                    Text("Лента на доску")
                                    Text("крупные буквы на 4 листах, повесить в ряд", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = config.mode == RibbonMode.COLORING, onClick = { config = config.copy(mode = RibbonMode.COLORING) })
                                Column {
                                    Text("Раскрась ленту букв")
                                    Text("задание + ответы", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = config.mode == RibbonMode.MISSING, onClick = { config = config.copy(mode = RibbonMode.MISSING) })
                                Column {
                                    Text("Впиши пропущенные буквы")
                                    Text("задание + ответы", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }

            // БЛОК: Буквы (регистр)
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Буквы", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = config.case == RibbonCase.BOTH, onClick = { config = config.copy(case = RibbonCase.BOTH) }, label = { Text("Аа") })
                            FilterChip(selected = config.case == RibbonCase.UPPERCASE, onClick = { config = config.copy(case = RibbonCase.UPPERCASE) }, label = { Text("А") })
                            FilterChip(selected = config.case == RibbonCase.LOWERCASE, onClick = { config = config.copy(case = RibbonCase.LOWERCASE) }, label = { Text("а") })
                        }
                    }
                }
            }

            // БЛОК: Опции (зависят от режима)
            if (config.mode == RibbonMode.MISSING || config.mode == RibbonMode.COLORING) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Ответы", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = config.showAnswers, onCheckedChange = { config = config.copy(showAnswers = it) })
                                Text("Добавить отдельный лист с ответами")
                            }

                            if (config.mode == RibbonMode.MISSING) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Сколько букв спрятать:", style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = config.missingCount == MissingCount.EIGHT, onClick = { config = config.copy(missingCount = MissingCount.EIGHT) }, label = { Text("8") })
                                    FilterChip(selected = config.missingCount == MissingCount.SIXTEEN, onClick = { config = config.copy(missingCount = MissingCount.SIXTEEN) }, label = { Text("16") })
                                    FilterChip(selected = config.missingCount == MissingCount.ALL, onClick = { config = config.copy(missingCount = MissingCount.ALL) }, label = { Text("Все") })
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = config.showHint, onCheckedChange = { config = config.copy(showHint = it) })
                                    Text("Подсказка: «Эти буквы потерялись»")
                                }
                            }
                        }
                    }
                }
            }

            // БЛОК: Фон листа и шрифт
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Фон листа", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = config.bgColor == RibbonBgColor.WHITE, onClick = { config = config.copy(bgColor = RibbonBgColor.WHITE) }, label = { Text("Белый") })
                            FilterChip(selected = config.bgColor == RibbonBgColor.PEACH, onClick = { config = config.copy(bgColor = RibbonBgColor.PEACH) }, label = { Text("Цветной") })
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Шрифт букв", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = config.font == RibbonFont.REGULAR, onClick = { config = config.copy(font = RibbonFont.REGULAR) }, label = { Text("Обычный") })
                            FilterChip(selected = config.font == RibbonFont.CURSIVE, onClick = { config = config.copy(font = RibbonFont.CURSIVE) }, label = { Text("Письменный") })
                        }
                    }
                }
            }

            // СОВЕТ
            item {
                Text(
                    text = "Все листы альбомные. При печати выберите масштаб 100%.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // КНОПКА: РЕШАТЬ НА ТЕЛЕФОНЕ
            item {
                Button(
                    onClick = onPlayInteractive,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Решать на телефоне")
                }
            }

            // ПРЕДПРОСМОТР
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр (4K):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(RibbonPdfDrawer.PAGE_WIDTH / RibbonPdfDrawer.PAGE_HEIGHT)
                    ) { page ->
                        if (page < previewPages.size) {
                            ZoomableImage(
                                bitmap = previewPages[page],
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ElevatedCard(
                        onClick = { showInfoSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Как заниматься по ленте",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Советы для родителей и педагогов",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
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
                    Text("Лента букв — распечатать для начальной школы", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Генератор печатает ленту букв и звуков, как в букваре для 1 класса: все 33 буквы в одной таблице, разложенные по звукам. Гласные — на красном, согласные — звонкие сверху, глухие снизу, всегда твёрдые синим, всегда мягкие зелёным.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Как устроена лента", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Гласные — на красном поле. Верхний ряд — А, О, У, Ы, Э: после них согласные звучат твёрдо. Нижний ряд — Я, Ё, Ю, И, Е: после них согласные звучат мягко.\n\n" +
                        "Звонкие и глухие согласные. Над согласными стоит колокольчик 🔔 — это звонкие, внизу наушники 🎧 — глухие. Пары стоят друг над другом: Б и П, В и Ф, Г и К, Д и Т, Ж и Ш, З и С.\n\n" +
                        "Непарные согласные: звонкие Л, М, Н, Р, Й — только в верхнем ряду, глухие Х, Ц, Ч, Щ — только в нижнем.\n\n" +
                        "Твёрдые и мягкие. Ж, Ш, Ц — всегда твёрдые, клетка синяя. Й, Ч, Щ — всегда мягкие, клетка зелёная. Остальные согласные бывают и твёрдыми, и мягкими — клетка сине-зелёная.\n\n" +
                        "Ь и Ъ звуков не обозначают — белая клетка.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Виды листов", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Лента на парту — вся лента на одном альбомном листе A4 с подсказками: что значит каждый цвет и значок. Удобно положить на стол или вклеить в тетрадь.\n\n" +
                        "• Лента на доску — крупные буквы на четырёх листах: гласные, звонкие непарные, пары звонкий — глухой, глухие непарные с Ь и Ъ. Листы вешаются в ряд и складываются в длинную ленту для класса.\n\n" +
                        "• Раскрась ленту букв — контур ленты. Ребёнок сам раскрашивает клетки по правилам и так запоминает, какая буква к какой группе относится. На втором листе — ответ.\n\n" +
                        "• Впиши пропущенные буквы — цветная лента, где спрятаны 8, 16 или все 33 буквы. Цвет клетки и соседи подсказывают, какая буква должна стоять. Можно показать потерявшиеся буквы под лентой. Ответы — на втором листе.\n\n" +
                        "Буквы можно печатать парой «Аа», только заглавными или только строчными.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Для какого возраста", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "5–6 лет — лента на стене у стола. Ребёнок привыкает, что гласные «красные», и сам начинает показывать их в словах.\n\n" +
                        "1 класс — главный помощник при звуковом анализе: ученик ищет букву на ленте и сразу видит, звонкий звук или глухой, твёрдый или мягкий.\n\n" +
                        "2–3 класс — задания «Раскрась» и «Впиши» для повторения парных согласных и правил о твёрдости и мягкости.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Советы родителям и учителям", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Повесьте ленту там, где ребёнок делает уроки, — к ней будут обращаться чаще, чем к учебнику.\n\n" +
                        "• Начните с гласных: найдите на ленте все красные клетки и вместе их «спойте».\n\n" +
                        "• Звонкие и глухие проверяйте ладонью на горле: на звонких горло дрожит, на глухих — нет.\n\n" +
                        "• «Впиши пропущенные буквы» начинайте с 8 букв и подсказкой, потом переходите к 16 и ко всей ленте.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
