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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.PrepositionConfig
import com.example.a4umka.data.PrepositionLevel
import com.example.a4umka.data.PrepositionTaskType
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.PrepositionPdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrepositionGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(PrepositionConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = PrepositionPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Предлоги в картинках") },
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
                        val result = PdfExporter.exportPreposition(context, config)
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
                Text("Вид задания:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.taskType == PrepositionTaskType.FIND_WORD, onClick = { config = config.copy(taskType = PrepositionTaskType.FIND_WORD) })
                        Text("Где находится? — обведи верные слова")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.taskType == PrepositionTaskType.FIND_PICTURE, onClick = { config = config.copy(taskType = PrepositionTaskType.FIND_PICTURE) })
                        Text("Найди картинку — выбери подходящую")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.taskType == PrepositionTaskType.DRAW_SELF, onClick = { config = config.copy(taskType = PrepositionTaskType.DRAW_SELF) })
                        Text("Нарисуй сам — дорисуй предмет")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Какие предлоги:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.level == PrepositionLevel.SIMPLE, onClick = { config = config.copy(level = PrepositionLevel.SIMPLE) })
                        Text("Простые — на, под, в")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.level == PrepositionLevel.BASIC, onClick = { config = config.copy(level = PrepositionLevel.BASIC) })
                        Text("Основные — плюс над, за, перед")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.level == PrepositionLevel.ALL, onClick = { config = config.copy(level = PrepositionLevel.ALL) })
                        Text("Все — плюс между, слева от, справа от")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Заданий на листе:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(4, 6).forEach { count ->
                        FilterChip(
                            selected = config.tasksCount == count,
                            onClick = { config = config.copy(tasksCount = count) },
                            label = { Text("$count") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showAnswers, onCheckedChange = { config = config.copy(showAnswers = it) })
                    Text("Добавить вторым листом ответы")
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(PrepositionPdfDrawer.PAGE_WIDTH / PrepositionPdfDrawer.PAGE_HEIGHT)
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
                    Text("Как заниматься по этим листам", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "«Где мяч?» — «Там». Знакомо? Ребёнок видит, где лежит предмет, но ему не хватает слов, чтобы это описать. Наши листы помогают превратить указательный жест в полноценную фразу: «мяч под столом», «кот в корзине».\n\n" +
                        "Задания развивают:\n" +
                        "• Связную речь — ребёнок строит предложение целиком.\n" +
                        "• Пространственное мышление — понимание верха, низа, переда и зада.\n" +
                        "• Грамматику — предлог требует правильного падежа.\n" +
                        "• Ориентировку «лево — право», так необходимую в школе.\n\n" +
                        "Для кого подходит:\n" +
                        "• 4 года — начните с «Простых» (на, под, в).\n" +
                        "• 5 лет — переходите к «Основным» (над, за, перед).\n" +
                        "• 6–7 лет — используйте все предлоги, включая сложные (между, слева, справа).\n\n" +
                        "Совет: даже если ребёнок правильно обвёл слово, попросите его проговорить всю фразу вслух. А режим «Нарисуй сам» отлично тренирует слуховое внимание — ведь ответ нельзя подсмотреть, его нужно услышать!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
