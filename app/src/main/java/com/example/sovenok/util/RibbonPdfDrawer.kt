package com.example.sovenok.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.example.sovenok.R
import com.example.sovenok.data.*
import kotlin.math.ceil
import kotlin.random.Random

object RibbonPdfDrawer {
    // Альбомная ориентация: 842 × 595 (A4 landscape)
    const val PAGE_WIDTH = 842f
    const val PAGE_HEIGHT = 595f
    private const val PREVIEW_SCALE = 6  // 4K: 5052×3570 px

    private val PEACH_COLOR = Color.parseColor("#FAFAF5") // Нежный кремовый фон

    // ============ ПРЕДПРОСМОТР (1 лист) ============
    fun getPreview(config: RibbonConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()

        // Для BOARD — 4 страницы
        if (config.mode == RibbonMode.BOARD) {
            val boardPages = getBoardPages()
            for ((index, page) in boardPages.withIndex()) {
                val bmp = Bitmap.createBitmap(
                    (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
                    (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bmp)
                canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
                drawBoardPage(canvas, config, page, index + 1, boardPages.size, context)
                pages.add(bmp)
            }
            return pages
        }

        // Для остальных — 1 страница + страница ответов
        val bmp = Bitmap.createBitmap(
            (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
            (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
        drawRibbon(canvas, config, context, isAnswerKey = false)
        pages.add(bmp)

        if (config.showAnswers && (config.mode == RibbonMode.MISSING || config.mode == RibbonMode.COLORING)) {
            val ansBmp = Bitmap.createBitmap(
                (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
                (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
                Bitmap.Config.ARGB_8888
            )
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
            drawRibbon(ansCanvas, config, context, isAnswerKey = true)
            pages.add(ansBmp)
        }

        return pages
    }

    // ============ ОБЩАЯ ОТРИСОВКА ============
    fun drawRibbon(canvas: Canvas, config: RibbonConfig, context: Context, isAnswerKey: Boolean = false) {
        val bg = if (config.bgColor == RibbonBgColor.PEACH) PEACH_COLOR else Color.WHITE
        canvas.drawColor(bg)

        // Выбор шрифта: обычный или письменный
        val font = if (config.font == RibbonFont.CURSIVE) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

        // Для COLORING — отдельная отрисовка
        if (config.mode == RibbonMode.COLORING) {
            drawColoringPage(canvas, config, font, isAnswerKey)
            return
        }

        val titleText = when {
            isAnswerKey -> "Ответы"
            config.mode == RibbonMode.DESK -> "Лента букв"
            config.mode == RibbonMode.MISSING -> "Впиши пропущенные буквы"
            else -> "Лента букв"
        }

        val subtitleText = when {
            isAnswerKey -> "лента букв целиком"
            config.mode == RibbonMode.DESK -> "Гласные и согласные, звонкие и глухие, твердые и мягкие"
            config.mode == RibbonMode.MISSING -> "Впиши буквы в пустые клетки: цвет клетки и соседи — подсказка"
            else -> ""
        }

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = Color.DKGRAY
            typeface = font; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(titleText, PAGE_WIDTH / 2f, 50f, titlePaint)

        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.GRAY
            typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitleText, PAGE_WIDTH / 2f, 70f, subtitlePaint)

        if (config.mode != RibbonMode.DESK || isAnswerKey) {
            val fieldPaint = Paint().apply {
                isAntiAlias = true; textSize = 16f; color = Color.DKGRAY
                typeface = Typeface.DEFAULT
            }
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

            canvas.drawText("Имя:", 40f, 110f, fieldPaint)
            canvas.drawLine(80f, 112f, 200f, 112f, linePaint)
            canvas.drawText("Дата:", PAGE_WIDTH - 160f, 110f, fieldPaint)
            canvas.drawLine(PAGE_WIDTH - 115f, 112f, PAGE_WIDTH - 40f, 112f, linePaint)
        }

        // Лента (для DESK и MISSING)
        val cellSize = 36f
        val gap = 2f
        val colWidth = cellSize + gap
        val rowHeight = cellSize + gap

        val totalWidth = 5 * colWidth + 20f + 11 * colWidth + 10f + colWidth
        val startX = (PAGE_WIDTH - totalWidth) / 2f
        val startY = 150f

        val missingSet = mutableSetOf<String>()
        if (config.mode == RibbonMode.MISSING && !isAnswerKey) {
            val allLetters = (RibbonData.topVowels + RibbonData.bottomVowels + RibbonData.topConsonants + RibbonData.bottomConsonants).map { it.char }
            val countToHide = when (config.missingCount) {
                MissingCount.EIGHT -> 8
                MissingCount.SIXTEEN -> 16
                MissingCount.ALL -> allLetters.size
            }
            val random = Random(config.seed)
            missingSet.addAll(allLetters.shuffled(random).take(countToHide))
        }

        val vowelStartX = startX
        val consonantStartX = vowelStartX + 5 * colWidth + 20f
        val signStartX = consonantStartX + 11 * colWidth + 10f

        val sectionLabelPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = Color.parseColor("#9E9E9E")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER
        }
        val labelY = startY - 8f
        canvas.drawText("гласные", vowelStartX + 2.5f * colWidth, labelY, sectionLabelPaint)
        canvas.drawText("согласные", consonantStartX + 5.5f * colWidth, labelY, sectionLabelPaint)
        canvas.drawText("знаки", signStartX + 0.5f * colWidth, labelY, sectionLabelPaint)

        // Гласные
        RibbonData.topVowels.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, vowelStartX + index * colWidth, startY, cellSize, missingSet, font, isAnswerKey)
            drawCell(canvas, RibbonData.bottomVowels[index], config, vowelStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        // Согласные
        RibbonData.topConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY, cellSize, missingSet, font, isAnswerKey)
        }
        RibbonData.bottomConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        // Знаки (вертикально)
        RibbonData.signs.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, signStartX, startY + index * rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        // Подсказка «Эти буквы потерялись»
        if (config.mode == RibbonMode.MISSING && config.showHint && missingSet.isNotEmpty()) {
            val hintBoxTop = startY + 2 * rowHeight + 30f
            drawLostLettersBox(canvas, startX, hintBoxTop, PAGE_WIDTH - startX, missingSet, font)
        }

        drawLegend(canvas, 40f, PAGE_HEIGHT - 110f)
    }

    // ============ РЕЖИМ «РАСКРАСЬ ЛЕНТУ БУКВ» (КАК НА САЙТЕ) ============
    private fun drawColoringPage(canvas: Canvas, config: RibbonConfig, font: Typeface, isAnswerKey: Boolean) {
        // 1. Шапка (Имя, Раскрась, Дата)
        val headerY = 30f
        val headerRectHeight = 30f
        
        // Рамка Имя
        val nameRect = RectF(40f, headerY, 200f, headerY + headerRectHeight)
        drawHeaderBox(canvas, nameRect, "Имя:")
        
        // Рамка Дата
        val dateRect = RectF(PAGE_WIDTH - 200f, headerY, PAGE_WIDTH - 40f, headerY + headerRectHeight)
        drawHeaderBox(canvas, dateRect, "Дата:")

        // Бейдж "Раскрась" по центру
        val badgeRect = RectF(PAGE_WIDTH/2f - 60f, headerY, PAGE_WIDTH/2f + 60f, headerY + headerRectHeight)
        val badgeBgPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(badgeRect, 8f, 8f, badgeBgPaint)
        val badgeTextPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = Color.DKGRAY
            textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🎨 Раскрась", PAGE_WIDTH/2f, headerY + 20f, badgeTextPaint)

        // 2. Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 42f; color = Color.parseColor("#33691E") // Тёмно-зелёный
            typeface = font; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isAnswerKey) "Ответы: Раскрась ленту букв" else "Раскрась ленту букв",
            PAGE_WIDTH / 2f, 105f, titlePaint
        )

        // 3. Подзаголовок
        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = Color.GRAY
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Гласные — красным, всегда твёрдые — синим, всегда мягкие — зелёным, твёрдые или мягкие — пополам",
            PAGE_WIDTH / 2f, 130f, subtitlePaint
        )

        // 4. Сетка (Лента)
        val marginLeft = 40f
        
        // Размеры клеток
        val cellW = 44f
        val cellH = 55f
        val gap = 3f
        
        // Координаты X для секций
        val vowelStartX = marginLeft
        val consStartX = vowelStartX + 5 * (cellW + gap) + 25f
        val signStartX = consStartX + 11 * (cellW + gap) + 25f
        
        val startY = 170f
        val row2Y = startY + cellH + gap

        // Заголовки секций (текст и цветные линии)
        drawSectionHeader(canvas, "гласные", vowelStartX, startY - 15f, 5 * (cellW + gap), Color.parseColor("#E57373"))
        drawSectionHeader(canvas, "согласные", consStartX, startY - 15f, 11 * (cellW + gap), Color.parseColor("#64B5F6"))
        drawSectionHeader(canvas, "знаки", signStartX, startY - 15f, cellW, Color.parseColor("#9E9E9E"))

        // --- Иконки ---
        val iconPaint = Paint().apply { textSize = 22f; textAlign = Paint.Align.CENTER }
        // Колокольчик (над Лл - 1-я буква верхнего ряда согласных)
        canvas.drawText("🔔", consStartX - 20f, startY + cellH / 2f + 8f, iconPaint)
        // Наушники (над Пп - 1-я буква нижнего ряда согласных, сдвинутого вправо)
        canvas.drawText("🎧", consStartX + 5 * (cellW + gap) - 20f, row2Y + cellH / 2f + 8f, iconPaint)

        // --- Гласные ---
        RibbonData.topVowels.forEachIndexed { index, letter ->
            drawColoringCell(canvas, letter, config, vowelStartX + index * (cellW + gap), startY, cellW, cellH, font, isAnswerKey)
            drawColoringCell(canvas, RibbonData.bottomVowels[index], config, vowelStartX + index * (cellW + gap), row2Y, cellW, cellH, font, isAnswerKey)
        }

        // --- Согласные (верхний ряд - 11 букв) ---
        RibbonData.topConsonants.forEachIndexed { index, letter ->
            drawColoringCell(canvas, letter, config, consStartX + index * (cellW + gap), startY, cellW, cellH, font, isAnswerKey)
        }

        // --- Согласные (нижний ряд - 10 букв, выровнены вправо) ---
        val bottomConsStartOffset = 5 // Сдвиг на 5 клеток вправо, чтобы начать под 'Бб'
        RibbonData.bottomConsonants.forEachIndexed { index, letter ->
            drawColoringCell(canvas, letter, config, consStartX + (bottomConsStartOffset + index) * (cellW + gap), row2Y, cellW, cellH, font, isAnswerKey)
        }

        // --- Знаки (2 клетки вертикально) ---
        RibbonData.signs.forEachIndexed { index, letter ->
            drawColoringCell(canvas, letter, config, signStartX, startY + index * (cellH + gap), cellW, cellH, font, isAnswerKey)
        }

        // 5. Легенда-чеклист внизу
        val legendRect = RectF(40f, 380f, PAGE_WIDTH - 40f, PAGE_HEIGHT - 40f)
        drawColoringLegend(canvas, legendRect)
    }

    // Вспомогательная функция: Рамки для Имени/Даты
    private fun drawHeaderBox(canvas: Canvas, rect: RectF, label: String) {
        val bgPaint = Paint().apply { color = Color.parseColor("#FFFBF0"); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor("#D4C4A8"); style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, borderPaint)

        val textPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText(label, rect.left + 10f, rect.centerY() + 4f, textPaint)
        
        val linePaint = Paint().apply { color = Color.parseColor("#D4C4A8"); strokeWidth = 1f }
        val textWidth = textPaint.measureText(label)
        canvas.drawLine(rect.left + 10f + textWidth + 5f, rect.centerY() + 2f, rect.right - 10f, rect.centerY() + 2f, linePaint)
    }

    // Вспомогательная функция: Заголовки над секциями
    private fun drawSectionHeader(canvas: Canvas, title: String, startX: Float, y: Float, width: Float, color: Int) {
        val textPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = color
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, startX + width / 2f, y, textPaint)

        val linePaint = Paint().apply { this.color = color; strokeWidth = 2f }
        canvas.drawLine(startX, y + 5f, startX + width, y + 5f, linePaint)
    }

    // Отрисовка контурной клетки для раскраски
    private fun drawColoringCell(
        canvas: Canvas, letter: RibbonLetter, config: RibbonConfig,
        x: Float, y: Float, w: Float, h: Float, font: Typeface, isAnswerKey: Boolean
    ) {
        val fillColor = when (letter.color) {
            RibbonColor.RED -> Color.parseColor("#EF5350")
            RibbonColor.BLUE -> Color.parseColor("#42A5F5")
            RibbonColor.GREEN -> Color.parseColor("#66BB6A")
            RibbonColor.WHITE -> Color.WHITE
            RibbonColor.BOTH -> Color.WHITE
        }

        // Если это лист ответов — закрашиваем
        if (isAnswerKey) {
            val fillPaint = Paint().apply { color = fillColor; style = Paint.Style.FILL }
            canvas.drawRect(x, y, x + w, y + h, fillPaint)
            if (letter.color == RibbonColor.BOTH) {
                val p1 = Path().apply { moveTo(x, y); lineTo(x + w, y); lineTo(x, y + h); close() }
                canvas.drawPath(p1, Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
                val p2 = Path().apply { moveTo(x + w, y); lineTo(x + w, y + h); lineTo(x, y + h); close() }
                canvas.drawPath(p2, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
            }
        } else {
            // Если задание — просто белая заливка
            canvas.drawRect(x, y, x + w, y + h, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        }

        // Контур клетки (серый, скругленный)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#757575"); style = Paint.Style.STROKE; strokeWidth = 1.5f
        }
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 4f, 4f, borderPaint)

        // Буква внутри
        val text = when (config.case) {
            RibbonCase.BOTH -> letter.char
            RibbonCase.UPPERCASE -> letter.char.substring(0, 1)
            RibbonCase.LOWERCASE -> letter.char.substring(letter.char.length - 1)
        }
        
        val textSize = h * 0.45f
        val textPaint = Paint().apply {
            isAntiAlias = true; this.textSize = textSize; typeface = font; textAlign = Paint.Align.CENTER
            color = if (isAnswerKey && letter.color != RibbonColor.WHITE) Color.WHITE else Color.DKGRAY
        }
        val baselineY = y + h / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(text, x + w / 2f, baselineY, textPaint)
    }

    // ============ ЛЕГЕНДА-ЧЕКЛИСТ (КАК НА САЙТЕ) ============
    private fun drawColoringLegend(canvas: Canvas, rect: RectF) {
        // Рамка
        val bgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor("#D4C4A8"); style = Paint.Style.STROKE; strokeWidth = 1.5f }
        canvas.drawRoundRect(rect, 12f, 12f, bgPaint)
        canvas.drawRoundRect(rect, 12f, 12f, borderPaint)

        val padding = 20f
        val col1X = rect.left + padding
        val col2X = rect.left + (rect.width() / 2f) + 10f
        var currentY = rect.top + 30f // <--- ИСПРАВЛЕНО: теперь var, а не val
        val lineH = 35f

        val textPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val boldTextPaint = Paint(textPaint).apply { typeface = Typeface.DEFAULT_BOLD }
        val boxPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val boxBorder = Paint().apply { color = Color.parseColor("#BDBDBD"); style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val iconPaint = Paint().apply { textSize = 16f; textAlign = Paint.Align.CENTER }

        // --- Колонка 1 ---
        
        // 1. Гласные
        drawCheckbox(canvas, col1X, currentY - 12f, 16f, boxPaint, boxBorder)
        canvas.drawText("гласные — А О У Ы Э — после твёрдых, Я Ё Ю И Е — после", col1X + 26f, currentY, textPaint)
        canvas.drawText("мягких", col1X + 26f, currentY + 15f, textPaint)
        currentY += lineH + 10f

        // 2. Всегда мягкие
        drawCheckbox(canvas, col1X, currentY - 12f, 16f, boxPaint, boxBorder)
        canvas.drawText("всегда мягкие — Й Ч Щ", col1X + 26f, currentY, textPaint)
        currentY += lineH

        // 3. Звука не обозначают
        drawCheckbox(canvas, col1X, currentY - 12f, 16f, boxPaint, boxBorder)
        canvas.drawText("звука не обозначают — Ь и Ъ", col1X + 26f, currentY, textPaint)
        currentY += lineH

        // 4. Наушники (глухие)
        canvas.drawText("🎧", col1X + 8f, currentY + 4f, iconPaint)
        canvas.drawText("нижний ряд согласных — глухие", col1X + 26f, currentY, boldTextPaint)

        // --- Колонка 2 ---
        currentY = rect.top + 30f

        // 1. Всегда твёрдые
        drawCheckbox(canvas, col2X, currentY - 12f, 16f, boxPaint, boxBorder)
        canvas.drawText("всегда твёрдые — Ж Ш Ц", col2X + 26f, currentY, textPaint)
        currentY += lineH

        // 2. Твёрдые или мягкие
        drawCheckbox(canvas, col2X, currentY - 12f, 16f, boxPaint, boxBorder)
        canvas.drawText("твёрдые или мягкие — остальные согласные", col2X + 26f, currentY, textPaint)
        currentY += lineH

        // 3. Колокольчик (звонкие)
        canvas.drawText("🔔", col2X + 8f, currentY + 4f, iconPaint)
        canvas.drawText("верхний ряд согласных — звонкие", col2X + 26f, currentY, boldTextPaint)
    }

    private fun drawCheckbox(canvas: Canvas, x: Float, y: Float, size: Float, fill: Paint, border: Paint) {
        val r = RectF(x, y, x + size, y + size)
        canvas.drawRoundRect(r, 4f, 4f, fill)
        canvas.drawRoundRect(r, 4f, 4f, border)
    }

    // ============ СТРУКТУРА СТРАНИЦЫ «ЛЕНТА НА ДОСКУ» ============
    data class BoardPage(
        val title: String,
        val subtitle: String,
        val category: String,
        val rows: List<List<RibbonLetter>>,
        val showBell: Boolean,
        val showHeadphones: Boolean,
        val rightRows: List<List<RibbonLetter>>? = null,
        val rightTitle: String? = null
    )

    // ============ 4 страницы BOARD ============
    private fun getBoardPages(): List<BoardPage> {
        val page1 = BoardPage(
            title = "Гласные",
            subtitle = "вверху — после твёрдых, внизу — после мягких",
            category = "гласные",
            rows = listOf(RibbonData.topVowels, RibbonData.bottomVowels),
            showBell = false,
            showHeadphones = false
        )
        val sonorants = RibbonData.topConsonants.subList(0, 5)  // Л М Н Р Й
        val page2 = BoardPage(
            title = "Звонкие непарные",
            subtitle = "нет пары среди глухих",
            category = "согласные",
            rows = listOf(sonorants),
            showBell = true,
            showHeadphones = false
        )
        val pairedVoiced = RibbonData.topConsonants.subList(5, 11)  // Б В Г Д Ж З
        val pairedVoiceless = RibbonData.bottomConsonants.subList(0, 6)  // П Ф К Т Ш С
        val page3 = BoardPage(
            title = "Звонкие и глухие пары",
            subtitle = "звонкие вверху, глухие под ними",
            category = "согласные",
            rows = listOf(pairedVoiced, pairedVoiceless),
            showBell = true,
            showHeadphones = true
        )
        val unpairedVoiceless = RibbonData.bottomConsonants.subList(6, 10)  // Х Ц Ч Щ
        val signsVertical = RibbonData.signs.map { listOf(it) }
        val page4 = BoardPage(
            title = "Глухие непарные, Ь и Ъ",
            subtitle = "Х Ц Ч Щ — нет звонкой пары, Ь и Ъ звуков не обозначают",
            category = "согласные",
            rows = listOf(unpairedVoiceless),
            showBell = false,
            showHeadphones = true,
            rightRows = signsVertical,
            rightTitle = "знаки"
        )
        return listOf(page1, page2, page3, page4)
    }

    // ============ ЛЕНТА НА ДОСКУ — ОДНА СТРАНИЦА ============
    fun drawBoardPage(
        canvas: Canvas, config: RibbonConfig,
        page: BoardPage, pageNum: Int, totalPages: Int, context: Context
    ) {
        val bg = if (config.bgColor == RibbonBgColor.PEACH) PEACH_COLOR else Color.WHITE
        canvas.drawColor(bg)

        val font = if (config.font == RibbonFont.CURSIVE) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 40f; color = Color.DKGRAY
            typeface = font; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(page.title, PAGE_WIDTH / 2f, 55f, titlePaint)

        val subPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.GRAY
            typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(page.subtitle, PAGE_WIDTH / 2f, 78f, subPaint)

        val catPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = Color.parseColor("#9E9E9E")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(page.category, PAGE_WIDTH - 40f, 55f, catPaint)

        val rows = page.rows
        val rightRows = page.rightRows

        val reservedRight = if (rightRows != null) 3 * 110f + 60f else 0f
        val maxColsLeft = rows.maxOf { it.size }
        val rowCount = rows.size

        val availableWidth = PAGE_WIDTH - 80f - reservedRight
        val maxCellByCols = availableWidth / maxColsLeft - 4f
        val maxCellByRows = (PAGE_HEIGHT - 270f) / rowCount - 4f
        var cellSize = minOf(maxCellByCols, maxCellByRows, 110f)
        if (rightRows != null) {
            val rightRowCount = rightRows.size
            val maxCellByRightRows = (PAGE_HEIGHT - 270f) / rightRowCount - 4f
            cellSize = minOf(cellSize, maxCellByRightRows, 110f)
        }
        val gap = 4f
        val colWidth = cellSize + gap
        val rowHeight = cellSize + gap

        val totalWidth = maxColsLeft * colWidth - gap
        val totalHeight = rowCount * rowHeight - gap
        val baseX = if (rightRows != null) {
            60f
        } else {
            (PAGE_WIDTH - totalWidth) / 2f
        }
        val startY = 120f + (PAGE_HEIGHT - 120f - 100f - totalHeight) / 2f

        val emptyMissing = emptySet<String>()
        rows.forEachIndexed { rowIndex, rowLetters ->
            val rowWidth = rowLetters.size * colWidth - gap
            val rowStartX = baseX + (totalWidth - rowWidth) / 2f
            val rowY = startY + rowIndex * rowHeight

            if (page.showBell && rowIndex == 0) {
                val iconPaint = Paint().apply {
                    isAntiAlias = true; textSize = 24f; textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🔔", rowStartX - 25f, rowY + cellSize / 2f + 8f, iconPaint)
            }
            if (page.showHeadphones && rowIndex == rows.size - 1 &&
