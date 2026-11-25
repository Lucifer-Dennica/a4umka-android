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
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1f }

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
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1f }

        // Метка стоит ВПЛОТНУЮ к линии предыдущего поля (как на сайте)
        val labelGap = 2f
        val rowStep = 25f
        val fieldGap = 8f

        fun drawField(label: String, linesCount: Int = 1) {
            currentY += labelGap
            canvas.drawText(label, MARGIN, currentY, labelPaint)
            for (i in 0 until linesCount) {
                currentY += rowStep
                canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            }
            currentY += fieldGap
        }

        if (config.showAuthor) drawField("Автор")
        if (config.showTitle) drawField("Название")
        if (config.showGenre) drawField("Жанр")
        if (config.showPages) drawField("Сколько страниц")
        if (config.showDates) drawField("Начал(а) / закончил(а)")
        if (config.showCharacters) drawField("Главные герои", 1)
        if (config.showPlot) drawField("О чём эта книга", 4)
        if (config.showMemorable) drawField("Что запомнилось больше всего", 2)
        if (config.showLesson) drawField("Чему научила эта книга", 1)
        if (config.showNewWords) drawField("Новые слова", 1)

        if (config.showDrawing) {
            val drawingHeight = 110f
            currentY += labelGap
            canvas.drawText("Рисунок к книге", MARGIN, currentY, labelPaint)
            currentY += 6f
            val framePaint = Paint().apply {
                color = colors.lineColor; style = Paint.Style.STROKE; strokeWidth = 1.5f
                pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
            }
            canvas.drawRoundRect(RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + drawingHeight), 10f, 10f, framePaint)
            currentY += drawingHeight + fieldGap
        }

        if (config.showRating) {
            currentY += labelGap
            canvas.drawText("Моя оценка", MARGIN, currentY, labelPaint)
            currentY += 6f
            val starPaint = Paint().apply {
                isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 1.5f; color = colors.lineColor
            }
            val starR = 13f
            val step = 38f
            var cx = MARGIN + starR
            for (s in 0 until 5) {
                drawStarOutline(canvas, cx, currentY + starR + 2f, starR, starPaint)
                cx += step
            }
            val hintPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = colors.subtitleColor }
            canvas.drawText("раскрась звёзды — от 1 до 5", MARGIN, currentY + starR * 2 + 16f, hintPaint)
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

    private fun drawPictureBookPage(canvas: Canvas, config: DiaryConfig, colors: PageColors, titleTF: Typeface) {
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = colors.textColor; typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Моя книга", PAGE_WIDTH / 2f, 50f, titlePaint)
        val framePaint = Paint().apply { color = colors.lineColor; style = Paint.Style.STROKE; strokeWidth = 2f }
        canvas.drawRect(RectF(MARGIN, 80f, PAGE_WIDTH - MARGIN, 500f), framePaint)
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1.5f }
        canvas.drawLine(MARGIN, 560f, PAGE_WIDTH - MARGIN, 560f, linePaint)
        canvas.drawLine(MARGIN, 620f, PAGE_WIDTH - MARGIN, 620f, linePaint)
        val ratingPaint = Paint().apply { isAntiAlias = true; textSize = 36f; color = colors.textColor }
        canvas.drawText("😞   😐   😊", MARGIN, 750f, ratingPaint)
        val labelPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = colors.subtitleColor }
        canvas.drawText("Обведи нужное", MARGIN, 780f, labelPaint)
    }

    private fun drawTable(
        canvas: Canvas, title: String, subtitle: String,
        headers: List<String>, colWidths: List<Float>, rows: Int,
        colors: PageColors, titleTF: Typeface, withNumbers: Boolean
    ) {
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

        val headerBgPaint = Paint().apply { color = colors.headerBg; style = Paint.Style.FILL }
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1f }
        val headerTextPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = colors.textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val cellTextPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = colors.textColor; textAlign = Paint.Align.CENTER }

        // Шапка залита цветом
        canvas.drawRect(RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableTop + headerHeight), headerBgPaint)

        // Заголовки колонок с переносом
        for (i in headers.indices) {
            val lines = headers[i].split("\n")
            val cx = xs[i] + widths[i] / 2f
            val totalTextHeight = lines.size * 12f
            var ty = tableTop + headerHeight / 2f - totalTextHeight / 2f + 9f
            for (l in lines) {
                canvas.drawText(l, cx, ty, headerTextPaint)
                ty += 12f
            }
        }

        // Горизонтальные линии
        for (r in 0..rows) {
            val y = tableTop + headerHeight + r * rowHeight
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        }

        // Вертикальные линии
        for (x in xs) {
            canvas.drawLine(x, tableTop, x, tableBottom, linePaint)
        }

        // Внешняя рамка
        canvas.drawRect(RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableBottom), linePaint)

        // Номера строк
        if (withNumbers) {
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
            rows = config.listRowsCount,
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
            rows = 12,
            colors = colors,
            titleTF = titleTF,
            withNumbers = false
        )
    }
}
