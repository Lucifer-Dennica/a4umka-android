package com.example.a4umka.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.a4umka.R
import com.example.a4umka.data.LetterCase
import com.example.a4umka.data.PdfExportConfig

object PdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun drawWorksheet(
        canvas: Canvas,
        letter: String,
        config: PdfExportConfig,
        context: Context
    ) {
        canvas.drawColor(Color.WHITE)

        val margin = 40f
        val contentTop = 100f
        val contentBottom = PAGE_HEIGHT - 40f
        val rowHeight = (contentBottom - contentTop) / config.rowsCount

        val typeface = if (config.isCursive) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = rowHeight * 0.55f
            this.typeface = typeface
            color = Color.parseColor("#FF7043")
        }

        // Кисть для пунктирного контура букв
        val outlinePaint = Paint(textPaint).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }

        // Кисти для линий
        val solidLinePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#D0D0D0")
            strokeWidth = 1.5f
        }
        val dashedLinePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 1.5f
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }

        // Заголовок листа
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 28f
            color = Color.DKGRAY
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Пропись: $letter", margin, 60f, titlePaint)

        for (i in 0 until config.rowsCount) {
            val rowTop = contentTop + i * rowHeight
            val rowBottom = rowTop + rowHeight
            val centerY = rowTop + rowHeight / 2f

            if (config.showGuides) {
                // Верхняя линия
                canvas.drawLine(margin, rowTop + rowHeight * 0.15f, PAGE_WIDTH - margin, rowTop + rowHeight * 0.15f, solidLinePaint)
                // Нижняя линия
                canvas.drawLine(margin, rowBottom - rowHeight * 0.15f, PAGE_WIDTH - margin, rowBottom - rowHeight * 0.15f, solidLinePaint)
                // Пунктирная средняя
                canvas.drawLine(margin, centerY, PAGE_WIDTH - margin, centerY, dashedLinePaint)
            }

            // Формируем текст
            val textToDraw = when (config.letterCase) {
                LetterCase.UPPERCASE -> "$letter  $letter  $letter  $letter  $letter  $letter"
                LetterCase.LOWERCASE -> "${letter.lowercase()}  ${letter.lowercase()}  ${letter.lowercase()}  ${letter.lowercase()}  ${letter.lowercase()}  ${letter.lowercase()}"
                LetterCase.BOTH -> "$letter ${letter.lowercase()}   $letter ${letter.lowercase()}   $letter ${letter.lowercase()}"
            }

            // Центрируем текст
            val textWidth = textPaint.measureText(textToDraw)
            val x = margin + (PAGE_WIDTH - margin * 2 - textWidth) / 2f

            val fontMetrics = textPaint.fontMetrics
            val baselineY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f

            // Рисуем контуром
            val textPath = Path()
            textPaint.getTextPath(textToDraw, 0, textToDraw.length, x, baselineY, textPath)
            canvas.drawPath(textPath, outlinePaint)
        }
    }
}
