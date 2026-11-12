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
import com.example.a4umka.data.CrosswordDifficulty
import com.example.a4umka.data.CrosswordGrid

object CrosswordPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: CrosswordConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val grid = CrosswordData.generateGrid(config.difficulty)

        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        
        if (config.perPage == 1) {
            drawSingleCrossword(canvas, grid, 0f, PAGE_HEIGHT / 2f, config)
        } else {
            // 2 на страницу
            drawSingleCrossword(canvas, grid, -PAGE_HEIGHT / 4f, PAGE_HEIGHT / 4f, config)
            val grid2 = CrosswordData.generateGrid(config.difficulty)
            drawSingleCrossword(canvas, grid2, PAGE_HEIGHT / 4f, PAGE_HEIGHT / 4f, config)
        }
        
        pages.add(bmp)

        if (config.showAnswers) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.drawColor(Color.WHITE)
            drawAnswers(ansCanvas, grid) // Для простоты ответ только на первый
            pages.add(ansBmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: CrosswordConfig, context: Context) {
        val grid = CrosswordData.generateGrid(config.difficulty)

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        
        if (config.perPage == 1) {
            drawSingleCrossword(page.canvas, grid, 0f, PAGE_HEIGHT / 2f, config)
        } else {
            drawSingleCrossword(page.canvas, grid, -PAGE_HEIGHT / 4f, PAGE_HEIGHT / 4f, config)
            val grid2 = CrosswordData.generateGrid(config.difficulty)
            drawSingleCrossword(page.canvas, grid2, PAGE_HEIGHT / 4f, PAGE_HEIGHT / 4f, config)
        }
        
        document.finishPage(page)

        if (config.showAnswers) {
            val ansPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 2).create()
            val ansPage = document.startPage(ansPageInfo)
            ansPage.canvas.drawColor(Color.WHITE)
            drawAnswers(ansPage.canvas, grid)
            document.finishPage(ansPage)
        }
    }

    private fun drawSingleCrossword(canvas: Canvas, grid: CrosswordGrid, yOffset: Float, availableHeight: Float, config: CrosswordConfig) {
        val textColor = Color.parseColor("#33691E")
        val scale = if (config.perPage == 1) 1f else 0.6f
        
        val centerY = availableHeight / 2f + yOffset
        val titleY = centerY - (availableHeight * 0.4f)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 36f * scale; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Кроссворд по картинкам", PAGE_WIDTH / 2f, titleY, titlePaint)

        // Отрисовка сетки
        val cellSize = 30f * scale
        val gridStartX = PAGE_WIDTH / 2f - (5 * cellSize) // примерно по центру
        val gridStartY = titleY + 40f * scale

        val boxPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val linePaint = Paint().apply { color = Color.GRAY; strokeWidth = 1.5f; style = Paint.Style.STROKE }
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 10f * scale; color = Color.parseColor("#D32F2F"); typeface = Typeface.DEFAULT_BOLD }
        val letterPaint = Paint().apply { isAntiAlias = true; textSize = 20f * scale; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }

        for ((pos, char) in grid.cells) {
            val cx = gridStartX + pos.first * cellSize
            val cy = gridStartY + pos.second * cellSize

            canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, boxPaint)
            canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, linePaint)
            canvas.drawText(char.toString(), cx + cellSize / 2f, cy + cellSize * 0.7f, letterPaint)
        }

        // Номера
        for (word in grid.words) {
            val cx = gridStartX + word.x * cellSize
            val cy = gridStartY + word.y * cellSize
            canvas.drawText(word.num.toString(), cx + 2f, cy + 10f * scale, numPaint)
        }

        // Список вопросов
        var clueY = gridStartY + 9 * cellSize + 20f
        val clueTitlePaint = Paint().apply { isAntiAlias = true; textSize = 16f * scale; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        
        canvas.drawText("По горизонтали →", 40f, clueY, clueTitlePaint)
        canvas.drawText("По вертикали ↓", PAGE_WIDTH / 2f + 20f, clueY, clueTitlePaint)
        clueY += 20f * scale

        val cluePaint = Paint().apply { isAntiAlias = true; textSize = 14f * scale; color = Color.BLACK }
        val emojiPaint = Paint().apply { textSize = 20f * scale }
        
        var acrossY = clueY
        var downY = clueY

        for (word in grid.words) {
            if (word.isAcross) {
                canvas.drawText("${word.num}.", 40f, acrossY, cluePaint)
                canvas.drawText(word.emoji, 70f, acrossY, emojiPaint)
                if (config.showWordsInClues) canvas.drawText(word.word, 100f, acrossY, cluePaint)
                acrossY += 25f * scale
            } else {
                canvas.drawText("${word.num}.", PAGE_WIDTH / 2f + 20f, downY, cluePaint)
                canvas.drawText(word.emoji, PAGE_WIDTH / 2f + 50f, downY, emojiPaint)
                if (config.showWordsInClues) canvas.drawText(word.word, PAGE_WIDTH / 2f + 80f, downY, cluePaint)
                downY += 25f * scale
            }
        }
    }

    private fun drawAnswers(canvas: Canvas, grid: CrosswordGrid) {
        val margin = 40f
        var currentY = 60f
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        
        for (word in grid.words) {
            canvas.drawText("${word.num} ${if (word.isAcross) "→" else "↓"} ${word.emoji} ${word.word}", margin, currentY, ansPaint)
            currentY += 25f
        }
    }
}
