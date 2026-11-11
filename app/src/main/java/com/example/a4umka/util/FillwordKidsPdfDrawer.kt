package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.FillwordKidsConfig
import com.example.a4umka.data.FillwordKidsData
import com.example.a4umka.data.FillwordKidsDifficulty
import com.example.a4umka.data.KidsWord

object FillwordKidsPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: FillwordKidsConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val data = getGridData(config)

        // Основной лист
        var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(canvas, config, data, isAnswer = false)
        pages.add(bmp)

        // Лист ответов
        if (config.addAnswers) {
            bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawPage(canvas, config, data, isAnswer = true)
            pages.add(bmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: FillwordKidsConfig, context: Context) {
        val data = getGridData(config)
        var pageNum = 1

        // Основной лист
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
        var page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(page.canvas, config, data, isAnswer = false)
        document.finishPage(page)

        // Лист ответов
        if (config.addAnswers) {
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawPage(page.canvas, config, data, isAnswer = true)
            document.finishPage(page)
        }
    }

    private data class GridData(
        val grid: FillwordGrid,
        val words: List<KidsWord>
    )

    private fun getGridData(config: FillwordKidsConfig): GridData {
        val size: Int
        val count: Int
        val allowVertical: Boolean

        when (config.difficulty) {
            FillwordKidsDifficulty.EASY -> { size = 7; count = 5; allowVertical = false }
            FillwordKidsDifficulty.MEDIUM -> { size = 9; count = 6; allowVertical = true }
            FillwordKidsDifficulty.HARD -> { size = 11; count = 8; allowVertical = true }
            FillwordKidsDifficulty.SUPER_HARD -> { size = 13; count = 10; allowVertical = true }
        }

        val selectedWords = FillwordKidsData.allWords.shuffled().take(count)
        val stringWords = selectedWords.map { it.word }
        
        // Генерируем сетку (горизонталь, и вертикаль если разрешено)
        var grid = FillwordGenerator.generate(stringWords, size, allowBackwards = false, allowVertical = allowVertical)

        // Ловушки
        if (config.addTraps) {
            grid = addTraps(grid)
        }

        return GridData(grid, selectedWords)
    }

    private fun addTraps(grid: FillwordGrid): FillwordGrid {
        val newLetters = Array(grid.size) { y -> Array(grid.size) { x -> grid.letters[y][x] } }
        val vowels = listOf('А', 'О', 'У', 'Ы', 'Э')

        // Ищем слово ЛИСА и делаем ЛИСО, если есть место
        for (pw in grid.placedWords) {
            if (pw.word == "ЛИСА") {
                val lastX = pw.startX + (pw.word.length - 1) * pw.dx
                val lastY = pw.startY + (pw.word.length - 1) * pw.dy
                val nextX = lastX + pw.dx
                val nextY = lastY + pw.dy
                if (nextX in 0 until grid.size && nextY in 0 until grid.size) {
                    newLetters[nextY][nextX] = vowels.random()
                }
            }
        }

        return FillwordGrid(newLetters, grid.size, grid.placedWords)
    }

    private fun drawPage(canvas: Canvas, config: FillwordKidsConfig, data: GridData, isAnswer: Boolean) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // ШАПКА
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        canvas.drawText("Дата:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)
        
        val countStr = "Слов: ${data.words.size}"
        val countWidth = headerPaint.measureText(countStr)
        val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - countWidth/2f - 10f, 25f, PAGE_WIDTH/2f + countWidth/2f + 10f, 45f), 5f, 5f, rectPaint)
        canvas.drawText(countStr, PAGE_WIDTH/2f - countWidth/2f, 40f, headerPaint)
        
        canvas.drawText("Время:", PAGE_WIDTH - 120f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // ЗАГОЛОВОК
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 28f; color = textColor; 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Филворд", PAGE_WIDTH / 2f, 90f, titlePaint)
        
        val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("слова спрятаны слева направо и сверху вниз — найди и обведи", PAGE_WIDTH / 2f, 110f, subtitlePaint)

        // СЕТКА
        val tableSize = 400f
        val startY = 150f
        val cellW = tableSize / data.grid.size
        val cellH = tableSize / data.grid.size
        val tableLeft = (PAGE_WIDTH - tableSize) / 2f

        val gridPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }
        val letterPaint = Paint().apply { isAntiAlias = true; textSize = cellW * 0.6f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val foundPaint = Paint().apply { color = Color.argb(100, 76, 175, 80); style = Paint.Style.FILL }

        if (isAnswer) {
            for (pw in data.grid.placedWords) {
                for (i in pw.word.indices) {
                    val x = pw.startX + i * pw.dx
                    val y = pw.startY + i * pw.dy
                    val left = tableLeft + x * cellW
                    val top = startY + y * cellH
                    canvas.drawRect(left, top, left + cellW, top + cellH, foundPaint)
                }
            }
        }

        for (y in 0 until data.grid.size) {
            for (x in 0 until data.grid.size) {
                val left = tableLeft + x * cellW
                val top = startY + y * cellH
                
                canvas.drawRect(left, top, left + cellW, top + cellH, gridPaint)
                
                val letter = data.grid.letters[y][x].toString()
                val textBaseline = top + cellH / 2f - (letterPaint.descent() + letterPaint.ascent()) / 2f
                canvas.drawText(letter, left + cellW / 2f, textBaseline, letterPaint)
            }
        }

        // СПИСОК СЛОВ / ЭМОДЗИ
        val listStartY = startY + tableSize + 40f
        val listPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("🔍 Найди слова:", margin, listStartY, listPaint)
        
        var currentX = margin + 110f
        var currentY = listStartY
        val wordPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        val emojiPaint = Paint().apply { textSize = 20f; textAlign = Paint.Align.CENTER }
        val wordBoxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }

        for (wordItem in data.words) {
            val textToDraw = if (config.onlyPictures) wordItem.emoji else "${wordItem.emoji} ${wordItem.word}"
            val wordWidth = wordPaint.measureText(textToDraw) + 20f
            if (currentX + wordWidth > PAGE_WIDTH - margin) {
                currentX = margin + 110f
                currentY += 30f
            }
            
            val boxRect = RectF(currentX, currentY - 18f, currentX + wordWidth, currentY + 8f)
            canvas.drawRoundRect(boxRect, 5f, 5f, wordBoxPaint)
            
            if (config.onlyPictures) {
                canvas.drawText(wordItem.emoji, currentX + wordWidth / 2f, currentY, emojiPaint)
            } else {
                canvas.drawText(textToDraw, currentX + wordWidth / 2f, currentY, wordPaint)
            }
            
            currentX += wordWidth + 10f
        }
    }
}
