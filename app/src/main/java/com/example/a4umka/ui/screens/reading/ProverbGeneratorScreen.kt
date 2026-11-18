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
import com.example.a4umka.data.ProverbAge
import com.example.a4umka.data.ProverbConfig
import com.example.a4umka.data.ProverbSortType
import com.example.a4umka.ui.components.ZoomableImage // <-- ИМПОРТ ЗУМА
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.ProverbPdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProverbGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: (ProverbConfig) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(ProverbConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = ProverbPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Собери пословицу") },
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
                        val result = PdfExporter.exportProverb(context, config)
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
                Text("Что расставлять по порядку:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = config.sortTypes.contains(ProverbSortType.NUMBERS), onCheckedChange = { 
                            config = config.copy(sortTypes = toggle(config.sortTypes, ProverbSortType.NUMBERS))
                        })
                        Text("Числа — от меньшего к большему")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = config.sortTypes.contains(ProverbSortType.DAYS), onCheckedChange = { 
                            config = config.copy(sortTypes = toggle(config.sortTypes, ProverbSortType.DAYS))
                        })
                        Text("Дни недели — с понедельника")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = config.sortTypes.contains(ProverbSortType.MONTHS), onCheckedChange = { 
                            config = config.copy(sortTypes = toggle(config.sortTypes, ProverbSortType.MONTHS))
                        })
                        Text("Месяцы — с января по декабрь")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = config.sortTypes.contains(ProverbSortType.LETTERS), onCheckedChange = { 
                            config = config.copy(sortTypes = toggle(config.sortTypes, ProverbSortType.LETTERS))
                        })
                        Text("Буквы — по алфавиту")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Возраст:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.age == ProverbAge.AGE_5_6, onClick = { config = config.copy(age = ProverbAge.AGE_5_6) })
                        Text("5–6 лет (короткие фразы)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.age == ProverbAge.AGE_6_7, onClick = { config = config.copy(age = ProverbAge.AGE_6_7) })
                        Text("6–7 лет (длинные фразы)")
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    enabled = config.sortTypes.isNotEmpty()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Решать на экране")
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
                        modifier = Modifier.fillMaxWidth().aspectRatio(ProverbPdfDrawer.PAGE_WIDTH / ProverbPdfDrawer.PAGE_HEIGHT)
                    ) { page ->
                        if (page < previewPages.size) {
                            // ЗАМЕНИЛИ Image НА ZoomableImage
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
                    Text("Собери пословицу", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "На карточках вперемешку месяцы, дни недели, числа или буквы, и на каждой карточке слог. Расставь карточки по порядку, выпиши слоги в клеточки, и сложится пословица или скороговорка.\n\n" +
                        "Если вышла бессмыслица, значит, где-то нарушен порядок. Задание проверяет само себя.\n\n" +
                        "Главное, что развивает это задание — понимание последовательности. Одно дело — отбарабанить «январь, февраль, март», другое — найти среди перемешанных карточек, какой месяц идёт после мая.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

private fun toggle(set: Set<ProverbSortType>, type: ProverbSortType): Set<ProverbSortType> {
    val newSet = set.toMutableSet()
    if (newSet.contains(type)) newSet.remove(type) else newSet.add(type)
    return newSet
}
