package com.example.a4umka.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.a4umka.data.SyllableConfig

object SyllablePdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun drawSinglePage(
        canvas: Canvas,
        title: String,
        text: String,
        questions: List<String>,
        config: SyllableConfig,
        context: Context
    ) {
        canvas.drawColor(Color.parseColor("#FAFAFA"))

        // --- ШАПКА ---
        val topPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT
        }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        canvas.drawText("Дата:", 40f, 40f, topPaint)
        canvas.drawLine(80f, 42f, 180f, 42f, linePaint)
        
        canvas.drawText("Время:", PAGE_WIDTH - 160f, 40f, topPaint)
        canvas.drawLine(PAGE_WIDTH - 110f, 42f, PAGE_WIDTH - 40f, 42f, linePaint)

        // Количество слов
        val wordCount = text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val countBgPaint = Paint().apply { color = Color.parseColor("#A5D6A7"); style = Paint.Style.FILL }
        val countRect = RectF(PAGE_WIDTH / 2f - 40f, 25f, PAGE_WIDTH / 2f + 40f, 48f)
        canvas.drawRoundRect(countRect, 8f, 8f, countBgPaint)
        
        val countTextPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Слов: $wordCount", PAGE_WIDTH / 2f, 42f, countTextPaint)

        // --- ЗАГОЛОВОК ---
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 28f; color = Color.parseColor("#33691E")
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title.ifEmpty { "Чтение по слогам" }, PAGE_WIDTH / 2f, 90f, titlePaint)

        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("рассказ для чтения по слогам", PAGE_WIDTH / 2f, 110f, subtitlePaint)

        // --- ТЕКСТ ---
        val bodyPaint = Paint().apply {
            isAntiAlias = true
            textSize = if (config.isLargeFont) 32f else 24f
            color = Color.BLACK
            typeface = Typeface.DEFAULT
        }

        val margin = 40f
        var currentY = 150f
        val lineHeight = bodyPaint.textSize * 1.6f

        val paragraphs = text.split("\n")
        val lines = mutableListOf<String>()

        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = ""
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
        }

        for (line in lines) {
            var currentX = margin
            val lineWords = line.split(" ")

            for (word in lineWords) {
                if (word.isEmpty()) continue
                val syllables = SyllableUtils.splitIntoSyllables(word)
                for (syllable in syllables) {
                    canvas.drawText(syllable, currentX, currentY, bodyPaint)
                    val textWidth = bodyPaint.measureText(syllable)

                    val arcPaint = Paint().apply {
                        isAntiAlias = true; color = Color.DKGRAY; style = Paint.Style.STROKE; strokeWidth = 2f
                    }
                    val arcRect = RectF(currentX, currentY + 2f, currentX + textWidth, currentY + 18f)
                    canvas.drawArc(arcRect, 0f, 180f, false, arcPaint)

                    if (syllable.length == 1 && syllable[0].toString().lowercase() in "аеёиоуыэюя") {
                        val dotPaint = Paint().apply { color = Color.DKGRAY }
                        canvas.drawCircle(currentX + textWidth / 2f, currentY + 22f, 2.5f, dotPaint)
                    }

                    currentX += textWidth + 4f
                }
                currentX += bodyPaint.measureText(" ")
            }
            currentY += lineHeight
        }

        // --- ВОПРОСЫ ---
        if (config.showQuestions && questions.isNotEmpty()) {
            currentY += 20f
            val questionTitlePaint = Paint().apply {
                isAntiAlias = true; textSize = 18f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("Вопросы:", margin, currentY, questionTitlePaint)
            currentY += 25f

            val questionPaint = Paint().apply {
                isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT
            }

            questions.forEachIndexed { index, q ->
                val qPrefix = "${index + 1}. "
                canvas.drawText(qPrefix + q, margin + 10f, currentY, questionPaint)
                currentY += 25f
            }
        }
    }
}
