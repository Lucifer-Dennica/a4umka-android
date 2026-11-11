package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.RebusConfig
import com.example.a4umka.data.RebusData
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.RebusPdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RebusGeneratorScreen(onBack: () -> Unit) {
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
                Text("До 8 строк, до 18 букв в каждой. Слов: ${RebusData.getParsedWords(config.wordsInput).size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))

                // РАСПОЛОЖЕНИЕ
                var layoutExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = layoutExpanded, onExpandedChange = { layoutExpanded = it }) {
                    OutlinedTextField(
                        value = when(config.cardsPerRow) {
                            6 -> "6 в ряд — компактно"
                            5 -> "5 в ряд — просторнее"
                            4 -> "4 в ряд — крупные клетки"
                            else -> "6 в ряд"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Расположение картинок") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = layoutExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = layoutExpanded, onDismissRequest = { layoutExpanded = false }) {
                        listOf(6 to "6 в ряд — компактно", 5 to "5 в ряд — просторнее", 4 to "4 в ряд — крупные клетки").forEach { (num, text) ->
                            DropdownMenuItem(
                                text = { Text(text) },
                                onClick = { config = config.copy(cardsPerRow = num); layoutExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ГАЛОЧКИ
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showHints, onCheckedChange = { config = config.copy(showHints = it) })
                    Text("Подписать названия картинок (подсказка)", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.addAnswers, onCheckedChange = { config = config.copy(addAnswers = it) })
                    Text("Добавить отдельные листы ответов", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.isBlackAndWhite, onCheckedChange = { config = config.copy(isBlackAndWhite = it) })
                    Text("Чёрно-белая печать", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр листов A4:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(RebusPdfDrawer.PAGE_WIDTH / RebusPdfDrawer.PAGE_HEIGHT)
                    ) { page ->
                        if (page < previewPages.size) {
                            Image(
                                bitmap = previewPages[page].asImageBitmap(),
                                contentDescription = "Предпросмотр",
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
                        "Введите имя, тему урока или короткую фразу — и получите ребус из своего слова на A4. Каждая картинка обозначает первую букву своего названия, а последовательность букв складывается в ответ.\n\n" +
                        "Как устроены ребусы по первым буквам:\n" +
                        "Это отдельный вид ребуса с понятным правилом: назвать предмет и записать первую букву. Например, кот, облако и торт дают К, О, Т — слово «КОТ». На листе картинки пронумерованы, под каждой есть клетка для буквы, а ниже — место для ответа.\n\n" +
                        "Какие слова можно зашифровать:\n" +
                        "Добавьте от одного до восьми слов или коротких словосочетаний, каждое на новой строке. В строке допускается до 18 русских букв без учета пробелов.\n\n" +
                        "Буква Ё остается Ё: «ёж» дает именно её, не Е. Для Й, Ы, Ь и Ъ используются готовые буквенные карточки. Ребёнок просто переписывает данный знак в клетку.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
