package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.CustomCrosswordConfig
import com.example.a4umka.data.CustomCrosswordData
import com.example.a4umka.data.CrosswordGrid

object CustomCrosswordPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: CustomCrosswordConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val pairs = CustomCrosswordData.parseInput(config.wordsInput)
        if (pairs.isEmpty()) return pages

        val grid = CustomCrosswordData.generateGrid(pairs, config.seed)

        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(canvas, grid, pairs, config.title, isAnswers = false)
        pages.add(bmp)

        if (config.showAnswers) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.drawColor(Color.WHITE)
            drawPage(ansCanvas, grid, pairs, config.title, isAnswers = true)
            pages.add(ansBmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: CustomCrosswordConfig, context: Context) {
        val pairs = CustomCrosswordData.parseInput(config.wordsInput)
        if (pairs.isEmpty()) return
        val grid = CustomCrosswordData.generateGrid(pairs, config.seed)

        var pageNum = 1
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(page.canvas, grid, pairs, config.title, isAnswers = false)
        document.finishPage(page)

        if (config.showAnswers) {
            val ansPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val ansPage = document.startPage(ansPageInfo)
            ansPage.canvas.drawColor(Color.WHITE)
            drawPage(ansPage.canvas, grid, pairs, config.title, isAnswers = true)
            document.finishPage(ansPage)
        }
    }

    private fun drawPage(canvas: Canvas, grid: CrosswordGrid, pairs: List<com.example.a4umka.data.CustomWordPair>, title: String, isAnswers: Boolean) {
        val textColor = Color.parseColor("#33691E")

        // Шапка
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawText("Дата:", 40f, 40f, headerPaint)
        canvas.drawLine(70f, 42f, 140f, 42f, linePaint)
        
        val countStr = "Слов: ${grid.words.size}"
        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - 40f, 25f, PAGE_WIDTH/2f + 40f, 45f), 5f, 5f, rectPaint)
        val centerTextPaint = Paint(headerPaint).apply { textAlign = Paint.Align.CENTER; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText(countStr, PAGE_WIDTH/2f, 40f, centerTextPaint)

        canvas.drawText("Имя:", PAGE_WIDTH - 120f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - 40f, 42f, linePaint)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 30f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(title, PAGE_WIDTH / 2f, 90f, titlePaint)

        val italicTypeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        val subPaint = Paint().apply { 
            isAntiAlias = true; textSize = 12f; color = Color.GRAY; 
            textAlign = Paint.Align.CENTER; typeface = italicTypeface 
        }
        canvas.drawText(if (isAnswers) "ответы" else "разгадай вопросы и впиши слова в клетки", PAGE_WIDTH / 2f, 110f, subPaint)

        // Сетка
        val minX = grid.cells.keys.minOfOrNull { it.first } ?: 0
        val maxX = grid.cells.keys.maxOfOrNull { it.first } ?: 0
        val minY = grid.cells.keys.minOfOrNull { it.second } ?: 0
        val maxY = grid.cells.keys.maxOfOrNull { it.second } ?: 0

        val cols = maxX - minX + 1
        val rows = maxY - minY + 1
        
        val cellSize = minOf(24f, 350f / rows, (PAGE_WIDTH - 100f) / cols)
        
        val gridWidth = cols * cellSize
        val gridHeight = rows * cellSize
        
        val gridStartX = (PAGE_WIDTH - gridWidth) / 2f
        val gridStartY = 140f

        val boxPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.GRAY; strokeWidth = 1.5f; style = Paint.Style.STROKE }
        val letterPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 8f; color = Color.parseColor("#D32F2F"); typeface = Typeface.DEFAULT_BOLD }

        for ((pos, char) in grid.cells) {
            val cx = gridStartX + (pos.first - minX) * cellSize
            val cy = gridStartY + (pos.second - minY) * cellSize
            canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, boxPaint)
            canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, borderPaint)
            if (isAnswers) {
                canvas.drawText(char.toString(), cx + cellSize / 2f, cy + cellSize * 0.7f, letterPaint)
            }
        }

        for (word in grid.words) {
            val cx = gridStartX + (word.x - minX) * cellSize
            val cy = gridStartY + (word.y - minY) * cellSize
            canvas.drawText(word.num.toString(), cx + 2f, cy + 10f, numPaint)
        }

        // Вопросы
        val clueStartY = gridStartY + gridHeight + 30f
        val clueTitlePaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        
        canvas.drawText("По горизонтали →", 40f, clueStartY, clueTitlePaint)
        canvas.drawText("По вертикали ↓", PAGE_WIDTH / 2f + 20f, clueStartY, clueTitlePaint)
        
        val cluePaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.BLACK }
        
        var acrossY = clueStartY + 20f
        var downY = clueStartY + 20f

        for (word in grid.words) {
            val question = pairs.find { it.word == word.word }?.question ?: ""
            if (word.isAcross) {
                canvas.drawText("${word.num}. $question", 40f, acrossY, cluePaint)
                acrossY += 18f
            } else {
                canvas.drawText("${word.num}. $question", PAGE_WIDTH / 2f + 20f, downY, cluePaint)
                downY += 18f
            }
        }
    }
}
