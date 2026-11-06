package com.example.a4umka.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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

    fun drawRibbon(canvas: Canvas, config: RibbonConfig, context: Context) {
        canvas.drawColor(Color.parseColor("#FAFAFA"))

        val typeface = ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        
        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 32f
            color = Color.DKGRAY
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Лента букв", PAGE_WIDTH / 2f, 50f, titlePaint)

        // Настройки сетки
        val startX = 40f
        val startY = 100f
        val cellSize = 36f
        val gap = 2f
        val colWidth = cellSize + gap
        val rowHeight = cellSize + gap
        
        // Определяем пропущенные буквы (если режим MISSING)
        val missingSet = mutableSetOf<String>()
        if (config.mode == RibbonMode.MISSING) {
            val allLetters = (RibbonData.topVowels + RibbonData.bottomVowels + RibbonData.topConsonants + RibbonData.bottomConsonants).map { it.char }
            val countToHide = when(config.missingCount) {
                MissingCount.EIGHT -> 8
                MissingCount.SIXTEEN -> 16
                MissingCount.ALL -> allLetters.size
            }
            // Используем seed для стабильности в превью (чтобы не мигало при скролле)
            val random = Random(config.case.ordinal + config.missingCount.ordinal)
            missingSet.addAll(allLetters.shuffled(random).take(countToHide))
        }

        // --- РИСУЕМ ГЛАСНЫЕ ---
        val vowelStartX = startX
        RibbonData.topVowels.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, vowelStartX + index * colWidth, startY, cellSize, missingSet, typeface)
            drawCell(canvas, RibbonData.bottomVowels[index], config, vowelStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, typeface)
        }

        // --- РИСУЕМ СОГЛАСНЫЕ ---
        val consonantStartX = vowelStartX + 5 * colWidth + 20f // Отступ после гласных
        RibbonData.topConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY, cellSize, missingSet, typeface)
        }
        RibbonData.bottomConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, typeface)
        }

        // --- РИСУЕМ ЗНАКИ ---
        val signStartX = consonantStartX + 11 * colWidth + 10f
        RibbonData.signs.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, signStartX, startY + index * rowHeight, cellSize, missingSet, typeface)
        }

        // Подсказка (если включена)
        if (config.mode == RibbonMode.MISSING && config.showHint && missingSet.isNotEmpty()) {
            val hintPaint = Paint().apply {
                isAntiAlias = true
                textSize = 16f
                color = Color.GRAY
                typeface = Typeface.DEFAULT
            }
            canvas.drawText("Пропущенные буквы: ${missingSet.joinToString(", ")}", startX, startY + 2 * rowHeight + 30f, hintPaint)
        }

        // Рисуем легенду (как на скрине) в самом низу
        drawLegend(canvas, startX, PAGE_HEIGHT - 200f)
    }

    private fun drawCell(canvas: Canvas, letter: RibbonLetter, config: RibbonConfig, x: Float, y: Float, size: Float, missing: Set<String>, font: Typeface) {
        val rectPaint = Paint().apply { style = Paint.Style.FILL }
        
        // Цвета
        val color = when (letter.color) {
            RibbonColor.RED -> Color.parseColor("#EF5350")
            RibbonColor.BLUE -> Color.parseColor("#42A5F5")
            RibbonColor.GREEN -> Color.parseColor("#66BB6A")
            RibbonColor.WHITE -> Color.WHITE
            RibbonColor.BOTH -> Color.WHITE // Обработаем отдельно
        }

        // Фон ячейки
        if (config.mode == RibbonMode.COLORING) {
            rectPaint.color = Color.WHITE
            rectPaint.style = Paint.Style.STROKE
            rectPaint.strokeWidth = 2f
            canvas.drawRect(x, y, x + size, y + size, rectPaint)
            
            val outlinePaint = Paint().apply { 
                color = if(letter.color == RibbonColor.WHITE) Color.GRAY else color
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(x, y, x + size, y + size, outlinePaint)
        } else {
            rectPaint.color = color
            canvas.drawRect(x, y, x + size, y + size, rectPaint)

            // Диагональ для BOTH (сине-зеленый)
            if (letter.color == RibbonColor.BOTH) {
                val path = Path().apply {
                    moveTo(x, y + size)
                    lineTo(x + size, y)
                    lineTo(x + size, y + size)
                    close()
                }
                val diagPaint = Paint().apply { 
                    color = Color.parseColor("#66BB6A")
                    style = Paint.Style.FILL 
                }
                canvas.drawPath(path, diagPaint)
                
                // Добавляем синюю половину (уже закрашено выше, но для верности нарисуем синий треугольник)
                val pathBlue = Path().apply {
                    moveTo(x, y)
                    lineTo(x + size, y)
                    lineTo(x, y + size)
                    close()
                }
                val bluePaint = Paint().apply { 
                    color = Color.parseColor("#42A5F5")
                    style = Paint.Style.FILL 
                }
                canvas.drawPath(pathBlue, bluePaint)
            }

            // Рамка
            val borderPaint = Paint().apply { 
                color = if(letter.color == RibbonColor.WHITE) Color.LTGRAY else Color.TRANSPARENT
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRect(x, y, x + size, y + size, borderPaint)
        }

        // Текст
        if (config.mode != RibbonMode.COLORING) {
            val text = when (config.case) {
                RibbonCase.BOTH -> letter.char
                RibbonCase.UPPERCASE -> letter.char.substring(0, 1)
                RibbonCase.LOWERCASE -> letter.char.substring(letter.char.length - 1)
            }

            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 22f
                this.typeface = font
                textAlign = Paint.Align.CENTER
            }

            if (letter.char in missing) {
                // Если буква пропущена - рисуем пустой квадрат с пунктиром
                val missPaint = Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                canvas.drawRect(x + 2, y + 2, x + size - 2, y + size - 2, missPaint)
                val borderPaint = Paint().apply {
                    color = Color.GRAY
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f)
                }
                canvas.drawRect(x, y, x + size, y + size, borderPaint)
            } else {
                textPaint.color = if (letter.color == RibbonColor.WHITE) Color.DKGRAY else Color.WHITE
                val baselineY = y + (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
                canvas.drawText(text, x + size / 2f, baselineY, textPaint)
            }
        }
    }

    private fun drawLegend(canvas: Canvas, x: Float, y: Float) {
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 14f
            color = Color.DKGRAY
            typeface = Typeface.DEFAULT
        }
        
        var currentY = y
        // Легенда: Гласные
        canvas.drawRect(x, currentY - 10f, x + 15f, currentY + 5f, Paint().apply { color = Color.parseColor("#EF5350") })
        canvas.drawText("Гласные: А О У Ы Э — после твердых, Я Ё Ю И Е — после мягких", x + 25f, currentY, textPaint)
        currentY += 25f
        
        // Легенда: Согласные всегда твердые
        canvas.drawRect(x, currentY - 10f, x + 15f, currentY + 5f, Paint().apply { color = Color.parseColor("#42A5F5") })
        canvas.drawText("Всегда твердые: Ж Ш Ц", x + 25f, currentY, textPaint)
        currentY += 25f

        // Легенда: Согласные всегда мягкие
        canvas.drawRect(x, currentY - 10f, x + 15f, currentY + 5f, Paint().apply { color = Color.parseColor("#66BB6A") })
        canvas.drawText("Всегда мягкие: Й Ч Щ", x + 25f, currentY, textPaint)
        currentY += 25f

        // Легенда: Согласные тв/мяг
        val bothRect = Paint().apply { color = Color.parseColor("#42A5F5") }
        canvas.drawRect(x, currentY - 10f, x + 15f, currentY + 5f, bothRect)
        val greenPath = Path().apply {
            moveTo(x, currentY + 5f)
            lineTo(x + 15f, currentY - 10f)
            lineTo(x + 15f, currentY + 5f)
            close()
        }
        canvas.drawPath(greenPath, Paint().apply { color = Color.parseColor("#66BB6A") })
        canvas.drawText("Твердые или мягкие: остальные согласные", x + 25f, currentY, textPaint)
    }
}
