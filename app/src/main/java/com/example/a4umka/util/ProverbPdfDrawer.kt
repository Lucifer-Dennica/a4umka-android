package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.ProverbConfig
import com.example.a4umka.data.ProverbData
import com.example.a4umka.data.ProverbTask

object ProverbPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: ProverbConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val tasksPerPage = 2
        val totalTasks = config.pagesCount * tasksPerPage

        // Генерируем уникальные задачи
        val tasks = mutableListOf<ProverbTask>()
        var attempts = 0
        while (tasks.size < totalTasks && attempts < 100) {
            val task = ProverbData.generateTask(config)
            if (task != null && tasks.none { it.phrase == task.phrase }) {
                tasks.add(task)
            }
            attempts++
        }

        for (p in 0 until config.pagesCount) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            
            val pageTasks = tasks.subList(p * tasksPerPage, minOf((p + 1) * tasksPerPage, tasks.size))
            drawPage(canvas, config, pageTasks, p + 1)
            pages.add(bmp)
        }

        // Лист ответов
        val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val ansCanvas = Canvas(ansBmp)
        ansCanvas.drawColor(Color.WHITE)
        drawAnswersPage(ansCanvas, tasks)
        pages.add(ansBmp)

        return pages
    }

    fun drawDocument(document: PdfDocument, config: ProverbConfig, context: Context) {
        val tasksPerPage = 2
        val totalTasks = config.pagesCount * tasksPerPage
        val tasks = mutableListOf<ProverbTask>()
        var attempts = 0
        while (tasks.size < totalTasks && attempts < 100) {
            val task = ProverbData.generateTask(config)
            if (task != null && tasks.none { it.phrase == task.phrase }) {
                tasks.add(task)
            }
            attempts++
        }

        var pageNum = 1
        for (p in 0 until config.pagesCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.parseColor("#FAFAFA"))
            val pageTasks = tasks.subList(p * tasksPerPage, minOf((p + 1) * tasksPerPage, tasks.size))
            drawPage(page.canvas, config, pageTasks, p + 1)
            document.finishPage(page)
        }

        val ansPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        val ansPage = document.startPage(ansPageInfo)
        ansPage.canvas.drawColor(Color.WHITE)
        drawAnswersPage(ansPage.canvas, tasks)
        document.finishPage(ansPage)
    }

    private fun drawPage(canvas: Canvas, config: ProverbConfig, tasks: List<ProverbTask>, pageNum: Int) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // ШАПКА
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        canvas.drawText("Имя:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)
        
        val ageStr = if (config.age == com.example.a4umka.data.ProverbAge.AGE_5_6) "5–6 лет" else "6–7 лет"
        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - 40f, 25f, PAGE_WIDTH/2f + 40f, 45f), 5f, 5f, rectPaint)
        val centerTextPaint = Paint(headerPaint).apply { textAlign = Paint.Align.CENTER; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("📚 $ageStr", PAGE_WIDTH/2f, 40f, centerTextPaint)
        
        canvas.drawText("Дата:", PAGE_WIDTH - 120f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // ЗАГОЛОВОК
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 32f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Расставь по порядку", PAGE_WIDTH / 2f, 90f, titlePaint)

        val subPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("Если всё верно, из слогов сложится пословица или скороговорка", PAGE_WIDTH / 2f, 110f, subPaint)

        var currentY = 140f
        val taskHeight = 300f

        for ((index, task) in tasks.withIndex()) {
            drawTask(canvas, task, index + 1, currentY, margin, PAGE_WIDTH - margin * 2, taskHeight)
            currentY += taskHeight
        }

        val footerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("a4umka.ru — задания и игры для детей • лист $pageNum", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, footerPaint)
    }

    private fun drawTask(canvas: Canvas, task: ProverbTask, taskNum: Int, top: Float, left: Float, width: Float, height: Float) {
        val cardBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        
        val taskRect = RectF(left, top, left + width, top + height)
        canvas.drawRoundRect(taskRect, 10f, 10f, cardBgPaint)
        canvas.drawRoundRect(taskRect, 10f, 10f, cardBorderPaint)

        // Номер задачи
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
        val numCirclePaint = Paint().apply { color = Color.parseColor("#4CAF50"); style = Paint.Style.FILL }
        canvas.drawCircle(left + 20f, top + 25f, 12f, numCirclePaint)
        canvas.drawText("$taskNum", left + 20f, top + 30f, numPaint)

        // Инструкция
        val instPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText(task.instruction, left + 40f, top + 30f, instPaint)

        // Карточки
        val cardW = 45f
        val cardH = 50f
        val gap = 5f
        var currentX = left + 20f
        val cardY = top + 50f

        val itemBgPaint = Paint().apply { color = Color.parseColor("#64B5F6"); style = Paint.Style.FILL }
        val syllBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.GRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        val itemTextPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.WHITE; textAlign = Paint.Align.CENTER }
        val syllTextPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }

        for (i in task.cards.indices) {
            val card = task.cards[i]
            
            // Верхняя часть (предмет)
            canvas.drawRoundRect(RectF(currentX, cardY, currentX + cardW, cardY + cardH / 2), 4f, 4f, itemBgPaint)
            canvas.drawText(card.first, currentX + cardW / 2f, cardY + cardH / 4f + 4f, itemTextPaint)
            
            // Нижняя часть (слог)
            canvas.drawRoundRect(RectF(currentX, cardY + cardH / 2, currentX + cardW, cardY + cardH), 4f, 4f, syllBgPaint)
            canvas.drawText(card.second, currentX + cardW / 2f, cardY + cardH * 0.8f, syllTextPaint)
            
            // Рамка
            canvas.drawRoundRect(RectF(currentX, cardY, currentX + cardW, cardY + cardH), 4f, 4f, borderPaint)

            currentX += cardW + gap
        }

        // Клеточки для ответа
        val boxY = top + 120f
        val boxSize = 20f
        val boxSpacing = 3f
        var boxX = left + 20f

        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val hintPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY }
        
        canvas.drawText("Выпиши слоги по порядку в клеточки:", left + 20f, boxY - 10f, hintPaint)

        for (i in task.cards.indices) {
            canvas.drawRect(boxX, boxY, boxX + boxSize, boxY + boxSize, boxPaint)
            boxX += boxSize + boxSpacing

            // Добавляем пробел (разрыв слова)
            if (task.wordBreaks.contains(i + 1) && i < task.cards.size - 1) {
                boxX += 10f
            }
        }

        // Финальная строка
        val answerLineY = boxY + 40f
        canvas.drawText("Прочитай, что получилось:", left + 20f, answerLineY, hintPaint)
        canvas.drawLine(left + 170f, answerLineY, left + width - 20f, answerLineY, Paint().apply { color = Color.GRAY; strokeWidth = 1f })
    }

    private fun drawAnswersPage(canvas: Canvas, tasks: List<ProverbTask>) {
        val margin = 40f
        var currentY = 60f
        
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы для взрослого", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val subPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.GRAY; typeface = Typeface.DEFAULT }
        
        for (task in tasks) {
            canvas.drawText("Фраза: ${task.phrase}", margin, currentY, ansPaint)
            currentY += 20f
            val orderStr = task.correctSequence.joinToString(" → ")
            canvas.drawText("Порядок: $orderStr", margin + 10f, currentY, subPaint)
            currentY += 30f
        }
    }
}
