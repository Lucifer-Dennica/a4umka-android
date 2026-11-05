package com.example.a4umka.ui.components

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun LetterCanvas(
    letter: String,
    modifier: Modifier = Modifier,
    letterColor: Color = Color(0xFFFF7043),
    drawColor: Color = Color(0xFF2E1F1A),
    strokeWidthDp: Float = 4f,
    onPathsChanged: (List<Path>) -> Unit = {}
) {
    // Все штрихи, нарисованные пользователем
    val strokes = remember(letter) { mutableStateListOf<Path>() }
    var currentPath by remember(letter) { mutableStateOf<Path?>(null) }

    LaunchedEffect(strokes.size) {
        onPathsChanged(strokes.toList())
    }

    Canvas(
        modifier = modifier.pointerInput(letter) {
            detectDragGestures(
                onDragStart = { offset ->
                    val p = Path().apply { moveTo(offset.x, offset.y) }
                    currentPath = p
                    strokes.add(p)
                },
                onDrag = { change, _ ->
                    change.consume()
                    currentPath?.lineTo(change.position.x, change.position.y)
                },
                onDragEnd = { currentPath = null },
                onDragCancel = { currentPath = null }
            )
        }
    ) {
        val w = size.width
        val h = size.height

        // 1) Рисуем пунктирную букву-образец
        val outlinePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = letterColor.toArgb()
            strokeWidth = strokeWidthDp * 2f
            pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
        }

        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = minOf(w, h) * 0.7f
            color = letterColor.toArgb()
        }

        val letterPath = Path()
        textPaint.getTextPath(
            letter, 0, letter.length,
            w * 0.15f,
            h * 0.8f,
            letterPath
        )
        drawContext.canvas.nativeCanvas.drawPath(letterPath, outlinePaint)

        // 2) Рисуем штрихи пользователя
        val userPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
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
