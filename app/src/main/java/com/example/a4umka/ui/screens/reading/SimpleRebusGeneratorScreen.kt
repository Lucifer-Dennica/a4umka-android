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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.SimpleRebusConfig
import com.example.a4umka.data.SimpleRebusLength
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.SimpleRebusPdfDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleRebusGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(SimpleRebusConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var previewKey by remember { mutableIntStateOf(0) } // Для кнопки "Другие ребусы"

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config, previewKey) {
        delay(300)
        previewPages = SimpleRebusPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ребусы") },
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
                        val result = PdfExporter.exportSimpleRebus(context, config)
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
                Text("Длина слов:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.length == SimpleRebusLength.THREE, onClick = { config = config.copy(length = SimpleRebusLength.THREE) })
                        Text("3 буквы — КОТ, ДОМ, ЛУК")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.length == SimpleRebusLength.THREE_FOUR, onClick = { config = config.copy(length = SimpleRebusLength.THREE_FOUR) })
                        Text("3–4 буквы — + ЛИСА, РОЗА, ЛУНА")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.addAnswers, onCheckedChange = { config = config.copy(addAnswers = it) })
                    Text("Добавить лист с ответами", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { previewKey++ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Другие ребусы")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Стр. ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(SimpleRebusPdfDrawer.PAGE_WIDTH / SimpleRebusPdfDrawer.PAGE_HEIGHT)
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
                    Text("Ребусы по первым буквам", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Ребусы по первым буквам — это увлекательная игра-головоломка, в которой ребёнок смотрит на ряд картинок, берёт первую букву из названия каждой и складывает из этих букв спрятанное слово. Например, картинки «кот», «облако» и «тигр» дают слово КОТ.\n\n" +
                        "Наш генератор создаёт такие ребусы с картинками для дошкольников прямо в приложении: нажимаете кнопку — и получаете готовый лист A4, который остаётся только распечатать.\n\n" +
                        "Главная польза — тренировка звуко-буквенного анализа, то есть умения слышать, из каких звуков состоит слово, и обозначать их буквами. Это фундаментальный навык для обучения чтению и грамотному письму.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
