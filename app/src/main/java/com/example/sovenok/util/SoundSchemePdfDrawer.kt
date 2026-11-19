package com.example.sovenok.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.Bitmap
import com.example.sovenok.data.SoundSchemeColors
import com.example.sovenok.data.SoundSchemeConfig
import com.example.sovenok.data.SoundSchemeData
import com.example.sovenok.data.SoundType
import com.example.sovenok.data.SoundWord

object SoundSchemePdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: SoundSchemeConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val words = SoundSchemeData.getWords(config)

        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(canvas, config, words, isAnswer = false)
        pages.add(bmp)

        if (config.showAnswers) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.drawColor(Color.WHITE)
            drawPage(ansCanvas, config, words, isAnswer = true)
            pages.add(ansBmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: SoundSchemeConfig, context: Context) {
        val words = SoundSchemeData.getWords(config)
        var pageNum = 1

        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
        var page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(page.canvas, config, words, isAnswer = false)
        document.finishPage(page)

        if (config.showAnswers) {
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.WHITE)
            drawPage(page.canvas, config, words, isAnswer = true)
            document.finishPage(page)
        }
    }

    private fun drawPage(canvas: Canvas, config: SoundSchemeConfig, words: List<SoundWord>, isAnswer: Boolean) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // Шапка
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        canvas.drawText("Дата:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)

        val countStr = "Слов: ${words.size}"
        val countWidth = headerPaint.measureText(countStr)
        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - countWidth/2f - 10f, 25f, PAGE_WIDTH/2f + countWidth/2f + 10f, 45f), 5f, 5f, rectPaint)
        canvas.drawText(countStr, PAGE_WIDTH/2f - countWidth/2f, 40f, headerPaint)

        canvas.drawText("Имя:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 32f; color = textColor; 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        val titleText = if (isAnswer) "Звуковая схема слова (Ответы)" else "Звуковая схема слова"
        canvas.drawText(titleText, PAGE_WIDTH / 2f, 90f, titlePaint)

        val subPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }
        canvas.drawText("раскрась схему: посчитай звуки и закрась клетки по цветам", PAGE_WIDTH / 2f, 110f, subPaint)

        // Легенда
        val legendY = 140f
        val squareSize = 14f
        val legendTextPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY }
        
        var currentX = PAGE_WIDTH / 2f - 150f
        
        // Гласный
        canvas.drawRect(currentX, legendY - 10f, currentX + squareSize, legendY + 4f, Paint().apply { color = Color.parseColor("#E53935") })
        canvas.drawText("гласный звук", currentX + 20f, legendY, legendTextPaint)
        
        // Согласный (твердый)
        currentX += 110f
        canvas.drawRect(currentX, legendY - 10f, currentX + squareSize, legendY + 4f, Paint().apply { color = Color.parseColor("#1E88E5") })
        canvas.drawText("согласный звук", currentX + 20f, legendY, legendTextPaint)

        // Мягкий согласный (если 3 цвета)
        if (config.colors == SoundSchemeColors.THREE) {
            currentX += 130f
            canvas.drawRect(currentX, legendY - 10f, currentX + squareSize, legendY + 4f, Paint().apply { color = Color.parseColor("#43A047") })
            canvas.drawText("мягкий согласный", currentX + 20f, legendY, legendTextPaint)
        }

        // Сетка слов (2 колонки x 3 строки = 6 слов)
        val startY = 200f
        val rowHeight = 100f
        val colWidth = (PAGE_WIDTH - margin * 2) / 2f

        val emojiPaint = Paint().apply { textSize = 36f; textAlign = Paint.Align.CENTER }
        val wordPaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.LEFT }
        val boxPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1.5f; color = Color.GRAY }
        val fillPaint = Paint().apply { style = Paint.Style.FILL }
        val boxSize = 34f
        val boxSpacing = 6f

        for (i in words.indices) {
            val col = i % 2
            val row = i / 2
            val itemLeft = margin + col * colWidth
            val itemTop = startY + row * rowHeight

            val word = words[i]

            // Эмодзи
            canvas.drawText(word.emoji, itemLeft + 20f, itemTop + 40f, emojiPaint)

            // Слово
            canvas.drawText(word.word, itemLeft + 60f, itemTop + 40f, wordPaint)

            // Клеточки
            var boxX = itemLeft + 60f
            val boxY = itemTop + 55f

            for (j in word.sounds.indices) {
                val type = word.sounds[j]
                val rect = RectF(boxX, boxY, boxX + boxSize, boxY + boxSize)
                
                if (isAnswer) {
                    val color = when (type) {
                        SoundType.VOWEL -> Color.parseColor("#E53935")
                        SoundType.HARD -> Color.parseColor("#1E88E5")
                        SoundType.SOFT -> if (config.colors == SoundSchemeColors.TWO) Color.parseColor("#1E88E5") else Color.parseColor("#43A047")
                    }
                    fillPaint.color = color
                    canvas.drawRoundRect(rect, 4f, 4f, fillPaint)
                } else {
                    canvas.drawRoundRect(rect, 4f, 4f, boxPaint)
                }
                
                boxX += boxSize + boxSpacing
            }
        }
    }
}
