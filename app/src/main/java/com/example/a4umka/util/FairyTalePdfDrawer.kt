package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.FairyTaleConfig
import com.example.a4umka.data.FairyTaleData
import com.example.a4umka.data.FairyTaleTask

object FairyTalePdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: FairyTaleConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val tasksPerPage = 2
        val totalTasks = config.pagesCount * tasksPerPage

        val tasks = List(totalTasks) { FairyTaleData.generateTask(config) }

        for (p in 0 until config.pagesCount) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            val pageTasks = tasks.subList(p * tasksPerPage, minOf((p + 1) * tasksPerPage, tasks.size))
            drawPage(canvas, config, pageTasks, p + 1)
            pages.add(bmp)
        }

        val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val ansCanvas = Canvas(ansBmp)
        ansCanvas.drawColor(Color.WHITE)
        drawAnswersPage(ansCanvas, config, tasks)
        pages.add(ansBmp)

        return pages
    }

    fun drawDocument(document: PdfDocument, config: FairyTaleConfig, context: Context) {
        val tasksPerPage = 2
        val totalTasks = config.pagesCount * tasksPerPage
        val tasks = List(totalTasks) { FairyTaleData.generateTask(config) }

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
        drawAnswersPage(ansPage.canvas, config, tasks)
        document.finishPage(ansPage)
    }

    private fun drawPage(canvas: Canvas, config: FairyTaleConfig, tasks: List<FairyTaleTask>, pageNum: Int) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // Шапка
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawText("Имя:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)
        
        val ageStr = if (config.age == com.example.a4umka.data.FairyTaleAge.AGE_5_6) "5–6 лет" else "6–7 лет"
        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - 40f, 25f, PAGE_WIDTH/2f + 40f, 45f), 5f, 5f, rectPaint)
        val centerTextPaint = Paint(headerPaint).apply { textAlign = Paint.Align.CENTER; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("📚 $ageStr", PAGE_WIDTH/2f, 40f, centerTextPaint)
        
        canvas.drawText("Дата:", PAGE_WIDTH - 120f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 32f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Назови сказку правильно", PAGE_WIDTH / 2f, 90f, titlePaint)

        var currentY = 130f
        var taskCounter = 1

        for (task in tasks) {
            if (task.fixNames.isNotEmpty()) {
                taskCounter = drawFixNames(canvas, task.fixNames, taskCounter, currentY, margin)
                currentY += (task.fixNames.size * 40f) + 60f
            }
            if (task.guessPictures.isNotEmpty()) {
                taskCounter = drawGuessPictures(canvas, task.guessPictures, taskCounter, currentY, margin)
                currentY += (task.guessPictures.size * 45f) + 60f
            }
            if (task.connectHalves != null) {
                taskCounter = drawConnectHalves(canvas, task.connectHalves, taskCounter, currentY, margin)
                currentY += 200f
            }
        }

        val footerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("a4umka.ru — задания и игры для детей • лист $pageNum", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, footerPaint)
    }

    private fun drawFixNames(canvas: Canvas, names: List<com.example.a4umka.data.FixNameTask>, startNum: Int, startY: Float, margin: Float): Int {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("$startNum. Исправь названия", margin, startY, titlePaint)
        
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.GRAY; strokeWidth = 1f }
        
        var y = startY + 25f
        for (i in names.indices) {
            canvas.drawText("«${names[i].brokenName}»", margin + 10f, y, textPaint)
            canvas.drawText("→", margin + 220f, y, textPaint)
            canvas.drawLine(margin + 240f, y + 2f, PAGE_WIDTH - margin, y + 2f, linePaint)
            y += 35f
        }
        return startNum + 1
    }

    private fun drawGuessPictures(canvas: Canvas, tales: List<com.example.a4umka.data.FairyTale>, startNum: Int, startY: Float, margin: Float): Int {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("$startNum. Угадай сказку по картинкам", margin, startY, titlePaint)
        
        val emojiPaint = Paint().apply { textSize = 20f; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.GRAY; strokeWidth = 1f }
        
        var y = startY + 30f
        for (tale in tales) {
            canvas.drawText(tale.emojis, margin + 10f, y, emojiPaint)
            canvas.drawText("→", margin + 220f, y, emojiPaint)
            canvas.drawLine(margin + 240f, y + 2f, PAGE_WIDTH - margin, y + 2f, linePaint)
            y += 40f
        }
        return startNum + 1
    }

    private fun drawConnectHalves(canvas: Canvas, task: com.example.a4umka.data.ConnectHalvesTask, startNum: Int, startY: Float, margin: Float): Int {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("$startNum. Соедини половинки", margin, startY, titlePaint)
        
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        
        var leftY = startY + 30f
        var rightY = startY + 30f
        
        for (start in task.starts) {
            canvas.drawText(start, margin + 10f, leftY, textPaint)
            leftY += 30f
        }
        for (end in task.ends) {
            canvas.drawText(end, PAGE_WIDTH / 2f + 40f, rightY, textPaint)
            rightY += 30f
        }
        return startNum + 1
    }

    private fun drawAnswersPage(canvas: Canvas, config: FairyTaleConfig, tasks: List<FairyTaleTask>) {
        val margin = 40f
        var currentY = 60f
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        
        for (task in tasks) {
            if (task.fixNames.isNotEmpty()) {
                canvas.drawText("Исправь названия:", margin, currentY, Paint(ansPaint).apply { typeface = Typeface.DEFAULT_BOLD })
                currentY += 20f
                task.fixNames.forEach {
                    canvas.drawText("«${it.brokenName}» → ${it.correctName}", margin + 10f, currentY, ansPaint)
                    currentY += 20f
                }
                currentY += 10f
            }
            if (task.guessPictures.isNotEmpty()) {
                canvas.drawText("Угадай по картинкам:", margin, currentY, Paint(ansPaint).apply { typeface = Typeface.DEFAULT_BOLD })
                currentY += 20f
                task.guessPictures.forEach {
                    canvas.drawText("${it.emojis} → ${it.title}", margin + 10f, currentY, ansPaint)
                    currentY += 20f
                }
                currentY += 10f
            }
            if (task.connectHalves != null) {
                canvas.drawText("Соедини половинки:", margin, currentY, Paint(ansPaint).apply { typeface = Typeface.DEFAULT_BOLD })
                currentY += 20f
                task.connectHalves.answers.forEach {
                    canvas.drawText("${it.first} + ${it.second}", margin + 10f, currentY, ansPaint)
                    currentY += 20f
                }
                currentY += 10f
            }
            if (currentY > PAGE_HEIGHT - 100f) break
        }
    }
}
