package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.*
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.SoundAutomationPdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SoundAutomationGeneratorScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(SoundAutomationConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = SoundAutomationPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Автоматизация звуков") },
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
                        val result = PdfExporter.exportSoundAutomation(context, config)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (config.mode != SpeechMode.PAIRS) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Звук", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SpeechSound.values().forEach { sound ->
                                    FilterChip(
                                        selected = config.sound == sound,
                                        onClick = { config = config.copy(sound = sound) },
                                        label = { Text(sound.letter) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Различаем пару звуков", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SoundPair.values().forEach { pair ->
                                    FilterChip(
                                        selected = config.pair == pair,
                                        onClick = { config = config.copy(pair = pair) },
                                        label = { Text(pair.title) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Что напечатать", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))

                        ModeRadioButton(
                            title = "Комплект",
                            desc = "вся лесенка: слоги, слова, «где живёт звук», фразы + ответы (4–5 листов)",
                            selected = config.mode == SpeechMode.KIT,
                            onClick = { config = config.copy(mode = SpeechMode.KIT) }
                        )
                        ModeRadioButton(
                            title = "Слоги",
                            desc = "РА-РО-РУ, АР-ОР, АРА, ТРА и цепочки слогов",
                            selected = config.mode == SpeechMode.SYLLABLES,
                            onClick = { config = config.copy(mode = SpeechMode.SYLLABLES) }
                        )
                        ModeRadioButton(
                            title = "Слова",
                            desc = "звук в начале, в середине и в конце слова, с картинками",
                            selected = config.mode == SpeechMode.WORDS,
                            onClick = { config = config.copy(mode = SpeechMode.WORDS) }
                        )
                        ModeRadioButton(
                            title = "Где живёт звук?",
                            desc = "12 картинок, ребёнок сам отмечает место звука + ответы",
                            selected = config.mode == SpeechMode.WHERE_IS_SOUND,
                            onClick = { config = config.copy(mode = SpeechMode.WHERE_IS_SOUND) }
                        )
                        ModeRadioButton(
                            title = "Фразы и чистоговорки",
                            desc = "«Ра-ра-ра — начинается игра»",
                            selected = config.mode == SpeechMode.PHRASES,
                            onClick = { config = config.copy(mode = SpeechMode.PHRASES) }
                        )
                        ModeRadioButton(
                            title = "Различаем пару",
                            desc = "дифференциация звуков С–Ш, Р–Л, З–Ж",
                            selected = config.mode == SpeechMode.PAIRS,
                            onClick = { config = config.copy(mode = SpeechMode.PAIRS) }
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Стр. ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(SoundAutomationPdfDrawer.PAGE_WIDTH / SoundAutomationPdfDrawer.PAGE_HEIGHT)
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
                    Text("Как заниматься", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Автоматизация звука — это этап, когда ребенок уже умеет говорить звук отдельно, но в словах он еще «теряется». Чтобы звук стал привычным, его нужно много раз повторить.\n\n" +
                        "Как работать с листом:\n" +
                        "• Выберите один звук, который ребенок уже уверенно произносит изолированно.\n" +
                        "• Попросите повторить слог (или слово) 3 раза.\n" +
                        "• После каждого правильного повтора закрашивайте один кружок.\n" +
                        "• Не перескакивайте ступеньки: если в словах звук теряется, вернитесь к слогам.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ModeRadioButton(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )
        Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            if (desc.isNotEmpty()) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) Color.Gray else Color.LightGray,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
