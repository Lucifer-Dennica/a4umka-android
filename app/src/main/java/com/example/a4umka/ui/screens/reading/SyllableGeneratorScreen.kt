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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a4umka.data.*
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.SyllablePdfDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllableGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(SyllableConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    val previewBitmap = remember(config) {
        val bmp = Bitmap.createBitmap(SyllablePdfDrawer.PAGE_WIDTH.toInt(), SyllablePdfDrawer.PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bmp)
        SyllablePdfDrawer.drawPage(canvas, config, context)
        bmp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чтение по слогам") },
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
                        val result = PdfExporter.exportSyllableText(context, config)
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
                Text("Источник текста:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.source == SyllableSource.READY, onClick = { config = config.copy(source = SyllableSource.READY) }, label = { Text("Готовый") })
                    FilterChip(selected = config.source == SyllableSource.CUSTOM, onClick = { config = config.copy(source = SyllableSource.CUSTOM) }, label = { Text("Свой") })
                }

                if (config.source == SyllableSource.READY) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Выберите текст:", style = MaterialTheme.typography.labelLarge)
                    // В реальном приложении здесь был бы выпадающий список,
                    // для примера просто покажем первый текст
                    Button(onClick = {
                        val sample = SyllableData.readyTexts.first()
                        config = config.copy(title = sample.title, text = sample.text, showQuestions = sample.questions.isNotEmpty())
                    }) {
                        Text("Загрузить пример: \"${SyllableData.readyTexts.first().title}\"")
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = config.title,
                        onValueChange = { config = config.copy(title = it) },
                        label = { Text("Название рассказа") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = config.text,
                        onValueChange = { config = config.copy(text = it) },
                        label = { Text("Текст рассказа") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Настройки печати:", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Крупный шрифт", modifier = Modifier.weight(1f))
                    Switch(checked = config.isLargeFont, onCheckedChange = { config = config.copy(isLargeFont = it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Вопросы по тексту", modifier = Modifier.weight(1f))
                    Switch(checked = config.showQuestions, onCheckedChange = { config = config.copy(showQuestions = it) })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Предпросмотр",
                        modifier = Modifier.fillMaxWidth().aspectRatio(SyllablePdfDrawer.PAGE_WIDTH / SyllablePdfDrawer.PAGE_HEIGHT)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Чтение по слогам", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "• Этот раздел помогает детям 5–7 лет учиться читать слитно. Под каждым слогом рисуется дуга, а под одиночными буквами (например, предлогами «и», «в») — точка.\n\n" +
                        "• Вы можете выбрать готовый текст или вставить свой (сказку, стихотворение, рассказ). Программа автоматически разобьёт слова на слоги.\n\n" +
                        "• Для первых занятий включайте «Крупный шрифт», чтобы буквы были больше, а строки — просторнее.\n\n" +
                        "• Для рассказов 1 класса можно включить «Вопросы по тексту», чтобы проверить, насколько осмысленно ребёнок прочитал материал.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
