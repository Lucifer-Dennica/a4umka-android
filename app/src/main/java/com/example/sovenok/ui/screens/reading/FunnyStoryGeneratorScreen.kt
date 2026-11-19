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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import com.example.sovenok.util.FunnyStoryPdfDrawer
import com.example.sovenok.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunnyStoryGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: (FunnyStory) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(FunnyStoryConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(config.theme, config.count) {
        if (config.currentStories.isEmpty() || config.currentStories.size != config.count) {
            config = config.copy(currentStories = FunnyStoryData.getStoriesForTheme(config.theme, config.count))
        }
    }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = FunnyStoryPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Весёлые истории") },
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
                        val result = PdfExporter.exportFunnyStory(context, config)
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
            // БЛОК: Тема и количество
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Тема и количество", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

                        var themeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = themeExpanded, onExpandedChange = { themeExpanded = it }) {
                            OutlinedTextField(
                                value = when (config.theme) {
                                    FunnyStoryTheme.ALL -> "Все темы вперемешку"
                                    FunnyStoryTheme.FAIRY_TALE -> "Сказка наизнанку"
                                    FunnyStoryTheme.ABOUT_ME -> "Про меня"
                                    FunnyStoryTheme.ADVENTURE -> "Приключения"
                                    FunnyStoryTheme.ANIMALS -> "Животные и зоопарк"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Тема историй") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                                FunnyStoryTheme.entries.forEach { theme ->
                                    DropdownMenuItem(
                                        text = { Text(when (theme) { FunnyStoryTheme.ALL -> "Все темы"; FunnyStoryTheme.FAIRY_TALE -> "Сказка наизнанку"; FunnyStoryTheme.ABOUT_ME -> "Про меня"; FunnyStoryTheme.ADVENTURE -> "Приключения"; FunnyStoryTheme.ANIMALS -> "Животные" }) },
                                        onClick = {
                                            config = config.copy(theme = theme, currentStories = FunnyStoryData.getStoriesForTheme(theme, config.count))
                                            themeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        var countExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = countExpanded, onExpandedChange = { countExpanded = it }) {
                            OutlinedTextField(
                                value = "Историй: ${config.count}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Количество") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = countExpanded, onDismissRequest = { countExpanded = false }) {
                                (1..8).forEach { num ->
                                    DropdownMenuItem(
                                        text = { Text("$num") },
                                        onClick = {
                                            config = config.copy(count = num, currentStories = FunnyStoryData.getStoriesForTheme(config.theme, num))
                                            countExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // БЛОК: Кто вписывает слова
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Кто вписывает слова", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = config.writer == FunnyStoryWriter.ADULT, onClick = { config = config.copy(writer = FunnyStoryWriter.ADULT) })
                            Text("Взрослый")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = config.writer == FunnyStoryWriter.CHILD, onClick = { config = config.copy(writer = FunnyStoryWriter.CHILD) })
                            Text("Ребёнок сам")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.isBlackAndWhite, onCheckedChange = { config = config.copy(isBlackAndWhite = it) })
                            Text("Чёрно-белая печать")
                        }
                    }
                }
            }

            // БЛОК: Действия
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Действия", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (config.currentStories.isNotEmpty()) {
                                        onPlayInteractive(config.currentStories.first())
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Играть")
                            }
                            OutlinedButton(
                                onClick = {
                                    config = config.copy(currentStories = FunnyStoryData.getStoriesForTheme(config.theme, config.count))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Другие")
                            }
                        }
                    }
                }
            }

            // БЛОК: Что попадёт на листы
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Что попадёт на листы", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        config.currentStories.forEachIndexed { i, story ->
                            Text("${i + 1}. ${story.title} — вопросов: ${story.questions.size}", fontSize = 14.sp)
                        }
                    }
                }
            }

            // ПРЕДПРОСМОТР
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр листов A4 (4K):", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(FunnyStoryPdfDrawer.PAGE_WIDTH / FunnyStoryPdfDrawer.PAGE_HEIGHT)
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
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Весёлые истории", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Взрослый спрашивает слова — «назови животное», «назови смешное слово», — а ребёнок отвечает наугад, не видя текста. Потом слова вставляются в историю, и она читается вслух.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
