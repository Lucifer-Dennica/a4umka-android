package com.example.a4umka.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.example.a4umka.data.SyllableConfig
import com.example.a4umka.data.SyllableData
import com.example.a4umka.data.SyllableSource
import kotlin.math.max

object SyllablePdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun drawPage(canvas: Canvas, config: SyllableConfig, context: Context) {
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 28f
            color = Color.DKGRAY
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(config.title.ifEmpty { "Чтение по слогам" }, PAGE_WIDTH / 2f, 50f, titlePaint)

        val bodyPaint = Paint().apply {
            isAntiAlias = true
            textSize = if (config.isLargeFont) 32f else 24f
            color = Color.BLACK
            typeface = Typeface.DEFAULT
        }

        val textToDraw = config.text
        val margin = 40f
        var currentY = 100f
        val lineHeight = bodyPaint.textSize * 1.5f

        // Разбиваем текст на строки (по словам, чтобы влезало в ширину)
        val words = textToDraw.split(" ")
        var currentLine = ""
        val lines = mutableListOf<String>()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        for (line in lines) {
            var currentX = margin
            val lineWords = line.split(" ")

            for (word in lineWords) {
                if (word.isEmpty()) continue
                val syllables = SyllableUtils.splitIntoSyllables(word)
                for (syllable in syllables) {
                    // Рисуем текст
                    canvas.drawText(syllable, currentX, currentY, bodyPaint)
                    val textWidth = bodyPaint.measureText(syllable)

                    // Рисуем дугу под слогом
                    val arcPaint = Paint().apply {
                        isAntiAlias = true
                        color = Color.DKGRAY
                        style = Paint.Style.STROKE
                        strokeWidth = 2f
                    }
                    val rect = RectF(currentX, currentY, currentX + textWidth, currentY + 20f)
                    canvas.drawArc(rect, 0f, 180f, false, arcPaint)

                    // Если слог из одной буквы и это гласная, ставим точку
                    if (syllable.length == 1 && syllable[0].toString().lowercase() in "аеёиоуыэюя") {
                        val dotPaint = Paint().apply { color = Color.DKGRAY }
                        canvas.drawCircle(currentX + textWidth / 2f, currentY + 25f, 3f, dotPaint)
                    }

                    currentX += textWidth + 5f // небольшой отступ между слогами
                }
                currentX += bodyPaint.measureText(" ") // пробел между словами
            }
            currentY += lineHeight
        }

        // Вопросы
        if (config.showQuestions) {
            currentY += 20f
            val questionPaint = Paint().apply {
                isAntiAlias = true
                textSize = 18f
                color = Color.DKGRAY
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("Вопросы:", margin, currentY, questionPaint)
            currentY += 25f

            val readyText = SyllableData.readyTexts.find { it.text == config.text }
            readyText?.questions?.forEachIndexed { index, question ->
                canvas.drawText("${index + 1}. $question", margin + 10f, currentY, questionPaint.apply { typeface = Typeface.DEFAULT })
                currentY += 25f
            }
        }
    }
}
