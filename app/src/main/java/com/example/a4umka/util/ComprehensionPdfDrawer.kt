package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.ComprehensionConfig

object ComprehensionPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    // Генерация превью для экрана
    fun generatePreviewPages(config: ComprehensionConfig, context: Context): List<Bitmap> {
        val story = config.selectedStory ?: return emptyList()
        val pages = mutableListOf<Bitmap>()
        
        // Упрощенная логика: генерируем только первую страницу для превью,
        // чтобы не нагружать память, если текст очень длинный.
        // (В реальном PDF будут все страницы)
        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawPageContent(canvas, config, story, 1)
        pages.add(bmp)
        
        return pages
    }

    // Генерация самого PDF документа
    fun drawDocument(document: PdfDocument, config: ComprehensionConfig, context: Context) {
        val story = config.selectedStory ?: return

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        var page = document.startPage(pageInfo)
        
        // Рисуем контент. Если текст не влезает, drawPageContent вернет true
        // (В данной реализации мы рисуем всё на одной странице для простоты,
        // но код можно расширить для многостраничности)
        drawPageContent(page.canvas, config, story, pageNum)
        document.finishPage(page)

        // Если нужен отдельный лист ответов
        if (config.showAnswerSheet) {
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            drawAnswerSheet(page.canvas, config, story)
            document.finishPage(page)
        }
    }

    private fun drawPageContent(canvas: Canvas, config: ComprehensionConfig, story: com.example.a4umka.data.ComprehensionStory, pageNum: Int) {
        val bgColor = if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA")
        canvas.drawColor(bgColor)
        
        var currentY = 60f
        val margin = 40f
        
        // Шапка
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        canvas.drawText("Имя:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 35f, 42f, margin + 150f, 42f, linePaint)
        
        val gradeText = "${config.grade} класс"
        val gradeWidth = headerPaint.measureText(gradeText)
        canvas.drawText(gradeText, PAGE_WIDTH / 2f - gradeWidth / 2f, 40f, headerPaint)
        
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - margin, 42f, linePaint)
        
        currentY = 90f
        
        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 26f; color = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#33691E"); typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(story.title, PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 25f
        
        val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        canvas.drawText("Читай, замечай, размышляй", PAGE_WIDTH / 2f, currentY, subtitlePaint)
        currentY += 30f
        
        // Инструкция
        val instPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        canvas.drawText("Прочитай рассказ. Затем ответь на вопросы — устно или письменно.", margin, currentY, instPaint)
        currentY += 25f
        
        // Текст
        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = 18f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val lineNumPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = Typeface.DEFAULT }
        val lineHeight = bodyPaint.textSize * 1.5f
        
        val paragraphs = story.text.split("\n")
        var globalLineCount = 0
        
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = ""
            val lines = mutableListOf<String>()
            
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2 - 30f) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) lines.add(currentLine)
            
            for (line in lines) {
                globalLineCount++
                canvas.drawText(line, margin, currentY, bodyPaint)
                
                if (config.showWordCount) {
                    canvas.drawText(globalLineCount.toString(), PAGE_WIDTH - margin, currentY, lineNumPaint)
                }
                currentY += lineHeight
            }
            currentY += 10f
        }
        
        currentY += 20f
        
        // Вопросы (если это не лист ответов)
        if (!config.showAnswerSheet) {
            val qTitlePaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText("Вопросы:", margin, currentY, qTitlePaint)
            currentY += 30f
            
            val qPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
            
            story.questions.forEachIndexed { index, question ->
                val circlePaint = Paint().apply { color = if (config.isBlackAndWhite) Color.DKGRAY else Color.parseColor("#A5D6A7"); style = Paint.Style.FILL }
                canvas.drawCircle(margin + 10f, currentY - 5f, 10f, circlePaint)
                val numPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
                canvas.drawText((index + 1).toString(), margin + 10f, currentY, numPaint)
                
                canvas.drawText(question, margin + 30f, currentY, qPaint)
                currentY += 20f
                
                val ansBox = RectF(margin + 30f, currentY, PAGE_WIDTH - margin, currentY + 30f)
                canvas.drawRoundRect(ansBox, 5f, 5f, boxPaint)
                currentY += 50f
            }
        }
    }

    private fun drawAnswerSheet(canvas: Canvas, config: ComprehensionConfig, story: com.example.a4umka.data.ComprehensionStory) {
        val bgColor = if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA")
        canvas.drawColor(bgColor)
        val margin = 40f
        
        val ansTitlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы для взрослого", PAGE_WIDTH / 2f, 60f, ansTitlePaint)
        
        val hintText = if (config.showAdultHints) {
            "Проверьте технику чтения: скорость (слов/мин), понимание прочитанного, выразительность."
        } else {
            "Проверьте правильность ответов ребёнка."
        }
        val hintPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.GRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        canvas.drawText(hintText, PAGE_WIDTH / 2f, 90f, hintPaint)
        
        var ansY = 140f
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        
        story.questions.forEachIndexed { index, q ->
            canvas.drawText("Вопрос ${index + 1}: $q", margin, ansY, ansPaint)
            ansY += 20f
            canvas.drawText("Ответ: __________________________________________________", margin, ansY, ansPaint)
            ansY += 40f
        }
    }
}
