package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.*
import kotlin.random.Random

object AssembleWordPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: AssembleWordConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val scale = 1f

        // Страница 1
        var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bmp)
        canvas.scale(scale, scale)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        val words = AssembleWordData.getWordsForTask(config)
        drawMainPage(canvas, config, words)
        pages.add(bmp)

        // Страница 2 (Ответы)
        if (config.addAnswerSheet) {
            bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            canvas = Canvas(bmp)
            canvas.scale(scale, scale)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawAnswerSheet(canvas, words)
            pages.add(bmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: AssembleWordConfig, context: Context) {
        var pageNum = 1
        val words = AssembleWordData.getWordsForTask(config)
        
        // Страница 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        var page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawMainPage(page.canvas, config, words)
        document.finishPage(page)

        // Страница 2
        if (config.addAnswerSheet) {
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawAnswerSheet(page.canvas, words)
            document.finishPage(page)
        }
    }

    private fun drawMainPage(canvas: Canvas, config: AssembleWordConfig, words: List<WordItem>) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")
        
        // ШАПКА
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        canvas.drawText("Дата:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)
        
        val countStr = "Слов: ${words.size}"
        val countWidth = headerPaint.measureText(countStr)
        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - countWidth/2f - 10f, 25f, PAGE_WIDTH/2f + countWidth/2f + 10f, 45f), 5f, 5f, rectPaint)
        canvas.drawText(countStr, PAGE_WIDTH/2f - countWidth/2f, 40f, headerPaint)
        
        canvas.drawText("Время:", PAGE_WIDTH - 120f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // ЗАГОЛОВОК
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 28f; color = textColor; 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Собери слово", PAGE_WIDTH / 2f, 90f, titlePaint)
        
        val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        val subtitle = when(config.taskType) {
            AssembleTaskType.CONNECT -> "проведи линии от слога к слогу — получатся слова"
            AssembleTaskType.PICTURE -> "посмотри на картинку и впиши слоги в клеточки"
            AssembleTaskType.CARDS -> "вырежи карточки и собери из них слова"
        }
        canvas.drawText(subtitle, PAGE_WIDTH / 2f, 110f, subtitlePaint)

        when(config.taskType) {
            AssembleTaskType.CONNECT -> drawConnectTask(canvas, config, words)
            AssembleTaskType.PICTURE -> drawPictureTask(canvas, config, words)
            AssembleTaskType.CARDS -> drawCardsTask(canvas, config, words)
        }
    }

    private fun drawSyllable(canvas: Canvas, x: Float, y: Float, syllable: String, paint: Paint, colorize: Boolean) {
        if (!colorize) {
            canvas.drawText(syllable, x, y, paint)
            return
        }
        
        val redPaint = Paint(paint).apply { color = Color.parseColor("#D32F2F") }
        val bluePaint = Paint(paint).apply { color = Color.parseColor("#1976D2") }
        
        var currentX = x
        for (char in syllable) {
            val charStr = char.toString()
            val isVowel = charStr.lowercase() in "аеёиоуыэюя"
            val p = if (isVowel) redPaint else bluePaint
            canvas.drawText(charStr, currentX, y, p)
            currentX += p.measureText(charStr)
        }
    }

    private fun drawConnectTask(canvas: Canvas, config: AssembleWordConfig, words: List<WordItem>) {
        val margin = 40f
        val startY = 180f
        val rowHeight = 60f
        
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 24f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val dotPaint = Paint().apply { color = Color.GRAY; style = Paint.Style.FILL }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        
        val leftX = PAGE_WIDTH / 4f
        val rightX = PAGE_WIDTH * 3 / 4f
        
        // Разделяем слоги на левые и правые
        val leftSyllables = mutableListOf<String>()
        val rightSyllables = mutableListOf<String>()
        
        for (w in words) {
            leftSyllables.add(w.syllables.first())
            rightSyllables.add(w.syllables.drop(1).joinToString(""))
        }
        
        val shuffledRight = rightSyllables.shuffled()

        for (i in words.indices) {
            val y = startY + i * rowHeight
            
            // Левая часть
            val leftSyl = leftSyllables[i].uppercase()
            val lw = textPaint.measureText(leftSyl) + 30f
            val lRect = RectF(leftX - lw/2f, y - 25f, leftX + lw/2f, y + 15f)
            canvas.drawRoundRect(lRect, 8f, 8f, boxPaint)
            drawSyllable(canvas, leftX - textPaint.measureText(leftSyl)/2f, y, leftSyl, textPaint, config.colorSyllables)
            
            // Точка слева
            canvas.drawCircle(leftX + lw/2f + 15f, y - 5f, 4f, dotPaint)
            
            // Точка справа
            canvas.drawCircle(rightX - lw/2f - 15f, y - 5f, 4f, dotPaint)
            
            // Правая часть (перемешанная)
            val rightSyl = shuffledRight[i].uppercase()
            val rw = textPaint.measureText(rightSyl) + 30f
            val rRect = RectF(rightX - rw/2f, y - 25f, rightX + rw/2f, y + 15f)
            canvas.drawRoundRect(rRect, 8f, 8f, boxPaint)
            drawSyllable(canvas, rightX - textPaint.measureText(rightSyl)/2f, y, rightSyl, textPaint, config.colorSyllables)
            
            // Эмодзи (если включено)
            if (config.emojiHints) {
                val emojiPaint = Paint().apply { textSize = 20f; textAlign = Paint.Align.CENTER }
                canvas.drawText(words[i].emoji, PAGE_WIDTH / 2f, y, emojiPaint)
            }
        }
    }

    private fun drawPictureTask(canvas: Canvas, config: AssembleWordConfig, words: List<WordItem>) {
        val margin = 40f
        val startY = 160f
        val rowHeight = 90f
        
        val emojiPaint = Paint().apply { textSize = 36f }
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        
        for (i in words.indices) {
            if (i >= 7) break
            val word = words[i]
            val y = startY + i * rowHeight
            
            // Эмодзи
            canvas.drawText(word.emoji, margin, y, emojiPaint)
            
            // Перемешанные слоги
            val shuffled = word.syllables.shuffled()
            var xOffset = margin + 50f
            
            for (syl in shuffled) {
                val sylText = syl.uppercase()
                val w = textPaint.measureText(sylText) + 20f
                val rect = RectF(xOffset, y - 25f, xOffset + w, y + 15f)
                canvas.drawRoundRect(rect, 5f, 5f, boxPaint)
                drawSyllable(canvas, xOffset + 10f, y, sylText, textPaint, config.colorSyllables)
                xOffset += w + 10f
            }
            
            // Пустые клеточки
            xOffset += 20f
            for (j in word.syllables.indices) {
                val cellRect = RectF(xOffset, y - 25f, xOffset + 30f, y + 15f)
                canvas.drawRect(cellRect, boxPaint)
                xOffset += 35f
            }
        }
    }

    private fun drawCardsTask(canvas: Canvas, config: AssembleWordConfig, words: List<WordItem>) {
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 36f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
        
        val allSyllables = mutableListOf<String>()
        for (w in words) {
            allSyllables.addAll(w.syllables)
        }
        allSyllables.shuffle()

        val cols = 3
        val rows = 4
        val cardW = (PAGE_WIDTH - 80f) / cols
        val cardH = (PAGE_HEIGHT - 200f) / rows
        
        for (i in 0 until (cols * rows)) {
            if (i >= allSyllables.size) break
            val col = i % cols
            val row = i / cols
            val left = 40f + col * cardW
            val top = 160f + row * cardH
            
            canvas.drawRoundRect(RectF(left + 5f, top + 5f, left + cardW - 5f, top + cardH - 5f), 10f, 10f, boxPaint)
            
            val sylText = allSyllables[i].uppercase()
            drawSyllable(canvas, left + cardW/2f - textPaint.measureText(sylText)/2f, top + cardH/2f + 10f, sylText, textPaint, config.colorSyllables)
        }
    }

    private fun drawAnswerSheet(canvas: Canvas, words: List<WordItem>) {
        val margin = 40f
        var currentY = 80f
        
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы для взрослого", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        
        for (word in words) {
            val text = "${word.emoji} ${word.syllables.joinToString("-")} (${word.word})"
            canvas.drawText(text, margin, currentY, ansPaint)
            currentY += 35f
        }
    }
}
