package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.CrosswordConfig
import com.example.a4umka.data.CrosswordData
import com.example.a4umka.data.CrosswordGrid

object CrosswordPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: CrosswordConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        
        val grid1 = CrosswordData.generateGrid(config.difficulty)
        val grid2 = if (config.perPage == 2) CrosswordData.generateGrid(config.difficulty) else null

        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        
        if (config.perPage == 1) {
            drawSingleCrossword(canvas, grid1, 0f, PAGE_HEIGHT, config)
        } else {
            drawSingleCrossword(canvas, grid1, 0f, PAGE_HEIGHT / 2f, config)
            grid2?.let { drawSingleCrossword(canvas, it, PAGE_HEIGHT / 2f, PAGE_HEIGHT / 2f, config) }
        }
        
        pages.add(bmp)

        if (config.showAnswers) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.drawColor(Color.WHITE)
            drawAnswers(ansCanvas, grid1, 0f, PAGE_HEIGHT / 2f)
            grid2?.let { drawAnswers(ansCanvas, it, PAGE_HEIGHT / 2f, PAGE_HEIGHT / 2f) }
            pages.add(ansBmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: CrosswordConfig, context: Context) {
        val grid1 = CrosswordData.generateGrid(config.difficulty)
        val grid2 = if (config.perPage == 2) CrosswordData.generateGrid(config.difficulty) else null

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        
        if (config.perPage == 1) {
            drawSingleCrossword(page.canvas, grid1, 0f, PAGE_HEIGHT, config)
        } else {
            drawSingleCrossword(page.canvas, grid1, 0f, PAGE_HEIGHT / 2f, config)
            grid2?.let { drawSingleCrossword(page.canvas, it, PAGE_HEIGHT / 2f, PAGE_HEIGHT / 2f, config) }
        }
        
        document.finishPage(page)

        if (config.showAnswers) {
            val ansPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 2).create()
            val ansPage = document.startPage(ansPageInfo)
            ansPage.canvas.drawColor(Color.WHITE)
            drawAnswers(ansPage.canvas, grid1, 0f, PAGE_HEIGHT / 2f)
            grid2?.let { drawAnswers(ansPage.canvas, it, PAGE_HEIGHT / 2f, PAGE_HEIGHT / 2f) }
            document.finishPage(ansPage)
        }
    }

    private fun drawSingleCrossword(canvas: Canvas, grid: CrosswordGrid, topOffset: Float, availableHeight: Float, config: CrosswordConfig) {
        val textColor = Color.parseColor("#33691E")
        
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 24f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        val titleY = topOffset + 30f
        canvas.drawText("Кроссворд по картинкам", PAGE_WIDTH / 2f, titleY, titlePaint)

        val minX = grid.cells.keys.minOfOrNull { it.first } ?: 0
        val maxX = grid.cells.keys.maxOfOrNull { it.first } ?: 0
        val minY = grid.cells.keys.minOfOrNull { it.second } ?: 0
        val maxY = grid.cells.keys.maxOfOrNull { it.second } ?: 0

        val cols = maxX - minX + 1
        val rows = maxY - minY + 1
        
        val cellSize = minOf(24f, (availableHeight - 150f) / rows, (PAGE_WIDTH - 100f) / cols)
        
        val gridWidth = cols * cellSize
        val gridHeight = rows * cellSize
        
        val gridStartX = (PAGE_WIDTH - gridWidth) / 2f
        val gridStartY = titleY + 20f + (availableHeight - 100f - gridHeight) / 3f

        val boxPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val linePaint = Paint().apply { color = Color.GRAY; strokeWidth = 1.5f; style = Paint.Style.STROKE }
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 8f; color = Color.parseColor("#D32F2F"); typeface = Typeface.DEFAULT_BOLD }

        // 1. Рисуем ПУСТЫЕ клетки
        for ((pos, _) in grid.cells) {
            val cx = gridStartX + (pos.first - minX) * cellSize
            val cy = gridStartY + (pos.second - minY) * cellSize
            canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, boxPaint)
            canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, linePaint)
        }

        // 2. Номера
        for (word in grid.words) {
            val cx = gridStartX + (word.x - minX) * cellSize
            val cy = gridStartY + (word.y - minY) * cellSize
            canvas.drawText(word.num.toString(), cx + 2f, cy + 10f, numPaint)
        }

        // 3. Вопросы
        val clueStartY = gridStartY + gridHeight + 20f
        val clueTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("По горизонтали →", 40f, clueStartY, clueTitlePaint)
        canvas.drawText("По вертикали ↓", PAGE_WIDTH / 2f + 20f, clueStartY, clueTitlePaint)
        
        val cluePaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.BLACK }
        val emojiPaint = Paint().apply { textSize = 14f }
        
        var acrossY = clueStartY + 15f
        var downY = clueStartY + 15f

        for (word in grid.words) {
            if (word.isAcross) {
                canvas.drawText("${word.num}.", 40f, acrossY, cluePaint)
                canvas.drawText(word.emoji, 60f, acrossY, emojiPaint)
                if (config.showWordsInClues) canvas.drawText(word.word, 80f, acrossY, cluePaint)
                acrossY += 15f
            } else {
                canvas.drawText("${word.num}.", PAGE_WIDTH / 2f + 20f, downY, cluePaint)
                canvas.drawText(word.emoji, PAGE_WIDTH / 2f + 40f, downY, emojiPaint)
                if (config.showWordsInClues) canvas.drawText(word.word, PAGE_WIDTH / 2f + 60f, downY, cluePaint)
                downY += 15f
            }
        }
    }

    private fun drawAnswers(canvas: Canvas, grid: CrosswordGrid, topOffset: Float, availableHeight: Float) {
        val textColor = Color.parseColor("#33691E")
        
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 20f; color = Color.GRAY; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        val titleY = topOffset + 30f
        canvas.drawText("Ответы", PAGE_WIDTH / 2f, titleY, titlePaint)

        val minX = grid.cells.keys.minOfOrNull { it.first } ?: 0
        val maxX = grid.cells.keys.maxOfOrNull { it.first } ?: 0
        val minY = grid.cells.keys.minOfOrNull { it.second } ?: 0
        val maxY = grid.cells.keys.maxOfOrNull { it.second } ?: 0

        val cols = maxX - minX + 1
        val rows = maxY - minY + 1
        
        val cellSize = minOf(24f, (availableHeight - 100f) / rows, (PAGE_WIDTH - 100f) / cols)
        val gridWidth = cols * cellSize
        val gridHeight = rows * cellSize
        
        val gridStartX = (PAGE_WIDTH - gridWidth) / 2f
        val gridStartY = titleY + 20f

        val boxPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val linePaint = Paint().apply { color = Color.GRAY; strokeWidth = 1.5f; style = Paint.Style.STROKE }
        val letterPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 8f; color = Color.parseColor("#D32F2F"); typeface = Typeface.DEFAULT_BOLD }

        for ((pos, char) in grid.cells) {
            val cx = gridStartX + (pos.first - minX) * cellSize
            val cy = gridStartY + (pos.second - minY) * cellSize
            canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, boxPaint)
            canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, linePaint)
            canvas.drawText(char.toString(), cx + cellSize / 2f, cy + cellSize * 0.7f, letterPaint)
        }

        for (word in grid.words) {
            val cx = gridStartX + (word.x - minX) * cellSize
            val cy = gridStartY + (word.y - minY) * cellSize
            canvas.drawText(word.num.toString(), cx + 2f, cy + 10f, numPaint)
        }
    }
}
