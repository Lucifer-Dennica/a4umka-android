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
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.a4umka.data.*
import com.example.a4umka.ui.components.ZoomableImage
import com.example.a4umka.util.FillwordPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FillwordGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: (FillwordConfig) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(FillwordConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = FillwordPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Филворд из своих слов") },
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
                        val result = PdfExporter.exportFillword(context, config)
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
                Text("Ваши слова:", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = config.wordsInput,
                    onValueChange = { config = config.copy(wordsInput = it) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text("МЕДВЕДЬ, ЛИСА, ВОЛК...") }
                )
                Text("Слов: ${config.getParsedWords().size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(16.dp))

                Text("Размер сетки:", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = config.sizeMode == FillwordSizeMode.AUTO, onClick = { config = config.copy(sizeMode = FillwordSizeMode.AUTO) })
                    Text("Автоматически")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = config.sizeMode == FillwordSizeMode.MANUAL, onClick = { config = config.copy(sizeMode = FillwordSizeMode.MANUAL) })
                    Text("Вручную:")
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = config.manualSize.toString(),
                        onValueChange = { 
                            val num = it.toIntOrNull() ?: 11
                            config = config.copy(manualSize = num.coerceIn(7, 20))
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                    Text(" x ${config.manualSize}", modifier = Modifier.padding(start = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.allowBackwards, onCheckedChange = { config = config.copy(allowBackwards = it) })
                    Text("Разрешить ← и ↑ (сложнее)", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Настройки листа:", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = config.title,
                    onValueChange = { config = config.copy(title = it) },
                    label = { Text("Заголовок листа") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showNameField, onCheckedChange = { config = config.copy(showNameField = it) })
                    Text("Строка «Имя ____»")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.showWordList, onCheckedChange = { config = config.copy(showWordList = it) })
                    Text("Печатать список слов (без него сложнее)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.addAnswers, onCheckedChange = { config = config.copy(addAnswers = it) })
                    Text("Добавить лист ответов")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Сколько вариантов:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.variants == FillwordVariants.ONE, onClick = { config = config.copy(variants = FillwordVariants.ONE) }, label = { Text("1") })
                    FilterChip(selected = config.variants == FillwordVariants.TWO, onClick = { config = config.copy(variants = FillwordVariants.TWO) }, label = { Text("2") })
                    FilterChip(selected = config.variants == FillwordVariants.FOUR, onClick = { config = config.copy(variants = FillwordVariants.FOUR) }, label = { Text("4") })
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { onPlayInteractive(config) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Решать на экране")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge)
                if (previewPages.size > 1) {
                    Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(...)
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
                    Text("Филворд из своих слов", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "На этой странице можно создать филворд из своих слов и сразу распечатать его на A4 — бесплатно и без регистрации. Вы вписываете любые слова, а генератор сам прячет их в сетку букв, подбирает размер поля и печатает второй лист с ответом. Готовых наборов ждать не нужно: слова берутся ваши, поэтому лист получается точно под ваше занятие — словарные слова на неделю, слова на нужный звук, тема утренника или имена гостей на дне рождения.\n\n" +
                        "Обычный филворд — это задание, которое кто-то придумал за вас. Здесь наоборот: задание придумываете вы, а работу по укладке слов, заливке пустых клеток и проверке делает генератор.\n\n" +
                        "Что можно настроить:\n" +
                        "• Размер сетки — подобрать автоматически под ваши слова или задать от 7×7 до 15×15;\n" +
                        "• направления — слова читаются слева направо и сверху вниз, а по галочке ещё и в обратную сторону;\n" +
                        "• заголовок листа — своё название вместо слова «Филворд»;\n" +
                        "• строка «Имя ____» — если раздаёте листы группе;\n" +
                        "• список слов на листе — оставить (проще) или убрать (сложнее);\n" +
                        "• лист с ответами — отдельной страницей в конце, для взрослого;\n" +
                        "• 1, 2 или 4 варианта — те же слова, но другая раскладка, чтобы соседи по парте не списали.\n\n" +
                        "Советы взрослым:\n" +
                        "• Начните с малого: 5 слов найти и обвести приятнее, чем застрять на пятнадцати;\n" +
                        "• слова берите знакомые — филворд закрепляет написание, а не учит новым значениям;\n" +
                        "• не смешивайте в одном листе слова разной длины слишком сильно;\n" +
                        "• если ребёнок застрял, подскажите первую букву и её строку.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
