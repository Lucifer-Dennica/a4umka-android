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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.a4umka.data.*
import com.example.a4umka.util.PdfExporter
import com.example.a4umka.util.RibbonPdfDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RibbonGeneratorScreen(
    onBack: () -> Unit,
    onPlayInteractive: () -> Unit // <-- НОВЫЙ КОЛБЭК
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(RibbonConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    val previewBitmap = remember(config) {
        val bmp = Bitmap.createBitmap(RibbonPdfDrawer.PAGE_WIDTH.toInt(), RibbonPdfDrawer.PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bmp)
        RibbonPdfDrawer.drawRibbon(canvas, config, context, isAnswerKey = false)
        bmp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Лента букв") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (config.mode == RibbonMode.MISSING) {
                        IconButton(onClick = { 
                            config = config.copy(seed = System.currentTimeMillis()) 
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Другой вариант")
                        }
                    }
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Справка")
                    }
                    IconButton(onClick = {
                        val result = PdfExporter.exportRibbon(context, config)
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
                Text("Что печатаем:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.mode == RibbonMode.DESK, onClick = { config = config.copy(mode = RibbonMode.DESK) }, label = { Text("Лента") })
                    FilterChip(selected = config.mode == RibbonMode.MISSING, onClick = { config = config.copy(mode = RibbonMode.MISSING) }, label = { Text("Пропуски") })
                    FilterChip(selected = config.mode == RibbonMode.COLORING, onClick = { config = config.copy(mode = RibbonMode.COLORING) }, label = { Text("Раскраска") })
                }

                if (config.mode == RibbonMode.MISSING) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Сколько букв спрятать:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = config.missingCount == MissingCount.EIGHT, onClick = { config = config.copy(missingCount = MissingCount.EIGHT) }, label = { Text("8 букв") })
                        FilterChip(selected = config.missingCount == MissingCount.SIXTEEN, onClick = { config = config.copy(missingCount = MissingCount.SIXTEEN) }, label = { Text("16 букв") })
                        FilterChip(selected = config.missingCount == MissingCount.ALL, onClick = { config = config.copy(missingCount = MissingCount.ALL) }, label = { Text("Все 33") })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Подсказка (какие буквы пропущены)", modifier = Modifier.weight(1f))
                        Switch(checked = config.showHint, onCheckedChange = { config = config.copy(showHint = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Печатать ответы (второй лист)", modifier = Modifier.weight(1f))
                        Switch(checked = config.showAnswers, onCheckedChange = { config = config.copy(showAnswers = it) })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Буквы:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.case == RibbonCase.BOTH, onClick = { config = config.copy(case = RibbonCase.BOTH) }, label = { Text("Аа") })
                    FilterChip(selected = config.case == RibbonCase.UPPERCASE, onClick = { config = config.copy(case = RibbonCase.UPPERCASE) }, label = { Text("А") })
                    FilterChip(selected = config.case == RibbonCase.LOWERCASE, onClick = { config = config.copy(case = RibbonCase.LOWERCASE) }, label = { Text("а") })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Text(
                    text = "💡 Совет: повесьте ленту на уровне глаз ребёнка.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Предпросмотр",
                        modifier = Modifier.fillMaxWidth().aspectRatio(RibbonPdfDrawer.PAGE_WIDTH / RibbonPdfDrawer.PAGE_HEIGHT)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // КНОПКА "РЕШАТЬ НА ЭКРАНЕ" (появляется только в режиме Пропусков)
                if (config.mode == RibbonMode.MISSING) {
                    Button(
                        onClick = onPlayInteractive,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Решать на экране", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                ElevatedCard(
                    onClick = { showInfoSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Как заниматься по ленте букв", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Советы родителям и учителям", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Как заниматься по ленте букв", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "• Повесьте ленту там, где ребёнок делает уроки. К ней будут обращаться чаще, чем к учебнику.\n\n" +
                        "• Начните с гласных: найдите на ленте все красные клетки и вместе их «спойте».\n\n" +
                        "• Звонкие и глухие проверяйте ладонью на горле: на звонких горло дрожит, на глухих — нет.\n\n" +
                        "• Задания «Впиши пропущенные буквы» начинайте с 8 букв и включенной подсказки, постепенно усложняя задачу.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
