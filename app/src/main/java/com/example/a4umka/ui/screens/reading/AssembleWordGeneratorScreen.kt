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
import com.example.a4umka.data.*
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.AssembleWordPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssembleWordGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(AssembleWordConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = AssembleWordPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Собери слово") },
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
                        val result = PdfExporter.exportAssembleWord(context, config)
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
                Text("Тип задания:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.taskType == AssembleTaskType.CONNECT, onClick = { config = config.copy(taskType = AssembleTaskType.CONNECT) })
                        Text("Соедини слоги (проводим линии)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.taskType == AssembleTaskType.PICTURE, onClick = { config = config.copy(taskType = AssembleTaskType.PICTURE) })
                        Text("Собери по картинке (эмодзи + клеточки)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.taskType == AssembleTaskType.CARDS, onClick = { config = config.copy(taskType = AssembleTaskType.CARDS) })
                        Text("Карточки-резалки")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Сложность:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == AssembleDifficulty.EASY, onClick = { config = config.copy(difficulty = AssembleDifficulty.EASY) })
                        Text("Легко (2 слога)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == AssembleDifficulty.MEDIUM, onClick = { config = config.copy(difficulty = AssembleDifficulty.MEDIUM) })
                        Text("Средне (3 слога)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == AssembleDifficulty.HARD, onClick = { config = config.copy(difficulty = AssembleDifficulty.HARD) })
                        Text("Сложно (2 и 3 вперемешку)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == AssembleDifficulty.SUPER_HARD, onClick = { config = config.copy(difficulty = AssembleDifficulty.SUPER_HARD) })
                        Text("Суперсложно (4 слога)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.colorSyllables, onCheckedChange = { config = config.copy(colorSyllables = it) })
                    Text("Раскраска слогов (гласные красные)", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.firstStepVowelsOnly, onCheckedChange = { config = config.copy(firstStepVowelsOnly = it) })
                    Text("Только гласные первой ступени", fontSize = 14.sp)
                }
                if (config.taskType == AssembleTaskType.CONNECT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = config.emojiHints, onCheckedChange = { config = config.copy(emojiHints = it) })
                        Text("Эмодзи-подсказки (в центре)", fontSize = 14.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.addAnswerSheet, onCheckedChange = { config = config.copy(addAnswerSheet = it) })
                    Text("Добавить лист с ответами", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge)
                if (previewPages.size > 1) {
                    Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
                Text("Двумя пальцами можно приблизить лист", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(AssembleWordPdfDrawer.PAGE_WIDTH / AssembleWordPdfDrawer.PAGE_HEIGHT)
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
                    Text("Собери слово из слогов", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Чтение начинается не с целого слова, а с маленького кирпичика — слога. Когда ребёнок учится складывать КО и ЗА в понятное «коза», в его голове происходит важная работа: он перестаёт угадывать слова по первой букве и начинает читать осознанно.\n\n" +
                        "Три типа заданий:\n" +
                        "1. Соедини слоги линиями.\n" +
                        "2. Собери по картинке.\n" +
                        "3. Карточки-резалки.\n\n" +
                        "Сложность задаётся длиной слова: от 2 до 4 слогов.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
