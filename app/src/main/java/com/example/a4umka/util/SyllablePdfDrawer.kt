package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.SyllableConfig
import com.example.a4umka.data.SyllableSource
import com.example.a4umka.data.SyllableText

object SyllablePdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreviewPages(config: SyllableConfig, context: Context): List<Bitmap> {
        val texts = getTextsToPrint(config)
        if (texts.isEmpty()) return emptyList()

        val pages = mutableListOf<Bitmap>()
        var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bmp)
        var currentY = drawHeader(canvas, config, texts)

        for (textObj in texts) {
            if (currentY > PAGE_HEIGHT - 120f) {
                pages.add(bmp)
                bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                canvas = Canvas(bmp)
                currentY = drawHeader(canvas, config, texts)
            }

            val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val titlePaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize + 4f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }

            canvas.drawText(textObj.title, 40f, currentY, titlePaint)
            currentY += titlePaint.textSize + 10f

            val margin = 40f
            val lineHeight = bodyPaint.textSize * 1.6f
            val paragraphs = textObj.text.split("\n")

            for (paragraph in paragraphs) {
                val words = paragraph.split(" ")
                var currentLine = ""
                val lines = mutableListOf<String>()

                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2) {
                        lines.add(currentLine)
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) lines.add(currentLine)

                for (line in lines) {
                    if (currentY > PAGE_HEIGHT - 60f) {
                        pages.add(bmp)
                        bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                        canvas = Canvas(bmp)
                        currentY = drawHeader(canvas, config, texts)
                    }

                    var currentX = margin
                    val lineWords = line.split(" ")
                    for (word in lineWords) {
                        if (word.isEmpty()) continue
                        val syllables = SyllableUtils.splitIntoSyllables(word)
                        for (syllable in syllables) {
                            canvas.drawText(syllable, currentX, currentY, bodyPaint)
                            val textWidth = bodyPaint.measureText(syllable)

                            val arcPaint = Paint().apply { isAntiAlias = true; color = Color.DKGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
                            val arcRect = RectF(currentX, currentY + 2f, currentX + textWidth, currentY + 15f)
                            canvas.drawArc(arcRect, 0f, 180f, false, arcPaint)

                            if (syllable.length == 1 && syllable[0].toString().lowercase() in "аеёиоуыэюя") {
                                val dotPaint = Paint().apply { color = Color.DKGRAY }
                                canvas.drawCircle(currentX + textWidth / 2f, currentY + 18f, 2f, dotPaint)
                            }
                            currentX += textWidth + 4f
                        }
                        currentX += bodyPaint.measureText(" ")
                    }
                    currentY += lineHeight
                }
            }

            if (config.showQuestions && textObj.questions.isNotEmpty()) {
                currentY += 10f
                val questionPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize - 4f; color = Color.DKGRAY }
                for (q in textObj.questions) {
                    if (currentY > PAGE_HEIGHT - 60f) {
                        pages.add(bmp)
                        bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                        canvas = Canvas(bmp)
                        currentY = drawHeader(canvas, config, texts)
                    }
                    canvas.drawText("• $q", margin, currentY, questionPaint)
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
        var currentY = drawHeader(page.canvas, config, texts)

        for (textObj in texts) {
            if (currentY > PAGE_HEIGHT - 120f) {
                document.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                page = document.startPage(pageInfo)
                currentY = drawHeader(page.canvas, config, texts)
            }
            
            val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val titlePaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize + 4f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }

            page.canvas.drawText(textObj.title, 40f, currentY, titlePaint)
            currentY += titlePaint.textSize + 10f

            val margin = 40f
            val lineHeight = bodyPaint.textSize * 1.6f
            val paragraphs = textObj.text.split("\n")

            for (paragraph in paragraphs) {
                val words = paragraph.split(" ")
                var currentLine = ""
                val lines = mutableListOf<String>()

                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2) {
                        lines.add(currentLine)
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) lines.add(currentLine)

                for (line in lines) {
                    if (currentY > PAGE_HEIGHT - 60f) {
                        document.finishPage(page)
                        pageNum++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                        page = document.startPage(pageInfo)
                        currentY = drawHeader(page.canvas, config, texts)
                    }

                    var currentX = margin
                    val lineWords = line.split(" ")
                    for (word in lineWords) {
                        if (word.isEmpty()) continue
                        val syllables = SyllableUtils.splitIntoSyllables(word)
                        for (syllable in syllables) {
                            page.canvas.drawText(syllable, currentX, currentY, bodyPaint)
                            val textWidth = bodyPaint.measureText(syllable)

                            val arcPaint = Paint().apply { isAntiAlias = true; color = Color.DKGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
                            val arcRect = RectF(currentX, currentY + 2f, currentX + textWidth, currentY + 15f)
                            page.canvas.drawArc(arcRect, 0f, 180f, false, arcPaint)

                            if (syllable.length == 1 && syllable[0].toString().lowercase() in "аеёиоуыэюя") {
                                val dotPaint = Paint().apply { color = Color.DKGRAY }
                                page.canvas.drawCircle(currentX + textWidth / 2f, currentY + 18f, 2f, dotPaint)
                            }
                            currentX += textWidth + 4f
                        }
                        currentX += bodyPaint.measureText(" ")
                    }
                    currentY += lineHeight
                }
            }

            if (config.showQuestions && textObj.questions.isNotEmpty()) {
                currentY += 10f
                val questionPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize - 4f; color = Color.DKGRAY }
                for (q in textObj.questions) {
                    if (currentY > PAGE_HEIGHT - 60f) {
                        document.finishPage(page)
                        pageNum++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                        page = document.startPage(pageInfo)
                        currentY = drawHeader(page.canvas, config, texts)
                    }
                    page.canvas.drawText("• $q", margin, currentY, questionPaint)
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

    private fun drawHeader(canvas: Canvas, config: SyllableConfig, texts: List<SyllableText>): Float {
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        val topPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        canvas.drawText("Дата:", 40f, 40f, topPaint)
        canvas.drawLine(80f, 42f, 180f, 42f, linePaint)
        canvas.drawText("Время:", PAGE_WIDTH - 160f, 40f, topPaint)
        canvas.drawLine(PAGE_WIDTH - 110f, 42f, PAGE_WIDTH - 40f, 42f, linePaint)

        val totalWords = texts.sumOf { it.text.split("\\s+".toRegex()).filter { w -> w.isNotBlank() }.size }
        val countBgPaint = Paint().apply { color = Color.parseColor("#A5D6A7"); style = Paint.Style.FILL }
        val countRect = RectF(PAGE_WIDTH / 2f - 40f, 25f, PAGE_WIDTH / 2f + 40f, 48f)
        canvas.drawRoundRect(countRect, 8f, 8f, countBgPaint)
        val countTextPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Слов: $totalWords", PAGE_WIDTH / 2f, 42f, countTextPaint)

        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.parseColor("#33691E"); typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Чтение по слогам", PAGE_WIDTH / 2f, 90f, titlePaint)

        return 130f
    }
}
