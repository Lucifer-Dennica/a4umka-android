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
import com.example.a4umka.data.*
import kotlin.random.Random

object RibbonPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun drawRibbon(canvas: Canvas, config: RibbonConfig, context: Context, isAnswerKey: Boolean = false) {
        canvas.drawColor(Color.parseColor("#FAFAFA"))

        val font = ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        
        val titleText = when {
            isAnswerKey -> "Ответы"
            config.mode == RibbonMode.DESK -> "Лента букв"
            config.mode == RibbonMode.MISSING -> "Впиши пропущенные буквы"
            config.mode == RibbonMode.COLORING -> "Раскрась ленту букв"
            else -> "Лента букв"
        }

        val subtitleText = when {
            isAnswerKey -> "лента букв целиком"
            config.mode == RibbonMode.DESK -> "Гласные и согласные, звонкие и глухие, твердые и мягкие"
            config.mode == RibbonMode.MISSING -> "Впиши буквы в пустые клетки: цвет клетки и соседи — подсказка"
            config.mode == RibbonMode.COLORING -> "Раскрась клетки по правилам"
            else -> ""
        }

        val titlePaint = Paint().apply {
            this.isAntiAlias = true
            this.textSize = 32f
            this.color = Color.DKGRAY
            this.typeface = font
            this.textAlign = Paint.Align.CENTER
        }
        canvas.drawText(titleText, PAGE_WIDTH / 2f, 50f, titlePaint)

        val subtitlePaint = Paint().apply {
            this.isAntiAlias = true
            this.textSize = 14f
            this.color = Color.GRAY
            this.typeface = Typeface.DEFAULT
            this.textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitleText, PAGE_WIDTH / 2f, 70f, subtitlePaint)

        if (config.mode != RibbonMode.DESK || isAnswerKey) {
            val fieldPaint = Paint().apply {
                this.isAntiAlias = true
                this.textSize = 16f
                this.color = Color.DKGRAY
                this.typeface = Typeface.DEFAULT
            }
            val linePaint = Paint().apply { this.color = Color.LTGRAY; this.strokeWidth = 1f }
            
            canvas.drawText("Имя:", 40f, 110f, fieldPaint)
            canvas.drawLine(80f, 112f, 200f, 112f, linePaint)
            canvas.drawText("Дата:", PAGE_WIDTH - 160f, 110f, fieldPaint)
            canvas.drawLine(PAGE_WIDTH - 115f, 112f, PAGE_WIDTH - 40f, 112f, linePaint)
        }

        val startX = 40f
        val startY = 140f
        val cellSize = 36f
        val gap = 2f
        val colWidth = cellSize + gap
        val rowHeight = cellSize + gap
        
        val missingSet = mutableSetOf<String>()
        if (config.mode == RibbonMode.MISSING && !isAnswerKey) {
            val allLetters = (RibbonData.topVowels + RibbonData.bottomVowels + RibbonData.topConsonants + RibbonData.bottomConsonants).map { it.char }
            val countToHide = when(config.missingCount) {
                MissingCount.EIGHT -> 8
                MissingCount.SIXTEEN -> 16
                MissingCount.ALL -> allLetters.size
            }
            // ИСПОЛЬЗУЕМ SEED ИЗ КОНФИГА ДЛЯ ГЕНЕРАЦИИ РАЗНЫХ ВАРИАНТОВ
            val random = Random(config.seed) 
            missingSet.addAll(allLetters.shuffled(random).take(countToHide))
        }

        val vowelStartX = startX
        RibbonData.topVowels.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, vowelStartX + index * colWidth, startY, cellSize, missingSet, font, isAnswerKey)
            drawCell(canvas, RibbonData.bottomVowels[index], config, vowelStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        val consonantStartX = vowelStartX + 5 * colWidth + 20f
        RibbonData.topConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY, cellSize, missingSet, font, isAnswerKey)
        }
        RibbonData.bottomConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        val signStartX = consonantStartX + 11 * colWidth + 10f
        RibbonData.signs.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, signStartX, startY + index * rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        if (config.mode == RibbonMode.MISSING && config.showHint && missingSet.isNotEmpty()) {
            val hintBoxTop = startY + 2 * rowHeight + 20f
            val hintBoxBottom = hintBoxTop + 40f
            val hintBoxPaint = Paint().apply { this.color = Color.WHITE; this.style = Paint.Style.FILL }
            val hintBoxBorder = Paint().apply { this.color = Color.LTGRAY; this.style = Paint.Style.STROKE; this.strokeWidth = 1f; this.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f) }
            canvas.drawRect(startX, hintBoxTop, PAGE_WIDTH - startX, hintBoxBottom, hintBoxPaint)
            canvas.drawRect(startX, hintBoxTop, PAGE_WIDTH - startX, hintBoxBottom, hintBoxBorder)

            val hintTextPaint = Paint().apply { this.isAntiAlias = true; this.textSize = 16f; this.color = Color.DKGRAY; this.typeface = Typeface.DEFAULT }
            val lostLettersStr = missingSet.joinToString(", ")
            canvas.drawText("Эти буквы потерялись: $lostLettersStr", startX + 10f, hintBoxTop + 25f, hintTextPaint)
        }

        drawLegend(canvas, startX, PAGE_HEIGHT - 200f)
    }

    private fun drawCell(canvas: Canvas, letter: RibbonLetter, config: RibbonConfig, x: Float, y: Float, size: Float, missing: Set<String>, font: Typeface, isAnswerKey: Boolean) {
        val rectPaint = Paint().apply { this.style = Paint.Style.FILL }
        
        val cellColor = when (letter.color) {
            RibbonColor.RED -> Color.parseColor("#EF5350")
            RibbonColor.BLUE -> Color.parseColor("#42A5F5")
            RibbonColor.GREEN -> Color.parseColor("#66BB6A")
            RibbonColor.WHITE -> Color.WHITE
            RibbonColor.BOTH -> Color.WHITE
        }

        if (config.mode == RibbonMode.COLORING) {
            rectPaint.color = Color.WHITE
            rectPaint.style = Paint.Style.STROKE
            rectPaint.strokeWidth = 2f
            canvas.drawRect(x, y, x + size, y + size, rectPaint)
            
            val outlinePaint = Paint().apply { this.color = if(letter.color == RibbonColor.WHITE) Color.GRAY else cellColor; this.style = Paint.Style.STROKE; this.strokeWidth = 2f }
            canvas.drawRect(x, y, x + size, y + size, outlinePaint)
        } else {
            rectPaint.color = cellColor
            canvas.drawRect(x, y, x + size, y + size, rectPaint)

            if (letter.color == RibbonColor.BOTH) {
                val path = Path().apply { moveTo(x, y + size); lineTo(x + size, y); lineTo(x + size, y + size); close() }
                canvas.drawPath(path, Paint().apply { this.color = Color.parseColor("#66BB6A"); this.style = Paint.Style.FILL })
                
                val pathBlue = Path().apply { moveTo(x, y); lineTo(x + size, y); lineTo(x, y + size); close() }
                canvas.drawPath(pathBlue, Paint().apply { this.color = Color.parseColor("#42A5F5"); this.style = Paint.Style.FILL })
            }

            val borderPaint = Paint().apply { this.color = if(letter.color == RibbonColor.WHITE) Color.LTGRAY else Color.TRANSPARENT; this.style = Paint.Style.STROKE; this.strokeWidth = 1f }
            canvas.drawRect(x, y, x + size, y + size, borderPaint)
        }

        if (config.mode != RibbonMode.COLORING) {
            val text = when (config.case) {
                RibbonCase.BOTH -> letter.char
                RibbonCase.UPPERCASE -> letter.char.substring(0, 1)
                RibbonCase.LOWERCASE -> letter.char.substring(letter.char.length - 1)
            }

            val textPaint = Paint().apply { this.isAntiAlias = true; this.textSize = 22f; this.typeface = font; this.textAlign = Paint.Align.CENTER }

            if (letter.char in missing && !isAnswerKey) {
                // Рисуем пустую клетку с пунктиром
                canvas.drawRect(x + 2, y + 2, x + size - 2, y + size - 2, Paint().apply { this.color = Color.WHITE; this.style = Paint.Style.FILL })
                canvas.drawRect(x, y, x + size, y + size, Paint().apply { this.color = Color.GRAY; this.style = Paint.Style.STROKE; this.strokeWidth = 2f; this.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f) })
            } else {
                // Обычная отрисовка буквы
                textPaint.color = if (letter.color == RibbonColor.WHITE) Color.DKGRAY else Color.WHITE
                
                // ЕСЛИ ЭТО ОТВЕТ И БУКВА БЫЛА ПРОПУЩЕНА - ВЫДЕЛЯЕМ ЕЕ КРАСНЫМ ЦВЕТОМ
                if (isAnswerKey && letter.char in missing) {
                    textPaint.color = Color.RED
                    textPaint.isFakeBoldText = true
                }
                
                val baselineY = y + (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
                canvas.drawText(text, x + size / 2f, baselineY, textPaint)
            }
        }
    }

    private fun drawLegend(canvas: Canvas, x: Float, y: Float) {
        // (Код легенды остался без изменений, он идентичен предыдущему)
        val textPaint = Paint().apply { this.isAntiAlias = true; this.textSize = 14f; this.color = Color.DKGRAY; this.typeface = Typeface.DEFAULT }
        var currentY = y
        
        canvas.drawRect(x, currentY - 10f, x + 15f, currentY + 5f, Paint().apply { this.color = Color.parseColor("#EF5350") })
        canvas.drawText("Гласные: А О У Ы Э — после твердых, Я Ё Ю И Е — после мягких", x + 25f, currentY, textPaint)
        currentY += 25f
        
        canvas.drawRect(x, currentY - 10f, x + 15f, currentY + 5f, Paint().apply { this.color = Color.parseColor("#42A5F5") })
        canvas.drawText("Всегда твердые: Ж Ш Ц", x + 25f, currentY, textPaint)
        currentY += 25f

        canvas.drawRect(x, currentY - 10f, x + 15f, currentY + 5f, Paint().apply { this.color = Color.parseColor("#66BB6A") })
        canvas.drawText("Всегда мягкие: Й Ч Щ", x + 25f, currentY, textPaint)
        currentY += 25f

        canvas.drawRect(x, currentY - 10f, x + 15f, currentY + 5f, Paint().apply { this.color = Color.parseColor("#42A5F5") })
        val greenPath = Path().apply { moveTo(x, currentY + 5f); lineTo(x + 15f, currentY - 10f); lineTo(x + 15f, currentY + 5f); close() }
        canvas.drawPath(greenPath, Paint().apply { this.color = Color.parseColor("#66BB6A") })
        canvas.drawText("Твердые или мягкие: остальные согласные", x + 25f, currentY, textPaint)
    }
}
