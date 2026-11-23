package com.example.sovenok.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.sovenok.data.ComprehensionConfig
import com.example.sovenok.data.ComprehensionStory

object ComprehensionPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreviewPages(config: ComprehensionConfig, context: Context): List<Bitmap> {
        val story = config.selectedStory ?: return emptyList()
        val pages = mutableListOf<Bitmap>()
        
        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawPageContent(canvas, config, story, 1)
        pages.add(bmp)
        
        if (config.showAnswerSheet) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            drawAnswerSheet(ansCanvas, config, story)
            pages.add(ansBmp)
        }
        return pages
    }

    fun drawDocument(document: PdfDocument, config: ComprehensionConfig, context: Context) {
        val story = config.selectedStory ?: return
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        var page = document.startPage(pageInfo)
        
        drawPageContent(page.canvas, config, story, pageNum)
        document.finishPage(page)

        if (config.showAnswerSheet) {
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            drawAnswerSheet(page.canvas, config, story)
            document.finishPage(page)
        }
    }

    private fun drawPageContent(canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory, pageNum: Int) {
        val bgColor = if (config.isPeachBackground) Color.parseColor("#FFF3E0") else Color.WHITE
        canvas.drawColor(bgColor)
        
        var currentY = 60f
        val margin = 40f
        
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
        
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 26f; color = Color.parseColor("#33691E"); typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText(story.title, PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 30f
        
        val instPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        canvas.drawText("Прочитай рассказ. Затем ответь на вопросы — устно или письменно.", margin, currentY, instPaint)
        currentY += 25f
        
        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val lineNumPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = Typeface.DEFAULT }
        val lineHeight = bodyPaint.textSize * 1.6f
        
        val paragraphs = story.text.split("\n")
        var cumulativeWordCount = 0
        
        for (paragraph in paragraphs) {
            val words = paragraph.split(Regex("\\s+")).filter { it.isNotBlank() }
            var currentLineWords = mutableListOf<String>()
            var currentLineWidth = 0f
            val maxLineWidth = PAGE_WIDTH - margin * 2 - 40f
            
            for (word in words) {
                val wordWidth = bodyPaint.measureText(word + " ")
                if (currentLineWidth + wordWidth > maxLineWidth && currentLineWords.isNotEmpty()) {
                    val lineText = currentLineWords.joinToString(" ")
                    cumulativeWordCount += currentLineWords.size
                    canvas.drawText(lineText, margin, currentY, bodyPaint)
                    if (config.showWordCount) {
                        canvas.drawText(cumulativeWordCount.toString(), PAGE_WIDTH - margin, currentY, lineNumPaint)
                    }
                    currentY += lineHeight
                    currentLineWords.clear()
                    currentLineWidth = 0f
                }
                currentLineWords.add(word)
                currentLineWidth += wordWidth
            }
            if (currentLineWords.isNotEmpty()) {
                val lineText = currentLineWords.joinToString(" ")
                cumulativeWordCount += currentLineWords.size
                canvas.drawText(lineText, margin, currentY, bodyPaint)
                if (config.showWordCount) {
                    canvas.drawText(cumulativeWordCount.toString(), PAGE_WIDTH - margin, currentY, lineNumPaint)
                }
                currentY += lineHeight
            }
            currentY += 10f
        }
        
        currentY += 20f
        
        val footerPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        // БЕЗОПАСНЫЙ ПОДСЧЁТ: если totalWords == 0, считаем динамически
        val wordCount = if (story.totalWords > 0) story.totalWords else story.text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        canvas.drawText("В тексте $wordCount слов. Заголовок не учитывается. Слова с дефисом считаются одним словом.", margin, currentY, footerPaint)
        currentY += 16f
        canvas.drawText("За одну минуту: прочитано _____ слов, ошибок _____. Отметьте последнее полностью прочитанное слово.", margin, currentY, footerPaint)
        currentY += 16f
        canvas.drawText("Число справа — сумма слов до конца строки. Если текст закончился раньше минуты, запишите общее время: _____ сек.", margin, currentY, footerPaint)
        currentY += 30f
        
        if (!config.showAnswerSheet) {
            val qTitlePaint = Paint().apply { isAntiAlias = true; textSize = 18f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText("Вопросы:", margin, currentY, qTitlePaint)
            currentY += 25f
            
            val qPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
            
            story.questions.forEachIndexed { index, question ->
                val circlePaint = Paint().apply { color = Color.parseColor("#A5D6A7"); style = Paint.Style.FILL }
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

    private fun drawAnswerSheet(canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory) {
        val bgColor = if (config.isPeachBackground) Color.parseColor("#FFF3E0") else Color.WHITE
        canvas.drawColor(bgColor)
        val margin = 40f
        
        val ansTitlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы — для взрослого", PAGE_WIDTH / 2f, 60f, ansTitlePaint)
        
        val hintText = "Ребёнок может ответить своими словами. Ниже — смысловые ориентиры, а не обязательные формулировки."
        val hintPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.GRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        canvas.drawText(hintText, PAGE_WIDTH / 2f, 90f, hintPaint)
        
        var ansY = 140f
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val qPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        
        story.questions.forEachIndexed { index, q ->
            canvas.drawText("Вопрос ${index + 1}: $q", margin, ansY, qPaint)
            ansY += 22f
            // БЕЗОПАСНЫЙ ДОСТУП: если ответа нет, выводим заглушку
            val answer = story.answers.getOrElse(index) { "Ответ не указан" }
            canvas.drawText("Ответ: $answer", margin, ansY, ansPaint)
            ansY += 40f
        }
    }
}
