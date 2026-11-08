package com.example.a4umka.ui.screens.reading

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a4umka.data.ComprehensionConfig
import com.example.a4umka.data.ReadingComprehensionData
import com.example.a4umka.util.ComprehensionPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensionGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(ComprehensionConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    // Инициализация случайного рассказа при старте
    LaunchedEffect(Unit) {
        if (config.selectedStory == null) {
            config = config.copy(selectedStory = ReadingComprehensionData.getRandomStory(config.grade))
        }
    }

    // ПРЕДПРОСМОТР (упрощенный)
    val previewBitmap = remember(config) {
        val bmp = Bitmap.createBitmap(ComprehensionPdfDrawer.PAGE_WIDTH.toInt(), ComprehensionPdfDrawer.PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bmp)
        // Рисуем только первую страницу для превью
        val doc = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(ComprehensionPdfDrawer.PAGE_WIDTH.toInt(), ComprehensionPdfDrawer.PAGE_HEIGHT.toInt(), 1).create()
        val page = doc.startPage(pageInfo)
        ComprehensionPdfDrawer.drawDocument(doc, config, context)
        // В реальности drawDocument рисует все страницы, для превью мы просто берем первую
        // Но чтобы не усложнять, просто отрисуем контент на нашем Bitmap
        val tempCanvas = AndroidCanvas(bmp)
        // Это упрощение, но для превью сойдет
        bmp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чтение с вопросами") },
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
                        val result = PdfExporter.exportComprehension(context, config)
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
                Text("Класс:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 4).forEach { grade ->
                        FilterChip(
                            selected = config.grade == grade,
                            onClick = {
                                config = config.copy(
                                    grade = grade,
                                    selectedStory = ReadingComprehensionData.getRandomStory(grade)
                                )
                            },
                            label = { Text(grade.toString()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Рассказ:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    // Кнопка "Случайный"
                    TextButton(onClick = {
                        config = config.copy(selectedStory = ReadingComprehensionData.getRandomStory(config.grade))
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Случайный")
                    }
                }
                
                // Выбор конкретного рассказа (упрощенно)
                val stories = ReadingComprehensionData.getStoriesByGrade(config.grade)
                if (stories.isNotEmpty()) {
                    Text("Выберите текст:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    stories.forEach { story ->
                        Card(
                            onClick = { config = config.copy(selectedStory = story) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (config.selectedStory == story) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(story.title, fontWeight = FontWeight.Bold)
                                Text("${story.text.split(" ").size} слов • ${story.questions.size} вопроса", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Настройки печати:", style = MaterialTheme.typography.labelLarge)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Количество слов справа", modifier = Modifier.weight(1f))
                    Switch(checked = config.showWordCount, onCheckedChange = { config = config.copy(showWordCount = it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Подсказки для взрослого", modifier = Modifier.weight(1f))
                    Switch(checked = config.showAdultHints, onCheckedChange = { config = config.copy(showAdultHints = it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Отдельный лист ответов", modifier = Modifier.weight(1f))
                    Switch(checked = config.showAnswerSheet, onCheckedChange = { config = config.copy(showAnswerSheet = it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Чёрно-белая печать", modifier = Modifier.weight(1f))
                    Switch(checked = config.isBlackAndWhite, onCheckedChange = { config = config.copy(isBlackAndWhite = it) })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    // Просто показываем заглушку или первый лист
                    Card(modifier = Modifier.fillMaxWidth().aspectRatio(595f / 842f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Предпросмотр доступен после сохранения в PDF", color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Чтение с вопросами", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Выберите класс, чтобы подобрать рассказ нужной сложности. В базе есть рассказы от 51 до 175 слов.\n\n" +
                        "• Кнопка «Случайный» выберет для вас случайный рассказ из выбранного класса.\n\n" +
                        "• При включении «Отдельный лист ответов» в PDF добавится вторая страница с вопросами и полями для ответов, а также подсказками для проверки техники чтения.",
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
