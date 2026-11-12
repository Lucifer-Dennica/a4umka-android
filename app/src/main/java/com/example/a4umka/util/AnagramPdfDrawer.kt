package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.AnagramConfig
import com.example.a4umka.data.AnagramData
import com.example.a4umka.data.AnagramTask

object AnagramPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: AnagramConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val tasksPerPage = 8
        val totalTasks = config.pagesCount * tasksPerPage

        val tasks = List(totalTasks) { AnagramData.generateTask(config) }

        for (p in 0 until config.pagesCount) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            val pageTasks = tasks.subList(p * tasksPerPage, minOf((p + 1) * tasksPerPage, tasks.size))
            drawPage(canvas, config, pageTasks, p + 1)
            pages.add(bmp)
        }

        if (config.showAnswers) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.drawColor(Color.WHITE)
            drawAnswersPage(ansCanvas, tasks)
            pages.add(ansBmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: AnagramConfig, context: Context) {
        val tasksPerPage = 8
        val totalTasks = config.pagesCount * tasksPerPage
        val tasks = List(totalTasks) { AnagramData.generateTask(config) }

        var pageNum = 1
        for (p in 0 until config.pagesCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.parseColor("#FAFAFA"))
            val pageTasks = tasks.subList(p * tasksPerPage, minOf((p + 1) * tasksPerPage, tasks.size))
            drawPage(page.canvas, config, pageTasks, p + 1)
            document.finishPage(page)
        }

        if (config.showAnswers) {
            val ansPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val ansPage = document.startPage(ansPageInfo)
            ansPage.canvas.drawColor(Color.WHITE)
            drawAnswersPage(ansPage.canvas, tasks)
            document.finishPage(ansPage)
        }
    }

    private fun drawPage(canvas: Canvas, config: AnagramConfig, tasks: List<AnagramTask>, pageNum: Int) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // Шапка
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        canvas.drawText("Дата:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)

        val typeStr = if (config.type == com.example.a4umka.data.AnagramType.SYLLABLES) "Слоги" else "Буквы"
        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - 30f, 25f, PAGE_WIDTH/2f + 30f, 45f), 5f, 5f, rectPaint)
        val centerTextPaint = Paint(headerPaint).apply { textAlign = Paint.Align.CENTER; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText(typeStr, PAGE_WIDTH/2f, 40f, centerTextPaint)

        canvas.drawText("Время:", PAGE_WIDTH - 120f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 36f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Анаграммы", PAGE_WIDTH / 2f, 90f, titlePaint)

        // ИСПРАВЛЕНИЕ: используем Typeface.create для курсива
        val italicTypeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        val subPaint = Paint().apply { 
            isAntiAlias = true; textSize = 12f; color = Color.GRAY; 
            textAlign = Paint.Align.CENTER; typeface = italicTypeface 
        }
        canvas.drawText("переставь ${if (config.type == com.example.a4umka.data.AnagramType.SYLLABLES) "слоги" else "буквы"} и впиши слово — подскажет картинка", PAGE_WIDTH / 2f, 110f, subPaint)

        // Список заданий
        var currentY = 160f
        val itemHeight = 60f

        val emojiPaint = Paint().apply { textSize = 30f; textAlign = Paint.Align.CENTER }
        val boxPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        val boxBorderPaint = Paint().apply { color = Color.parseColor("#81C784"); style = Paint.Style.STROKE; strokeWidth = 1f }
        val partPaint = Paint().apply { isAntiAlias = true; textSize = 18f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val emptyBoxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 4f), 0f) }

        for (task in tasks) {
            if (currentY > PAGE_HEIGHT - 60f) break

            // Эмодзи
            canvas.drawText(task.word.emoji, margin + 20f, currentY + 10f, emojiPaint)

            // Перемешанные части
            var currentX = margin + 60f
            val boxSpacing = 4f

            for (part in task.shuffledParts) {
                val width = partPaint.measureText(part) + 16f
                canvas.drawRoundRect(RectF(currentX, currentY - 15f, currentX + width, currentY + 15f), 6f, 6f, boxPaint)
                canvas.drawRoundRect(RectF(currentX, currentY - 15f, currentX + width, currentY + 15f), 6f, 6f, boxBorderPaint)
                canvas.drawText(part, currentX + width / 2f, currentY + 6f, partPaint)
                currentX += width + boxSpacing
            }

            // Стрелка
            canvas.drawText("→", currentX + 15f, currentY + 6f, partPaint)
            currentX += 40f

            // Пустые клеточки для ответа
            val answerPartsCount = if (config.type == com.example.a4umka.data.AnagramType.SYLLABLES) task.word.syllables.size else task.word.letters.size
            val boxSize = 28f
            for (i in 0 until answerPartsCount) {
                canvas.drawRect(currentX, currentY - 15f, currentX + boxSize, currentY + 15f, emptyBoxPaint)
                currentX += boxSize + boxSpacing
            }

            currentY += itemHeight
        }

        val footerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("a4umka.ru — задания и игры для детей • лист $pageNum", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, footerPaint)
    }

    private fun drawAnswersPage(canvas: Canvas, tasks: List<AnagramTask>) {
        val margin = 40f
        var currentY = 60f
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        
        for (task in tasks) {
            val text = "${task.word.emoji}  ${task.word.word}"
            canvas.drawText(text, margin, currentY, ansPaint)
            currentY += 30f
        }
    }
}
