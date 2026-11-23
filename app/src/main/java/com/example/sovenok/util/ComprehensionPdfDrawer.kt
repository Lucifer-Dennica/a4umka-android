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

object ComprehensionPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f
    const val MARGIN = 40f
    
    // Высота элементов
    const val HEADER_HEIGHT = 70f // Шапка с Имя/Класс/Дата
    const val TITLE_BLOCK_HEIGHT = 80f // Заголовок + подзаголовок + инструкция
    const val CONTENT_START_Y = HEADER_HEIGHT + TITLE_BLOCK_HEIGHT // 150f
    const val BOTTOM_MARGIN = 50f
    const val AVAILABLE_HEIGHT = PAGE_HEIGHT - CONTENT_START_Y - BOTTOM_MARGIN // 642f
    
    fun generatePreviewPages(config: ComprehensionConfig, context: Context): List<Bitmap> {
        val story = config.selectedStory ?: return emptyList()
        val pages = mutableListOf<Bitmap>()
        
        // Рассчитываем страницы
        val layout = calculateLayout(config, story)
        
        // Рисуем страницы с текстом
        for (page in 1..layout.textPages) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawTextPage(canvas, config, story, page, layout)
            pages.add(bmp)
        }
        
        // Рисуем страницу ответов
        if (config.showAnswerSheet) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawAnswerSheet(canvas, config, story)
            pages.add(bmp)
        }
        
        return pages
    }

    fun drawDocument(document: PdfDocument, config: ComprehensionConfig, context: Context) {
        val story = config.selectedStory ?: return
        
        val layout = calculateLayout(config, story)
        var pageNum = 1
        
        // Страницы с текстом
        for (page in 1..layout.textPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val pdfPage = document.startPage(pageInfo)
            drawTextPage(pdfPage.canvas, config, story, page, layout)
            document.finishPage(pdfPage)
            pageNum++
        }
        
        // Страница ответов
        if (config.showAnswerSheet) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val pdfPage = document.startPage(pageInfo)
            drawAnswerSheet(pdfPage.canvas, config, story)
            document.finishPage(pdfPage)
        }
    }
    
    data class PageLayout(
        val textPages: Int,
        val textLinesPerPage: List<Int>, // количество строк текста на каждой странице
        val questionsOnLastPage: Boolean // вопросы на последней странице текста
    )
    
    private fun calculateLayout(config: ComprehensionConfig, story: com.example.sovenok.data.ComprehensionStory): PageLayout {
        val bodyPaint = Paint().apply {
            isAntiAlias = true
            textSize = config.fontSize
            typeface = Typeface.DEFAULT
        }
        
        val lineHeight = config.fontSize * 1.5f
        val maxWidth = PAGE_WIDTH - MARGIN * 2 - 30f
        
        // Разбиваем текст на строки
        val textLines = splitTextIntoLines(story.text, bodyPaint, maxWidth)
        
        // Считаем высоту вопросов
        val qPaint = Paint().apply {
            isAntiAlias = true
            textSize = if (config.grade >= 3) 13f else 14f
            typeface = Typeface.DEFAULT
        }
        
        var questionsHeight = 30f // заголовок "Вопросы:"
        story.questions.forEach { question ->
            questionsHeight += 15f // номер
            questionsHeight += 15f // текст вопроса
            questionsHeight += 30f // рамка ответа
            questionsHeight += 20f // отступ
        }
        
        val maxLinesPerPage = ((AVAILABLE_HEIGHT - questionsHeight) / lineHeight).toInt()
        
        // Разбиваем строки на страницы
        val pages = mutableListOf<List<String>>()
        var currentPage = mutableListOf<String>()
        
        for (line in textLines) {
            if (currentPage.size >= maxLinesPerPage && currentPage.isNotEmpty()) {
                pages.add(currentPage)
                currentPage = mutableListOf()
            }
            currentPage.add(line)
        }
        if (currentPage.isNotEmpty()) {
            pages.add(currentPage)
        }
        
        val textPages = pages.size.coerceAtLeast(1)
        val textLinesPerPage = pages.map { it.size }
        
        return PageLayout(
            textPages = textPages,
            textLinesPerPage = textLinesPerPage,
            questionsOnLastPage = true
        )
    }
    
    private fun splitTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.split("\n")
        
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = ""
            
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                    }
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
            lines.add("") // пустая строка между абзацами
        }
        
        return lines
    }
    
    private fun drawTextPage(canvas: Canvas, config: ComprehensionConfig, story: com.example.sovenok.data.ComprehensionStory, pageNum: Int, layout: PageLayout) {
        val bgColor = if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA")
        canvas.drawColor(bgColor)
        
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        // Шапка
        canvas.drawText("Имя:", MARGIN, 40f, headerPaint)
        canvas.drawLine(MARGIN + 35f, 42f, MARGIN + 150f, 42f, linePaint)
        
        val gradeText = "${config.grade} класс"
        val gradeWidth = headerPaint.measureText(gradeText)
        canvas.drawText(gradeText, PAGE_WIDTH / 2f - gradeWidth / 2f, 40f, headerPaint)
        
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - MARGIN, 42f, linePaint)
        
        var currentY = CONTENT_START_Y
        
        // Заголовок (только на первой странице)
        if (pageNum == 1) {
            val titlePaint = Paint().apply { 
                isAntiAlias = true
                textSize = 26f
                color = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#33691E")
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER 
            }
            canvas.drawText(story.title, PAGE_WIDTH / 2f, currentY, titlePaint)
            currentY += 25f
            
            val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
            canvas.drawText("Читай, замечай, размышляй", PAGE_WIDTH / 2f, currentY, subtitlePaint)
            currentY += 30f
            
            val instPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
            canvas.drawText("Прочитай рассказ. Затем ответь на вопросы — устно или письменно.", MARGIN, currentY, instPaint)
            currentY += 25f
        }
        
        val bodyPaint = Paint().apply { 
            isAntiAlias = true
            textSize = config.fontSize
            color = Color.BLACK
            typeface = Typeface.DEFAULT 
        }
        val lineNumPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = Typeface.DEFAULT }
        val lineHeight = config.fontSize * 1.5f
        val maxWidth = PAGE_WIDTH - MARGIN * 2 - 30f
        
        // Получаем строки для этой страницы
        val textLines = splitTextIntoLines(story.text, bodyPaint, maxWidth)
        val linesForThisPage = if (pageNum <= layout.textLinesPerPage.size) {
            val startIndex = layout.textLinesPerPage.take(pageNum - 1).sum()
            val endIndex = startIndex + layout.textLinesPerPage[pageNum - 1]
            textLines.subList(startIndex.coerceAtMost(textLines.size), endIndex.coerceAtMost(textLines.size))
        } else {
            emptyList()
        }
        
        var globalLineCount = if (pageNum > 1) {
            layout.textLinesPerPage.take(pageNum - 1).sum()
        } else {
            0
        }
        
        for (line in linesForThisPage) {
            if (line.isNotEmpty()) {
                globalLineCount++
                canvas.drawText(line, MARGIN, currentY, bodyPaint)
                
                if (config.showWordCount) {
                    canvas.drawText(globalLineCount.toString(), PAGE_WIDTH - MARGIN, currentY, lineNumPaint)
                }
            }
            currentY += lineHeight
        }
        
        currentY += 20f
        
        // Вопросы (только на последней странице текста)
        if (pageNum == layout.textPages && layout.questionsOnLastPage) {
            val qTitlePaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText("Вопросы:", MARGIN, currentY, qTitlePaint)
            currentY += 30f
            
            val qPaint = Paint().apply { 
                isAntiAlias = true
                textSize = if (config.grade >= 3) 13f else 14f
                color = Color.BLACK
                typeface = Typeface.DEFAULT 
            }
            val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
            
            story.questions.forEachIndexed { index, question ->
                val circlePaint = Paint().apply { color = if (config.isBlackAndWhite) Color.DKGRAY else Color.parseColor("#A5D6A7"); style = Paint.Style.FILL }
                canvas.drawCircle(MARGIN + 10f, currentY - 5f, 10f, circlePaint)
                val numPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
                canvas.drawText((index + 1).toString(), MARGIN + 10f, currentY, numPaint)
                
                canvas.drawText(question, MARGIN + 30f, currentY, qPaint)
                currentY += 20f
                
                val ansBox = RectF(MARGIN + 30f, currentY, PAGE_WIDTH - MARGIN, currentY + 30f)
                canvas.drawRoundRect(ansBox, 5f, 5f, boxPaint)
                currentY += 50f
            }
        }
    }
    
    private fun drawAnswerSheet(canvas: Canvas, config: ComprehensionConfig, story: com.example.sovenok.data.ComprehensionStory) {
        val bgColor = if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA")
        canvas.drawColor(bgColor)
        
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        // Шапка
        canvas.drawText("Имя:", MARGIN, 40f, headerPaint)
        canvas.drawLine(MARGIN + 35f, 42f, MARGIN + 150f, 42f, linePaint)
        
        val gradeText = "${config.grade} класс"
        val gradeWidth = headerPaint.measureText(gradeText)
        canvas.drawText(gradeText, PAGE_WIDTH / 2f - gradeWidth / 2f, 40f, headerPaint)
        
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - MARGIN, 42f, linePaint)
        
        var currentY = 90f
        
        val titlePaint = Paint().apply { 
            isAntiAlias = true
            textSize = 26f
            color = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#33691E")
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(story.title, PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 25f
        
        val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы — для взрослого", PAGE_WIDTH / 2f, currentY, subtitlePaint)
        currentY += 30f
        
        // Блок "Как проверять"
        val hintTitlePaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("Как проверять", MARGIN, currentY, hintTitlePaint)
        currentY += 20f
        
        val hintText = if (config.showAdultHints) {
            "Ребёнок может ответить своими словами. Ниже — смысловые ориентиры, а не обязательные формулировки."
        } else {
            "Проверьте правильность ответов ребёнка."
        }
        val hintPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        drawWrappedText(canvas, hintText, MARGIN, currentY, PAGE_WIDTH - MARGIN * 2, hintPaint, 18f)
        currentY += 40f
        
        // Вопросы с ответами
        val qPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        
        story.questions.forEachIndexed { index, question ->
            canvas.drawText("${index + 1}. $question", MARGIN, currentY, qPaint)
            currentY += 25f
            canvas.drawText("Ответ: __________________________________________________", MARGIN, currentY, qPaint)
            currentY += 40f
        }
        
        // Блок "В тексте N слов"
        currentY += 20f
        val wordCount = story.text.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val wordCountTitlePaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("В тексте $wordCount ${getWordForm(wordCount)}", MARGIN, currentY, wordCountTitlePaint)
        currentY += 25f
        
        val techPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val techText = "Заголовков не учитывается. Слова с дефисом считаются одним словом, отдельное тире словом не считается.\n\n" +
            "За одну минуту: прочитано _____ слов, ошибок _____. Отметьте последнее полностью прочитанное слово. Число справа — сумма слов до конца строки; для неполной строки прибавьте прочитанные слова к числу предыдущей.\n\n" +
            "Если текст закончился раньше минуты, запишите общее время: _____ секунд. Не пересчитывайте его автоматически в минутный норматив. Для следующей проверки выберите незнакомый текст. Обсудите содержание после чтения."
        
        drawWrappedText(canvas, techText, MARGIN, currentY, PAGE_WIDTH - MARGIN * 2, techPaint, 16f)
    }
    
    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint, lineHeight: Float) {
        var currentY = y
        val paragraphs = text.split("\n")
        
        for (paragraph in paragraphs) {
            if (paragraph.isEmpty()) {
                currentY += lineHeight / 2
                continue
            }
            
            val words = paragraph.split(" ")
            var currentLine = ""
            
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    canvas.drawText(currentLine, x, currentY, paint)
                    currentY += lineHeight
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, x, currentY, paint)
                currentY += lineHeight
            }
        }
    }
    
    private fun getWordForm(count: Int): String {
        val lastTwoDigits = count % 100
        val lastDigit = count % 10
        
        return when {
            lastTwoDigits in 11..19 -> "слов"
            lastDigit == 1 -> "слово"
            lastDigit in 2..4 -> "слова"
            else -> "слов"
        }
    }
}
