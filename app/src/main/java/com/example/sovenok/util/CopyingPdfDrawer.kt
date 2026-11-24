package com.example.sovenok.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.sovenok.data.CopyingConfig
import com.example.sovenok.data.CopyingMode
import com.example.sovenok.data.CopyingStory

object CopyingPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    private fun renderDocument(
        config: CopyingConfig,
        canvasProvider: (Int) -> Canvas,
        onPageFinished: () -> Unit
    ) {
        var pageIndex = 0
        var canvas = canvasProvider(pageIndex)
        var currentY = 50f
        val margin = 40f
        val lineSpacingPx = config.lineStep * 3.78f
        val bottomLimit = PAGE_HEIGHT - 40f

        fun drawHeader() {
            val headerInfo = "${if (config.mode == CopyingMode.COPYING) "СПИСЫВАНИЕ" else "ДИКТАНТ"} • ${config.grade} КЛАСС • ОРИЕНТИР"
            val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = Typeface.DEFAULT }
            canvas.drawText(headerInfo, margin, currentY, headerPaint)
            currentY += 30f
            val fieldPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
            canvas.drawText("Имя:", margin, currentY, fieldPaint)
            canvas.drawLine(margin + 35f, currentY + 2f, margin + 150f, currentY + 2f, linePaint)
            if (config.signature.isNotEmpty()) {
                canvas.drawText(config.signature, PAGE_WIDTH / 2f - fieldPaint.measureText(config.signature) / 2f, currentY, fieldPaint)
            }
            canvas.drawText("Дата:", PAGE_WIDTH - 140f, currentY, fieldPaint)
            canvas.drawLine(PAGE_WIDTH - 100f, currentY + 2f, PAGE_WIDTH - margin, currentY + 2f, linePaint)
            currentY += 30f
        }

        fun drawWrappedTextWithBreaks(
            text: String,
            x: Float,
            startY: Float,
            maxWidth: Float,
            paint: Paint,
            lineHeight: Float
        ): Float {
            var cy = startY
            val words = text.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth) {
                    if (cy + lineHeight > bottomLimit) {
                        onPageFinished()
                        pageIndex++
                        canvas = canvasProvider(pageIndex)
                        cy = 50f
                        val contPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT }
                        canvas.drawText("Продолжение листа подготовки и проверки", margin, cy, contPaint)
                        cy += 30f
                    }
                    canvas.drawText(line, x, cy, paint)
                    cy += lineHeight
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                if (cy + lineHeight > bottomLimit) {
                    onPageFinished()
                    pageIndex++
                    canvas = canvasProvider(pageIndex)
                    cy = 50f
                    val contPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT }
                    canvas.drawText("Продолжение листа подготовки и проверки", margin, cy, contPaint)
                    cy += 30f
                }
                canvas.drawText(line, x, cy, paint)
                cy += lineHeight
            }
            return cy
        }

        drawHeader()
        if (config.combineOnOnePage) {
            for (story in config.selectedStories) {
                val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
                val words = story.text.split(" ")
                var line = ""
                val lines = mutableListOf<String>()
                for (word in words) {
                    val testLine = if (line.isEmpty()) word else "$line $word"
                    if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2) {
                        lines.add(line); line = word
                    } else { line = testLine }
                }
                if (line.isNotEmpty()) lines.add(line)
                val textHeight = lines.size * bodyPaint.textSize * 1.5f
                val titleHeight = 25f
                if (currentY + titleHeight + textHeight > bottomLimit) {
                    onPageFinished()
                    pageIndex++
                    canvas = canvasProvider(pageIndex)
                    currentY = 50f
                    drawHeader()
                }
                val titlePaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
                canvas.drawText(story.title, margin, currentY, titlePaint)
                currentY += 25f
                for (l in lines) {
                    canvas.drawText(l, margin, currentY, bodyPaint)
                    currentY += bodyPaint.textSize * 1.5f
                }
                currentY += 20f 
            }
        } else {
            for (story in config.selectedStories) {
                for (copy in 0 until config.childrenCount) {
                    if (currentY > 150f) { 
                        onPageFinished()
                        pageIndex++
                        canvas = canvasProvider(pageIndex)
                        currentY = 50f
                        drawHeader()
                    }
                    val titlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
                    canvas.drawText(story.title, margin, currentY, titlePaint)
                    currentY += 30f
                    if (config.mode == CopyingMode.COPYING) {
                        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
                        val words = story.text.split(" ")
                        var line = ""
                        for (word in words) {
                            val testLine = if (line.isEmpty()) word else "$line $word"
                            if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2) {
                                canvas.drawText(line, margin, currentY, bodyPaint)
                                currentY += bodyPaint.textSize * 1.5f
                                line = word
                            } else { line = testLine }
                        }
                        if (line.isNotEmpty()) {
                            canvas.drawText(line, margin, currentY, bodyPaint)
                            currentY += bodyPaint.textSize * 1.5f
                        }
                        currentY += 15f
                    }
                    val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
                    val boxRect = RectF(margin, currentY, PAGE_WIDTH - margin, currentY + 40f)
                    canvas.drawRect(boxRect, boxPaint)
                    val hintPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
                    if (config.mode == CopyingMode.COPYING) {
                        canvas.drawText("Прочитай текст. Списывай по одному предложению.", margin + 5f, currentY + 15f, hintPaint)
                    } else {
                        canvas.drawText("Взрослый читает текст. Ребёнок записывает под диктовку.", margin + 5f, currentY + 15f, hintPaint)
                    }
                    canvas.drawText("Проверь: начало предложений, буквы в словах, пробелы, знаки в конце.", margin + 5f, currentY + 30f, hintPaint)
                    currentY += 60f
                    val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
                    while (currentY < bottomLimit) {
                        canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
                        currentY += lineSpacingPx
                    }
                }
            }
        }
        if (config.addBlankPage) {
            onPageFinished()
            pageIndex++
            canvas = canvasProvider(pageIndex)
            currentY = 60f
            val headerPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
            canvas.drawText("Дополнительный лист для письма", PAGE_WIDTH / 2f - 100f, currentY, headerPaint)
            currentY += 40f
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
            while (currentY < bottomLimit) {
                canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
                currentY += lineSpacingPx
            }
        }
        
        // --- ИСПРАВЛЕНИЕ: используем Color.parseColor вместо недопустимого Color(0x...) ---
        if (config.showPrepSheet) {
            onPageFinished()
            pageIndex++
            canvas = canvasProvider(pageIndex)
            currentY = 50f
            
            val bodyPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.parseColor("#2E3B2E"); typeface = Typeface.DEFAULT }
            val bigTitlePaint = Paint().apply { isAntiAlias = true; textSize = 26f; color = Color.parseColor("#2E3B2E"); typeface = Typeface.DEFAULT_BOLD }
            val smallHeaderPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; typeface = Typeface.DEFAULT }
            val storyTitlePaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.parseColor("#2E3B2E"); typeface = Typeface.DEFAULT_BOLD }
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
            val lineHeight = bodyPaint.textSize * 1.5f
            
            canvas.drawText("ОБЩИЙ ЛИСТ ВЗРОСЛОГО · ТЕКСТЫ И ПОРЯДОК РАБОТЫ", margin, currentY, smallHeaderPaint)
            currentY += 35f
            canvas.drawText("Подготовка и проверка", margin, currentY, bigTitlePaint)
            currentY += 35f
            
            val instruction = if (config.mode == CopyingMode.COPYING) {
                "Ребёнок читает текст и списывает его в своём темпе. Образец печатный: ребёнок пишет привычным ему почерком."
            } else {
                "Прочитайте весь текст. Затем читайте по предложениям, при необходимости делите на короткие смысловые части. Повторите текст целиком для проверки. Заголовок можно не диктовать."
            }
            currentY = drawWrappedTextWithBreaks(instruction, margin, currentY, PAGE_WIDTH - margin * 2, bodyPaint, lineHeight)
            currentY += 20f
            
            val totalPages = config.selectedStories.size * config.childrenCount + 1
            val pensCount = config.childrenCount
            val infoText = "${config.childrenCount} ${pluralChild(config.childrenCount)} · " +
                "${config.selectedStories.size} ${pluralText(config.selectedStories.size)} · " +
                "$totalPages А4 на весь комплект. Ручки: $pensCount. Общий образец: 1. " +
                "При работе в тетради бумажный комплект не требуется. " +
                "Класс — ориентир, а не соответствие программе."
            currentY = drawWrappedTextWithBreaks(infoText, margin, currentY, PAGE_WIDTH - margin * 2, bodyPaint, lineHeight)
            currentY += 25f
            
            canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
            currentY += 25f
            
            for ((index, story) in config.selectedStories.withIndex()) {
                if (currentY > PAGE_HEIGHT - 150f) {
                    onPageFinished()
                    pageIndex++
                    canvas = canvasProvider(pageIndex)
                    currentY = 50f
                    val contPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT }
                    canvas.drawText("Продолжение листа подготовки и проверки", margin, currentY, contPaint)
                    currentY += 30f
                }
                val storyId = "copy-${String.format("%02d", index + 1)}"
                val storyHeader = "${story.title} · ${story.wordCount} слов · $storyId"
                canvas.drawText(storyHeader, margin, currentY, storyTitlePaint)
                currentY += storyTitlePaint.textSize + 10f
                currentY = drawWrappedTextWithBreaks(story.text, margin, currentY, PAGE_WIDTH - margin * 2, bodyPaint, lineHeight)
                currentY += 25f
            }
            
            if (currentY > PAGE_HEIGHT - 100f) {
                onPageFinished()
                pageIndex++
                canvas = canvasProvider(pageIndex)
                currentY = 50f
                val contPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT }
                canvas.drawText("Продолжение листа подготовки и проверки", margin, currentY, contPaint)
                currentY += 30f
            }
            val finalText = "Не оценивайте скорость. При проверке предложите сначала найти конец каждого предложения, затем сравнить слова по порядку. Незнакомые написания объясните до диктанта. Это авторские тренировочные тексты, не контрольные работы по программе."
            currentY = drawWrappedTextWithBreaks(finalText, margin, currentY, PAGE_WIDTH - margin * 2, bodyPaint, lineHeight)
        }
        onPageFinished()
    }

    fun generatePreview(config: CopyingConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        var currentBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        var currentCanvas = Canvas(currentBmp)
        currentCanvas.drawColor(Color.WHITE)
        renderDocument(config,
            canvasProvider = { 
                if (it > 0) {
                    currentBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                    currentCanvas = Canvas(currentBmp)
                }
                currentCanvas.drawColor(Color.WHITE)
                currentCanvas
            },
            onPageFinished = {
                pages.add(currentBmp)
            }
        )
        return if (pages.isEmpty()) listOf(currentBmp) else pages
    }

    fun drawDocument(document: PdfDocument, config: CopyingConfig, context: Context) {
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
                page.canvas.drawColor(Color.WHITE)
                page.canvas
            },
            onPageFinished = {
                document.finishPage(page)
            }
        )
    }

    private fun pluralChild(n: Int): String {
        val lastTwo = n % 100
        val last = n % 10
        return when {
            lastTwo in 11..19 -> "детей"
            last == 1 -> "ребёнок"
            last in 2..4 -> "ребёнка"
            else -> "детей"
        }
    }

    private fun pluralText(n: Int): String {
        val lastTwo = n % 100
        val last = n % 10
        return when {
            lastTwo in 11..19 -> "текстов"
            last == 1 -> "текст"
            last in 2..4 -> "текста"
            else -> "текстов"
        }
    }
}
