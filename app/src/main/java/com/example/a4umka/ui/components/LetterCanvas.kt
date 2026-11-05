package com.example.a4umka.ui.components

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput

enum class LetterStyle { PRINT, CURSIVE }

@Composable
fun LetterCanvas(
    letter: String,
    modifier: Modifier = Modifier,
    letterStyle: LetterStyle = LetterStyle.PRINT,
    letterColor: Color = Color(0xFFFF7043),
    drawColor: Color = Color(0xFF2E1F1A),
    strokeWidthDp: Float = 4f,
    onPathsChanged: (List<Path>) -> Unit = {}
) {
    val strokes = remember(letter, letterStyle) { mutableStateListOf<Path>() }
    var currentPath by remember(letter, letterStyle) { mutableStateOf<Path?>(null) }
    var redrawTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(strokes.size) {
        onPathsChanged(strokes.toList())
    }

    Canvas(
        modifier = modifier.pointerInput(letter, letterStyle) {
            detectDragGestures(
                onDragStart = { offset ->
                    val p = Path().apply { moveTo(offset.x, offset.y) }
                    currentPath = p
                    strokes.add(p)
                    redrawTrigger++
                },
                onDrag = { change, _ ->
                    change.consume()
                    currentPath?.lineTo(change.position.x, change.position.y)
                    redrawTrigger++
                },
                onDragEnd = { currentPath = null },
                onDragCancel = { currentPath = null }
            )
        }
    ) {
        @Suppress("UNUSED_EXPRESSION")
        redrawTrigger

        val w = size.width
        val h = size.height

        val typeface = when (letterStyle) {
            LetterStyle.PRINT -> Typeface.DEFAULT
            LetterStyle.CURSIVE -> Typeface.create("cursive", Typeface.NORMAL)
        }

        // 1) Пунктирный образец буквы
        val outlinePaint = Paint().apply {
            isAntiAlias = true
            this.style = Paint.Style.STROKE   // ← явно this, чтобы не спутать с letterStyle
            color = letterColor.toArgb()
            strokeWidth = strokeWidthDp * 2f
            pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
        }
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = minOf(w, h) * 0.7f
            color = letterColor.toArgb()
            this.typeface = typeface
        }
        val letterPath = Path()
        textPaint.getTextPath(letter, 0, letter.length, w * 0.15f, h * 0.8f, letterPath)
        drawContext.canvas.nativeCanvas.drawPath(letterPath, outlinePaint)

        // 2) Штрихи пользователя
        val userPaint = Paint().apply {
            isAntiAlias = true
            this.style = Paint.Style.STROKE   // ← тоже явно
            color = drawColor.toArgb()
            strokeWidth = strokeWidthDp * 3f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        strokes.forEach { path ->
            drawContext.canvas.nativeCanvas.drawPath(path, userPaint)
        }
    }
}
