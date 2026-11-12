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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.*
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.SyllableTablePdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SyllableTableGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(SyllableTableConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = SyllableTablePdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Слоговые таблицы") },
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
                        val result = PdfExporter.exportSyllableTable(context, config)
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
                Text("Вид работы:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.workType == SyllableWorkType.TABLE, onClick = { config = config.copy(workType = SyllableWorkType.TABLE) }, label = { Text("Таблица") })
                    FilterChip(selected = config.workType == SyllableWorkType.CARDS, onClick = { config = config.copy(workType = SyllableWorkType.CARDS) }, label = { Text("Карточки") })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Согласные (до 8):", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = { config = config.copy(consonants = SyllableTableData.firstConsonants.toSet()) }) { Text("Первые", fontSize = 10.sp) }
                    TextButton(onClick = { config = config.copy(consonants = SyllableTableData.allConsonants.take(8).toSet()) }) { Text("8", fontSize = 10.sp) }
                    TextButton(onClick = { config = config.copy(consonants = emptySet()) }) { Text("Снять", fontSize = 10.sp) }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SyllableTableData.allConsonants.forEach { letter ->
                        val isSelected = config.consonants.contains(letter)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newSet = config.consonants.toMutableSet()
                                if (isSelected) newSet.remove(letter) else {
                                    if (newSet.size < 8) newSet.add(letter)
                                }
                                config = config.copy(consonants = newSet)
                            },
                            label = { Text(letter) },
                            modifier = Modifier.width(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Гласные:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.vowelStep == SyllableVowelStep.FIRST, onClick = { config = config.copy(vowelStep = SyllableVowelStep.FIRST) })
                        Text("Первая ступень — А О У Ы И")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.vowelStep == SyllableVowelStep.ALL, onClick = { config = config.copy(vowelStep = SyllableVowelStep.ALL) })
                        Text("Все 10 — А О У Ы Э Я Ё Ю И Е")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Тип слога:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.syllableType == SyllableType.OPEN, onClick = { config = config.copy(syllableType = SyllableType.OPEN) })
                        Text("Открытый — МА МО МУ")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.syllableType == SyllableType.REVERSE, onClick = { config = config.copy(syllableType = SyllableType.REVERSE) })
                        Text("Обратный — АМ ОМ УМ")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.syllableType == SyllableType.CLOSED, onClick = { config = config.copy(syllableType = SyllableType.CLOSED) })
                        Text("Закрытый — МАМ ЛАМ САМ")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.syllableType == SyllableType.CLUSTER, onClick = { config = config.copy(syllableType = SyllableType.CLUSTER) })
                        Text("Со стечением — СТА ТРА КРО")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.shuffle, onCheckedChange = { config = config.copy(shuffle = it) })
                    Text("Вперемешку (слоги вразброс)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.findGame, onCheckedChange = { config = config.copy(findGame = it) })
                    Text("Игра «найди слог» внизу листа")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.syllableOfTheDay, onCheckedChange = { config = config.copy(syllableOfTheDay = it) })
                    Text("«Слог дня» (выделяется рамкой)")
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(SyllableTablePdfDrawer.PAGE_WIDTH / SyllableTablePdfDrawer.PAGE_HEIGHT)
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
                    Text("Слоговые таблицы", style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Чтение начинается не с букв, а со слогов. Ребёнок может бойко называть все буквы алфавита, но застывать на месте, когда нужно соединить «М» и «А» в живое «МА».\n\n" +
                        "Наш генератор создаёт готовые листы для такой тренировки. Вы выбираете согласные и гласные — и получаете новую таблицу.\n\n" +
                        "Почему в таблице бывают пустые клетки? Сочетаний ЖЫ, ЩЮ, ЧЯ в русском языке не существует — это правила «жи-ши», «ча-ща», «чу-щу».",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
