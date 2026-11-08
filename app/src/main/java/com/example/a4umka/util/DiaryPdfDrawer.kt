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

    fun generatePreview(config: DiaryConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        var pageIndex = 0
        var currentBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        var currentCanvas = Canvas(currentBmp)
        
        renderDocument(config,
            canvasProvider = { 
                if (it > 0) {
                    currentBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                    currentCanvas = Canvas(currentBmp)
                }
                currentCanvas.drawColor(getBgColor(config.color))
                currentCanvas
            },
            onPageFinished = {
                pages.add(currentBmp)
            }
        )
        return if (pages.isEmpty()) listOf(currentBmp) else pages
    }

    fun drawDocument(document: PdfDocument, config: DiaryConfig, context: Context) {
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        var page = document.startPage(pageInfo)

        renderDocument(config,
            canvasProvider = { 
                if (it > 0) {
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                    page = document.startPage(pageInfo)
                }
                page.canvas.drawColor(getBgColor(config.color))
                page.canvas
            },
            onPageFinished = {
                document.finishPage(page)
            }
        )
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

    private fun renderDocument(
        config: DiaryConfig,
        canvasProvider: (Int) -> Canvas,
        onPageFinished: () -> Unit
    ) {
        var pageIndex = 0
        var canvas = canvasProvider(pageIndex)
        val margin = 40f

        val isBW = config.color == DiaryColor.BLACK_WHITE
        val textColor = if (isBW) Color.BLACK else Color.parseColor("#1B5E20")
        val lineColor = if (isBW) Color.LTGRAY else Color.parseColor("#A5D6A7")

        // 1. Титульный лист
        if (config.includeTitlePage) {
            drawTitlePage(canvas, config, margin, textColor, lineColor)
            onPageFinished()
            pageIndex++
            canvas = canvasProvider(pageIndex)
        }

        // 2. Страницы для книг
        for (i in 1..config.bookPagesCount) {
            if (config.pageType == DiaryPageType.SCHOOL) {
                drawSchoolBookPage(canvas, config, margin, i, textColor, lineColor)
            } else {
                drawPictureBookPage(canvas, config, margin, i, textColor, lineColor)
            }
            onPageFinished()
            pageIndex++
            canvas = canvasProvider(pageIndex)
        }

        // 3. Список прочитанного
        if (config.includeBookList) {
            drawListPage(canvas, config, margin, textColor, lineColor)
            onPageFinished()
            pageIndex++
            canvas = canvasProvider(pageIndex)
        }

        // 4. Лист техники чтения
        if (config.includeSpeedTest) {
            drawSpeedTestPage(canvas, config, margin, textColor, lineColor)
            onPageFinished()
        }
    }

    private fun drawTitlePage(canvas: Canvas, config: DiaryConfig, margin: Float, textColor: Int, lineColor: Int) {
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 36f
            color = textColor
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
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

        // Рамка для рисунка
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
        val fieldSpacing = 40f

        fun drawField(label: String, heightMultiplier: Float = 1f) {
            if (currentY > PAGE_HEIGHT - 60f) return
            canvas.drawText(label, margin, currentY, labelPaint)
            currentY += 15f
            
            val fieldHeight = fieldSpacing * heightMultiplier
            if (config.ruling == DiaryRuling.NONE) {
                canvas.drawLine(margin, currentY + fieldHeight - 10f, PAGE_WIDTH - margin, currentY + fieldHeight - 10f, linePaint)
            } else {
                var y = currentY
                val step = if (config.ruling == DiaryRuling.WIDE) 12f * 2.83f else 8f * 2.83f
                while (y < currentY + fieldHeight) {
                    canvas.drawLine(margin, y, PAGE_WIDTH - margin, y, linePaint)
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
        if (config.showCharacters) drawField("Главные герои", 2f)
        if (config.showPlot) drawField("О чём эта книга", 3f)
        if (config.showMemorable) drawField("Что запомнилось больше всего", 3f)
        if (config.showLesson) drawField("Чему научила эта книга", 2f)
        if (config.showNewWords) drawField("Новые слова", 1.5f)
        
        if (config.showDrawing) {
            if (currentY < PAGE_HEIGHT - 120f) {
                canvas.drawText("Рисунок к книге", margin, currentY, labelPaint)
                currentY += 15f
                val rect = RectF(margin, currentY, PAGE_WIDTH - margin, PAGE_HEIGHT - 60f)
                canvas.drawRect(rect, linePaint)
                currentY = PAGE_HEIGHT - 50f
            }
        }

        if (config.showRating) {
            val ratingY = PAGE_HEIGHT - 40f
            canvas.drawText("Моя оценка:", margin, ratingY, labelPaint)
            val starPaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = textColor }
            canvas.drawText("★ ★ ★ ★ ★", margin + 90f, ratingY + 5f, starPaint)
        }
    }

    private fun drawPictureBookPage(canvas: Canvas, config: DiaryConfig, margin: Float, pageNum: Int, textColor: Int, lineColor: Int) {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Моя книга", PAGE_WIDTH / 2f, 50f, titlePaint)

        // Большая рамка для рисунка
        val framePaint = Paint().apply { color = lineColor; style = Paint.Style.STROKE; strokeWidth = 2f }
        val rect = RectF(margin, 80f, PAGE_WIDTH - margin, 500f)
        canvas.drawRect(rect, framePaint)

        // 2 крупные строки
        val linePaint = Paint().apply { color = lineColor; strokeWidth = 1.5f }
        canvas.drawLine(margin, 560f, PAGE_WIDTH - margin, 560f, linePaint)
        canvas.drawLine(margin, 620f, PAGE_WIDTH - margin, 620f, linePaint)

        // Оценка смайликом
        val ratingPaint = Paint().apply { isAntiAlias = true; textSize = 36f; color = textColor }
        canvas.drawText("😞   😐   😊", margin, 750f, ratingPaint)
        val labelPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.GRAY }
        canvas.drawText("Обведи нужное", margin, 780f, labelPaint)
    }

    private fun drawListPage(canvas: Canvas, config: DiaryConfig, margin: Float, textColor: Int, lineColor: Int) {
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Список прочитанного", PAGE_WIDTH / 2f, 50f, titlePaint)

        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = textColor; typeface = Typeface.DEFAULT_BOLD }
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.BLACK }
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
        
        // Вертикальные линии
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
