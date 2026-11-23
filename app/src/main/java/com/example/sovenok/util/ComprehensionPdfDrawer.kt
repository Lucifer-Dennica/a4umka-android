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
    private const val PREVIEW_SCALE = 3  // 4K-предпросмотр (×3 безопасно для памяти)

    // ============ ПРЕДПРОСМОТР (несколько страниц, если не помещается) ============
    fun generatePreviewPages(config: ComprehensionConfig, context: Context): List<Bitmap> {
        val story = config.selectedStory ?: return emptyList()
        val pages = mutableListOf<Bitmap>()

        // Считаем макет: какие блоки куда попадут
        val layout = buildLayout(config, story)

        for (i in 0 until layout.pageCount) {
            val bmp = Bitmap.createBitmap(
                (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
                (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bmp)
            canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
            drawPage(canvas, config, story, layout, i)
            pages.add(bmp)
        }

        return pages
    }

    // ============ PDF ============
    fun drawDocument(document: PdfDocument, config: ComprehensionConfig, context: Context) {
        val story = config.selectedStory ?: return
        val layout = buildLayout(config, story)

        for (i in 0 until layout.pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), i + 1).create()
            val page = document.startPage(pageInfo)
            drawPage(page.canvas, config, story, layout, i)
            document.finishPage(page)
        }
    }

    // ============ МАКЕТ СТРАНИЦ (какие блоки на какой странице) ============
    private data class Layout(
        val storyOnPage: Int,         // 0 = первая страница
        val questionsStartPage: Int,  // на какой странице начинаются вопросы
        val questionsEndPage: Int,    // на какой странице заканчиваются вопросы
        val answerSheetPage: Int,     // на какой странице лист ответов (или -1)
        val pageCount: Int
    )

    private fun buildLayout(config: ComprehensionConfig, story: ComprehensionStory): Layout {
        // Резервируем место на странице: шапка (~110pt) + заголовок рассказа (~50pt) + инструкции
        val usableHeight = PAGE_HEIGHT - 180f  // с учётом шапки и заголовков
        var storyHeight = 0f
        val bodyPaint = Paint().apply { textSize = config.fontSize }
        val lineHeight = bodyPaint.textSize * 1.5f
        for (paragraph in story.text.split("\n")) {
            val words = paragraph.split(" ")
            var line = ""
            var lines = 0
            for (word in words) {
                val test = if (line.isEmpty()) word else "$line $word"
                if (bodyPaint.measureText(test) > PAGE_WIDTH - 80f - 30f) {
                    lines++
                    line = word
                } else {
                    line = test
                }
            }
            if (line.isNotEmpty()) lines++
            storyHeight += lines * lineHeight + 10f
        }

        // Высота вопросов (если они будут на отдельной странице)
        var questionsHeight = 30f  // заголовок "Вопросы:"
        story.questions.forEach {
            questionsHeight += 20f + 50f  // вопрос + поле для ответа
        }

        // Если рассказ не помещается на одной странице с вопросами — вопросы уходят на следующую
        val storyFitsWithQuestions = storyHeight + questionsHeight + 50f <= usableHeight
        val storyFitsAlone = storyHeight <= usableHeight

        return if (storyFitsWithQuestions || config.showAnswerSheet) {
            // Рассказ + вопросы на 1-й странице, или только рассказ (если showAnswerSheet, вопросы будут на листе ответов)
            val answerSheetPage = if (config.showAnswerSheet) 1 else -1
            val pageCount = if (config.showAnswerSheet) 2 else 1
            Layout(
                storyOnPage = 0,
                questionsStartPage = if (config.showAnswerSheet) -1 else 0,
                questionsEndPage = if (config.showAnswerSheet) -1 else 0,
                answerSheetPage = answerSheetPage,
                pageCount = pageCount
            )
        } else if (storyFitsAlone) {
            // Рассказ на 1-й странице, вопросы на 2-й
            val answerSheetPage = if (config.showAnswerSheet) 2 else -1
            val pageCount = if (config.showAnswerSheet) 3 else 2
            Layout(
                storyOnPage = 0,
                questionsStartPage = 1,
                questionsEndPage = 1,
                answerSheetPage = answerSheetPage,
                pageCount = pageCount
            )
        } else {
            // Рассказ не помещается даже один — рисуем как есть (вопросы на 2-й странице)
            val answerSheetPage = if (config.showAnswerSheet) 2 else -1
            val pageCount = if (config.showAnswerSheet) 3 else 2
            Layout(
                storyOnPage = 0,
                questionsStartPage = 1,
                questionsEndPage = 1,
                answerSheetPage = answerSheetPage,
                pageCount = pageCount
            )
        }
    }

    // ============ ОТРИСОВКА ОДНОЙ СТРАНИЦЫ ============
    private fun drawPage(
        canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory,
        layout: Layout, pageIndex: Int
    ) {
        // Фон всегда белый
        canvas.drawColor(Color.WHITE)

        val margin = 40f
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        // Шапка (на всех страницах)
        canvas.drawText("Имя:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 35f, 42f, margin + 150f, 42f, linePaint)

        val gradeText = "${config.grade} класс"
        val gradeWidth = headerPaint.measureText(gradeText)
        canvas.drawText(gradeText, PAGE_WIDTH / 2f - gradeWidth / 2f, 40f, headerPaint)

        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // Лист ответов — отдельная страница
        if (pageIndex == layout.answerSheetPage) {
            drawAnswerSheet(canvas, config, story)
            return
        }

        // Страница с рассказом
        if (pageIndex == layout.storyOnPage) {
            var currentY = 90f

            val titlePaint = Paint().apply {
                isAntiAlias = true; textSize = 26f
                color = Color.parseColor("#33691E")
                typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
            }
            canvas.drawText(story.title, PAGE_WIDTH / 2f, currentY, titlePaint)
            currentY += 25f

            val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
            canvas.drawText("Читай, замечай, размышляй", PAGE_WIDTH / 2f, currentY, subtitlePaint)
            currentY += 30f

            val instPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
            canvas.drawText("Прочитай рассказ. Затем ответь на вопросы — устно или письменно.", margin, currentY, instPaint)
            currentY += 25f

            // Используем размер шрифта из конфига
            val bodyPaint = Paint().apply {
                isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT
            }
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
                    // Если рассказ не помещается на этой странице и вопросы на следующей — обрезаем рассказ
                    if (layout.questionsStartPage != pageIndex && currentY > PAGE_HEIGHT - 60f) continue
                    canvas.drawText(line, margin, currentY, bodyPaint)

                    if (config.showWordCount) {
                        canvas.drawText(globalLineCount.toString(), PAGE_WIDTH - margin, currentY, lineNumPaint)
                    }
                    currentY += lineHeight
                }
                currentY += 10f
            }

            // Если вопросы тоже на этой странице — рисуем их
            if (layout.questionsStartPage == pageIndex && !config.showAnswerSheet) {
                currentY += 20f
                drawQuestions(canvas, config, story, currentY, margin)
            }
            return
        }

        // Страница с вопросами (если рассказ не уместился с вопросами на одной странице)
        if (pageIndex >= layout.questionsStartPage && pageIndex <= layout.questionsEndPage && !config.showAnswerSheet) {
            var currentY = 90f
            val qTitlePaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText("Вопросы:", margin, currentY, qTitlePaint)
            currentY += 30f

            drawQuestions(canvas, config, story, currentY, margin)
        }
    }

    // ============ ОТРИСОВКА ВОПРОСОВ ============
    private fun drawQuestions(
        canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory,
        startY: Float, margin: Float
    ) {
        var currentY = startY
        val qPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }

        story.questions.forEachIndexed { index, question ->
            // Если не помещается — пропускаем (пагинация не реализована для >1 страницы вопросов)
            if (currentY > PAGE_HEIGHT - 60f) return@forEachIndexed

            val circlePaint = Paint().apply {
                color = Color.parseColor("#A5D6A7")
                style = Paint.Style.FILL
            }
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

    // ============ ЛИСТ ОТВЕТОВ ============
    private fun drawAnswerSheet(canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory) {
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
            if (ansY > PAGE_HEIGHT - 60f) return@forEachIndexed
            canvas.drawText("Вопрос ${index + 1}: $q", margin, ansY, ansPaint)
            ansY += 20f
            canvas.drawText("Ответ: __________________________________________________", margin, ansY, ansPaint)
            ansY += 40f
        }
    }
}
