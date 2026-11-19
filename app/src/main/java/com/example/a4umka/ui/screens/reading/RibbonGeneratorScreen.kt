package com.example.a4umka.ui.screens.reading

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
import com.example.a4umka.data.*
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.RibbonPdfDrawer
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
                    IconButton(onClick = onPlayInteractive) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Играть")
                    }
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

            // БЛОК: Фон листа
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
                    Text("Лента букв", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Лента букв помогает запомнить, какие буквы обозначают гласные и согласные звуки, а также звонкие и глухие согласные.\n\n" +
                        "• Красные клетки — гласные: А О У Ы Э (после твёрдых), Я Ё Ю И Е (после мягких).\n" +
                        "• Синие клетки — всегда твёрдые согласные: Ж Ш Ц.\n" +
                        "• Зелёные клетки — всегда мягкие согласные: Й Ч Щ.\n" +
                        "• Сине-зелёные клетки — согласные, которые могут быть и твёрдыми, и мягкими.\n" +
                        "• Ъ и Ь не обозначают звуков — они вынесены в конец.\n\n" +
                        "Виды листов:\n" +
                        "• Лента на парту — вся лента на одном листе, чтобы положить перед собой.\n" +
                        "• Лента на доску — 4 листа с крупными буквами, чтобы повесить в ряд.\n" +
                        "• Раскрась ленту — ребёнок сам раскрашивает клетки по правилам.\n" +
                        "• Впиши пропущенные буквы — клетки пустые, нужно вписать буквы по подсказке цвета и соседей.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
