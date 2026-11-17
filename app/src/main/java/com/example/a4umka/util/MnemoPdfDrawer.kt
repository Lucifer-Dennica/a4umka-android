package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.*

/**
 * Генератор листов мнемотаблиц A4.
 *
 * 3 режима:
 *  - READY_TABLE: эмодзи в клетках + номера + текст под таблицей (для взрослого)
 *  - DRAW_YOURSELF: пустые пронумерованные клетки + текст
 *  - DESCRIPTIVE_STORY: 2 описательные схемы на лист (эмодзи + 6 вопросов)
 */
object MnemoPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    private val bgColor = Color.parseColor("#FAFAF5")
    private val cardColor = Color.WHITE
    private val borderColor = Color.parseColor("#D4D4C8")
    private val dashedColor = Color.parseColor("#B0B0A8")
    private val textColor = Color.parseColor("#333333")
    private val numBgColor = Color.parseColor("#4A7A40")
    private val highlightColor = Color.parseColor("#D84315")
    private val titleColor = Color.parseColor("#2E5C8A")
    private val grayColor = Color.GRAY
    private val answerLineColor = Color.parseColor("#E0E0E0")

    private val sans = Typeface.create("sans-serif", Typeface.NORMAL)
    private val sansBold = Typeface.create("sans-serif", Typeface.BOLD)
    private val cursive = Typeface.create("cursive", Typeface.BOLD)

    // =====================================================================
    //                          ПУБЛИЧНЫЕ МЕТОДЫ
    // =====================================================================

    fun generatePreview(config: MnemoConfig, context: Context): List<Bitmap> {
        return renderPages(config)
    }

    fun drawDocument(document: PdfDocument, config: MnemoConfig, context: Context) {
        var pageNum = 1
        val bitmaps = renderPages(config)
        // Каждая «логическая страница» уже отрисована в отдельный Bitmap — переносим в PDF как есть.
        bitmaps.forEach { bmp ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            document.finishPage(page)
        }
    }

    private fun renderPages(config: MnemoConfig): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val resolver = resolveText(config)

        when (config.mode) {
            MnemoMode.READY_TABLE -> {
                resolver?.let { text ->
                    val bmp = newBitmap()
                    val canvas = android.graphics.Canvas(bmp)
                    drawReadyTable(canvas, text, config.showText)
                    pages.add(bmp)
                }
            }
            MnemoMode.DRAW_YOURSELF -> {
                resolver?.let { text ->
                    val bmp = newBitmap()
                    val canvas = android.graphics.Canvas(bmp)
                    drawDrawYourself(canvas, text, config.showText)
                    pages.add(bmp)
                }
            }
            MnemoMode.DESCRIPTIVE_STORY -> {
                val schemes = resolveSchemes(config)
                // По 2 схемы на лист
                schemes.chunked(2).forEach { pair ->
                    val bmp = newBitmap()
                    val canvas = android.graphics.Canvas(bmp)
                    drawDescriptivePair(canvas, pair)
                    pages.add(bmp)
                }
            }
        }
        return pages
    }

    private fun newBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        // Заливаем фоновым цветом заранее — это безопаснее, чем надеяться, что
        // каждый draw-метод сделает это сам.
        val c = android.graphics.Canvas(bmp)
        c.drawColor(bgColor)
        return bmp
    }

    private fun resolveText(config: MnemoConfig): MnemoText? {
        config.selectedText?.let { return it }
        return MnemoData.getRandomText(config.theme)
    }

    private fun resolveSchemes(config: MnemoConfig): List<DescriptiveScheme> {
        if (config.selectedSchemeId != null) {
            MnemoData.getSchemeById(config.selectedSchemeId)?.let { return listOf(it) }
        }
        // Если выбран конкретный — он один. Если «любой из категории» — берём 2 случайные.
        val fromCategory = MnemoData.getSchemesByCategory(config.descriptiveCategory)
        return if (fromCategory.size >= 2) fromCategory.shuffled().take(2) else fromCategory
    }

    // =====================================================================
    //                       1) ГОТОВАЯ МНЕМОТАБЛИЦА
    // =====================================================================

    private fun drawReadyTable(canvas: android.graphics.Canvas, text: MnemoText, showText: Boolean) {
        val margin = 40f
        drawHeader(canvas, margin, text)

        val usableWidth = PAGE_WIDTH - margin * 2

        // === Сетка ячеек ===
        val cols = when (text.cellsCount) {
            4 -> 2
            6 -> 2
            9 -> 3
            else -> 3
        }
        val rows = text.cellsCount / cols
        val gap = 12f
        val cellSize = (usableWidth - gap * (cols - 1)) / cols

        val gridTop = 145f
        val gridHeight = rows * cellSize + (rows - 1) * gap

        // Карточка с инструкторской надписью над сеткой
        val instrPaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = grayColor; typeface = sans
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Рассматривай картинки по порядку и рассказывай ${typeToWord(text.type).lowercase()}",
            PAGE_WIDTH / 2f, gridTop - 10f, instrPaint
        )

        val cardBg = Paint().apply { color = cardColor; style = Paint.Style.FILL }
        val cardBorder = Paint().apply {
            color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1.5f
        }
        val numBg = Paint().apply { color = numBgColor; style = Paint.Style.FILL }
        val numText = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }
        val emojiPaint = Paint().apply {
            textSize = cellSize * 0.55f; textAlign = Paint.Align.CENTER
        }

        for (i in text.lines.indices) {
            val line = text.lines[i]
            val col = i % cols
            val row = i / cols
            val left = margin + col * (cellSize + gap)
            val top = gridTop + row * (cellSize + gap)
            val rect = RectF(left, top, left + cellSize, top + cellSize)

            canvas.drawRoundRect(rect, 14f, 14f, cardBg)
            canvas.drawRoundRect(rect, 14f, 14f, cardBorder)

            // Номер в кружке
            val numCx = left + 18f
            val numCy = top + 18f
            canvas.drawCircle(numCx, numCy, 12f, numBg)
            canvas.drawText("${i + 1}", numCx, numCy + 5f, numText)

            // Эмодзи в центре
            val cx = left + cellSize / 2f
            val cy = top + cellSize / 2f + emojiPaint.textSize / 3f
            canvas.drawText(line.emoji, cx, cy, emojiPaint)
        }

        // === Текст под таблицей ===
        if (showText) {
            val textTop = gridTop + gridHeight + 30f
            drawTextBlock(canvas, text, margin, textTop, usableWidth)
        }
    }

    // =====================================================================
    //                       2) НАРИСУЙ САМ
    // =====================================================================

    private fun drawDrawYourself(canvas: android.graphics.Canvas, text: MnemoText, showText: Boolean) {
        val margin = 40f
        drawHeader(canvas, margin, text)

        val usableWidth = PAGE_WIDTH - margin * 2

        val cols = when (text.cellsCount) {
            4 -> 2; 6 -> 2; 9 -> 3; else -> 3
        }
        val rows = text.cellsCount / cols
        val gap = 12f
        val cellSize = (usableWidth - gap * (cols - 1)) / cols

        val gridTop = 145f
        val gridHeight = rows * cellSize + (rows - 1) * gap

        val instrPaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = grayColor; typeface = sans
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Нарисуй в каждой клеточке свою картинку-подсказку к строчке",
            PAGE_WIDTH / 2f, gridTop - 10f, instrPaint
        )

        val cardBg = Paint().apply { color = cardColor; style = Paint.Style.FILL }
        val dashedBorder = Paint().apply {
            color = dashedColor; style = Paint.Style.STROKE; strokeWidth = 1.5f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        val numBg = Paint().apply { color = numBgColor; style = Paint.Style.FILL }
        val numText = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }

        for (i in text.lines.indices) {
            val col = i % cols
            val row = i / cols
            val left = margin + col * (cellSize + gap)
            val top = gridTop + row * (cellSize + gap)
            val rect = RectF(left, top, left + cellSize, top + cellSize)

            canvas.drawRoundRect(rect, 14f, 14f, cardBg)
            canvas.drawRoundRect(rect, 14f, 14f, dashedBorder)

            val numCx = left + 18f
            val numCy = top + 18f
            canvas.drawCircle(numCx, numCy, 12f, numBg)
            canvas.drawText("${i + 1}", numCx, numCy + 5f, numText)
        }

        if (showText) {
            val textTop = gridTop + gridHeight + 30f
            drawTextBlock(canvas, text, margin, textTop, usableWidth)
        }
    }

    // =====================================================================
    //                  3) ОПИСАТЕЛЬНЫЙ РАССКАЗ (2 на лист)
    // =====================================================================

    private fun drawDescriptivePair(canvas: android.graphics.Canvas, schemes: List<DescriptiveScheme>) {
        val margin = 40f
        val usableWidth = PAGE_WIDTH - margin * 2
        val gap = 16f
        val blockHeight = (PAGE_HEIGHT - margin * 2 - gap) / 2

        // Заголовок листа
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 24f; color = titleColor; typeface = cursive
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Описательный рассказ", PAGE_WIDTH / 2f, 40f, titlePaint)
        val subPaint = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = grayColor; typeface = sans
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Расскажи о предмете по плану — отвечай полным предложением", PAGE_WIDTH / 2f, 56f, subPaint)

        val topStart = 75f
        schemes.forEachIndexed { index, scheme ->
            val top = topStart + index * (blockHeight + gap)
            drawDescriptiveBlock(canvas, scheme, margin, top, usableWidth, blockHeight)
        }
    }

    private fun drawDescriptiveBlock(
        canvas: android.graphics.Canvas,
        scheme: DescriptiveScheme,
        x: Float, y: Float, width: Float, height: Float
    ) {
        val cardBg = Paint().apply { color = cardColor; style = Paint.Style.FILL }
        val cardBorder = Paint().apply {
            color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1.5f
        }
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, 12f, 12f, cardBg)
        canvas.drawRoundRect(rect, 12f, 12f, cardBorder)

        // Слева — большой эмодзи
        val emojiSize = height * 0.55f
        val emojiPaint = Paint().apply { textSize = emojiSize; textAlign = Paint.Align.CENTER }
        val emojiCx = x + height / 2f
        val emojiCy = y + height / 2f + emojiSize / 3f
        canvas.drawText(scheme.emoji, emojiCx, emojiCy, emojiPaint)

        // Подпись под эмодзи
        val emojiLabelPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = textColor; typeface = sansBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(scheme.title, emojiCx, y + height - 14f, emojiLabelPaint)

        // Справа — 6 вопросов с местом для ответа
        val textLeft = x + height + 16f
        val textWidth = width - height - 24f
        val questionPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = textColor; typeface = sansBold
        }
        val numBg = Paint().apply { color = numBgColor; style = Paint.Style.FILL }
        val numText = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }
        val answerLine = Paint().apply {
            color = answerLineColor; strokeWidth = 1f
        }

        val questionCount = scheme.questions.size
        val qHeight = (height - 24f) / questionCount
        for (i in 0 until questionCount) {
            val qy = y + 12f + i * qHeight + qHeight / 2f
            // Номер вопроса
            val nCx = textLeft + 10f
            canvas.drawCircle(nCx, qy - 4f, 9f, numBg)
            canvas.drawText("${i + 1}", nCx, qy - 1f, numText)
            // Сам вопрос
            canvas.drawText(scheme.questions[i], textLeft + 24f, qy, questionPaint)
            // Линия для ответа
            val answerY = qy + 9f
            canvas.drawLine(textLeft + 24f, answerY, x + width - 12f, answerY, answerLine)
        }
    }

    // =====================================================================
    //                         ОБЩИЕ ХЕЛПЕРЫ
    // =====================================================================

    private fun drawHeader(canvas: android.graphics.Canvas, margin: Float, text: MnemoText) {
        // Имя / Дата прямоугольники
        val boxPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val boxBorder = Paint().apply {
            color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1f
        }

        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxBorder)

        val labelPaint = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = grayColor; typeface = sans
        }
        canvas.drawText("Имя:", margin + 5f, 38f, labelPaint)
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawLine(margin + 30f, 40f, margin + 110f, 40f, linePaint)

        // Метка посередине
        val centerBg = Paint().apply {
            color = Color.parseColor("#FFF3E0"); style = Paint.Style.FILL
        }
        val centerRect = RectF(PAGE_WIDTH / 2f - 80f, 25f, PAGE_WIDTH / 2f + 80f, 45f)
        canvas.drawRoundRect(centerRect, 5f, 5f, centerBg)
        val centerText = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = highlightColor; typeface = sansBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Мнемотаблица · ${typeToWord(text.type)}",
            PAGE_WIDTH / 2f, 38f, centerText
        )

        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxBorder)
        canvas.drawText("Дата:", PAGE_WIDTH - margin - 115f, 38f, labelPaint)
        canvas.drawLine(PAGE_WIDTH - margin - 85f, 40f, PAGE_WIDTH - margin - 10f, 40f, linePaint)

        // Заголовок листа
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 26f; color = titleColor; typeface = cursive
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text.title, PAGE_WIDTH / 2f, 85f, titlePaint)

        // Возрастная метка
        val agePaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = grayColor; typeface = sans
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "${text.age.title} · ${text.cellsCount} клеток · тема: ${text.theme.title}",
            PAGE_WIDTH / 2f, 105f, agePaint
        )
    }

    private fun drawTextBlock(
        canvas: android.graphics.Canvas,
        text: MnemoText,
        x: Float, y: Float, width: Float
    ) {
        val labelPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = grayColor; typeface = sansBold
        }
        canvas.drawText("Текст для взрослого:", x, y, labelPaint)

        val linePaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = textColor; typeface = sans
        }
        val numBg = Paint().apply { color = numBgColor; style = Paint.Style.FILL }
        val numText = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }

        var cy = y + 25f
        text.lines.forEachIndexed { index, line ->
            // Номер строки
            val nCx = x + 8f
            canvas.drawCircle(nCx, cy - 4f, 9f, numBg)
            canvas.drawText("${index + 1}", nCx, cy - 1f, numText)
            // Текст строки
            canvas.drawText(line.text, x + 24f, cy, linePaint)
            cy += 22f
        }
    }

    private fun typeToWord(type: MnemoType): String = when (type) {
        MnemoType.POEM -> "Стих"
        MnemoType.TALE -> "Сказка"
        MnemoType.STORY -> "Рассказ"
        MnemoType.POTESHKA -> "Потешка"
        MnemoType.DESCRIPTIVE -> "Описательный рассказ"
    }
}
