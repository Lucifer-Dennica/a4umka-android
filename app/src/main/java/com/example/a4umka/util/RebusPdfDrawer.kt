package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.RebusConfig
import com.example.a4umka.data.RebusData

object RebusPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: RebusConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val words = RebusData.getParsedWords(config.wordsInput)
        if (words.isEmpty()) return pages

        val perPage = config.wordsPerPage
        val totalPages = (words.size + perPage - 1) / perPage

        for (p in 0 until totalPages) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA"))
            
            val pageWords = words.subList(p * perPage, minOf((p + 1) * perPage, words.size))
            drawMultiPage(canvas, config, pageWords)
            pages.add(bmp)
        }

        if (config.addAnswers) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.WHITE)
            drawAnswersPage(canvas, config, words)
            pages.add(bmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: RebusConfig, context: Context) {
        val words = RebusData.getParsedWords(config.wordsInput)
        if (words.isEmpty()) return

        val perPage = config.wordsPerPage
        val totalPages = (words.size + perPage - 1) / perPage
        var pageNum = 1

        for (p in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA"))
            val pageWords = words.subList(p * perPage, minOf((p + 1) * perPage, words.size))
            drawMultiPage(page.canvas, config, pageWords)
            document.finishPage(page)
        }

        if (config.addAnswers) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.WHITE)
            drawAnswersPage(page.canvas, config, words)
            document.finishPage(page)
        }
    }

    private fun drawMultiPage(canvas: Canvas, config: RebusConfig, words: List<String>) {
        val margin = 20f
        val linePaint = Paint().apply { color = Color.parseColor("#E0E0E0"); strokeWidth = 2f; style = Paint.Style.STROKE }
        
        when (config.wordsPerPage) {
            1 -> drawSingleRebus(canvas, config, words[0], margin, 0f, PAGE_WIDTH - margin * 2, PAGE_HEIGHT, 1f)
            2 -> {
                val halfH = PAGE_HEIGHT / 2f
                // Линия-разделитель
                canvas.drawLine(10f, halfH, PAGE_WIDTH - 10f, halfH, linePaint)
                
                for (i in words.indices) {
                    drawSingleRebus(canvas, config, words[i], margin, i * halfH + 10f, PAGE_WIDTH - margin * 2, halfH - 20f, 0.75f)
                }
            }
            4 -> {
                val halfW = PAGE_WIDTH / 2f
                val halfH = PAGE_HEIGHT / 2f
                
                // Рисуем четкую таблицу (крест)
                canvas.drawLine(halfW, 10f, halfW, PAGE_HEIGHT - 10f, linePaint)
                canvas.drawLine(10f, halfH, PAGE_WIDTH - 10f, halfH, linePaint)

                for (i in words.indices) {
                    val row = i / 2
                    val col = i % 2
                    val left = col * halfW + 10f
                    val top = row * halfH + 10f
                    val width = halfW - 20f
                    val height = halfH - 20f
                    
                    // Легкая рамка для каждой ячейки таблицы
                    val cellBorder = Paint().apply { color = Color.parseColor("#F0F0F0"); strokeWidth = 1f; style = Paint.Style.STROKE }
                    canvas.drawRect(left, top, left + width, top + height, cellBorder)

                    drawSingleRebus(canvas, config, words[i], left + 5f, top + 5f, width - 10f, height - 10f, 0.65f)
                }
            }
        }
    }

    private fun drawSingleRebus(canvas: Canvas, config: RebusConfig, word: String, left: Float, top: Float, width: Float, height: Float, scale: Float) {
        val centerX = left + width / 2f
        val textColor = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#33691E")
        val rebuses = RebusData.getRebusWordsForWord(word)

        // Заголовок
        val titleSize = 28f * scale
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = titleSize; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(config.title, centerX, top + 40f * scale, titlePaint)

        // Инструкция
        val instPaint = Paint().apply { isAntiAlias = true; textSize = 10f * scale; color = Color.DKGRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("Назови картинку и запиши первую букву. Читай по номерам.", centerX, top + 60f * scale, instPaint)

        // Карточки
        val cols = config.cardsPerRow
        val totalCards = rebuses.size
        val rows = (totalCards + cols - 1) / cols
        
        val gridTop = top + 80f * scale
        val gridWidth = width
        val cellW = gridWidth / cols
        val cellH = minOf(120f * scale, (height - 180f * scale) / rows.coerceAtLeast(1))
        
        val cardBorderPaint = Paint().apply { color = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#A5D6A7"); style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 10f * scale; color = Color.GRAY; typeface = Typeface.DEFAULT }
        val emojiPaint = Paint().apply { textSize = cellH * 0.45f; textAlign = Paint.Align.CENTER }
        val letterBoxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        val hintPaint = Paint().apply { isAntiAlias = true; textSize = 8f * scale; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        val readyLetterPaint = Paint().apply { isAntiAlias = true; textSize = cellH * 0.4f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }

        rebuses.forEachIndexed { i, rebus ->
            val col = i % cols
            val row = i / cols
            val cardLeft = left + col * cellW
            val cardTop = gridTop + row * cellH
            
            canvas.drawRoundRect(RectF(cardLeft + 2f, cardTop + 2f, cardLeft + cellW - 2f, cardTop + cellH - 2f), 5f, 5f, cardBorderPaint)
            canvas.drawText("${i + 1}", cardLeft + 5f, cardTop + 15f * scale, numPaint)
            
            val cardCenterX = cardLeft + cellW / 2f
            if (rebus != null) {
                canvas.drawText(rebus.emoji, cardCenterX, cardTop + cellH * 0.55f, emojiPaint)
                if (config.showHints) {
                    canvas.drawText(rebus.word.lowercase(), cardCenterX, cardTop + cellH * 0.75f, hintPaint)
                }
            } else {
                val letter = word.getOrNull(i)?.toString() ?: " "
                canvas.drawText(letter, cardCenterX, cardTop + cellH * 0.55f, readyLetterPaint)
            }
            
            val boxSize = cellW * 0.3f
            val boxLeft = cardCenterX - boxSize / 2f
            val boxTop = cardTop + cellH - boxSize - 5f
            canvas.drawRect(boxLeft, boxTop, boxLeft + boxSize, boxTop + boxSize, letterBoxPaint)
        }

        // Ответ
        val answerY = gridTop + rows * cellH + 20f * scale
        val ansTitlePaint = Paint().apply { isAntiAlias = true; textSize = 18f * scale; color = textColor; typeface = Typeface.create("cursive", Typeface.BOLD) }
        canvas.drawText("Получилось слово:", left, answerY, ansTitlePaint)

        val boxY = answerY + 10f * scale
        val boxWidth = 20f * scale
        val boxSpacing = 3f * scale
        var currentX = left
        
        for (i in word.indices) {
            val char = word[i]
            if (char == ' ') {
                currentX += boxWidth / 2f
                continue
            }
            canvas.drawRect(currentX, boxY, currentX + boxWidth, boxY + 25f * scale, letterBoxPaint)
            currentX += boxWidth + boxSpacing
        }
    }

    private fun drawAnswersPage(canvas: Canvas, config: RebusConfig, words: List<String>) {
        val margin = 40f
        var currentY = 80f
        
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы для взрослого", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 50f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val subPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.GRAY; typeface = Typeface.DEFAULT }
        
        for (word in words) {
            canvas.drawText(word, margin, currentY, ansPaint)
            currentY += 20f
            val ans = RebusData.getAnswersForWord(word)
            val wordsInAns = ans.split(" ")
            var line = ""
            for (w in wordsInAns) {
                val test = if (line.isEmpty()) w else "$line $w"
                if (subPaint.measureText(test) > PAGE_WIDTH - margin * 2) {
                    canvas.drawText(line, margin + 20f, currentY, subPaint)
                    currentY += 18f
                    line = w
                } else {
                    line = test
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, margin + 20f, currentY, subPaint)
                currentY += 18f
            }
            currentY += 20f
        }
    }
}
