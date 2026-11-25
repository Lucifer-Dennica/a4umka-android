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
import com.example.sovenok.data.DiaryColor
import com.example.sovenok.data.DiaryConfig
import com.example.sovenok.data.DiaryFont
import com.example.sovenok.data.DiaryPageType
import com.example.sovenok.data.DiaryRuling

object DiaryPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f
    private const val MARGIN = 40f
    private const val BOTTOM_LIMIT = PAGE_HEIGHT - 40f

    private var cachedCaveat: Typeface? = null

    private fun caveatTypeface(context: Context): Typeface {
        if (cachedCaveat == null) {
            cachedCaveat = try {
                ResourcesCompat.getFont(context, R.font.caveat)
            } catch (e: Exception) { null }
        }
        return cachedCaveat ?: Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
    }

    private fun fontFor(context: Context, font: DiaryFont): Typeface =
        if (font == DiaryFont.CAVEAT) caveatTypeface(context) else Typeface.DEFAULT_BOLD

    fun generatePreview(config: DiaryConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val renderers = getPageRenderers(config, context)
        for (renderer in renderers) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(getBgColor(config.color))
            renderer(canvas)
            pages.add(bmp)
        }
        return if (pages.isEmpty()) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            pages.add(bmp)
            pages
        } else pages
    }

    fun drawDocument(document: PdfDocument, config: DiaryConfig, context: Context) {
        val renderers = getPageRenderers(config, context)
        var pageNum = 1
        for (renderer in renderers) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(getBgColor(config.color))
            renderer(page.canvas)
            document.finishPage(page)
            pageNum++
        }
    }

    private fun getPageRenderers(config: DiaryConfig, context: Context): List<(Canvas) -> Unit> {
        val renderers = mutableListOf<(Canvas) -> Unit>()
        val colors = getColors(config.color)
        val titleTF = fontFor(context, config.titleFont)
        val labelTF = fontFor(context, config.labelFont)
        val valueTF = fontFor(context, config.valueFont)

        if (config.includeTitlePage) {
            renderers.add { canvas -> drawTitlePage(canvas, config, colors, titleTF, labelTF, valueTF) }
        }
        for (i in 1..config.bookPagesCount) {
            if (config.pageType == DiaryPageType.SCHOOL) {
                renderers.add { canvas -> drawSchoolBookPage(canvas, config, i, colors, titleTF, labelTF) }
            } else {
                renderers.add { canvas -> drawPictureBookPage(canvas, config, colors, titleTF) }
            }
        }
        if (config.includeBookList) {
            renderers.add { canvas -> drawListPage(canvas, config, colors, titleTF) }
        }
        if (config.includeSpeedTest) {
            renderers.add { canvas -> drawSpeedTestPage(canvas, config, colors, titleTF) }
        }
        return renderers
    }

    private data class PageColors(
        val textColor: Int,
        val lineColor: Int,
        val headerBg: Int,
        val subtitleColor: Int
    )

    private fun getColors(color: DiaryColor): PageColors = when (color) {
        DiaryColor.GREEN -> PageColors(
            Color.parseColor("#1B5E20"), Color.parseColor("#8B7355"),
            Color.parseColor("#C8E6C9"), Color.parseColor("#8A8A8A"))
        DiaryColor.ORANGE -> PageColors(
            Color.parseColor("#E65100"), Color.parseColor("#8B7355"),
            Color.parseColor("#FFE0B2"), Color.parseColor("#8A8A8A"))
        DiaryColor.BLUE -> PageColors(
            Color.parseColor("#0D47A1"), Color.parseColor("#5C7A99"),
            Color.parseColor("#B3CDE0"), Color.parseColor("#8A8A8A"))
        DiaryColor.PINK -> PageColors(
            Color.parseColor("#880E4F"), Color.parseColor("#8B7355"),
            Color.parseColor("#F8BBD0"), Color.parseColor("#8A8A8A"))
        DiaryColor.BLACK_WHITE -> PageColors(
            Color.BLACK, Color.DKGRAY, Color.LTGRAY, Color.GRAY)
    }

    private fun getBgColor(color: DiaryColor): Int = when (color) {
        DiaryColor.PINK -> Color.parseColor("#FFF5F7")
        DiaryColor.GREEN -> Color.parseColor("#F9FBF9")
        DiaryColor.BLUE -> Color.parseColor("#F5F8FB")
        DiaryColor.ORANGE -> Color.parseColor("#FFF9F0")
        DiaryColor.BLACK_WHITE -> Color.WHITE
    }

    // ============ ТИТУЛЬНЫЙ ЛИСТ ============
    private fun drawTitlePage(
        canvas: Canvas, config: DiaryConfig, colors: PageColors,
        titleTF: Typeface, labelTF: Typeface, valueTF: Typeface
    ) {
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 36f; color = colors.textColor
            typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Читательский дневник", PAGE_WIDTH / 2f, 80f, titlePaint)

        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = colors.subtitleColor
            typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("мои книги за этот год", PAGE_WIDTH / 2f, 105f, subtitlePaint)

        val rectTop = 200f
        val rectBottom = 620f
        val framePaint = Paint().apply {
            color = colors.lineColor; style = Paint.Style.STROKE; strokeWidth = 1.5f
            pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        canvas.drawRoundRect(RectF(MARGIN, rectTop, PAGE_WIDTH - MARGIN, rectBottom), 15f, 15f, framePaint)

        val hintPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = colors.subtitleColor; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("нарисуй обложку любимой книги", PAGE_WIDTH / 2f, rectBottom + 25f, hintPaint)

        val labelPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = colors.textColor; typeface = labelTF }
        val valuePaint = Paint().apply { isAntiAlias = true; textSize = 18f; color = colors.textColor; typeface = valueTF }
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1f; style = Paint.Style.STROKE }

        var currentY = rectBottom + 75f
        val valueX = MARGIN + 130f
        fun field(label: String, value: String) {
            canvas.drawText(label, MARGIN, currentY, labelPaint)
            if (value.isNotEmpty()) canvas.drawText(value, valueX, currentY, valuePaint)
            canvas.drawLine(valueX, currentY + 6f, PAGE_WIDTH - MARGIN, currentY + 6f, linePaint)
            currentY += 50f
        }
        field("Кто читал", config.childName)
        field("Класс", config.childGrade)
        field("Учебный год", config.academicYear)
    }

    // ============ СТРАНИЦА КНИГИ ============
    private fun drawSchoolBookPage(
        canvas: Canvas, config: DiaryConfig, pageNum: Int,
        colors: PageColors, titleTF: Typeface, labelTF: Typeface
    ) {
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = colors.textColor
            typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Прочитанная книга", PAGE_WIDTH / 2f, 50f, titlePaint)

        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = colors.subtitleColor
            typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("страница $pageNum", PAGE_WIDTH / 2f, 72f, subtitlePaint)

        var currentY = 100f
        val labelPaint = Paint().apply { isAntiAlias = true; textSize = 13f; color = colors.textColor; typeface = labelTF }
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1f; style = Paint.Style.STROKE }

        val labelGap = 8f
        val baseStep = 25f   // базовый шаг = обычная линейка
        val fieldGap = 8f
        val gridStep = 25f   // размер клетки

        // Разлиновка внутри поля: высота поля НЕ меняется, меняются только линии
        fun drawField(label: String, linesCount: Int = 1) {
            currentY += labelGap
            canvas.drawText(label, MARGIN, currentY, labelPaint)
            val fieldTop = currentY
            val fieldHeight = linesCount * baseStep

            when (config.ruling) {
                DiaryRuling.NONE -> {
                    // пустое место — только метка
                }
                DiaryRuling.GRID -> {
                    // горизонтальные строки + вертикальные = клетка
                    var y = fieldTop + baseStep
                    while (y <= fieldTop + fieldHeight + 0.5f) {
                        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
                        y += baseStep
                    }
                    var x = MARGIN + gridStep
                    while (x < PAGE_WIDTH - MARGIN) {
                        canvas.drawLine(x, fieldTop, x, fieldTop + fieldHeight, linePaint)
                        x += gridStep
                    }
                }
                else -> {
                    val step = if (config.ruling == DiaryRuling.WIDE) 40f else baseStep
                    val n = (fieldHeight / step).toInt().coerceAtLeast(1)
                    for (i in 1..n) {
                        val y = fieldTop + fieldHeight * i / n
                        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
                    }
                }
            }
            currentY = fieldTop + fieldHeight + fieldGap
        }

        if (config.showAuthor) drawField("Автор")
        if (config.showTitle) drawField("Название")
        if (config.showGenre) drawField("Жанр")
        if (config.showPages) drawField("Сколько страниц")
        if (config.showDates) drawField("Начал(а) / закончил(а)")
        if (config.showCharacters) drawField("Главные герои", 1)
        if (config.showPlot) drawField("О чём эта книга", 4)
        if (config.showMemorable) drawField("Что запомнилось больше всего", 3)
        if (config.showLesson) drawField("Чему научила эта книга", 2)
        if (config.showNewWords) drawField("Новые слова", 1)

        // ===== Динамическая нижняя часть =====
        val ratingReserve = if (config.showRating) 64f else 0f
        val usableBottom = PAGE_HEIGHT - 40f - ratingReserve

        val boxTop = currentY + labelGap + 6f
        val drawingFits = config.showDrawing && boxTop + 55f <= usableBottom

        if (drawingFits) {
            canvas.drawText("Рисунок к книге", MARGIN, currentY + labelGap, labelPaint)
            val framePaint = Paint().apply {
                color = colors.lineColor; style = Paint.Style.STROKE; strokeWidth = 1.5f
                pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
            }
            canvas.drawRoundRect(RectF(MARGIN, boxTop, PAGE_WIDTH - MARGIN, usableBottom), 10f, 10f, framePaint)
        } else {
            // Свободное место заполняем в зависимости от разлиновки
            val areaTop = currentY + 10f
            val areaBottom = usableBottom
            when (config.ruling) {
                DiaryRuling.NONE -> {
                    // без линий — пустое пространство
                }
                DiaryRuling.GRID -> {
                    var y = areaTop + baseStep
                    while (y <= areaBottom) {
                        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
                        y += baseStep
                    }
                    var x = MARGIN + gridStep
                    while (x < PAGE_WIDTH - MARGIN) {
                        canvas.drawLine(x, areaTop, x, areaBottom, linePaint)
                        x += gridStep
                    }
                }
                else -> {
                    val step = if (config.ruling == DiaryRuling.WIDE) 40f else baseStep
                    var y = areaTop
                    while (y + step <= areaBottom) {
                        y += step
                        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
                    }
                }
            }
        }

        // ===== Моя оценка — всегда у самого низа =====
        if (config.showRating) {
            val ratingLabelY = PAGE_HEIGHT - 40f - 44f
            canvas.drawText("Моя оценка", MARGIN, ratingLabelY, labelPaint)
            val starPaint = Paint().apply {
                isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 1.5f; color = colors.lineColor
            }
            val starR = 13f
            val step = 38f
            var cx = MARGIN + starR
            val starCy = ratingLabelY + 8f + starR
            for (s in 0 until 5) {
                drawStarOutline(canvas, cx, starCy, starR, starPaint)
                cx += step
            }
            val hintPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = colors.subtitleColor }
            canvas.drawText("раскрась звёзды — от 1 до 5", MARGIN, starCy + starR + 12f, hintPaint)
        }
    }

    private fun drawStarOutline(canvas: Canvas, cx: Float, cy: Float, outerR: Float, paint: Paint) {
        val path = Path()
        val innerR = outerR * 0.45f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = Math.PI / 2 + i * Math.PI / 5
            val x = cx + (r * Math.cos(angle)).toFloat()
            val y = cy - (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    // ============ СТРАНИЦА В КАРТИНКАХ ============
    private fun drawPictureBookPage(canvas: Canvas, config: DiaryConfig, colors: PageColors, titleTF: Typeface) {
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = colors.textColor; typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Моя книга", PAGE_WIDTH / 2f, 50f, titlePaint)
        val framePaint = Paint().apply { color = colors.lineColor; style = Paint.Style.STROKE; strokeWidth = 2f }
        canvas.drawRect(RectF(MARGIN, 80f, PAGE_WIDTH - MARGIN, 500f), framePaint)
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1.5f; style = Paint.Style.STROKE }
        canvas.drawLine(MARGIN, 560f, PAGE_WIDTH - MARGIN, 560f, linePaint)
        canvas.drawLine(MARGIN, 620f, PAGE_WIDTH - MARGIN, 620f, linePaint)
        val ratingPaint = Paint().apply { isAntiAlias = true; textSize = 36f; color = colors.textColor }
        canvas.drawText("😞   😐   😊", MARGIN, 750f, ratingPaint)
        val labelPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = colors.subtitleColor }
        canvas.drawText("Обведи нужное", MARGIN, 780f, labelPaint)
    }

    // ============ ТАБЛИЦА: заливается ТОЛЬКО шапка ============
    private fun drawTable(
        canvas: Canvas, title: String, subtitle: String,
        headers: List<String>, colWidths: List<Float>, rowsIn: Int,
        colors: PageColors, titleTF: Typeface, withNumbers: Boolean
    ) {
        val rows = rowsIn.coerceAtLeast(1)

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = colors.textColor; typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, PAGE_WIDTH / 2f, 50f, titlePaint)
        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = colors.subtitleColor; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle, PAGE_WIDTH / 2f, 72f, subtitlePaint)

        val tableTop = 100f
        val headerHeight = 34f
        val tableBottom = BOTTOM_LIMIT
        val rowHeight = (tableBottom - tableTop - headerHeight) / rows

        val available = PAGE_WIDTH - MARGIN * 2
        val sum = colWidths.sum()
        val widths = colWidths.map { it / sum * available }
        val xs = mutableListOf(MARGIN)
        for (w in widths) xs.add(xs.last() + w)

        // --- 1. Заливаем ОДНУ полосу шапки (не всю таблицу!) ---
        val headerBgPaint = Paint().apply {
            isAntiAlias = true; color = colors.headerBg; style = Paint.Style.FILL
        }
        canvas.drawRect(RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableTop + headerHeight), headerBgPaint)

        // --- 2. Сетка: все линии рисуем поверх, только линиями (STROKE) ---
        val linePaint = Paint().apply {
            isAntiAlias = true; color = colors.lineColor; strokeWidth = 1f; style = Paint.Style.STROKE
        }
        for (r in 0..rows) {
            val y = tableTop + headerHeight + r * rowHeight
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        }
        canvas.drawLine(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableTop, linePaint)
        for (x in xs) {
            canvas.drawLine(x, tableTop, x, tableBottom, linePaint)
        }
        canvas.drawRect(RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableBottom), linePaint)

        // --- 3. Текст шапки (с переносом длинных заголовков) ---
        val headerTextPaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = colors.textColor
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        for (i in headers.indices) {
            val lines = headers[i].split("\n")
            val cx = xs[i] + widths[i] / 2f
            val lineHeight = 12f
            var ty = tableTop + headerHeight / 2f - (lines.size - 1) * lineHeight / 2f + 4f
            for (l in lines) {
                canvas.drawText(l, cx, ty, headerTextPaint)
                ty += lineHeight
            }
        }

        // --- 4. Номера строк ---
        if (withNumbers) {
            val cellTextPaint = Paint().apply {
                isAntiAlias = true; textSize = 12f; color = colors.textColor
                typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
            }
            for (r in 0 until rows) {
                val y = tableTop + headerHeight + r * rowHeight + rowHeight / 2f + 4f
                canvas.drawText("${r + 1}", xs[0] + widths[0] / 2f, y, cellTextPaint)
            }
        }
    }

    private fun drawListPage(canvas: Canvas, config: DiaryConfig, colors: PageColors, titleTF: Typeface) {
        drawTable(
            canvas,
            title = "Список прочитанного",
            subtitle = "мои книги за этот год",
            headers = listOf("№", "Дата", "Автор и название", "Стр.", "Оценка", "Подпись\nвзрослого"),
            colWidths = listOf(28f, 75f, 205f, 45f, 55f, 107f),
            rowsIn = config.listRowsCount,
            colors = colors,
            titleTF = titleTF,
            withNumbers = true
        )
    }

    private fun drawSpeedTestPage(canvas: Canvas, config: DiaryConfig, colors: PageColors, titleTF: Typeface) {
        drawTable(
            canvas,
            title = "Техника чтения",
            subtitle = "сколько слов в минуту получается сегодня",
            headers = listOf("Дата", "Что читал(а)", "слов в минуту", "Отметка\nвзрослого"),
            colWidths = listOf(80f, 230f, 100f, 105f),
            rowsIn = 12,
            colors = colors,
            titleTF = titleTF,
            withNumbers = false
        )
    }
}
