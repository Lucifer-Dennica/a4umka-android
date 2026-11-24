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
    private const val MARGIN = 40f
    private const val BOTTOM_LIMIT = 792f

    private val BEIGE_FILL = Color.parseColor("#F5E9D0")
    private val BEIGE_STROKE = Color.parseColor("#D9C9A3")
    private val FIELD_TEXT = Color.parseColor("#5D4037")
    private val GREEN_PILL = Color.parseColor("#A5D6A7")
    private val GREEN_TITLE = Color.parseColor("#33691E")
    private val SUBTITLE_GRAY = Color.parseColor("#8A8A8A")

    fun generatePreviewPages(config: SyllableConfig, context: Context): List<Bitmap> {
        val texts = getTextsToPrint(config)
        if (texts.isEmpty()) return emptyList()

        val pages = mutableListOf<Bitmap>()
        var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bmp)
        var currentY = drawHeader(canvas, texts)

        for (textObj in texts) {
            val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val lineHeight = bodyPaint.textSize * 2.2f
            val lines = splitTextToLines(textObj.text, bodyPaint, PAGE_WIDTH - MARGIN * 2)

            val titlePaint = Paint().apply {
                isAntiAlias = true; textSize = 26f; color = GREEN_TITLE
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            val subPaint = Paint().apply {
                isAntiAlias = true; textSize = 12f; color = SUBTITLE_GRAY
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            if (currentY + titlePaint.textSize + 14f + subPaint.textSize + 20f + lineHeight > BOTTOM_LIMIT) {
                pages.add(bmp)
                bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                canvas = Canvas(bmp)
                currentY = drawHeader(canvas, texts)
            }
            canvas.drawText(textObj.title, PAGE_WIDTH / 2f, currentY, titlePaint)
            currentY += titlePaint.textSize + 14f
            canvas.drawText("слова для чтения по слогам", PAGE_WIDTH / 2f, currentY, subPaint)
            currentY += subPaint.textSize + 20f

            for (line in lines) {
                if (currentY + lineHeight > BOTTOM_LIMIT) {
                    pages.add(bmp)
                    bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                    canvas = Canvas(bmp)
                    currentY = drawHeader(canvas, texts)
                }
                currentY = drawSyllableLine(canvas, line, MARGIN, currentY, bodyPaint, PAGE_WIDTH - MARGIN)
                currentY += lineHeight
            }

            if (config.showQuestions && textObj.questions.isNotEmpty()) {
                currentY += 30f
                val dashPaint = Paint().apply {
                    isAntiAlias = true; color = Color.parseColor("#BDBDBD")
                    style = Paint.Style.STROKE; strokeWidth = 1.5f
                    pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
                }
                if (currentY > BOTTOM_LIMIT) {
                    pages.add(bmp)
                    bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                    canvas = Canvas(bmp)
                    currentY = drawHeader(canvas, texts)
                }
                canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, dashPaint)
                currentY += 26f
                val questionTitlePaint = Paint().apply {
                    isAntiAlias = true; textSize = config.fontSize - 2f
                    color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText("Вопросы по тексту", MARGIN, currentY, questionTitlePaint)
                currentY += questionTitlePaint.textSize + 12f
                val questionPaint = Paint().apply {
                    isAntiAlias = true; textSize = config.fontSize - 4f; color = Color.DKGRAY
                }
                for ((index, q) in textObj.questions.withIndex()) {
                    if (currentY + questionPaint.textSize * 1.5f > BOTTOM_LIMIT) {
                        pages.add(bmp)
                        bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                        canvas = Canvas(bmp)
                        currentY = drawHeader(canvas, texts)
                    }
                    canvas.drawText("${index + 1}. $q", MARGIN, currentY, questionPaint)
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
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create())
        var currentY = drawHeader(page.canvas, texts)

        fun newPage() {
            document.finishPage(page)
            pageNum++
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create())
            currentY = drawHeader(page.canvas, texts)
        }

        for (textObj in texts) {
            val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val lineHeight = bodyPaint.textSize * 2.2f
            val lines = splitTextToLines(textObj.text, bodyPaint, PAGE_WIDTH - MARGIN * 2)

            val titlePaint = Paint().apply {
                isAntiAlias = true; textSize = 26f; color = GREEN_TITLE
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            val subPaint = Paint().apply {
                isAntiAlias = true; textSize = 12f; color = SUBTITLE_GRAY
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            if (currentY + titlePaint.textSize + 14f + subPaint.textSize + 20f + lineHeight > BOTTOM_LIMIT) newPage()
            page.canvas.drawText(textObj.title, PAGE_WIDTH / 2f, currentY, titlePaint)
            currentY += titlePaint.textSize + 14f
            page.canvas.drawText("слова для чтения по слогам", PAGE_WIDTH / 2f, currentY, subPaint)
            currentY += subPaint.textSize + 20f

            for (line in lines) {
                if (currentY + lineHeight > BOTTOM_LIMIT) newPage()
                currentY = drawSyllableLine(page.canvas, line, MARGIN, currentY, bodyPaint, PAGE_WIDTH - MARGIN)
                currentY += lineHeight
            }

            if (config.showQuestions && textObj.questions.isNotEmpty()) {
                currentY += 30f
                val dashPaint = Paint().apply {
                    isAntiAlias = true; color = Color.parseColor("#BDBDBD")
                    style = Paint.Style.STROKE; strokeWidth = 1.5f
                    pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
                }
                if (currentY > BOTTOM_LIMIT) newPage()
                page.canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, dashPaint)
                currentY += 26f
                val questionTitlePaint = Paint().apply {
                    isAntiAlias = true; textSize = config.fontSize - 2f
                    color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD
                }
                page.canvas.drawText("Вопросы по тексту", MARGIN, currentY, questionTitlePaint)
                currentY += questionTitlePaint.textSize + 12f
                val questionPaint = Paint().apply {
                    isAntiAlias = true; textSize = config.fontSize - 4f; color = Color.DKGRAY
                }
                for ((index, q) in textObj.questions.withIndex()) {
                    if (currentY + questionPaint.textSize * 1.5f > BOTTOM_LIMIT) newPage()
                    page.canvas.drawText("${index + 1}. $q", MARGIN, currentY, questionPaint)
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
        for (paragraph in text.split("\n")) {
            var currentLine = ""
            for (word in paragraph.split(" ")) {
                if (word.isEmpty()) continue
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    if (currentLine.isNotEmpty()) result.add(currentLine)
                    currentLine = word
                } else currentLine = testLine
            }
            if (currentLine.isNotEmpty()) result.add(currentLine)
        }
        return result
    }

    // Пунктуация без разметки + перенос длинных слов + точка/дуга по длине слога
    private fun drawSyllableLine(canvas: Canvas, line: String, startX: Float, y: Float, paint: Paint, rightLimit: Float): Float {
        var currentX = startX
        var currentY = y
        val lineHeight = paint.textSize * 2.2f
        for (word in line.split(" ")) {
            if (word.isEmpty()) continue

            val punctuation = StringBuilder()
            val cleanWord = StringBuilder()
            for (char in word) {
                if (char.isLetter()) cleanWord.append(char) else punctuation.append(char)
            }

            val syllables = if (cleanWord.isNotEmpty()) SyllableUtils.splitIntoSyllables(cleanWord.toString()) else emptyList()

            // ширина слова БЕЗ искусственного отступа между слогами
            var wordWidth = 0f
            for (syllable in syllables) wordWidth += paint.measureText(syllable)
            if (punctuation.isNotEmpty()) wordWidth += paint.measureText(punctuation.toString())

            if (currentX + wordWidth > rightLimit && currentX > startX) {
                currentX = startX
                currentY += lineHeight
            }

            for (syllable in syllables) {
                val textWidth = paint.measureText(syllable)
                canvas.drawText(syllable, currentX, currentY, paint)
                if (syllable.length == 1) {
                    val dotPaint = Paint().apply { isAntiAlias = true; color = Color.DKGRAY }
                    canvas.drawCircle(currentX + textWidth / 2f, currentY + 10f, 1.5f, dotPaint)
                } else {
                    val arcPath = android.graphics.Path()
                    arcPath.addArc(RectF(currentX, y + 1f, currentX + textWidth, y + 8f), 0f, 180f)
                    val arcPaint = Paint().apply { 
                        isAntiAlias = true
                        color = Color.DKGRAY
                        style = Paint.Style.STROKE
                        strokeWidth = 1.5f
                        strokeCap = Paint.Cap.ROUND  
                    }
                    canvas.drawPath(arcPath, arcPaint)
                }
                currentX += textWidth  
            }

            if (punctuation.isNotEmpty()) {
                val punctText = punctuation.toString()
                canvas.drawText(punctText, currentX, currentY, paint)
                currentX += paint.measureText(punctText)
            }
            currentX += paint.measureText(" ")  // пробел только МЕЖДУ словами
        }
        return currentY
    }

    private fun drawHeader(canvas: Canvas, texts: List<SyllableText>): Float {
        canvas.drawColor(Color.WHITE)
        drawBeigeField(canvas, "Дата:", MARGIN, 20f, 150f)
        drawBeigeField(canvas, "Время:", PAGE_WIDTH - MARGIN - 150f, 20f, 150f)
        val totalWords = texts.sumOf { it.text.split("\\s+".toRegex()).filter { w -> w.isNotBlank() }.size }
        drawGreenPill(canvas, "Слов: $totalWords", PAGE_WIDTH / 2f, 33f)
        return 92f
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
        val w = meas.measureText(text); val padH = 14f; val h = 24f
        canvas.drawRoundRect(RectF(cx - w / 2 - padH, cy - h / 2, cx + w / 2 + padH, cy + h / 2), 12f, 12f,
            Paint().apply { color = GREEN_PILL; style = Paint.Style.FILL; isAntiAlias = true })
        val tp = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText(text, cx, cy + 5f, tp)
    }
}
