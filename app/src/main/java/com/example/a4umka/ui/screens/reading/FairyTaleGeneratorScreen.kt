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
import com.example.a4umka.data.FairyTaleAge
import com.example.a4umka.data.FairyTaleConfig
import com.example.a4umka.data.FairyTaleTaskType
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.FairyTalePdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairyTaleGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: (FairyTaleConfig) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(FairyTaleConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = FairyTalePdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Назови сказку правильно") },
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
                        val result = PdfExporter.exportFairyTale(context, config)
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
                Text("Какие задания включить:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = config.taskTypes.contains(FairyTaleTaskType.FIX_NAMES), onCheckedChange = {
                            val newSet = config.taskTypes.toMutableSet()
                            if (it) newSet.add(FairyTaleTaskType.FIX_NAMES) else newSet.remove(FairyTaleTaskType.FIX_NAMES)
                            config = config.copy(taskTypes = newSet)
                        })
                        Text("Исправь названия (перепутанные сказки)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = config.taskTypes.contains(FairyTaleTaskType.CONNECT_HALVES), onCheckedChange = {
                            val newSet = config.taskTypes.toMutableSet()
                            if (it) newSet.add(FairyTaleTaskType.CONNECT_HALVES) else newSet.remove(FairyTaleTaskType.CONNECT_HALVES)
                            config = config.copy(taskTypes = newSet)
                        })
                        Text("Соедини половинки названий")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = config.taskTypes.contains(FairyTaleTaskType.GUESS_PICTURES), onCheckedChange = {
                            val newSet = config.taskTypes.toMutableSet()
                            if (it) newSet.add(FairyTaleTaskType.GUESS_PICTURES) else newSet.remove(FairyTaleTaskType.GUESS_PICTURES)
                            config = config.copy(taskTypes = newSet)
                        })
                        Text("Угадай сказку по картинкам")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Возраст:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.age == FairyTaleAge.AGE_5_6, onClick = { config = config.copy(age = FairyTaleAge.AGE_5_6) })
                        Text("5–6 лет (знакомые сказки)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.age == FairyTaleAge.AGE_6_7, onClick = { config = config.copy(age = FairyTaleAge.AGE_6_7) })
                        Text("6–7 лет (Андерсен, Гримм, Пушкин)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Сколько листов:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3).forEach { count ->
                        FilterChip(
                            selected = config.pagesCount == count,
                            onClick = { config = config.copy(pagesCount = count) },
                            label = { Text("$count") }
                        )
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
                    Text("Играть на экране")
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(FairyTalePdfDrawer.PAGE_WIDTH / FairyTalePdfDrawer.PAGE_HEIGHT)
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
                    Text("Назови сказку правильно", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Этот раздел — веселая викторина по сказкам. Кто-то перепутал названия: «Снежная шапочка» вместо «Красной Шапочки», «Кот на горошине» вместо «Кота в сапогах». Ребенку нужно вспомнить, как сказки называются на самом деле.\n\n" +
                        "Зачем это нужно:\n" +
                        "• Тренируется память и умение узнавать героев.\n" +
                        "• Ребенок учится читать и понимать смысл прочитанного.\n" +
                        "• Развивается чувство юмора (ведь перепутанные названия звучат очень смешно).\n" +
                        "• Вы можете узнать, какие сказки ребенок знает, а какие стоит прочитать на ночь.\n\n" +
                        "В генераторе можно выбрать задания (исправить названия, соединить половинки, угадать по эмодзи) и возраст. Для детей 5–6 лет подобраны самые известные сказки, а для 6–7 лет — более сложные (Андерсен, Пушкин, Гримм).",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
