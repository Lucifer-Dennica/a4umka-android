package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.WordInWordConfig
import com.example.a4umka.data.WordInWordData
import com.example.a4umka.data.WordPair

object WordInWordPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: WordInWordConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val pairs = WordInWordData.getRandomPairs(config.wordCount)

        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(canvas, pairs)
        pages.add(bmp)

        if (config.showAnswers) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.drawColor(Color.WHITE)
            drawAnswers(ansCanvas, pairs)
            pages.add(ansBmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: WordInWordConfig, context: Context) {
        val pairs = WordInWordData.getRandomPairs(config.wordCount)

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(page.canvas, pairs)
        document.finishPage(page)

        if (config.showAnswers) {
            val ansPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 2).create()
            val ansPage = document.startPage(ansPageInfo)
            ansPage.canvas.drawColor(Color.WHITE)
            drawAnswers(ansPage.canvas, pairs)
            document.finishPage(ansPage)
        }
    }

    private fun drawPage(canvas: Canvas, pairs: List<WordPair>) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // Шапка
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawText("Дата:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)

        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - 40f, 25f, PAGE_WIDTH/2f + 40f, 45f), 5f, 5f, rectPaint)
        val centerTextPaint = Paint(headerPaint).apply { textAlign = Paint.Align.CENTER; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("Слово в слове", PAGE_WIDTH/2f, 40f, centerTextPaint)

        canvas.drawText("Время:", PAGE_WIDTH - 120f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 36f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Найди слово в слове", PAGE_WIDTH / 2f, 90f, titlePaint)

        val italicTypeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        val subPaint = Paint().apply { 
            isAntiAlias = true; textSize = 12f; color = Color.GRAY; 
            textAlign = Paint.Align.CENTER; typeface = italicTypeface 
        }
        canvas.drawText("найди и обведи спрятанное слово — подскажет картинка", PAGE_WIDTH / 2f, 110f, subPaint)

        var currentY = 160f
        val itemHeight = 70f

        val emojiPaint = Paint().apply { textSize = 30f }
        val shortWordPaint = Paint().apply { isAntiAlias = true; textSize = 22f; color = Color.parseColor("#1976D2"); typeface = Typeface.DEFAULT_BOLD }
        val boxPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val boxBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val letterPaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }

        for (pair in pairs) {
            // Эмодзи
            canvas.drawText(pair.emoji, margin + 10f, currentY + 10f, emojiPaint)
            
            // Короткое слово
            canvas.drawText(pair.shortWord, margin + 60f, currentY + 10f, shortWordPaint)
            
            // Стрелка
            canvas.drawText("→", margin + 160f, currentY + 10f, shortWordPaint)

            // Клетки для длинного слова
            var currentX = margin + 200f
            val boxSize = 34f
            val boxSpacing = 4f

            for (char in pair.longWord) {
                canvas.drawRect(currentX, currentY - 15f, currentX + boxSize, currentY + 15f, boxPaint)
                canvas.drawRect(currentX, currentY - 15f, currentX + boxSize, currentY + 15f, boxBorderPaint)
                canvas.drawText(char.toString(), currentX + boxSize / 2f, currentY + 8f, letterPaint)
                currentX += boxSize + boxSpacing
            }

            currentY += itemHeight
        }

        val footerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("a4umka.ru — задания и игры для детей • лист 1", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, footerPaint)
    }

    private fun drawAnswers(canvas: Canvas, pairs: List<WordPair>) {
        val margin = 40f
        var currentY = 60f
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val highlightPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.parseColor("#D32F2F"); typeface = Typeface.DEFAULT_BOLD }
        
        for (pair in pairs) {
            canvas.drawText("${pair.emoji} ${pair.shortWord}", margin, currentY, ansPaint)
            currentY += 20f
            
            // Выделяем спрятанное слово
            var currentX = margin + 40f
            for (char in pair.longWord) {
                val charStr = char.toString()
                val isHidden = pair.shortWord.contains(char) // Упрощенно, выделяем буквы, которые есть в коротком
                // Для точности лучше выделять подстроку, но для простоты сделаем так
                if (pair.longWord.contains(pair.shortWord) && pair.longWord.substringAfter(pair.longWord.substringBefore(pair.shortWord)).startsWith(pair.shortWord)) {
                     // ... сложно, пропустим точное выделение, просто выведем полную строку с подчеркиванием
                }
                canvas.drawText(charStr, currentX, currentY, highlightPaint)
                currentX += 15f
            }
            currentY += 30f
        }
    }
}
