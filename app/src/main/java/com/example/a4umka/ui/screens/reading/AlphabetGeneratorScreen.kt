package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.a4umka.data.*
import com.example.a4umka.util.AlphabetPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphabetGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(AlphabetConfig()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Генератор Алфавита") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val result = PdfExporter.exportAlphabetPoster(context, config)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Какие буквы печатать:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.filter == AlphabetFilter.ALL, onClick = { config = config.copy(filter = AlphabetFilter.ALL) }, label = { Text("Все") })
                    FilterChip(selected = config.filter == AlphabetFilter.VOWELS, onClick = { config = config.copy(filter = AlphabetFilter.VOWELS) }, label = { Text("Гласные") })
                    FilterChip(selected = config.filter == AlphabetFilter.CONSONANTS, onClick = { config = config.copy(filter = AlphabetFilter.CONSONANTS) }, label = { Text("Согласные") })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Регистр букв:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.case == AlphabetCase.UPPERCASE, onClick = { config = config.copy(case = AlphabetCase.UPPERCASE) }, label = { Text("А Б В") })
                    FilterChip(selected = config.case == AlphabetCase.LOWERCASE, onClick = { config = config.copy(case = AlphabetCase.LOWERCASE) }, label = { Text("а б в") })
                    FilterChip(selected = config.case == AlphabetCase.BOTH, onClick = { config = config.copy(case = AlphabetCase.BOTH) }, label = { Text("Аа Бб") })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Фон листа:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.theme == PosterTheme.WHITE, onClick = { config = config.copy(theme = PosterTheme.WHITE) }, label = { Text("Белый") })
                    FilterChip(selected = config.theme == PosterTheme.GREEN, onClick = { config = config.copy(theme = PosterTheme.GREEN) }, label = { Text("Зеленый") })
                    FilterChip(selected = config.theme == PosterTheme.PASTEL, onClick = { config = config.copy(theme = PosterTheme.PASTEL) }, label = { Text("Пастель") })
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Печатать картинку и слово", modifier = Modifier.weight(1f))
                    Switch(checked = config.showImages, onCheckedChange = { config = config.copy(showImages = it) })
                }
            }

            HorizontalDivider()

            Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(AlphabetPdfDrawer.PAGE_WIDTH / AlphabetPdfDrawer.PAGE_HEIGHT)
                ) {
                    val scale = minOf(size.width / AlphabetPdfDrawer.PAGE_WIDTH, size.height / AlphabetPdfDrawer.PAGE_HEIGHT)
                    val offsetX = (size.width - AlphabetPdfDrawer.PAGE_WIDTH * scale) / 2f
                    val offsetY = (size.height - AlphabetPdfDrawer.PAGE_HEIGHT * scale) / 2f

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.translate(offsetX, offsetY)
                        canvas.nativeCanvas.scale(scale, scale)
                        AlphabetPdfDrawer.drawPoster(canvas.nativeCanvas, config, context)
                        canvas.nativeCanvas.restore()
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
