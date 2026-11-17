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
    private val dashedColor = Color.parseColor("#7A8A6A")  // тёмно-зелено-серый, заметнее чем #B0B0A8
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
        // Если выбран конкретный предмет — берём только его (1 схема = 1 лист с 1 блоком).
        if (config.selectedSchemeId != null) {
            MnemoData.getSchemeById(config.selectedSchemeId)?.let { return listOf(it) }
        }
        // Иначе — sheetsCount × 2 уникальных случайных схем из категории.
        // 1 лист = 2 картинки, 2 листа = 4, 3 листа = 6.
        // Используем randomSeed, чтобы кнопка «Обновить» давала новый набор.
        val needed = (config.descriptiveSheetsCount.coerceIn(1, 3)) * 2
        val fromCategory = MnemoData.getSchemesByCategory(config.descriptiveCategory)
        // Воспроизводимое перемешивание по seed — android.util.Random не deterministic на старых API,
        // поэтому используем kotlin.random.Random с явным seed.
        val random = kotlin.random.Random(config.randomSeed)
        return if (fromCategory.size >= needed) {
            fromCategory.shuffled(random).take(needed)
        } else {
            fromCategory
        }
    }

    // =====================================================================
    //                       1) ГОТОВАЯ МНЕМОТАБЛИЦА
    // =====================================================================

    private fun drawReadyTable(canvas: android.graphics.Canvas, text: MnemoText, showText: Boolean) {
        val margin = 40f
        drawHeader(canvas, margin, text)

        val usableWidth = PAGE_WIDTH - margin * 2

        // === Сетка ячеек ===
        // ИСПРАВЛЕНО: 6 клеток теперь 3×2 (а не 2×3) — клетки квадратнее, не вылезают за лист.
        val cols = when (text.cellsCount) {
            4 -> 2
            6 -> 3
            9 -> 3
            else -> 3
        }
        val rows = text.cellsCount / cols
        val gap = 12f
        val gridTop = 145f
        // ИСПРАВЛЕНО: размер клетки считаем от МИНИМУМА ширины и доступной высоты,
        // чтобы сетка не вылезала за лист и под неё оставалось место под текст.
        // Резервируем: gridTop (145) + подзаголовок-инструкция + текст под таблицей.
        val textBlockReserve = if (showText) (text.lines.size * 19f + 35f) else 0f
        val availableHeight = PAGE_HEIGHT - gridTop - 30f - textBlockReserve - 20f
        val cellByWidth = (usableWidth - gap * (cols - 1)) / cols
        val cellByHeight = (availableHeight - gap * (rows - 1)) / rows
        val cellSize = minOf(cellByWidth, cellByHeight)
        val gridHeight = rows * cellSize + (rows - 1) * gap
        // Центрируем сетку по горизонтали
        val gridLeft = margin + (usableWidth - (cols * cellSize + (cols - 1) * gap)) / 2f

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
            textSize = cellSize * 0.5f; textAlign = Paint.Align.CENTER
        }

        for (i in text.lines.indices) {
            val line = text.lines[i]
            val col = i % cols
            val row = i / cols
            val left = gridLeft + col * (cellSize + gap)
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

        // ИСПРАВЛЕНО: 6 клеток как 3×2; cellSize от min(ширина, доступная высота).
        val cols = when (text.cellsCount) {
            4 -> 2; 6 -> 3; 9 -> 3; else -> 3
        }
        val rows = text.cellsCount / cols
        // ИСПРАВЛЕНО: gap=8 (плотнее, как на сайте-эталоне), скругление 10 (аккуратнее).
        val gap = 8f
        val gridTop = 145f
        val textBlockReserve = if (showText) (text.lines.size * 17f + 35f) else 0f
        val availableHeight = PAGE_HEIGHT - gridTop - 30f - textBlockReserve - 20f
        val cellByWidth = (usableWidth - gap * (cols - 1)) / cols
        val cellByHeight = (availableHeight - gap * (rows - 1)) / rows
        val cellSize = minOf(cellByWidth, cellByHeight)
        val gridHeight = rows * cellSize + (rows - 1) * gap
        val gridLeft = margin + (usableWidth - (cols * cellSize + (cols - 1) * gap)) / 2f

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
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f, 4f), 0f)
        }
        val numBg = Paint().apply { color = numBgColor; style = Paint.Style.FILL }
        val numText = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }

        for (i in text.lines.indices) {
            val col = i % cols
            val row = i / cols
            val left = gridLeft + col * (cellSize + gap)
            val top = gridTop + row * (cellSize + gap)
            val rect = RectF(left, top, left + cellSize, top + cellSize)

            canvas.drawRoundRect(rect, 10f, 10f, cardBg)
            canvas.drawRoundRect(rect, 10f, 10f, dashedBorder)

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

        // === Слева — большая иконка предмета на цветном круге (как на сайте-эталоне) ===
        val emojiColWidth = 120f
        val emojiCx = x + emojiColWidth / 2f
        val emojiCy = y + height / 2f - 18f

        // Цветной круг-подложка (пастельный, по категории)
        val bgCircleRadius = 55f
        val bgCirclePaint = Paint().apply {
            color = categoryToPastelColor(scheme.category); style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(emojiCx, emojiCy, bgCircleRadius, bgCirclePaint)

        // Крупный эмодзи предмета
        val emojiPaint = Paint().apply {
            textSize = 72f; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(scheme.emoji, emojiCx, emojiCy + emojiPaint.textSize / 3f, emojiPaint)

        // Подпись предмета в "бейдже" под эмодзи
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = Color.WHITE; typeface = sansBold
            textAlign = Paint.Align.CENTER
        }
        val titleBgPaint = Paint().apply {
            color = numBgColor; style = Paint.Style.FILL
        }
        val titleWidth = titlePaint.measureText(scheme.title) + 28f
        val titleRect = RectF(
            emojiCx - titleWidth / 2f, y + height - 38f,
            emojiCx + titleWidth / 2f, y + height - 18f
        )
        canvas.drawRoundRect(titleRect, 10f, 10f, titleBgPaint)
        canvas.drawText(scheme.title, emojiCx, y + height - 23f, titlePaint)

        // === Справа — 6 КРУПНЫХ иконок-кнопок в сетке 2×3 (как на сайте-эталоне) ===
        val textLeft = x + emojiColWidth + 4f
        val textWidth = width - emojiColWidth - 14f
        val qCols = 2
        val qRows = 3
        val qGapX = 8f
        val qGapY = 6f
        val qCellW = (textWidth - qGapX * (qCols - 1)) / qCols
        val qCellH = (height - 16f - qGapY * (qRows - 1)) / qRows

        // Крупные иконки к вопросам (как на сайте-эталоне)
        val questionIcons = listOf("❓", "🎨", "📐", "👅", "🌱", "🍳")

        val qCardBg = Paint().apply {
            color = Color.parseColor("#FAFAF5"); style = Paint.Style.FILL
        }
        val qCardBorder = Paint().apply {
            color = Color.parseColor("#D0D0C8"); style = Paint.Style.STROKE; strokeWidth = 1f
        }
        // Цветной кружок под иконкой вопроса (по номеру — разные пастельные)
        val iconBgColors = listOf(
            Color.parseColor("#E3F2FD"),  // голубой для "Что это?"
            Color.parseColor("#FCE4EC"),  // розовый для "Цвет"
            Color.parseColor("#F3E5F5"),  // лиловый для "Форма"
            Color.parseColor("#FFF3E0"),  // оранжевый для "Вкус"
            Color.parseColor("#E8F5E9"),  // зелёный для "Где растёт"
            Color.parseColor("#FFFDE7")   // жёлтый для "Что готовят"
        )
        val iconCirclePaint = Paint().apply { style = Paint.Style.FILL; isAntiAlias = true }

        val iconPaint = Paint().apply {
            textSize = 28f; textAlign = Paint.Align.CENTER
        }
        val numBg = Paint().apply { color = numBgColor; style = Paint.Style.FILL }
        val numText = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }
        val questionPaint = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = textColor; typeface = sansBold
            textAlign = Paint.Align.CENTER
        }
        val answerLine = Paint().apply {
            color = answerLineColor; strokeWidth = 1f
        }

        for (i in 0 until 6) {
            val col = i % qCols
            val row = i / qCols
            val cellX = textLeft + col * (qCellW + qGapX)
            val cellY = y + 8f + row * (qCellH + qGapY)

            // Карточка вопроса
            val qRect = RectF(cellX, cellY, cellX + qCellW, cellY + qCellH)
            canvas.drawRoundRect(qRect, 8f, 8f, qCardBg)
            canvas.drawRoundRect(qRect, 8f, 8f, qCardBorder)

            // === Верх карточки: круглая иконка вопроса ===
            val iconCx = cellX + qCellW / 2f
            val iconCy = cellY + 22f
            iconCirclePaint.color = iconBgColors[i]
            canvas.drawCircle(iconCx, iconCy, 18f, iconCirclePaint)
            canvas.drawText(questionIcons[i], iconCx, iconCy + iconPaint.textSize / 3f, iconPaint)

            // === Центр карточки: текст вопроса ===
            canvas.drawText(scheme.questions[i], iconCx, iconCy + 26f, questionPaint)

            // === Низ карточки: номер + линия для ответа ===
            val nCx = cellX + 10f
            val answerY = cellY + qCellH - 12f
            canvas.drawCircle(nCx, answerY - 3f, 6f, numBg)
            canvas.drawText("${i + 1}", nCx, answerY - 1f, numText)
            // Линия для ответа от номера до правого края
            canvas.drawLine(cellX + 18f, answerY, cellX + qCellW - 6f, answerY, answerLine)
        }
    }

    // Пастельный цвет фона под эмодзи — по категории предмета
    private fun categoryToPastelColor(category: DescriptiveCategory): Int {
        return when (category) {
            DescriptiveCategory.VEGETABLES -> Color.parseColor("#FFE0B2")  // персиковый
            DescriptiveCategory.FRUITS    -> Color.parseColor("#FFCDD2")  // розовый
            DescriptiveCategory.ANIMALS   -> Color.parseColor("#D7CCC8")  // коричневый
            DescriptiveCategory.BIRDS     -> Color.parseColor("#BBDEFB")  // голубой
            DescriptiveCategory.TOYS      -> Color.parseColor("#F8BBD0")  // розовый
            DescriptiveCategory.TRANSPORT -> Color.parseColor("#CFD8DC")  // стальной
            DescriptiveCategory.CLOTHES   -> Color.parseColor("#C5CAE9")  // индиго
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
        // ИСПРАВЛЕНО: уменьшили шрифт (11f) и интерлиньяж (17f) — чтобы 9 строк
        // гарантированно помещались под таблицей на листе A4.
        val labelPaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = grayColor; typeface = sansBold
        }
        canvas.drawText("Текст для взрослого:", x, y, labelPaint)

        val linePaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = textColor; typeface = sans
        }
        val numBg = Paint().apply { color = numBgColor; style = Paint.Style.FILL }
        val numText = Paint().apply {
            isAntiAlias = true; textSize = 9f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }

        var cy = y + 20f
        text.lines.forEachIndexed { index, line ->
            // Номер строки
            val nCx = x + 7f
            canvas.drawCircle(nCx, cy - 3f, 8f, numBg)
            canvas.drawText("${index + 1}", nCx, cy - 1f, numText)
            // Текст строки
            canvas.drawText(line.text, x + 22f, cy, linePaint)
            cy += 17f
        }
    }

    private fun typeToWord(type: MnemoType): String {
        return when (type) {
            MnemoType.POEM -> "Стих"
            MnemoType.TALE -> "Сказка"
            MnemoType.STORY -> "Рассказ"
            MnemoType.POTESHKA -> "Потешка"
            MnemoType.DESCRIPTIVE -> "Описательный рассказ"
        }
    }
}
