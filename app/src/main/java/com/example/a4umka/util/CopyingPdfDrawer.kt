package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.CopyingConfig
import com.example.a4umka.data.CopyingMode
import com.example.a4umka.data.CopyingStory

object CopyingPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    // Генерация превью (список страниц)
    fun generatePreview(config: CopyingConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        
        for (story in config.selectedStories) {
            for (i in 0 until config.childrenCount) {
                val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawSheet(canvas, config, story, i + 1, config.childrenCount)
                pages.add(bmp)
            }
            if (config.addBlankPage) {
                val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawBlankSheet(canvas, config, story)
                pages.add(bmp)
            }
        }
        
        if (pages.isEmpty()) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.WHITE)
            pages.add(bmp)
        }
        
        return pages
    }

    // Генерация PDF
    fun drawDocument(document: PdfDocument, config: CopyingConfig, context: Context) {
        var pageNumber = 1
        for (story in config.selectedStories) {
            for (i in 0 until config.childrenCount) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNumber++).create()
                val page = document.startPage(pageInfo)
                drawSheet(page.canvas, config, story, i + 1, config.childrenCount)
                document.finishPage(page)
            }
            if (config.addBlankPage) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNumber++).create()
                val page = document.startPage(pageInfo)
                drawBlankSheet(page.canvas, config, story)
                document.finishPage(page)
            }
        }
    }

    private fun drawSheet(canvas: Canvas, config: CopyingConfig, story: CopyingStory, copyNum: Int, totalCopies: Int) {
        val bgColor = if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA")
        canvas.drawColor(bgColor)
        val margin = 40f
        var currentY = 50f
        
        // Заголовок (СПИСЫВАНИЕ • 1 КЛАСС • ОРИЕНТИР)
        val modeText = if (config.mode == CopyingMode.COPYING) "СПИСЫВАНИЕ" else "ДИКТАНТ"
        val headerInfo = "$modeText • ${config.grade} КЛАСС • ОРИЕНТИР"
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = Typeface.DEFAULT }
        canvas.drawText(headerInfo, margin, currentY, headerPaint)
        
        currentY += 30f
        
        // Название рассказа
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 24f; color = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#33691E"); typeface = Typeface.DEFAULT_BOLD 
        }
        canvas.drawText(story.title, margin, currentY, titlePaint)
        
        currentY += 30f
        
        // Имя и Дата
        val fieldPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        canvas.drawText("Имя:", margin, currentY, fieldPaint)
        canvas.drawLine(margin + 35f, currentY + 2f, margin + 150f, currentY + 2f, linePaint)
        
        if (config.signature.isNotEmpty()) {
            canvas.drawText(config.signature, PAGE_WIDTH / 2f - fieldPaint.measureText(config.signature) / 2f, currentY, fieldPaint)
        }
        
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, currentY, fieldPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, currentY + 2f, PAGE_WIDTH - margin, currentY + 2f, linePaint)
        
        currentY += 30f
        
        // Текст (только если режим СПИСЫВАНИЕ)
        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
        if (config.mode == CopyingMode.COPYING) {
            val words = story.text.split(" ")
            var line = ""
            val lines = mutableListOf<String>()
            
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2) {
                    lines.add(line)
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) lines.add(line)
            
            for (l in lines) {
                canvas.drawText(l, margin, currentY, bodyPaint)
                currentY += bodyPaint.textSize * 1.5f
            }
            currentY += 15f
        }
        
        // Рамка с правилами проверки
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        val boxRect = RectF(margin, currentY, PAGE_WIDTH - margin, currentY + 40f)
        canvas.drawRect(boxRect, boxPaint)
        
        val hintPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        if (config.mode == CopyingMode.COPYING) {
            canvas.drawText("Прочитай текст. Списывай по одному предложению.", margin + 5f, currentY + 15f, hintPaint)
            canvas.drawText("Проверь: начало предложений, буквы в словах, пробелы, знаки в конце.", margin + 5f, currentY + 30f, hintPaint)
        } else {
            canvas.drawText("Взрослый читает текст. Ребёнок записывает под диктовку.", margin + 5f, currentY + 15f, hintPaint)
            canvas.drawText("Проверь: начало предложений, буквы в словах, пробелы, знаки в конце.", margin + 5f, currentY + 30f, hintPaint)
        }
        
        currentY += 60f
        
        // Рисуем линии для письма
        val lineSpacingPx = config.lineStep * 3.78f // Перевод мм в пиксели (примерно)
        
        while (currentY < PAGE_HEIGHT - 40f) {
            canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
            currentY += lineSpacingPx
        }
    }

    private fun drawBlankSheet(canvas: Canvas, config: CopyingConfig, story: CopyingStory) {
        val bgColor = if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA")
        canvas.drawColor(bgColor)
        val margin = 40f
        var currentY = 60f
        
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        canvas.drawText("Дополнительный лист для письма", PAGE_WIDTH / 2f - 100f, currentY, headerPaint)
        currentY += 40f
        
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        val lineSpacingPx = config.lineStep * 3.78f
        
        while (currentY < PAGE_HEIGHT - 40f) {
            canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
            currentY += lineSpacingPx
        }
    }
}
