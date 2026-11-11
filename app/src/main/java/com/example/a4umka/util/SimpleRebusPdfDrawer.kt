package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.SimpleRebusConfig
import com.example.a4umka.data.SimpleRebusData
import com.example.a4umka.data.SimpleRebusLength
import com.example.a4umka.data.SimpleRebusWord

object SimpleRebusPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: SimpleRebusConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        // Генерируем сразу 6 слов на лист
        val words = SimpleRebusData.getRandomWords(config.length, 6)

        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawMainPage(canvas, config, words)
        pages.add(bmp)

        if (config.addAnswers) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.drawColor(Color.WHITE)
            drawAnswersPage(ansCanvas, config, words)
            pages.add(ansBmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: SimpleRebusConfig, context: Context) {
        val words = SimpleRebusData.getRandomWords(config.length, 6)
        var pageNum = 1

        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
        var page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawMainPage(page.canvas, config, words)
        document.finishPage(page)

        if (config.addAnswers) {
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.WHITE)
            drawAnswersPage(page.canvas, config, words)
            document.finishPage(page)
        }
    }

    private fun drawMainPage(canvas: Canvas, config: SimpleRebusConfig, words: List<SimpleRebusWord>) {
        val margin = 40f
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        // Шапка
        canvas.drawText("Дата:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)
        
        val lengthStr = if (config.length == SimpleRebusLength.THREE) "3 буквы" else "3–4 буквы"
        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - 40f, 25f, PAGE_WIDTH/2f + 40f, 45f), 5f, 5f, rectPaint)
        canvas.drawText(lengthStr, PAGE_WIDTH/2f - headerPaint.measureText(lengthStr)/2f, 40f, headerPaint)
        
        canvas.drawText("Время:", PAGE_WIDTH - 120f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 32f; color = Color.parseColor("#33691E"); 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Ребусы", PAGE_WIDTH / 2f, 90f, titlePaint)

        val subPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("впиши первую букву каждой картинки — получится слово", PAGE_WIDTH / 2f, 115f, subPaint)

        // Сетка ребусов
        val startY = 160f
        val rowHeight = 90f

        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 4f), 0f) }
        val emojiPaint = Paint().apply { textSize = 32f; textAlign = Paint.Align.CENTER }
        val plusPaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = Color.DKGRAY; textAlign = Paint.Align.CENTER }
        val equalPaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = Color.DKGRAY; textAlign = Paint.Align.CENTER }
        val answerLinePaint = Paint().apply { color = Color.GRAY; strokeWidth = 1.5f }

        for (i in words.indices) {
            val word = words[i]
            val top = startY + i * rowHeight
            var currentX = margin + 10f

            for (emoji in word.emojis) {
                // Рисуем эмодзи
                canvas.drawText(emoji, currentX + 15f, top + 25f, emojiPaint)
                
                // Рисуем клетку под ним
                canvas.drawRect(currentX, top + 35f, currentX + 30f, top + 65f, boxPaint)
                
                currentX += 30f + 10f

                // Рисуем плюс (если это не последняя буква)
                if (emoji != word.emojis.last()) {
                    canvas.drawText("+", currentX + 10f, top + 55f, plusPaint)
                    currentX += 30f
                }
            }

            // Рисуем равно
            canvas.drawText("=", currentX + 10f, top + 55f, equalPaint)
            currentX += 30f

            // Рисуем линию для ответа
            canvas.drawLine(currentX, top + 55f, PAGE_WIDTH - margin, top + 55f, answerLinePaint)
        }
    }

    private fun drawAnswersPage(canvas: Canvas, config: SimpleRebusConfig, words: List<SimpleRebusWord>) {
        val margin = 40f
        var currentY = 80f
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 18f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        
        for (word in words) {
            val text = "${word.emojis.joinToString(" + ")} = ${word.word}"
            canvas.drawText(text, margin, currentY, ansPaint)
            currentY += 30f
        }
    }
}
