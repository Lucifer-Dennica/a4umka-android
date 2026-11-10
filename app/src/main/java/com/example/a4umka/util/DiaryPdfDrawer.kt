package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.DiaryColor
import com.example.a4umka.data.DiaryConfig
import com.example.a4umka.data.DiaryPageType
import com.example.a4umka.data.DiaryRuling

object DiaryPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    // Генерация превью (список Bitmap)
    fun generatePreview(config: DiaryConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val scale = 0.5f
        val pw = (PAGE_WIDTH * scale).toInt()
        val ph = (PAGE_HEIGHT * scale).toInt()

        val renderers = getPageRenderers(config)
        for (renderer in renderers) {
            val bmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.scale(scale, scale)
            canvas.drawColor(getBgColor(config.color))
            renderer(canvas)
            pages.add(bmp)
        }
        return if (pages.isEmpty()) {
            val bmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
            pages.add(bmp)
            pages
        } else pages
    }

    // Генерация PDF
    fun drawDocument(document: PdfDocument, config: DiaryConfig, context: Context) {
        val renderers = getPageRenderers(config)
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

    // Список функций отрисовки для каждой страницы
    private fun getPageRenderers(config: DiaryConfig): List<(Canvas) -> Unit> {
        val renderers = mutableListOf<(Canvas) -> Unit>()
        val margin = 40f
        val isBW = config.color == DiaryColor.BLACK_WHITE
        val textColor = if (isBW) Color.BLACK else Color.parseColor("#1B5E20")
        val lineColor = if (isBW) Color.LTGRAY else Color.parseColor("#81C784")

        if (config.includeTitlePage) {
            renderers.add { canvas -> drawTitlePage(canvas, config, margin, textColor, lineColor) }
        }
        for (i in 1..config.bookPagesCount) {
            if (config.pageType == DiaryPageType.SCHOOL) {
                renderers.add { canvas -> drawSchoolBookPage(canvas, config, margin, i, textColor, lineColor) }
            } else {
                renderers.add { canvas -> drawPictureBookPage(canvas, config, margin, i, textColor, lineColor) }
            }
        }
        if (config.includeBookList) {
            renderers.add { canvas -> drawListPage(canvas, config, margin, textColor, lineColor) }
        }
        if (config.includeSpeedTest) {
            renderers.add { canvas -> drawSpeedTestPage(canvas, config, margin, textColor, lineColor) }
        }
        return renderers
    }

    private fun getBgColor(color: DiaryColor): Int {
        return when (color) {
            DiaryColor.PINK -> Color.parseColor("#FCE4EC")
            DiaryColor.GREEN -> Color.parseColor("#E8F5E9")
            DiaryColor.BLUE -> Color.parseColor("#E3F2FD")
            DiaryColor.ORANGE -> Color.parseColor("#FFF3E0")
            DiaryColor.BLACK_WHITE -> Color.WHITE
        }
    }

    private fun drawTitlePage(canvas: Canvas, config: DiaryConfig, margin: Float, textColor: Int, lineColor: Int) {
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 36f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Читательский дневник", PAGE_WIDTH / 2f, 100f, titlePaint)

        val fieldPaint = Paint().apply { isAntiAlias = true; textSize = 18f; color = textColor; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = lineColor; strokeWidth = 1.5f }
        
        var currentY = 200f
        val startX = 150f

        canvas.drawText("Ученика(цы):", startX, currentY, fieldPaint)
        canvas.drawLine(startX + 130f, currentY + 2f, PAGE_WIDTH - margin, currentY + 2f, linePaint)
        if (config.childName.isNotEmpty()) canvas.drawText(config.childName, startX + 140f, currentY, fieldPaint)
        currentY += 50f

        canvas.drawText("Класса:", startX, currentY, fieldPaint)
        canvas.drawLine(startX + 80f, currentY + 2f, PAGE_WIDTH - margin, currentY + 2f, linePaint)
        if (config.childGrade.isNotEmpty()) canvas.drawText(config.childGrade, startX + 90f, currentY, fieldPaint)
        currentY += 50f

        canvas.drawText("Учебный год:", startX, currentY, fieldPaint)
        canvas.drawLine(startX + 125f, currentY + 2f, PAGE_WIDTH - margin, currentY + 2f, linePaint)
        if (config.academicYear.isNotEmpty()) canvas.drawText(config.academicYear, startX + 135f, currentY, fieldPaint)
        currentY += 80f

        val framePaint = Paint().apply { color = lineColor; style = Paint.Style.STROKE; strokeWidth = 2f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f) }
        val rect = RectF(margin, currentY, PAGE_WIDTH - margin, PAGE_HEIGHT - 60f)
        canvas.drawRect(rect, framePaint)

        val hintPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("Нарисуй обложку любимой книги", PAGE_WIDTH / 2f, currentY + 30f, hintPaint)
    }

    private fun drawSchoolBookPage(canvas: Canvas, config: DiaryConfig, margin: Float, pageNum: Int, textColor: Int, lineColor: Int) {
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 24f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Прочитанная книга", PAGE_WIDTH / 2f, 50f, titlePaint)
        
        val pageNumPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("страница $pageNum", PAGE_WIDTH / 2f, 70f, pageNumPaint)

        var currentY = 100f
        val labelPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT_BOLD }
        val linePaint = Paint().apply { color = lineColor; strokeWidth = 1f }

        fun drawField(label: String, linesCount: Int = 1) {
            val fieldHeight = linesCount * 25f
            if (currentY + fieldHeight > PAGE_HEIGHT - 60f) return
            
            canvas.drawText(label, margin, currentY, labelPaint)
            currentY += 15f
            
            if (config.ruling == DiaryRuling.NONE) {
                canvas.drawLine(margin, currentY + fieldHeight - 10f, PAGE_WIDTH - margin, currentY + fieldHeight - 10f, linePaint)
            } else {
                var y = currentY
                val step = when(config.ruling) {
                    DiaryRuling.WIDE -> 12f * 2.83f
                    DiaryRuling.STANDARD -> 8f * 2.83f
                    DiaryRuling.GRID -> 5f * 2.83f
                    else -> 8f * 2.83f
                }
                while (y < currentY + fieldHeight) {
                    canvas.drawLine(margin, y, PAGE_WIDTH - margin, y, linePaint)
                    if (config.ruling == DiaryRuling.GRID) {
                        var x = margin
                        while (x < PAGE_WIDTH - margin) {
                            canvas.drawLine(x, y, x, y + step, linePaint)
                            x += step
                        }
                    }
                    y += step
                }
            }
            currentY += fieldHeight + 10f
        }

        if (config.showAuthor) drawField("Автор")
        if (config.showTitle) drawField("Название")
        if (config.showGenre) drawField("Жанр")
        if (config.showPages) drawField("Сколько страниц")
        if (config.showDates) drawField("Начал(а) / закончил(а) читать")
        if (config.showCharacters) drawField("Главные герои", 2)
        if (config.showPlot) drawField("О чём эта книга", 3)
        if (config.showMemorable) drawField("Что запомнилось больше всего", 3)
        if (config.showLesson) drawField("Чему научила эта книга", 2)
        if (config.showNewWords) drawField("Новые слова", 1)
        
        if (config.showDrawing) {
            val drawingHeight = 250f
            if (currentY + drawingHeight > PAGE_HEIGHT - 60f) return
            canvas.drawText("Рисунок к книге", margin, currentY, labelPaint)
            currentY += 15f
            val framePaint = Paint().apply { 
                color = if (config.color == DiaryColor.BLACK_WHITE) Color.BLACK else textColor
                style = Paint.Style.STROKE; strokeWidth = 2f 
            }
            val rect = RectF(margin, currentY, PAGE_WIDTH - margin, currentY + drawingHeight)
            canvas.drawRect(rect, framePaint)
            currentY += drawingHeight + 10f
        }

        if (config.showRating) {
            if (currentY > PAGE_HEIGHT - 60f) return
            val ratingY = currentY + 20f
            canvas.drawText("Моя оценка:", margin, ratingY, labelPaint)
            val starPaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = if (config.color == DiaryColor.BLACK_WHITE) Color.BLACK else Color.parseColor("#FFB300") }
            canvas.drawText("★ ★ ★ ★ ★", margin + 90f, ratingY + 5f, starPaint)
        }
    }

    private fun drawPictureBookPage(canvas: Canvas, config: DiaryConfig, margin: Float, pageNum: Int, textColor: Int, lineColor: Int) {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Моя книга", PAGE_WIDTH / 2f, 50f, titlePaint)

        val framePaint = Paint().apply { color = if (config.color == DiaryColor.BLACK_WHITE) Color.BLACK else textColor; style = Paint.Style.STROKE; strokeWidth = 2f }
        val rect = RectF(margin, 80f, PAGE_WIDTH - margin, 500f)
        canvas.drawRect(rect, framePaint)

        val linePaint = Paint().apply { color = lineColor; strokeWidth = 1.5f }
        canvas.drawLine(margin, 560f, PAGE_WIDTH - margin, 560f, linePaint)
        canvas.drawLine(margin, 620f, PAGE_WIDTH - margin, 620f, linePaint)

        val ratingPaint = Paint().apply { isAntiAlias = true; textSize = 36f; color = textColor }
        canvas.drawText("😞   😐   😊", margin, 750f, ratingPaint)
        val labelPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.GRAY }
        canvas.drawText("Обведи нужное", margin, 780f, labelPaint)
    }

    private fun drawListPage(canvas: Canvas, config: DiaryConfig, margin: Float, textColor: Int, lineColor: Int) {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Список прочитанного", PAGE_WIDTH / 2f, 50f, titlePaint)

        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = textColor; typeface = Typeface.DEFAULT_BOLD }
        val linePaint = Paint().apply { color = lineColor; strokeWidth = 1f }

        var currentY = 90f
        val cols = floatArrayOf(margin, margin + 60f, margin + 200f, margin + 380f, margin + 420f)
        
        canvas.drawText("Дата", cols[0], currentY, headerPaint)
        canvas.drawText("Автор и название", cols[1], currentY, headerPaint)
        canvas.drawText("Стр.", cols[2], currentY, headerPaint)
        canvas.drawText("Оценка", cols[3], currentY, headerPaint)
        canvas.drawText("Подпись", cols[4], currentY, headerPaint)

        currentY += 10f
        val rowHeight = 30f

        for (i in 0 until config.listRowsCount) {
            canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
            currentY += rowHeight
        }
        canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
        
        for (i in 1 until 4) {
            canvas.drawLine(cols[i] - 5f, 90f, cols[i] - 5f, currentY, linePaint)
        }
    }

    private fun drawSpeedTestPage(canvas: Canvas, config: DiaryConfig, margin: Float, textColor: Int, lineColor: Int) {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Техника чтения", PAGE_WIDTH / 2f, 50f, titlePaint)

        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = textColor; typeface = Typeface.DEFAULT_BOLD }
        val linePaint = Paint().apply { color = lineColor; strokeWidth = 1f }

        var currentY = 90f
        val cols = floatArrayOf(margin, margin + 70f, margin + 250f, margin + 350f, margin + 450f)
        
        canvas.drawText("Дата", cols[0], currentY, headerPaint)
        canvas.drawText("Что читал", cols[1], currentY, headerPaint)
        canvas.drawText("Слов/мин", cols[2], currentY, headerPaint)
        canvas.drawText("Ошибки", cols[3], currentY, headerPaint)
        canvas.drawText("Подпись", cols[4], currentY, headerPaint)

        currentY += 10f
        val rowHeight = 35f

        for (i in 0 until 12) {
            canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
            currentY += rowHeight
        }
        canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
        
        for (i in 1 until 4) {
            canvas.drawLine(cols[i] - 5f, 90f, cols[i] - 5f, currentY, linePaint)
        }
    }
}
