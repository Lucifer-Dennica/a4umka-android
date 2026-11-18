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
import com.example.a4umka.data.SoundSchemeColors
import com.example.a4umka.data.SoundSchemeConfig
import com.example.a4umka.data.SoundSchemeLength
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.SoundSchemePdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundSchemeGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(SoundSchemeConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config, refreshTrigger) {
        delay(300)
        previewPages = SoundSchemePdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Звуковая схема слова") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshTrigger++ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Справка")
                    }
                    IconButton(onClick = {
                        val result = PdfExporter.exportSoundScheme(context, config)
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
                Text("Число звуков в словах:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.length == SoundSchemeLength.THREE, onClick = { config = config.copy(length = SoundSchemeLength.THREE) })
                        Text("Короткие — 3 звука (КОТ, ДОМ, КИТ)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.length == SoundSchemeLength.FOUR, onClick = { config = config.copy(length = SoundSchemeLength.FOUR) })
                        Text("Средние — 4 звука (ЛУНА, ЛИСА, КРОТ)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.length == SoundSchemeLength.FIVE, onClick = { config = config.copy(length = SoundSchemeLength.FIVE) })
                        Text("Длинные — 5 звуков (ЗАБОР, БАНАН, ТРАВА)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.length == SoundSchemeLength.MIX, onClick = { config = config.copy(length = SoundSchemeLength.MIX) })
                        Text("Вперемешку — слова разной длины")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Сложность раскраски:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.colors == SoundSchemeColors.TWO, onClick = { config = config.copy(colors = SoundSchemeColors.TWO) })
                        Text("2 цвета — гласный (красный) / согласный (синий)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.colors == SoundSchemeColors.THREE, onClick = { config = config.copy(colors = SoundSchemeColors.THREE) })
                        Text("3 цвета — + мягкий согласный (зелёный)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showAnswers, onCheckedChange = { config = config.copy(showAnswers = it) })
                    Text("Добавить вторым листом ответы (раскрашенные схемы)")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(SoundSchemePdfDrawer.PAGE_WIDTH / SoundSchemePdfDrawer.PAGE_HEIGHT)
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
                    Text("Звуко-буквенный анализ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Звуковая схема — это наглядная модель слова, где каждый звук обозначен квадратиком определенного цвета. Это классическое задание из программы 1 класса, которое отлично тренирует фонематический слух.\n\n" +
                        "Как заниматься:\n" +
                        "• Сначала произнесите слово медленно вместе с ребенком, выделяя каждый звук.\n" +
                        "• Тяните звук, а не букву. Говорите короткое «м», а не «эм»: схема строится из звуков!\n" +
                        "• Начните с 2 цветов (красный/синий) на коротких словах.\n" +
                        "• Зеленый цвет (мягкие согласные) добавляйте, когда ребенок уверенно освоит базу.\n\n" +
                        "Цветовая подсказка (как в школе):\n" +
                        "🟥 Красный — гласный звук\n" +
                        "🟦 Синий — твёрдый согласный\n" +
                        "🟩 Зелёный — мягкий согласный\n\n" +
                        "Совет: распечатайте лист-ответ и предложите ребенку проверить себя самостоятельно. Это отлично тренирует самоконтроль!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
