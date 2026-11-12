package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.CrosswordConfig
import com.example.a4umka.data.CrosswordDifficulty
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.CrosswordPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrosswordGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(CrosswordConfig()) }
    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    
    LaunchedEffect(config) {
        delay(300)
        previewPages = CrosswordPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кроссворд по картинкам") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val result = PdfExporter.exportCrossword(context, config)
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
                Text("Сложность:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == CrosswordDifficulty.EASY, onClick = { config = config.copy(difficulty = CrosswordDifficulty.EASY) })
                        Text("Легко — 4 слова (3-4 буквы)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == CrosswordDifficulty.MEDIUM, onClick = { config = config.copy(difficulty = CrosswordDifficulty.MEDIUM) })
                        Text("Средне — 5 слов (до 5 букв)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == CrosswordDifficulty.HARD, onClick = { config = config.copy(difficulty = CrosswordDifficulty.HARD) })
                        Text("Сложно — до 7 слов (плотная сетка)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showWordsInClues, onCheckedChange = { config = config.copy(showWordsInClues = it) })
                    Text("Показать слова рядом с картинками")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showAnswers, onCheckedChange = { config = config.copy(showAnswers = it) })
                    Text("Добавить лист с ответами")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Кроссвордов на лист:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2).forEach { count ->
                        FilterChip(
                            selected = config.perPage == count,
                            onClick = { config = config.copy(perPage = count) },
                            label = { Text("$count") }
                        )
                    }
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(CrosswordPdfDrawer.PAGE_WIDTH / CrosswordPdfDrawer.PAGE_HEIGHT)
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
    }
}
