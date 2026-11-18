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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.*
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.UnstressedVowelsPdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UnstressedVowelsGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(UnstressedVowelsConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = UnstressedVowelsPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Безударные гласные") },
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
                        val result = PdfExporter.exportUnstressedVowels(context, config)
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
            // === Настройки ===
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Какие гласные",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VowelGroup.values().forEach { group ->
                                val selected = group in config.groups
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        config = config.copy(
                                            groups = if (selected) config.groups - group
                                            else config.groups + group
                                        )
                                    },
                                    label = { Text(group.title) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Задание",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = config.taskType == VowelTaskType.INSERT_AND_CHECK,
                                onClick = { config = config.copy(taskType = VowelTaskType.INSERT_AND_CHECK) }
                            )
                            Text("Вставь букву и запиши проверочное слово")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = config.taskType == VowelTaskType.INSERT_ONLY,
                                onClick = { config = config.copy(taskType = VowelTaskType.INSERT_ONLY) }
                            )
                            Text("Только вставь букву")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Сколько слов",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(12, 16, 20, 24).forEach { count ->
                                FilterChip(
                                    selected = config.wordsCount == count,
                                    onClick = { config = config.copy(wordsCount = count) },
                                    label = { Text("$count") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = config.showAnswers,
                                onCheckedChange = { config = config.copy(showAnswers = it) }
                            )
                            Text("Добавить страницу с ответами")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { config = config.copy(randomSeed = System.currentTimeMillis()) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Обновить — другие слова")
                        }
                    }
                }
            }

            // === Превью ===
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Стр. ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Text("Двумя пальцами можно приблизить лист", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(UnstressedVowelsPdfDrawer.PAGE_WIDTH / UnstressedVowelsPdfDrawer.PAGE_HEIGHT)
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
                    Text("Безударные гласные в корне", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Генератор составляет карточки для 2–3 класса: слово с пропущенной гласной, " +
                        "клетка для буквы и линия для проверочного слова. Сверху листа — правило проверки, " +
                        "последним листом — ответы.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Что умеет генератор", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "• Более 100 провераемых слов — существительные, прилагательные и глаголы.\n" +
                        "• Группы гласных — О/А, Е/И и Я, можно выбрать одну или несколько.\n" +
                        "• Два задания: «вставь букву и запиши проверочное слово» или «только вставь букву».\n" +
                        "• 12, 16, 20 или 24 слова на лист.\n" +
                        "• Ответы: вставленная буква выделена красным, рядом — проверочное слово.\n" +
                        "• Словарных слов, которые проверить нельзя (корова, воробей), здесь нет.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Как проверить", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Нужно изменить слово или подобрать однокоренное так, чтобы гласная оказалась под ударением: " +
                        "вода — во́ды, леса — ле́с, кричать — кри́к. Ударная гласная пишется так же, как слышится — " +
                        "значит, и в проверяемом слове будет та же буква.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Советы родителям", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "• Сначала найдите ударение — пусть ребёнок произнесёт слово и покажет ударный слог.\n" +
                        "• Проговаривайте проверку вслух: «Вода — во́ды, значит, о».\n" +
                        "• Начните с одной группы (сначала О/А, потом Е/И, потом вперемешку).\n" +
                        "• Понемногу и часто: 12 слов в день полезнее, чем 50 в выходные.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
