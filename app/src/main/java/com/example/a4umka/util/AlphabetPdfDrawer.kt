package com.example.a4umka.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.a4umka.R
import com.example.a4umka.data.*
import kotlin.math.ceil
import kotlin.math.sqrt

object AlphabetPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun drawPoster(canvas: Canvas, config: AlphabetConfig, context: Context) {
        val bgColor = when (config.theme) {
            PosterTheme.WHITE -> Color.WHITE
            PosterTheme.GREEN -> Color.parseColor("#E8F5E9")
            PosterTheme.PASTEL -> Color.parseColor("#FFF8F3")
        }
        canvas.drawColor(bgColor)

        // Выбор шрифта в зависимости от настроек
        val typeface = if (config.isCursive) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

        val letters = AlphabetData.russianAlphabet.filter {
            when (config.filter) {
                AlphabetFilter.ALL -> true
                AlphabetFilter.VOWELS -> it.char in listOf('А', 'Е', 'Ё', 'И', 'О', 'У', 'Ы', 'Э', 'Ю', 'Я')
                AlphabetFilter.CONSONANTS -> it.char !in listOf('А', 'Е', 'Ё', 'И', 'О', 'У', 'Ы', 'Э', 'Ю', 'Я', 'Ъ', 'Ь')
            }
        }

        val count = letters.size
        if (count == 0) return

        val cols = ceil(sqrt(count.toFloat())).toInt()
        val rows = ceil(count.toFloat() / cols).toInt()

        val margin = 40f
        val availableWidth = PAGE_WIDTH - margin * 2
        val availableHeight = PAGE_HEIGHT - margin * 2 - 60f
        val cellWidth = availableWidth / cols
        val cellHeight = availableHeight / rows

        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 32f
            color = Color.DKGRAY
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Алфавит", PAGE_WIDTH / 2f, margin + 20f, titlePaint)

        val letterPaint = Paint().apply {
            isAntiAlias = true
            textSize = minOf(cellWidth, cellHeight) * 0.35f
            color = Color.parseColor("#FF7043")
            this.typeface = typeface // <-- ПРИМЕНЯЕМ ШРИФТ
            textAlign = Paint.Align.CENTER
        }

        val wordPaint = Paint().apply {
            isAntiAlias = true
            textSize = minOf(cellWidth, cellHeight) * 0.15f
            color = Color.DKGRAY
            this.typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        letters.forEachIndexed { index, item ->
            val col = index % cols
            val row = index / cols
            
            val left = margin + col * cellWidth
            val top = margin + 60f + row * cellHeight

            canvas.drawRect(left, top, left + cellWidth, top + cellHeight, linePaint.apply { style = Paint.Style.STROKE })

            val centerX = left + cellWidth / 2f

            val text = when (config.case) {
                AlphabetCase.UPPERCASE -> item.uppercase
                AlphabetCase.LOWERCASE -> item.lowercase
                AlphabetCase.BOTH -> "${item.uppercase} ${item.lowercase}"
            }
            
            val fontMetrics = letterPaint.fontMetrics
            val baselineY = top + cellHeight * 0.35f - (fontMetrics.ascent + fontMetrics.descent) / 2f
            canvas.drawText(text, centerX, baselineY, letterPaint)

            if (config.showImages) {
                val picSize = minOf(cellWidth, cellHeight) * 0.25f
                val picPaint = Paint().apply { color = Color.parseColor("#FFD4C2") }
                canvas.drawCircle(centerX, top + cellHeight * 0.6f, picSize / 2f, picPaint)

                val wordBaselineY = top + cellHeight * 0.85f
                canvas.drawText(item.word, centerX, wordBaselineY, wordPaint)
            }
        }
    }
}
