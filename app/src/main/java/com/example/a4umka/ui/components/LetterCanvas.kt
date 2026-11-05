package com.example.a4umka.ui.components

import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.example.a4umka.R

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
    val context = LocalContext.current
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

        // Загружаем наш шрифт Caveat
        val typeface = when (letterStyle) {
            LetterStyle.PRINT -> Typeface.DEFAULT
            LetterStyle.CURSIVE -> ResourcesCompat.getFont(context, R.font.caveat_regular)
                ?: Typeface.create("cursive", Typeface.NORMAL)
        }

        // Если пропись, рисуем "А а", если печатная - просто "А"
        val textToDraw = if (letterStyle == LetterStyle.CURSIVE) {
            "$letter ${letter.lowercase()}"
        } else {
            letter
        }

        // 1) Пунктирный образец буквы
        val outlinePaint = Paint().apply {
            isAntiAlias = true
            this.style = Paint.Style.STROKE
            color = letterColor.toArgb()
            strokeWidth = strokeWidthDp * 2f
            pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
        }
        
        val textPaint = Paint().apply {
            isAntiAlias = true
            // Если рисуем две буквы (пропись), делаем размер чуть меньше, чтобы влезло
            textSize = if (letterStyle == LetterStyle.CURSIVE) minOf(w, h) * 0.45f else minOf(w, h) * 0.7f
            color = letterColor.toArgb()
            this.typeface = typeface
        }

        val letterPath = Path()
        // Рисуем текст в координатах (0,0), чтобы потом отцентрировать
        textPaint.getTextPath(textToDraw, 0, textToDraw.length, 0f, 0f, letterPath)

        // ЦЕНТРИРОВАНИЕ БУКВЫ
        val bounds = RectF()
        letterPath.computeBounds(bounds, true)
        val offsetX = (w - bounds.width()) / 2f - bounds.left
        val offsetY = (h - bounds.height()) / 2f - bounds.top
        
        val matrix = Matrix()
        matrix.postTranslate(offsetX, offsetY)
        letterPath.transform(matrix)

        drawContext.canvas.nativeCanvas.drawPath(letterPath, outlinePaint)

        // 2) Штрихи пользователя
        val userPaint = Paint().apply {
            isAntiAlias = true
            this.style = Paint.Style.STROKE
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
