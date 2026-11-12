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
import com.example.a4umka.data.RebusConfig
import com.example.a4umka.data.RebusData
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.RebusPdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RebusGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(RebusConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = RebusPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })
    val words = RebusData.getParsedWords(config.wordsInput)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ребус из своего слова") },
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
                        val result = PdfExporter.exportRebus(context, config)
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
                OutlinedTextField(
                    value = config.title,
                    onValueChange = { config = config.copy(title = it) },
                    label = { Text("Заголовок листа") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ваши слова или короткие фразы:", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = config.wordsInput,
                    onValueChange = { config = config.copy(wordsInput = it) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("ШКОЛА\nДРУЖБА") }
                )
                Text("Слов: ${words.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))
                var layoutExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = layoutExpanded, onExpandedChange = { layoutExpanded = it }) {
                    OutlinedTextField(
                        value = when(config.cardsPerRow) { 6 -> "6 в ряд — компактно"; 5 -> "5 в ряд — просторнее"; 4 -> "4 в ряд — крупные"; else -> "6 в ряд" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Картинок в ряду") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = layoutExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = layoutExpanded, onDismissRequest = { layoutExpanded = false }) {
                        listOf(6 to "6 в ряд — компактно", 5 to "5 в ряд — просторнее", 4 to "4 в ряд — крупные").forEach { (num, text) ->
                            DropdownMenuItem(text = { Text(text) }, onClick = { config = config.copy(cardsPerRow = num); layoutExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                var perPageExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = perPageExpanded, onExpandedChange = { perPageExpanded = it }) {
                    OutlinedTextField(
                        value = "Ребусов на лист: ${config.wordsPerPage}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Экономия бумаги (таблицей)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = perPageExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = perPageExpanded, onDismissRequest = { perPageExpanded = false }) {
                        listOf(1, 2, 4).forEach { num ->
                            DropdownMenuItem(text = { Text("$num на лист") }, onClick = { config = config.copy(wordsPerPage = num); perPageExpanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showHints, onCheckedChange = { config = config.copy(showHints = it) })
                    Text("Подписать названия картинок", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.addAnswers, onCheckedChange = { config = config.copy(addAnswers = it) })
                    Text("Добавить листы ответов", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.isBlackAndWhite, onCheckedChange = { config = config.copy(isBlackAndWhite = it) })
                    Text("Чёрно-белая печать", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (words.isNotEmpty()) {
                    Button(
                        onClick = { onPlayInteractive(words) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Решать на экране")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

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
                        modifier = Modifier.fillMaxWidth().aspectRatio(RebusPdfDrawer.PAGE_WIDTH / RebusPdfDrawer.PAGE_HEIGHT)
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
                    Text("Ребус из своего слова", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Введите имя, тему урока или короткую фразу — и получите ребус из своего слова на A4.\n\n" +
                        "Каждая картинка обозначает первую букву своего названия.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
