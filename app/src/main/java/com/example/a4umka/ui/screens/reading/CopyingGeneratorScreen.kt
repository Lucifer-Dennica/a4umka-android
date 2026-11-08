package com.example.a4umka.ui.screens.reading

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
import com.example.a4umka.data.CopyingConfig
import com.example.a4umka.data.CopyingLineType
import com.example.a4umka.data.CopyingMode
import com.example.a4umka.util.CopyingPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyingGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(CopyingConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    val previewBitmap = remember(config) {
        CopyingPdfDrawer.generatePreview(config, context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Списывание и диктанты") },
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
                        val result = PdfExporter.exportCopying(context, config)
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
                Text("Режим:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = config.mode == CopyingMode.COPYING,
                        onClick = { config = config.copy(mode = CopyingMode.COPYING) },
                        label = { Text("Списывание") }
                    )
                    FilterChip(
                        selected = config.mode == CopyingMode.DICTATION,
                        onClick = { config = config.copy(mode = CopyingMode.DICTATION) },
                        label = { Text("Диктант") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = config.title,
                    onValueChange = { config = config.copy(title = it) },
                    label = { Text("Заголовок") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.text,
                    onValueChange = { config = config.copy(text = it) },
                    label = { Text("Текст (для диктанта он не печатается)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Настройки листа:", style = MaterialTheme.typography.labelLarge)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Крупный шрифт", modifier = Modifier.weight(1f))
                    Switch(checked = config.isLargeFont, onCheckedChange = { config = config.copy(isLargeFont = it) })
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Наклонные линии (косая)", modifier = Modifier.weight(1f))
                    Switch(
                        checked = config.lineType == CopyingLineType.SLANTED,
                        onCheckedChange = { 
                            config = config.copy(lineType = if (it) CopyingLineType.SLANTED else CopyingLineType.STANDARD) 
                        }
                    )
                }

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("Количество строк: ${config.linesCount}", fontSize = 14.sp)
                    Slider(
                        value = config.linesCount.toFloat(),
                        onValueChange = { config = config.copy(linesCount = it.toInt()) },
                        valueRange = 5f..20f,
                        steps = 14
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Предпросмотр",
                        modifier = Modifier.fillMaxWidth().aspectRatio(CopyingPdfDrawer.PAGE_WIDTH / CopyingPdfDrawer.PAGE_HEIGHT)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Списывание и диктанты", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• В режиме «Списывание» текст печатается сверху, а ниже идут пустые строки для тренировки письма.\n\n" +
                        "• В режиме «Диктант» печатаются только строки. Взрослый читает текст с экрана телефона, а ребёнок пишет его на листе.\n\n" +
                        "• Вы можете выбрать крупный шрифт для первых занятий и включить наклонные линии, чтобы ребёнку было легче соблюдать наклон букв.",
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
