package com.example.sovenok.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.sovenok.data.SyllableConfig
import com.example.sovenok.data.SyllableSource
import com.example.sovenok.data.SyllableText

object SyllablePdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    // Цвета для листа (как на скрине)
    private val BEIGE_FILL = Color.parseColor("#F5E9D0")
    private val BEIGE_STROKE = Color.parseColor("#D9C9A3")
    private val FIELD_TEXT = Color.parseColor("#5D4037")
    private val GREEN_PILL = Color.parseColor("#A5D6A7")
    private val GREEN_TITLE = Color.parseColor("#33691E")

    // Двухколоночная верстка
    private const val COL_GAP = 30f
    private const val BOTTOM_LIMIT = 792f

    private fun colLeft(col: Int): Float =
        if (col == 0) 40f else (PAGE_WIDTH / 2f + COL_GAP / 2f)

    private fun colRight(col: Int): Float =
        if (col == 0) (PAGE_WIDTH / 2f - COL_GAP / 2f) else (PAGE_WIDTH - 40f)

    private fun colWidth(col: Int): Float = colRight(col) - colLeft(col)

    fun generatePreviewPages(config: SyllableConfig, context: Context): List<Bitmap> {
        val texts = getTextsToPrint(config)
        if (texts.isEmpty()) return emptyList()
        val pages = mutableListOf<Bitmap>()
        var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bmp)
        var currentCol = 0
        var colStartY = drawHeader(canvas, config, texts)
        var currentY = colStartY

        for (textObj in texts) {
            val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val titlePaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize + 4f; color = GREEN_TITLE; typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC) }
            val lineHeight = bodyPaint.textSize * 2.2f

            val lines = splitTextToLines(textObj.text, bodyPaint, colWidth(currentCol))
            val questionBlockHeight = if (config.showQuestions && textObj.questions.isNotEmpty()) {
                45f + 30f + (config.fontSize - 2f) + 15f + textObj.questions.size * (config.fontSize - 4f) * 1.5f
            } else 0f
            val totalHeight = titlePaint.textSize + 10f + lines.size * lineHeight + questionBlockHeight

            // Переход в следующую колонку/страницу, если не влезает
            if (currentY + totalHeight > BOTTOM_LIMIT) {
                if (currentCol == 0) {
                    currentCol = 1
                    currentY = colStartY
                } else {
                    pages.add(bmp)
                    bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                    canvas = Canvas(bmp)
                    currentCol = 0
                    colStartY = drawHeader(canvas, config, texts)
                    currentY = colStartY
                }
            }

            val cx = colLeft(currentCol)

            // Заголовок
            canvas.drawText(textObj.title, cx, currentY, titlePaint)
            currentY += titlePaint.textSize + 10f

            // Текст
            for (line in lines) {
                if (currentY > BOTTOM_LIMIT) {
                    if (currentCol == 0) {
                        currentCol = 1
                        currentY = colStartY
                    } else {
                        pages.add(bmp)
                        bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                        canvas = Canvas(bmp)
                        currentCol = 0
                        colStartY = drawHeader(canvas, config, texts)
                        currentY = colStartY
                    }
                }
                drawSyllableLine(canvas, line, cx, currentY, bodyPaint, colRight(currentCol))
                currentY += lineHeight
            }

            // Вопросы
            if (config.showQuestions && textObj.questions.isNotEmpty()) {
                currentY += 45f
                val dashPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#BDBDBD")
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
                }
                canvas.drawLine(cx, currentY, colRight(currentCol), currentY, dashPaint)
                currentY += 30f
                val questionTitlePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = config.fontSize - 2f
                    color = Color.DKGRAY
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText("Вопросы по тексту", cx, currentY, questionTitlePaint)
                currentY += questionTitlePaint.textSize + 15f
                val questionPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = config.fontSize - 4f
                    color = Color.DKGRAY
                }
                for ((index, q) in textObj.questions.withIndex()) {
                    if (currentY > BOTTOM_LIMIT) {
                        if (currentCol == 0) {
                            currentCol = 1
                            currentY = colStartY
                        } else {
                            pages.add(bmp)
                            bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                            canvas = Canvas(bmp)
                            currentCol = 0
                            colStartY = drawHeader(canvas, config, texts)
                            currentY = colStartY
                            currentY += 45f
                            canvas.drawLine(colLeft(0), currentY, colRight(0), currentY, dashPaint)
                            currentY += 30f
                            canvas.drawText("Вопросы по тексту", colLeft(0), currentY, questionTitlePaint)
                            currentY += questionTitlePaint.textSize + 15f
                        }
                    }
                    canvas.drawText("${index + 1}. $q", colLeft(currentCol), currentY, questionPaint)
                    currentY += questionPaint.textSize * 1.5f
                }
            }

            currentY += config.textSpacing
        }
        pages.add(bmp)
        return pages
    }

    fun drawDocument(document: PdfDocument, config: SyllableConfig, context: Context) {
        val texts = getTextsToPrint(config)
        if (texts.isEmpty()) return
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        var page = document.startPage(pageInfo)
        var currentCol = 0
        var colStartY = drawHeader(page.canvas, config, texts)
        var currentY = colStartY

        for (textObj in texts) {
            val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val titlePaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize + 4f; color = GREEN_TITLE; typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC) }
            val lineHeight = bodyPaint.textSize * 2.2f

            val lines = splitTextToLines(textObj.text, bodyPaint, colWidth(currentCol))
            val questionBlockHeight = if (config.showQuestions && textObj.questions.isNotEmpty()) {
                45f + 30f + (config.fontSize - 2f) + 15f + textObj.questions.size * (config.fontSize - 4f) * 1.5f
            } else 0f
            val totalHeight = titlePaint.textSize + 10f + lines.size * lineHeight + questionBlockHeight

            if (currentY + totalHeight > BOTTOM_LIMIT) {
                if (currentCol == 0) {
                    currentCol = 1
                    currentY = colStartY
                } else {
                    document.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                    page = document.startPage(pageInfo)
                    currentCol = 0
                    colStartY = drawHeader(page.canvas, config, texts)
                    currentY = colStartY
                }
            }

            val cx = colLeft(currentCol)

            page.canvas.drawText(textObj.title, cx, currentY, titlePaint)
            currentY += titlePaint.textSize + 10f

            for (line in lines) {
                if (currentY > BOTTOM_LIMIT) {
                    if (currentCol == 0) {
                        currentCol = 1
                        currentY = colStartY
                    } else {
                        document.finishPage(page)
                        pageNum++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                        page = document.startPage(pageInfo)
                        currentCol = 0
                        colStartY = drawHeader(page.canvas, config, texts)
                        currentY = colStartY
                    }
                }
                drawSyllableLine(page.canvas, line, cx, currentY, bodyPaint, colRight(currentCol))
                currentY += lineHeight
            }

            if (config.showQuestions && textObj.questions.isNotEmpty()) {
                currentY += 45f
                val dashPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#BDBDBD")
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
                }
                page.canvas.drawLine(cx, currentY, colRight(currentCol), currentY, dashPaint)
                currentY += 30f
                val questionTitlePaint = Paint().apply {
                    isAntiAlias = true
                    textSize = config.fontSize - 2f
                    color = Color.DKGRAY
                    typeface = Typeface.DEFAULT_BOLD
                }
                page.canvas.drawText("Вопросы по тексту", cx, currentY, questionTitlePaint)
                currentY += questionTitlePaint.textSize + 15f
                val questionPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = config.fontSize - 4f
                    color = Color.DKGRAY
                }
                for ((index, q) in textObj.questions.withIndex()) {
                    if (currentY > BOTTOM_LIMIT) {
                        if (currentCol == 0) {
                            currentCol = 1
                            currentY = colStartY
                        } else {
                            document.finishPage(page)
                            pageNum++
                            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                            page = document.startPage(pageInfo)
                            currentCol = 0
                            colStartY = drawHeader(page.canvas, config, texts)
                            currentY = colStartY
                            currentY += 45f
                            page.canvas.drawLine(colLeft(0), currentY, colRight(0), currentY, dashPaint)
                            currentY += 30f
                            page.canvas.drawText("Вопросы по тексту", colLeft(0), currentY, questionTitlePaint)
                            currentY += questionTitlePaint.textSize + 15f
                        }
                    }
                    page.canvas.drawText("${index + 1}. $q", colLeft(currentCol), currentY, questionPaint)
                    currentY += questionPaint.textSize * 1.5f
                }
            }

            currentY += config.textSpacing
        }
        document.finishPage(page)
    }

    private fun getTextsToPrint(config: SyllableConfig): List<SyllableText> {
        return if (config.source == SyllableSource.READY) config.selectedTexts
        else if (config.customText.isNotBlank()) listOf(SyllableText(config.customTitle, config.customText))
        else emptyList()
    }

    private fun splitTextToLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    if (currentLine.isNotEmpty()) result.add(currentLine)
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) result.add(currentLine)
        }
        return result
    }

    private fun drawSyllableLine(canvas: Canvas, line: String, startX: Float, y: Float, paint: Paint, rightLimit: Float) {
        var currentX = startX
        val words = line.split(" ")
        for (word in words) {
            if (word.isEmpty()) continue
            val syllables = SyllableUtils.splitIntoSyllables(word)
            for (syllable in syllables) {
                val textWidth = paint.measureText(syllable)
                if (currentX + textWidth > rightLimit) break
                canvas.drawText(syllable, currentX, y, paint)
                val arcPaint = Paint().apply { isAntiAlias = true; color = Color.DKGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
                val arcRect = RectF(currentX, y + 1f, currentX + textWidth, y + 8f)
                canvas.drawArc(arcRect, 0f, 180f, false, arcPaint)
                if (syllable.length == 1 && syllable[0].toString().lowercase() in "аеёиоуыэюя") {
                    val dotPaint = Paint().apply { color = Color.DKGRAY }
                    canvas.drawCircle(currentX + textWidth / 2f, y + 12f, 1.5f, dotPaint)
                }
                currentX += textWidth + 4f
            }
            currentX += paint.measureText(" ")
        }
    }

    private fun drawHeader(canvas: Canvas, config: SyllableConfig, texts: List<SyllableText>): Float {
        canvas.drawColor(Color.WHITE)

        // Бежевое поле "Дата" слева
        drawBeigeField(canvas, "Дата:", 40f, 20f, 150f)
        // Бежевое поле "Время" справа
        drawBeigeField(canvas, "Время:", PAGE_WIDTH - 190f, 20f, 150f)
        // Зелёная пилюля "Слов: N" по центру
        val totalWords = texts.sumOf { it.text.split("\\s+".toRegex()).filter { w -> w.isNotBlank() }.size }
        drawGreenPill(canvas, "Слов: $totalWords", PAGE_WIDTH / 2f, 33f)

        val title = if (texts.size == 1) texts.first().title else "Чтение по слогам"
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 28f
            color = GREEN_TITLE
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, PAGE_WIDTH / 2f, 90f, titlePaint)
        return 130f
    }

    private fun drawBeigeField(canvas: Canvas, label: String, x: Float, top: Float, width: Float) {
        val bottom = top + 26f
        val fill = Paint().apply { color = BEIGE_FILL; style = Paint.Style.FILL; isAntiAlias = true }
        val stroke = Paint().apply { color = BEIGE_STROKE; style = Paint.Style.STROKE; strokeWidth = 1f; isAntiAlias = true }
        canvas.drawRoundRect(RectF(x, top, x + width, bottom), 8f, 8f, fill)
        canvas.drawRoundRect(RectF(x, top, x + width, bottom), 8f, 8f, stroke)
        val tp = Paint().apply { isAntiAlias = true; textSize = 13f; color = FIELD_TEXT }
        canvas.drawText(label, x + 10f, top + 18f, tp)
        val line = Paint().apply { color = Color.argb(100, 93, 64, 55); strokeWidth = 1f; isAntiAlias = true }
        canvas.drawLine(x + 10f + tp.measureText(label) + 6f, top + 18f, x + width - 10f, top + 18f, line)
    }

    private fun drawGreenPill(canvas: Canvas, text: String, cx: Float, cy: Float) {
        val meas = Paint().apply { isAntiAlias = true; textSize = 14f; typeface = Typeface.DEFAULT_BOLD }
        val w = meas.measureText(text)
        val padH = 14f
        val h = 24f
        val fill = Paint().apply { color = GREEN_PILL; style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawRoundRect(RectF(cx - w / 2 - padH, cy - h / 2, cx + w / 2 + padH, cy + h / 2), 12f, 12f, fill)
        val tp = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText(text, cx, cy + 5f, tp)
    }
}
