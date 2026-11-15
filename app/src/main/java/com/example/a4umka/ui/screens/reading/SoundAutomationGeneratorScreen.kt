package com.example.a4umka.ui.screens.reading

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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.SoundAutomationConfig
import com.example.a4umka.data.SpeechMode
import com.example.a4umka.data.SpeechSound
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.SoundAutomationPdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundAutomationGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: (SoundAutomationConfig) -> Unit
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
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text("Выберите звук:", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpeechSound.values().forEach { sound ->
                        FilterChip(
                            selected = config.sound == sound,
                            onClick = { config = config.copy(sound = sound) },
                            label = { Text(sound.letter) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Что напечатать:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.mode == SpeechMode.KIT, onClick = { config = config.copy(mode = SpeechMode.KIT) })
                        Text("Комплект — все листы сразу")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.mode == SpeechMode.SYLLABLES, onClick = { config = config.copy(mode = SpeechMode.SYLLABLES) })
                        Text("Слоги — лесенка слогов")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.mode == SpeechMode.WORDS, onClick = { config = config.copy(mode = SpeechMode.WORDS) })
                        Text("Слова — с картинками")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.mode == SpeechMode.PHRASES, onClick = { config = config.copy(mode = SpeechMode.PHRASES) })
                        Text("Чистоговорки")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onPlayInteractive(config) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Решать на телефоне")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
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
                        "• Не перескакивайте ступеньки: если в словах звук теряется, вернитесь к слогам.\n\n" +
                        "В онлайн-режиме на телефоне вы можете нажимать на кружочки прямо на экране, чтобы отмечать повторы и переходить к следующему заданию.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
