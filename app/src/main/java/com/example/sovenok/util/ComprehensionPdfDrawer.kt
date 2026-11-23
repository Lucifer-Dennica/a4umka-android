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
    const val MARGIN = 40f
    const val START_Y = 70f
    const val BOTTOM_MARGIN = 50f

    data class LineData(val text: String, val cumulativeWords: Int)
    data class LayoutPage(val lines: List<LineData>, val hasQuestions: Boolean)

    fun generatePreviewPages(config: ComprehensionConfig, context: Context): List<Bitmap> {
        val story = config.selectedStory ?: return emptyList()
        val layout = calculateLayout(config, story)
        val pages = mutableListOf<Bitmap>()

        for (i in layout.indices) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawPage(canvas, config, story, layout[i], i + 1, layout.size)
            pages.add(bmp)
        }

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

        for (i in layout.indices) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val page = document.startPage(pageInfo)
            drawPage(page.canvas, config, story, layout[i], pageNum, layout.size)
            document.finishPage(page)
            pageNum++
        }

        if (config.showAnswerSheet) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val page = document.startPage(pageInfo)
            drawAnswerSheet(page.canvas, config, story)
            document.finishPage(page)
        }
    }

    private fun calculateLayout(config: ComprehensionConfig, story: ComprehensionStory): List<LayoutPage> {
        val paint = Paint().apply { textSize = config.fontSize; typeface = Typeface.DEFAULT }
        val lineHeight = config.fontSize * 1.4f
        val maxWidth = PAGE_WIDTH - (MARGIN * 2)
        val availableHeight = PAGE_HEIGHT - START_Y - BOTTOM_MARGIN

        // 1. Разбиваем текст на строки с накопительным счетчиком слов
        val allLines = mutableListOf<LineData>()
        var runningWordCount = 0
        val paragraphs = story.text.split("\n")

        for (para in paragraphs) {
            val words = para.split(" ")
            var currentLine = ""
            var lineWordCount = 0

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    if (currentLine.isNotEmpty()) {
                        runningWordCount += lineWordCount
                        allLines.add(LineData(currentLine, runningWordCount))
                    }
                    currentLine = word
                    lineWordCount = 1
                } else {
                    currentLine = testLine
                    lineWordCount++
                }
            }
            if (currentLine.isNotEmpty()) {
                runningWordCount += lineWordCount
                allLines.add(LineData(currentLine, runningWordCount))
            }
            allLines.add(LineData("", runningWordCount)) // Пустая строка для абзаца
        }

        // 2. Разбиваем на страницы
        val pages = mutableListOf<LayoutPage>()
        var currentPageLines = mutableListOf<LineData>()
        var currentY = START_Y
        
        // Высота, которую займут вопросы
        val qPaint = Paint().apply { textSize = if (config.grade >= 3) 13f else 14f }
        val qLineHeight = qPaint.textSize * 1.3f
        val questionBlockHeight = 40f + (story.questions.size * (qLineHeight + 30f + 20f)) // заголовок + (вопрос + рамка + отступ)

        fun isRoomFor(height: Float): Boolean = currentY + height <= availableHeight

        fun savePage(hasQuestions: Boolean = false) {
            if (currentPageLines.isNotEmpty() || hasQuestions) {
                pages.add(LayoutPage(currentPageLines.toList(), hasQuestions))
                currentPageLines.clear()
                currentY = START_Y
            }
        }

        for (line in allLines) {
            if (!isRoomFor(lineHeight)) {
                savePage()
            }
            currentPageLines.add(line)
            currentY += if (line.text.isEmpty()) lineHeight * 0.6f else lineHeight
        }

        // Проверяем, влезают ли вопросы на последнюю страницу с текстом
        val hasQuestions = !config.showAnswerSheet && story.questions.isNotEmpty()
        if (hasQuestions) {
            if (!isRoomFor(questionBlockHeight)) {
                savePage(hasQuestions = true) // Вопросы уйдут на новую страницу
            } else {
                savePage(hasQuestions = true) // Вопросы поместятся на текущей
            }
        } else if (currentPageLines.isNotEmpty()) {
            savePage()
        }

        return pages.ifEmpty { listOf(LayoutPage(emptyList(), false)) }
    }

    private fun drawPage(canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory, pageData: LayoutPage, pageNum: Int, totalPages: Int) {
        val bgColor = if (config.isPeachBackground) Color.parseColor("#FFF5E6") else Color.WHITE
        canvas.drawColor(bgColor)

        val headerPaint = Paint().apply { textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        // Шапка
        canvas.drawText("Имя:", MARGIN, 40f, headerPaint)
        canvas.drawLine(MARGIN + 35f, 42f, MARGIN + 150f, 42f, linePaint)
        
        val gradeText = "${config.grade} класс"
        canvas.drawText(gradeText, PAGE_WIDTH / 2f - headerPaint.measureText(gradeText) / 2f, 40f, headerPaint)
        
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - MARGIN, 42f, linePaint)

        var currentY = START_Y

        // Заголовок и инструкция только на 1-й странице
        if (pageNum == 1) {
            val titlePaint = Paint().apply { textSize = 26f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
            canvas.drawText(story.title, PAGE_WIDTH / 2f, currentY, titlePaint)
            currentY += 25f
            
            val subPaint = Paint().apply { textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
            canvas.drawText("Читай, замечай, размышляй", PAGE_WIDTH / 2f, currentY, subPaint)
            currentY += 30f
            
            val instPaint = Paint().apply { textSize = 12f; color = Color.DKGRAY }
            canvas.drawText("Прочитай рассказ. Затем ответь на вопросы.", MARGIN, currentY, instPaint)
            currentY += 25f
        }

        val bodyPaint = Paint().apply { textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val lineHeight = config.fontSize * 1.4f
        val countPaint = Paint().apply { textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.RIGHT }

        // Рисуем строки текста
        for (lineData in pageData.lines) {
            if (lineData.text.isNotEmpty()) {
                canvas.drawText(lineData.text, MARGIN, currentY, bodyPaint)
                if (config.showWordCount) {
                    canvas.drawText(lineData.cumulativeWords.toString(), PAGE_WIDTH - MARGIN, currentY, countPaint)
                }
            }
            currentY += if (lineData.text.isEmpty()) lineHeight * 0.6f else lineHeight
        }

        // Рисуем вопросы, если они на этой странице
        if (pageData.hasQuestions) {
            currentY += 20f
            val qTitlePaint = Paint().apply { textSize = 20f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText("Вопросы:", MARGIN, currentY, qTitlePaint)
            currentY += 30f

            val qPaint = Paint().apply { textSize = if (config.grade >= 3) 13f else 14f; color = Color.BLACK }
            val qLineHeight = qPaint.textSize * 1.3f
            val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
            val circlePaint = Paint().apply { color = Color.DKGRAY; style = Paint.Style.FILL }

            story.questions.forEachIndexed { index, question ->
                canvas.drawCircle(MARGIN + 10f, currentY - 5f, 10f, circlePaint)
                val numPaint = Paint().apply { textSize = 12f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
                canvas.drawText((index + 1).toString(), MARGIN + 10f, currentY, numPaint)
                
                canvas.drawText(question, MARGIN + 30f, currentY, qPaint)
                currentY += qLineHeight + 5f
                
                val ansBox = RectF(MARGIN + 30f, currentY, PAGE_WIDTH - MARGIN, currentY + 30f)
                canvas.drawRoundRect(ansBox, 5f, 5f, boxPaint)
                currentY += 40f
            }
        }
        
        // Номер страницы внизу
        if (totalPages > 1) {
            val pagePaint = Paint().apply { textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
            canvas.drawText("Страница $pageNum", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, pagePaint)
        }
    }

    private fun drawAnswerSheet(canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory) {
        canvas.drawColor(Color.WHITE)
        val margin = 40f
        var currentY = 60f

        val titlePaint = Paint().apply { textSize = 24f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы и методические подсказки", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f

        val hintPaint = Paint().apply { textSize = 12f; color = Color.DKGRAY }
        val hintText = if (config.grade <= 2) "Проверьте, называет ли ребёнок героев и предметы. Ответы могут быть краткими." 
                       else "Ребёнок может ответить своими словами. Ниже — смысловые ориентиры, а не обязательные формулировки."
        
        // Простой перенос текста подсказки
        val words = hintText.split(" ")
        var line = ""
        for (word in words) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (hintPaint.measureText(test) > PAGE_WIDTH - margin * 2) {
                canvas.drawText(line, margin, currentY, hintPaint)
                currentY += 18f
                line = word
            } else {
                line = test
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, margin, currentY, hintPaint)
            currentY += 30f
        }

        val qPaint = Paint().apply { textSize = 14f; color = Color.BLACK }
        story.questions.forEachIndexed { index, q ->
            canvas.drawText("${index + 1}. $q", margin, currentY, qPaint)
            currentY += 25f
            val ans = if (index < story.answers.size && story.answers[index].isNotEmpty()) story.answers[index] else "_______________________________"
            canvas.drawText("Ответ: $ans", margin, currentY, qPaint)
            currentY += 40f
        }
        
        // Блок подсчета слов внизу листа ответов
        currentY += 20f
        val techPaint = Paint().apply { textSize = 11f; color = Color.DKGRAY }
        val techText = "В тексте ${story.totalWords} ${getWordForm(story.totalWords)}. За одну минуту: прочитано _____ слов, ошибок _____. " +
            "Если текст закончился раньше минуты, запишите общее время: _____ сек. Для следующей проверки выберите незнакомый текст."
        
        var tLine = ""
        for (word in techText.split(" ")) {
            val test = if (tLine.isEmpty()) word else "$tLine $word"
            if (techPaint.measureText(test) > PAGE_WIDTH - margin * 2) {
                canvas.drawText(tLine, margin, currentY, techPaint)
                currentY += 16f
                tLine = word
            } else {
                tLine = test
            }
        }
        if (tLine.isNotEmpty()) canvas.drawText(tLine, margin, currentY, techPaint)
    }

    private fun getWordForm(count: Int): String {
        val lastTwo = count % 100
        val last = count % 10
        return when {
            lastTwo in 11..19 -> "слов"
            last == 1 -> "слово"
            last in 2..4 -> "слова"
            else -> "слов"
        }
    }
}
