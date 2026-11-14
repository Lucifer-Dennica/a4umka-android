package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.PrepositionConfig
import com.example.a4umka.data.PrepositionData
import com.example.a4umka.data.PrepositionTask
import com.example.a4umka.data.PrepositionTaskType

object PrepositionPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: PrepositionConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val tasks = PrepositionData.generateTasks(config)

        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawMainPage(canvas, config, tasks)
        pages.add(bmp)

        if (config.showAnswers) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.drawColor(Color.WHITE)
            drawAnswersPage(ansCanvas, tasks)
            pages.add(ansBmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: PrepositionConfig, context: Context) {
        val tasks = PrepositionData.generateTasks(config)
        var pageNum = 1

        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
        var page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawMainPage(page.canvas, config, tasks)
        document.finishPage(page)

        if (config.showAnswers) {
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.WHITE)
            drawAnswersPage(page.canvas, tasks)
            document.finishPage(page)
        }
    }

    private fun drawMainPage(canvas: Canvas, config: PrepositionConfig, tasks: List<PrepositionTask>) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // Шапка (Имя, Дата)
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawText("Имя:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 150f, 42f, linePaint)
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 32f; color = textColor; 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("🧭 Предлоги в картинках", PAGE_WIDTH / 2f, 90f, titlePaint)

        val subPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        val subText = when(config.taskType) {
            PrepositionTaskType.FIND_WORD -> "посмотри на картинку и обведи верные слова"
            PrepositionTaskType.FIND_PICTURE -> "прочитай текст и обведи подходящую картинку"
            PrepositionTaskType.DRAW_SELF -> "прочитай задание и нарисуй предмет сам"
        }
        canvas.drawText(subText, PAGE_WIDTH / 2f, 110f, subPaint)

        // Сетка задач (2 колонки)
        val cols = 2
        val rows = (tasks.size + 1) / 2
        val tableTop = 140f
        val availableHeight = PAGE_HEIGHT - tableTop - 60f
        val cellWidth = (PAGE_WIDTH - margin * 2) / cols
        val cellHeight = availableHeight / rows

        val cardBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val numCirclePaint = Paint().apply { color = textColor; style = Paint.Style.FILL }
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }

        for (i in tasks.indices) {
            val col = i % cols
            val row = i / cols
            val left = margin + col * cellWidth + 5f
            val top = tableTop + row * cellHeight + 5f
            val right = left + cellWidth - 10f
            val bottom = top + cellHeight - 10f
            
            val task = tasks[i]

            // Фон карточки
            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBorderPaint)

            // Номер
            canvas.drawCircle(left + 20f, top + 20f, 12f, numCirclePaint)
            canvas.drawText("${i + 1}", left + 20f, top + 25f, numPaint)

            // Контент карточки
            if (config.taskType == PrepositionTaskType.FIND_WORD) {
                drawFindWordTask(canvas, task, left, top, right, bottom)
            } else if (config.taskType == PrepositionTaskType.FIND_PICTURE) {
                drawFindPictureTask(canvas, task, left, top, right, bottom)
            } else {
                drawDrawSelfTask(canvas, task, left, top, right, bottom)
            }
        }
    }

    private fun drawFindWordTask(canvas: Canvas, task: PrepositionTask, left: Float, top: Float, right: Float, bottom: Float) {
        val centerX = left + (right - left) / 2f
        
        // Рисуем эмодзи (предмет + база)
        val emojiPaint = Paint().apply { textSize = 40f; textAlign = Paint.Align.CENTER }
        // Просто ставим рядом
        canvas.drawText(task.targetEmoji, centerX - 25f, top + 80f, emojiPaint)
        canvas.drawText(task.baseEmoji, centerX + 25f, top + 80f, emojiPaint)

        // Вопрос
        val qPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText(task.question, centerX, top + 115f, qPaint)

        // Варианты ответов (обводка)
        val optionPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.BLACK; textAlign = Paint.Align.CENTER }
        val bubblePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val bubbleBorder = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }

        var currentY = top + 135f
        val boxWidth = right - left - 40f
        val boxHeight = 24f

        task.options.forEach { option ->
            val rect = RectF(left + 20f, currentY, left + 20f + boxWidth, currentY + boxHeight)
            canvas.drawRoundRect(rect, 12f, 12f, bubblePaint)
            canvas.drawRoundRect(rect, 12f, 12f, bubbleBorder)
            canvas.drawText(option, left + 20f + boxWidth / 2f, currentY + 16f, optionPaint)
            currentY += boxHeight + 6f
        }
    }

    private fun drawFindPictureTask(canvas: Canvas, task: PrepositionTask, left: Float, top: Float, right: Float, bottom: Float) {
        val centerX = left + (right - left) / 2f
        
        // Текст задания сверху
        val qPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText(task.correctAnswer.replaceFirstChar { it.uppercase() }, centerX, top + 35f, qPaint)

        // Три варианта картинок
        val emojiPaint = Paint().apply { textSize = 30f; textAlign = Paint.Align.CENTER }
        
        val spacing = (right - left - 40f) / 3f
        val startX = left + 20f + spacing / 2f

        // Правильный + 2 рандомных неправильных (упрощенно - просто меняем базу)
        // В реальности тут надо генерировать другие сцены, но для наглядности сойдет
        for (i in 0 until 3) {
            val x = startX + i * spacing
            
            // Рамка
            val rect = RectF(x - 35f, top + 50f, x + 35f, top + 110f)
            canvas.drawRoundRect(rect, 8f, 8f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
            canvas.drawRoundRect(rect, 8f, 8f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })

            // Для упрощения рисуем правильный вариант и две другие базы
            val baseToDraw = if (i == 0) task.baseEmoji else if (i == 1) "📦" else "🪑"
            val targetToDraw = if (i == 0) task.targetEmoji else "🐭"

            canvas.drawText(targetToDraw, x - 10f, top + 85f, emojiPaint)
            canvas.drawText(baseToDraw, x + 15f, top + 85f, emojiPaint)
        }
    }

    private fun drawDrawSelfTask(canvas: Canvas, task: PrepositionTask, left: Float, top: Float, right: Float, bottom: Float) {
        val centerX = left + (right - left) / 2f
        
        // Задание
        val qPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Нарисуй ${task.targetName} ${task.correctAnswer}", centerX, top + 35f, qPaint)

        // Рисуем только базу
        val emojiPaint = Paint().apply { textSize = 50f; textAlign = Paint.Align.CENTER }
        canvas.drawText(task.baseEmoji, centerX, top + 90f, emojiPaint)

        // Место для рисования (пунктирная рамка)
        val drawRect = RectF(left + 20f, top + 110f, right - 20f, bottom - 10f)
        val dashPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
        canvas.drawRoundRect(drawRect, 8f, 8f, dashPaint)
    }

    private fun drawAnswersPage(canvas: Canvas, tasks: List<PrepositionTask>) {
        val margin = 40f
        var currentY = 60f
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы для взрослого", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        
        for ((index, task) in tasks.withIndex()) {
            val text = "${index + 1}. ${task.correctAnswer}"
            canvas.drawText(text, margin, currentY, ansPaint)
            currentY += 25f
        }
    }
}
