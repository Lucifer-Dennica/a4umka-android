package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.FillwordConfig
import com.example.a4umka.util.FillwordGenerator
import com.example.a4umka.util.FillwordGrid
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveFillwordScreen(
    config: FillwordConfig,
    onBack: () -> Unit
) {
    val words = config.getParsedWords()
    var grid by remember { mutableStateOf<FillwordGrid?>(null) }
    var foundWords by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSuccess by remember { mutableStateOf(false) }

    // Инициализация
    LaunchedEffect(config) {
        val manualSize = if (config.sizeMode == com.example.a4umka.data.FillwordSizeMode.MANUAL) config.manualSize else null
        grid = FillwordGenerator.generate(words, manualSize, config.allowBackwards)
        foundWords = emptySet()
        showSuccess = false
    }

    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragEnd by remember { mutableStateOf<Offset?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(config.title.ifEmpty { "Филворд" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        grid = FillwordGenerator.generate(words, null, config.allowBackwards)
                        foundWords = emptySet()
                        showSuccess = false
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Заново")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (grid != null) {
                Text("Найдено: ${foundWords.size} из ${words.size}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                // СЕТКА
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val sizePx = minOf(maxWidth, maxHeight)
                    val cellSize = sizePx / grid!!.size

                    Canvas(
                        modifier = Modifier
                            .size(sizePx)
                            .pointerInput(grid) {
                                detectDragGestures(
                                    onDragStart = { offset -> dragStart = offset },
                                    onDrag = { change, _ -> dragEnd = change.position },
                                    onDragEnd = {
                                        val start = dragStart
                                        val end = dragEnd
                                        if (start != null && end != null) {
                                            // Вычисляем ячейки
                                            val startCol = (start.x / cellSize.toPx()).roundToInt().coerceIn(0, grid!!.size - 1)
                                            val startRow = (start.y / cellSize.toPx()).roundToInt().coerceIn(0, grid!!.size - 1)
                                            val endCol = (end.x / cellSize.toPx()).roundToInt().coerceIn(0, grid!!.size - 1)
                                            val endRow = (end.y / cellSize.toPx()).roundToInt().coerceIn(0, grid!!.size - 1)

                                            val word = getWordFromPath(grid!!, startRow, startCol, endRow, endCol)
                                            if (word != null && words.contains(word) && !foundWords.contains(word)) {
                                                foundWords = foundWords + word
                                                if (foundWords.size == words.size) showSuccess = true
                                            }
                                        }
                                        dragStart = null
                                        dragEnd = null
                                    }
                                )
                            }
                    ) {
                        val cellW = size.width / grid!!.size
                        val cellH = size.height / grid!!.size
                        
                        // Отрисовка найденных слов (фон)
                        val highlightPaint = androidx.compose.ui.graphics.Paint().apply { color = Color(0x884CAF50) }
                        for (word in foundWords) {
                            val pw = grid!!.placedWords.find { it.word == word }
                            if (pw != null) {
                                for (i in word.indices) {
                                    val x = pw.startX + i * pw.dx
                                    val y = pw.startY + i * pw.dy
                                    drawRect(
                                        color = Color(0x884CAF50),
                                        topLeft = Offset(x * cellW, y * cellH),
                                        size = Size(cellW, cellH)
                                    )
                                }
                            }
                        }

                        // Отрисовка сетки и букв
                        for (y in 0 until grid!!.size) {
                            for (x in 0 until grid!!.size) {
                                val left = x * cellW
                                val top = y * cellH
                                
                                drawRect(
                                    color = Color.LightGray,
                                    topLeft = Offset(left, top),
                                    size = Size(cellW, cellH),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(1f)
                                )

                                drawIntoCanvas { canvas ->
                                    val paint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        textSize = cellW * 0.6f
                                        color = android.graphics.Color.BLACK
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    }
                                    val letter = grid!!.letters[y][x].toString()
                                    val textY = top + cellH / 2f - (paint.descent() + paint.ascent()) / 2f
                                    canvas.nativeCanvas.drawText(letter, left + cellW / 2f, textY, paint)
                                }
                            }
                        }

                        // Отрисовка текущего выделения
                        if (dragStart != null && dragEnd != null) {
                            drawLine(
                                color = Color.Red,
                                start = dragStart!!,
                                end = dragEnd!!,
                                strokeWidth = 5f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (showSuccess) {
                    Text("🎉 Молодец! Все слова найдены!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                // Список слов для поиска
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    words.forEach { word ->
                        val isFound = foundWords.contains(word)
                        Surface(
                            color = if (isFound) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text(
                                text = word,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                textDecoration = if (isFound) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                color = if (isFound) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательная функция для получения слова из координат
private fun getWordFromPath(grid: FillwordGrid, r1: Int, c1: Int, r2: Int, c2: Int): String? {
    val dRow = r2 - r1
    val dCol = c2 - c1
    
    // Проверяем, что это прямая линия (горизонталь, вертикаль)
    if (dRow != 0 && dCol != 0) return null // Диагонали пока не поддерживаем для простоты ввода
    
    val length = maxOf(abs(dRow), abs(dCol)) + 1
    val stepRow = if (dRow == 0) 0 else dRow / abs(dRow)
    val stepCol = if (dCol == 0) 0 else dCol / abs(dCol)
    
    val sb = StringBuilder()
    for (i in 0 until length) {
        val r = r1 + i * stepRow
        val c = c1 + i * stepCol
        if (r < 0 || r >= grid.size || c < 0 || c >= grid.size) return null
        sb.append(grid.letters[r][c])
    }
    return sb.toString()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(horizontalArrangement: Arrangement.Horizontal, content: @Composable FlowRowScope.() -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}
