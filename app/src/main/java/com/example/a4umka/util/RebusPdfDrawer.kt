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

        for (word in words) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA"))
            drawPage(canvas, config, word)
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

        var pageNum = 1
        for (word in words) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA"))
            drawPage(page.canvas, config, word)
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

    private fun drawPage(canvas: Canvas, config: RebusConfig, word: String) {
        val margin = 40f
        val textColor = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#33691E")
        val rebuses = RebusData.getRebusWordsForWord(word)

        // Шапка
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        val rectPaint = Paint().apply { color = if (config.isBlackAndWhite) Color.LTGRAY else Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        val headRect = RectF(PAGE_WIDTH / 2f - 70f, 20f, PAGE_WIDTH / 2f + 70f, 40f)
        canvas.drawRoundRect(headRect, 5f, 5f, rectPaint)
        canvas.drawText("Ребус • по первым буквам", PAGE_WIDTH / 2f, 33f, headerPaint)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 32f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(config.title, PAGE_WIDTH / 2f, 90f, titlePaint)

        // Инструкция
        val instPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("Назови картинку и запиши первую букву. Читай по номерам.", PAGE_WIDTH / 2f, 120f, instPaint)
        canvas.drawText("Буквы в рамке уже даны: перепиши их без изменений.", PAGE_WIDTH / 2f, 138f, instPaint)

        // Карточки
        val cols = config.cardsPerRow
        val totalCards = rebuses.size
        val rows = (totalCards + cols - 1) / cols
        
        val gridTop = 180f
        val gridWidth = PAGE_WIDTH - margin * 2
        val cellW = gridWidth / cols
        val cellH = minOf(150f, (PAGE_HEIGHT - gridTop - 200f) / rows.coerceAtLeast(1))
        
        val cardBorderPaint = Paint().apply { color = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#A5D6A7"); style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT }
        val emojiPaint = Paint().apply { textSize = cellH * 0.45f; textAlign = Paint.Align.CENTER }
        val letterBoxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        val hintPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.CENTER }

        rebuses.forEachIndexed { i, rebus ->
            val col = i % cols
            val row = i / cols
            val left = margin + col * cellW
            val top = gridTop + row * cellH
            
            // Рамка карточки
            canvas.drawRoundRect(RectF(left + 5f, top + 5f, left + cellW - 5f, top + cellH - 5f), 8f, 8f, cardBorderPaint)
            
            // Номер
            canvas.drawText("${i + 1}", left + 10f, top + 20f, numPaint)
            
            // Картинка
            val centerX = left + cellW / 2f
            if (rebus != null) {
                canvas.drawText(rebus.emoji, centerX, top + cellH * 0.55f, emojiPaint)
                if (config.showHints) {
                    canvas.drawText(rebus.word.lowercase(), centerX, top + cellH * 0.75f, hintPaint)
                }
            } else {
                // Готовая буква в рамочке
                val letter = word.getOrNull(i)?.toString() ?: " "
                val letterPaint = Paint().apply { isAntiAlias = true; textSize = cellH * 0.4f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                canvas.drawText(letter, centerX, top + cellH * 0.55f, letterPaint)
            }
            
            // Клетка для буквы
            val boxSize = cellW * 0.35f
            val boxLeft = centerX - boxSize / 2f
            val boxTop = top + cellH - boxSize - 15f
            canvas.drawRect(boxLeft, boxTop, boxLeft + boxSize, boxTop + boxSize, letterBoxPaint)
        }

        // Ответ
        val answerY = gridTop + rows * cellH + 40f
        val ansTitlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = textColor; typeface = Typeface.create("cursive", Typeface.BOLD) }
        canvas.drawText("Получилось слово:", margin, answerY, ansTitlePaint)

        val boxY = answerY + 20f
        val boxWidth = 30f
        val boxSpacing = 5f
        var currentX = margin
        
        for (i in word.indices) {
            val char = word[i]
            if (char == ' ') {
                currentX += boxWidth / 2f
                continue
            }
            canvas.drawRect(currentX, boxY, currentX + boxWidth, boxY + 40f, letterBoxPaint)
            currentX += boxWidth + boxSpacing
        }

        // Примечание
        val notePaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("Й, Ы, Ь и Ъ могут быть даны готовыми буквами. Ё не заменяем на Е.", PAGE_WIDTH / 2f, boxY + 70f, notePaint)
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
            // Перенос строк для длинных ответов
            val words = ans.split(" ")
            var line = ""
            for (w in words) {
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
