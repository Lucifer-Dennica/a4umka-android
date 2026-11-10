package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.FillwordConfig
import com.example.a4umka.data.FillwordSizeMode
import com.example.a4umka.util.FillwordGenerator
import com.example.a4umka.util.FillwordGrid
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InteractiveFillwordScreen(
    config: FillwordConfig,
    onBack: () -> Unit
) {
    val words = config.getParsedWords()
    var grid by remember { mutableStateOf<FillwordGrid?>(null) }
    var foundWords by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(config) {
        val manualSize = if (config.sizeMode == FillwordSizeMode.MANUAL) config.manualSize else null
        grid = FillwordGenerator.generate(words, manualSize, config.allowBackwards)
        foundWords = emptySet()
        showSuccess = false
    }

    var startCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var currentCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

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
                                    onDragStart = { offset ->
                                        val cellW = size.width / grid!!.size.toFloat()
                                        val cellH = size.height / grid!!.size.toFloat()
                                        val col = (offset.x / cellW).toInt().coerceIn(0, grid!!.size - 1)
                                        val row = (offset.y / cellH).toInt().coerceIn(0, grid!!.size - 1)
                                        startCell = Pair(row, col)
                                        currentCell = Pair(row, col)
                                    },
                                    onDrag = { change, _ ->
                                        val cellW = size.width / grid!!.size.toFloat()
                                        val cellH = size.height / grid!!.size.toFloat()
                                        val col = (change.position.x / cellW).toInt().coerceIn(0, grid!!.size - 1)
                                        val row = (change.position.y / cellH).toInt().coerceIn(0, grid!!.size - 1)
                                        currentCell = Pair(row, col)
                                        change.consume()
                                    },
                                    onDragEnd = {
                                        if (startCell != null && currentCell != null) {
                                            val path = getLineCells(startCell!!, currentCell!!)
                                            val word = path.joinToString("") { (r, c) -> grid!!.letters[r][c].toString() }
                                            if (words.contains(word) && !foundWords.contains(word)) {
                                                foundWords = foundWords + word
                                                if (foundWords.size == words.size) showSuccess = true
                                            }
                                        }
                                        startCell = null
                                        currentCell = null
                                    },
                                    onDragCancel = {
                                        startCell = null
                                        currentCell = null
                                    }
                                )
                            }
                    ) {
                        val cellW = size.width / grid!!.size
                        val cellH = size.height / grid!!.size

                        // 1. Отрисовка найденных слов (зеленый фон)
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

                        // 2. Отрисовка текущего выделения (зеленый или красный)
                        if (startCell != null && currentCell != null) {
                            val path = getLineCells(startCell!!, currentCell!!)
                            val word = path.joinToString("") { (r, c) -> grid!!.letters[r][c].toString() }
                            val isValid = words.contains(word) && !foundWords.contains(word)
                            val color = if (isValid) Color(0x664CAF50) else Color(0x66FF0000)

                            for ((r, c) in path) {
                                drawRect(
                                    color = color,
                                    topLeft = Offset(c * cellW, r * cellH),
                                    size = Size(cellW, cellH)
                                )
                            }
                        }

                        // 3. Отрисовка сетки и букв
                        for (y in 0 until grid!!.size) {
                            for (x in 0 until grid!!.size) {
                                val left = x * cellW
                                val top = y * cellH
                                
                                drawRect(
                                    color = Color.LightGray,
                                    topLeft = Offset(left, top),
                                    size = Size(cellW, cellH),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f)
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
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (showSuccess) {
                    Text("🎉 Молодец! Все слова найдены!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    words.forEach { word ->
                        val isFound = foundWords.contains(word)
                        Surface(
                            color = if (isFound) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, Color.Gray)
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

private fun getLineCells(start: Pair<Int, Int>, end: Pair<Int, Int>): List<Pair<Int, Int>> {
    val (r1, c1) = start
    val (r2, c2) = end
    val path = mutableListOf<Pair<Int, Int>>()
    
    if (r1 == r2) {
        // Горизонталь
        if (c1 <= c2) {
            for (c in c1..c2) path.add(Pair(r1, c))
        } else {
            for (c in c1 downTo c2) path.add(Pair(r1, c))
        }
    } else if (c1 == c2) {
        // Вертикаль
        if (r1 <= r2) {
            for (r in r1..r2) path.add(Pair(r, c1))
        } else {
            for (r in r1 downTo r2) path.add(Pair(r, c1))
        }
    } else {
        // Диагональ
        val dr = if (r1 < r2) 1 else -1
        val dc = if (c1 < c2) 1 else -1
        val len = minOf(abs(r2 - r1), abs(c2 - c1)) + 1
        for (i in 0 until len) {
            path.add(Pair(r1 + i * dr, c1 + i * dc))
        }
    }
    return path
}
