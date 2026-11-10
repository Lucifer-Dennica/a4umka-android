package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.FillwordConfig
import com.example.a4umka.data.FillwordVariants

object FillwordPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: FillwordConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val words = config.getParsedWords()
        if (words.isEmpty()) return pages

        val variantsCount = when(config.variants) {
            FillwordVariants.ONE -> 1
            FillwordVariants.TWO -> 2
            FillwordVariants.FOUR -> 4
        }

        for (v in 1..variantsCount) {
            // Генерируем сетку (если нужно)
            val manualSize = if (config.sizeMode == com.example.a4umka.data.FillwordSizeMode.MANUAL) config.manualSize else null
            val grid = FillwordGenerator.generate(words, manualSize, config.allowBackwards)
            
            // Основной лист
            var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawPage(canvas, config, grid, isAnswer = false, variantNum = v)
            pages.add(bmp)

            // Лист ответов
            if (config.addAnswers) {
                bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                canvas = Canvas(bmp)
                canvas.drawColor(Color.parseColor("#FAFAFA"))
                drawPage(canvas, config, grid, isAnswer = true, variantNum = v)
                pages.add(bmp)
            }
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: FillwordConfig, context: Context) {
        val words = config.getParsedWords()
        if (words.isEmpty()) return

        var pageNum = 1
        val variantsCount = when(config.variants) {
            FillwordVariants.ONE -> 1
            FillwordVariants.TWO -> 2
            FillwordVariants.FOUR -> 4
        }

        for (v in 1..variantsCount) {
            val manualSize = if (config.sizeMode == com.example.a4umka.data.FillwordSizeMode.MANUAL) config.manualSize else null
            val grid = FillwordGenerator.generate(words, manualSize, config.allowBackwards)

            // Основной лист
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            var page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawPage(page.canvas, config, grid, isAnswer = false, variantNum = v)
            document.finishPage(page)

            // Лист ответов
            if (config.addAnswers) {
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
                page = document.startPage(pageInfo)
                page.canvas.drawColor(Color.parseColor("#FAFAFA"))
                drawPage(page.canvas, config, grid, isAnswer = true, variantNum = v)
                document.finishPage(page)
            }
        }
    }

    private fun drawPage(canvas: Canvas, config: FillwordConfig, grid: FillwordGrid, isAnswer: Boolean, variantNum: Int) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // ШАПКА
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        if (config.showNameField) {
            canvas.drawText("Имя:", margin, 40f, headerPaint)
            canvas.drawLine(margin + 25f, 42f, margin + 120f, 42f, linePaint)
        }
        
        val countStr = "Слов: ${grid.placedWords.size}"
        val countWidth = headerPaint.measureText(countStr)
        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - countWidth/2f - 10f, 25f, PAGE_WIDTH/2f + countWidth/2f + 10f, 45f), 5f, 5f, rectPaint)
        canvas.drawText(countStr, PAGE_WIDTH/2f - countWidth/2f, 40f, headerPaint)
        
        val foundStr = if (isAnswer) "Ответы" else "Найдено: ___ из ${grid.placedWords.size}"
        val foundWidth = headerPaint.measureText(foundStr)
        canvas.drawText(foundStr, PAGE_WIDTH - margin - foundWidth, 40f, headerPaint)

        // ЗАГОЛОВОК
        val titleText = config.title.ifEmpty { "Филворд" } + (if (config.variants != FillwordVariants.ONE) " (Вариант $variantNum)" else "")
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 28f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(titleText, PAGE_WIDTH / 2f, 90f, titlePaint)
        
        val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        val dirText = if (config.allowBackwards) "слова спрятаны в любом направлении — найди и обведи" else "слова спрятаны слева направо и сверху вниз — найди и обведи"
        canvas.drawText(dirText, PAGE_WIDTH / 2f, 110f, subtitlePaint)

        // СЕТКА
        val tableSize = 400f // Максимальный размер сетки
        val startY = 150f
        val cellW = tableSize / grid.size
        val cellH = tableSize / grid.size
        val tableLeft = (PAGE_WIDTH - tableSize) / 2f

        val gridPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }
        val letterPaint = Paint().apply { isAntiAlias = true; textSize = cellW * 0.6f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val foundPaint = Paint().apply { color = Color.argb(100, 76, 175, 80); style = Paint.Style.FILL }

        // Если это ответ, сначала рисуем закрашенные области
        if (isAnswer) {
            for (pw in grid.placedWords) {
                for (i in pw.word.indices) {
                    val x = pw.startX + i * pw.dx
                    val y = pw.startY + i * pw.dy
                    val left = tableLeft + x * cellW
                    val top = startY + y * cellH
                    canvas.drawRect(left, top, left + cellW, top + cellH, foundPaint)
                }
            }
        }

        // Рисуем буквы и сетку
        for (y in 0 until grid.size) {
            for (x in 0 until grid.size) {
                val left = tableLeft + x * cellW
                val top = startY + y * cellH
                
                canvas.drawRect(left, top, left + cellW, top + cellH, gridPaint)
                
                val letter = grid.letters[y][x].toString()
                val textBaseline = top + cellH / 2f - (letterPaint.descent() + letterPaint.ascent()) / 2f
                canvas.drawText(letter, left + cellW / 2f, textBaseline, letterPaint)
            }
        }

        // СПИСОК СЛОВ
        if (config.showWordList || isAnswer) {
            val listStartY = startY + tableSize + 40f
            val listPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText("🔍 Найди слова:", margin, listStartY, listPaint)
            
            var currentX = margin + 110f
            var currentY = listStartY
            val wordPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
            val wordBoxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
            
            for (word in grid.placedWords.map { it.word }.sorted()) {
                val wordWidth = wordPaint.measureText(word) + 20f
                if (currentX + wordWidth > PAGE_WIDTH - margin) {
                    currentX = margin + 110f
                    currentY += 25f
                }
                
                val boxRect = RectF(currentX, currentY - 15f, currentX + wordWidth, currentY + 5f)
                canvas.drawRoundRect(boxRect, 5f, 5f, wordBoxPaint)
                canvas.drawText(word, currentX + wordWidth / 2f, currentY, wordPaint)
                currentX += wordWidth + 10f
            }
        }
    }
}
