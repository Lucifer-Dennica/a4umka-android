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

    private var cachedCaveat: Typeface? = null

    private fun titleTypeface(context: Context, config: DiaryConfig): Typeface {
        return when (config.titleFont) {
            DiaryFont.SYSTEM -> Typeface.DEFAULT_BOLD
            DiaryFont.CAVEAT -> {
                if (cachedCaveat == null) {
                    cachedCaveat = try {
                        ResourcesCompat.getFont(context, R.font.caveat)
                    } catch (e: Exception) {
                        null
                    }
                }
                cachedCaveat ?: Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
            }
        }
    }

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
        val margin = 40f
        val colors = getColors(config.color)
        val titleTF = titleTypeface(context, config)

        if (config.includeTitlePage) {
            renderers.add { canvas -> drawTitlePage(canvas, config, margin, colors, titleTF) }
        }
        for (i in 1..config.bookPagesCount) {
            if (config.pageType == DiaryPageType.SCHOOL) {
                renderers.add { canvas -> drawSchoolBookPage(canvas, config, margin, i, colors, titleTF) }
            } else {
                renderers.add { canvas -> drawPictureBookPage(canvas, config, margin, i, colors, titleTF) }
            }
        }
        if (config.includeBookList) {
            renderers.add { canvas -> drawListPage(canvas, config, margin, colors, titleTF) }
        }
        if (config.includeSpeedTest) {
            renderers.add { canvas -> drawSpeedTestPage(canvas, config, margin, colors, titleTF) }
        }
        return renderers
    }

    private data class PageColors(
        val textColor: Int,
        val lineColor: Int,
        val headerBg: Int,
        val subtitleColor: Int
    )

    private fun getColors(color: DiaryColor): PageColors {
        return when (color) {
            DiaryColor.GREEN -> PageColors(
                textColor = Color.parseColor("#1B5E20"),
                lineColor = Color.parseColor("#8B7355"),
                headerBg = Color.parseColor("#C8E6C9"),
                subtitleColor = Color.parseColor("#8A8A8A")
            )
            DiaryColor.ORANGE -> PageColors(
                textColor = Color.parseColor("#E65100"),
                lineColor = Color.parseColor("#8B7355"),
                headerBg = Color.parseColor("#FFE0B2"),
                subtitleColor = Color.parseColor("#8A8A8A")
            )
            DiaryColor.BLUE -> PageColors(
                textColor = Color.parseColor("#0D47A1"),
                lineColor = Color.parseColor("#8B7355"),
                headerBg = Color.parseColor("#BBDEFB"),
                subtitleColor = Color.parseColor("#8A8A8A")
            )
            DiaryColor.PINK -> PageColors(
                textColor = Color.parseColor("#880E4F"),
                lineColor = Color.parseColor("#8B7355"),
                headerBg = Color.parseColor("#F8BBD0"),
                subtitleColor = Color.parseColor("#8A8A8A")
            )
            DiaryColor.BLACK_WHITE -> PageColors(
                textColor = Color.BLACK,
                lineColor = Color.DKGRAY,
                headerBg = Color.LTGRAY,
                subtitleColor = Color.GRAY
            )
        }
    }

    private fun getBgColor(color: DiaryColor): Int {
        return when (color) {
            DiaryColor.PINK -> Color.parseColor("#FFF5F7")
            DiaryColor.GREEN -> Color.parseColor("#F9FBF9")
            DiaryColor.BLUE -> Color.parseColor("#F5F9FF")
            DiaryColor.ORANGE -> Color.parseColor("#FFF9F0")
            DiaryColor.BLACK_WHITE -> Color.WHITE
        }
    }

    private fun drawTitlePage(canvas: Canvas, config: DiaryConfig, margin: Float, colors: PageColors, titleTF: Typeface) {
        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 36f; color = colors.textColor; typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Читательский дневник", PAGE_WIDTH / 2f, 80f, titlePaint)

        // Подзаголовок
        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = colors.subtitleColor; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("мои книги за этот год", PAGE_WIDTH / 2f, 105f, subtitlePaint)

        // Прямоугольник для рисунка (скруглённые углы, пунктир)
        val rectTop = 200f
        val rectBottom = PAGE_HEIGHT - 200f
        val framePaint = Paint().apply {
            color = colors.lineColor; style = Paint.Style.STROKE; strokeWidth = 1.5f
            pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        val rect = RectF(margin, rectTop, PAGE_WIDTH - margin, rectBottom)
        canvas.drawRoundRect(rect, 15f, 15f, framePaint)

        // Подпись под прямоугольником
        val hintPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = colors.subtitleColor; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("нарисуй обложку любимой книги", PAGE_WIDTH / 2f, rectBottom + 25f, hintPaint)

        // Поля "Кто читал", "Класс", "Учебный год"
        val labelPaint = Paint().apply {
            isAntiAlias = true; textSize = 16f; color = colors.textColor; typeface = titleTF
        }
        val valuePaint = Paint().apply {
            isAntiAlias = true; textSize = 18f; color = colors.textColor; typeface = titleTF
        }
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1f }

        var currentY = rectBottom + 70f
        val labelX = margin
        val valueX = margin + 130f
        val lineEndX = PAGE_WIDTH - margin

        // Кто читал
        canvas.drawText("Кто читал", labelX, currentY, labelPaint)
        if (config.childName.isNotEmpty()) {
            canvas.drawText(config.childName, valueX, currentY, valuePaint)
        }
        canvas.drawLine(valueX, currentY + 5f, lineEndX, currentY + 5f, linePaint)
        currentY += 50f

        // Класс
        canvas.drawText("Класс", labelX, currentY, labelPaint)
        if (config.childGrade.isNotEmpty()) {
            canvas.drawText(config.childGrade, valueX, currentY, valuePaint)
        }
        canvas.drawLine(valueX, currentY + 5f, lineEndX, currentY + 5f, linePaint)
        currentY += 50f

        // Учебный год
        canvas.drawText("Учебный год", labelX, currentY, labelPaint)
        if (config.academicYear.isNotEmpty()) {
            canvas.drawText(config.academicYear, valueX, currentY, valuePaint)
        }
        canvas.drawLine(valueX, currentY + 5f, lineEndX, currentY + 5f, linePaint)
    }

    private fun drawSchoolBookPage(canvas: Canvas, config: DiaryConfig, margin: Float, pageNum: Int, colors: PageColors, titleTF: Typeface) {
        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = colors.textColor; typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Прочитанная книга", PAGE_WIDTH / 2f, 50f, titlePaint)

        // Подзаголовок
        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = colors.subtitleColor; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("страница $pageNum", PAGE_WIDTH / 2f, 75f, subtitlePaint)

        var currentY = 110f
        val labelPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = colors.textColor; typeface = titleTF
        }
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1f }

        // Функция рисования поля: название + одна линия под ним
        fun drawField(label: String, linesCount: Int = 1) {
            val fieldHeight = linesCount * 30f
            if (currentY + fieldHeight > PAGE_HEIGHT - 60f) return
            canvas.drawText(label, margin, currentY, labelPaint)
            currentY += 20f
            // Рисуем линии
            for (i in 0 until linesCount) {
                canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
                currentY += 30f
            }
            currentY += 10f
        }

        if (config.showAuthor) drawField("Автор")
        if (config.showTitle) drawField("Название")
        if (config.showGenre) drawField("Жанр")
        if (config.showPages) drawField("Сколько страниц")
        if (config.showDates) drawField("Начал(а) / закончил(а)")
        if (config.showCharacters) drawField("Главные герои", 2)
        if (config.showPlot) drawField("О чём эта книга", 3)
        if (config.showMemorable) drawField("Что запомнилось больше всего", 3)
        if (config.showLesson) drawField("Чему научила эта книга", 2)
        if (config.showNewWords) drawField("Новые слова", 1)

        // Рисунок к книге (пунктирный прямоугольник со скруглёнными углами)
        if (config.showDrawing) {
            val drawingHeight = 180f
            if (currentY + drawingHeight > PAGE_HEIGHT - 60f) return
            canvas.drawText("Рисунок к книге", margin, currentY, labelPaint)
            currentY += 15f
            val framePaint = Paint().apply {
                color = colors.lineColor; style = Paint.Style.STROKE; strokeWidth = 1.5f
                pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
            }
            val rect = RectF(margin, currentY, PAGE_WIDTH - margin, currentY + drawingHeight)
            canvas.drawRoundRect(rect, 10f, 10f, framePaint)
            currentY += drawingHeight + 20f
        }

        // Моя оценка — 5 контурных звёзд
        if (config.showRating) {
            if (currentY > PAGE_HEIGHT - 60f) return
            canvas.drawText("Моя оценка", margin, currentY, labelPaint)
            currentY += 25f
            val starPaint = Paint().apply {
                isAntiAlias = true; textSize = 28f; color = colors.lineColor
            }
            val starText = "☆  ☆  ☆  ☆  ☆"
            canvas.drawText(starText, margin, currentY, starPaint)
        }
    }

    private fun drawPictureBookPage(canvas: Canvas, config: DiaryConfig, margin: Float, pageNum: Int, colors: PageColors, titleTF: Typeface) {
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = colors.textColor; typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Моя книга", PAGE_WIDTH / 2f, 50f, titlePaint)

        val framePaint = Paint().apply {
            color = colors.lineColor; style = Paint.Style.STROKE; strokeWidth = 2f
        }
        val rect = RectF(margin, 80f, PAGE_WIDTH - margin, 500f)
        canvas.drawRect(rect, framePaint)

        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1.5f }
        canvas.drawLine(margin, 560f, PAGE_WIDTH - margin, 560f, linePaint)
        canvas.drawLine(margin, 620f, PAGE_WIDTH - margin, 620f, linePaint)

        val ratingPaint = Paint().apply { isAntiAlias = true; textSize = 36f; color = colors.textColor }
        canvas.drawText("😞   😐   😊", margin, 750f, ratingPaint)
        val labelPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = colors.subtitleColor }
        canvas.drawText("Обведи нужное", margin, 780f, labelPaint)
    }

    private fun drawListPage(canvas: Canvas, config: DiaryConfig, margin: Float, colors: PageColors, titleTF: Typeface) {
        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = colors.textColor; typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Список прочитанного", PAGE_WIDTH / 2f, 50f, titlePaint)

        // Подзаголовок
        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = colors.subtitleColor; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("мои книги за этот год", PAGE_WIDTH / 2f, 75f, subtitlePaint)

        // Таблица
        val headerPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = colors.textColor; typeface = Typeface.DEFAULT_BOLD
        }
        val cellPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = colors.textColor; typeface = Typeface.DEFAULT
        }
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1f }
        val headerBgPaint = Paint().apply { color = colors.headerBg; style = Paint.Style.FILL }

        var currentY = 100f
        val colWidths = floatArrayOf(30f, 80f, 200f, 50f, 60f, 100f)
        val colX = floatArrayOf(margin)
        for (i in 1 until colWidths.size) {
            colX.add(colX[i - 1] + colWidths[i - 1])
        }

        // Шапка таблицы (залита цветом)
        val headerHeight = 35f
        canvas.drawRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + headerHeight), headerBgPaint)

        val headers = listOf("№", "Дата", "Автор и название", "Стр.", "Оценка", "Подпись взрослого")
        for (i in headers.indices) {
            canvas.drawText(headers[i], colX[i] + 5f, currentY + 22f, headerPaint)
        }
        currentY += headerHeight

        // Горизонтальная линия под шапкой
        canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)

        // Строки таблицы
        val rowHeight = 35f
        for (row in 0 until config.listRowsCount) {
            // Вертикальные линии
            for (x in colX) {
                canvas.drawLine(x, currentY, x, currentY + rowHeight, linePaint)
            }
            canvas.drawLine(PAGE_WIDTH - margin, currentY, PAGE_WIDTH - margin, currentY + rowHeight, linePaint)

            // Номер строки
            canvas.drawText("${row + 1}", colX[0] + 10f, currentY + 22f, cellPaint)

            currentY += rowHeight
        }

        // Нижняя линия
        canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
        for (x in colX) {
            canvas.drawLine(x, currentY - rowHeight * config.listRowsCount, x, currentY, linePaint)
        }
        canvas.drawLine(PAGE_WIDTH - margin, currentY - rowHeight * config.listRowsCount, PAGE_WIDTH - margin, currentY, linePaint)
    }

    private fun drawSpeedTestPage(canvas: Canvas, config: DiaryConfig, margin: Float, colors: PageColors, titleTF: Typeface) {
        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = colors.textColor; typeface = titleTF; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Техника чтения", PAGE_WIDTH / 2f, 50f, titlePaint)

        // Подзаголовок
        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = colors.subtitleColor; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("сколько слов в минуту получается сегодня", PAGE_WIDTH / 2f, 75f, subtitlePaint)

        // Таблица
        val headerPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = colors.textColor; typeface = Typeface.DEFAULT_BOLD
        }
        val cellPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = colors.textColor; typeface = Typeface.DEFAULT
        }
        val linePaint = Paint().apply { color = colors.lineColor; strokeWidth = 1f }
        val headerBgPaint = Paint().apply { color = colors.headerBg; style = Paint.Style.FILL }

        var currentY = 100f
        val colWidths = floatArrayOf(80f, 250f, 100f, 100f)
        val colX = floatArrayOf(margin)
        for (i in 1 until colWidths.size) {
            colX.add(colX[i - 1] + colWidths[i - 1])
        }

        // Шапка таблицы
        val headerHeight = 35f
        canvas.drawRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + headerHeight), headerBgPaint)

        val headers = listOf("Дата", "Что читал(а)", "слов в минуту", "Отметка взрослого")
        for (i in headers.indices) {
            canvas.drawText(headers[i], colX[i] + 5f, currentY + 22f, headerPaint)
        }
        currentY += headerHeight

        // Горизонтальная линия под шапкой
        canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)

        // Строки таблицы (12 строк)
        val rowHeight = 45f
        for (row in 0 until 12) {
            // Вертикальные линии
            for (x in colX) {
                canvas.drawLine(x, currentY, x, currentY + rowHeight, linePaint)
            }
            canvas.drawLine(PAGE_WIDTH - margin, currentY, PAGE_WIDTH - margin, currentY + rowHeight, linePaint)

            currentY += rowHeight
        }

        // Нижняя линия
        canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
        for (x in colX) {
            canvas.drawLine(x, currentY - rowHeight * 12, x, currentY, linePaint)
        }
        canvas.drawLine(PAGE_WIDTH - margin, currentY - rowHeight * 12, PAGE_WIDTH - margin, currentY, linePaint)
    }
}
