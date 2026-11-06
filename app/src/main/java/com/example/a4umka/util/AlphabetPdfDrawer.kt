package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.example.a4umka.R
import com.example.a4umka.data.*
import kotlin.math.ceil
import kotlin.math.sqrt

object AlphabetPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun drawLayout(canvas: Canvas, config: AlphabetConfig, context: Context) {
        val bgColor = when (config.color) {
            PosterColor.WHITE -> Color.WHITE
            PosterColor.GREEN -> Color.parseColor("#E8F5E9")
            PosterColor.BLUE -> Color.parseColor("#E3F2FD")
            PosterColor.PINK -> Color.parseColor("#FCE4EC")
            PosterColor.ORANGE -> Color.parseColor("#FFF3E0")
            PosterColor.BLACK_WHITE -> Color.WHITE
        }
        canvas.drawColor(bgColor)

        val isBW = config.color == PosterColor.BLACK_WHITE
        val mainTextColor = if (isBW) Color.BLACK else Color.parseColor("#FF7043")

        val font = if (config.isCursive) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

        val letters = AlphabetData.getLettersByRange(config.startLetterIndex, config.endLetterIndex, config.filter)
        if (letters.isEmpty()) return

        when (config.layoutType) {
            AlphabetLayoutType.POSTER -> drawPoster(canvas, config, letters, font, mainTextColor, isBW, context)
            AlphabetLayoutType.FLASHCARDS -> drawFlashcards(canvas, config, letters, font, mainTextColor, isBW, context)
            AlphabetLayoutType.COLORING -> drawColoring(canvas, config, letters, font, mainTextColor, isBW)
        }
    }

    private fun drawPoster(canvas: Canvas, config: AlphabetConfig, letters: List<AlphabetLetter>, font: Typeface, textColor: Int, isBW: Boolean, context: Context) {
        val count = letters.size
        val cols = ceil(sqrt(count.toFloat())).toInt()
        val rows = ceil(count.toFloat() / cols).toInt()

        val margin = 40f
        val availableWidth = PAGE_WIDTH - margin * 2
        val availableHeight = PAGE_HEIGHT - margin * 2 - 60f
        val cellWidth = availableWidth / cols
        val cellHeight = availableHeight / rows

        drawTitle(canvas, "Алфавит", font)

        val linePaint = Paint().apply { 
            this.color = if (isBW) Color.BLACK else Color.LTGRAY
            this.strokeWidth = 1f
            this.style = Paint.Style.STROKE 
        }

        letters.forEachIndexed { index, item ->
            val col = index % cols
            val row = index / cols
            val left = margin + col * cellWidth
            val top = margin + 60f + row * cellHeight

            canvas.drawRect(left, top, left + cellWidth, top + cellHeight, linePaint)
            drawCellContent(canvas, item, config, left, top, cellWidth, cellHeight, font, textColor, isBW, context)
        }
    }

    private fun drawFlashcards(canvas: Canvas, config: AlphabetConfig, letters: List<AlphabetLetter>, font: Typeface, textColor: Int, isBW: Boolean, context: Context) {
        val perPage = config.cardsPerPage
        val cols = if (perPage == 1) 1 else 2
        val rows = if (perPage == 1) 1 else perPage / 2

        val margin = 20f
        val cellWidth = (PAGE_WIDTH - margin * 2) / cols
        val cellHeight = (PAGE_HEIGHT - margin * 2) / rows
        
        val cutPaint = Paint().apply { 
            this.color = Color.GRAY
            this.strokeWidth = 2f
            this.style = Paint.Style.STROKE
            this.pathEffect = DashPathEffect(floatArrayOf(15f, 15f), 0f)
        }

        letters.take(perPage).forEachIndexed { index, item ->
            val col = index % cols
            val row = index / cols
            val left = margin + col * cellWidth
            val top = margin + row * cellHeight

            canvas.drawRect(left, top, left + cellWidth, top + cellHeight, cutPaint)
            drawCellContent(canvas, item, config, left, top, cellWidth, cellHeight, font, textColor, isBW, context)
        }
    }

    private fun drawColoring(canvas: Canvas, config: AlphabetConfig, letters: List<AlphabetLetter>, font: Typeface, textColor: Int, isBW: Boolean) {
        val item = letters.firstOrNull() ?: return
        val margin = 40f
        
        drawTitle(canvas, "Раскраска", font)

        val letterPaint = Paint().apply {
            this.isAntiAlias = true
            this.textSize = 300f
            this.color = textColor
            this.typeface = font
            this.textAlign = Paint.Align.CENTER
            this.style = Paint.Style.STROKE
            this.strokeWidth = 3f
        }

        val text = if (config.case == AlphabetCase.LOWERCASE) item.lowercase else item.uppercase
        canvas.drawText(text, PAGE_WIDTH / 2f, 350f, letterPaint)

        val boxTop = 450f
        val boxBottom = 750f
        val boxLeft = 100f
        val boxRight = PAGE_WIDTH - 100f
        
        val boxPaint = Paint().apply { 
            this.color = if (isBW) Color.BLACK else Color.GRAY
            this.style = Paint.Style.STROKE
            this.strokeWidth = 2f
            this.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, boxPaint)

        val hintPaint = Paint().apply {
            this.isAntiAlias = true
            this.textSize = 24f
            this.color = Color.GRAY
            this.textAlign = Paint.Align.CENTER
            this.typeface = Typeface.DEFAULT
        }
        canvas.drawText("Нарисуй: ${item.word}", PAGE_WIDTH / 2f, boxTop - 20f, hintPaint)
    }

    private fun drawCellContent(
        canvas: Canvas, item: AlphabetLetter, config: AlphabetConfig, 
        left: Float, top: Float, cellWidth: Float, cellHeight: Float, 
        font: Typeface, textColor: Int, isBW: Boolean, context: Context
    ) {
        val centerX = left + cellWidth / 2f
        
        // АДАПТИВНЫЙ РАЗМЕР БУКВ (если нет картинок и слов, буква становится огромной)
        val letterSizeMultiplier = if (!config.showImage && !config.showWord) 0.6f else 0.35f
        
        val letterPaint = Paint().apply {
            this.isAntiAlias = true
            this.textSize = minOf(cellWidth, cellHeight) * letterSizeMultiplier
            this.color = if (isBW) Color.BLACK else textColor
            this.typeface = font
            this.textAlign = Paint.Align.CENTER
            this.style = if (isBW) Paint.Style.STROKE else Paint.Style.FILL
            this.strokeWidth = if (isBW) 3f else 0f
        }

        val text = when (config.case) {
            AlphabetCase.UPPERCASE -> item.uppercase
            AlphabetCase.LOWERCASE -> item.lowercase
            AlphabetCase.BOTH -> "${item.uppercase} ${item.lowercase}"
        }
        
        // Если нет ни картинки, ни слова — центрируем букву ровно посередине
        val letterCenterY = if (!config.showImage && !config.showWord) {
            top + cellHeight / 2f
        } else {
            top + cellHeight * 0.35f
        }

        val fontMetrics = letterPaint.fontMetrics
        val baselineY = letterCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(text, centerX, baselineY, letterPaint)

        // ЛОГИКА ОТРИСОВКИ КАРТИНКИ И СЛОВА
        if (config.showImage || config.showWord) {
            val wordPaint = Paint().apply {
                this.isAntiAlias = true
                // Если картинки нет, слово можно сделать чуть больше
                this.textSize = minOf(cellWidth, cellHeight) * (if (!config.showImage) 0.25f else 0.15f)
                this.color = Color.DKGRAY
                this.typeface = Typeface.DEFAULT
                this.textAlign = Paint.Align.CENTER
            }

            var currentY = top + cellHeight * 0.65f

            if (config.showImage) {
                val picSize = minOf(cellWidth, cellHeight) * 0.25f
                val bitmap = loadImageFromAssets(context, item.uppercase.lowercase())
                
                if (bitmap != null) {
                    val rect = RectF(centerX - picSize / 2f, currentY - picSize / 2f, centerX + picSize / 2f, currentY + picSize / 2f)
                    canvas.drawBitmap(bitmap, null, rect, null)
                } else {
                    val picPaint = Paint().apply { this.color = if (isBW) Color.LTGRAY else Color.parseColor("#FFD4C2") }
                    canvas.drawCircle(centerX, currentY, picSize / 2f, picPaint)
                }
                currentY += cellHeight * 0.2f // Смещаем координату для слова вниз
            }

            if (config.showWord) {
                // Если картинки нет, слово рисуем чуть ниже центра
                val wordY = if (!config.showImage) top + cellHeight * 0.75f else currentY
                canvas.drawText(item.word, centerX, wordY, wordPaint)
            }
        }
    }

    private fun loadImageFromAssets(context: Context, letter: String): Bitmap? {
        return try {
            val inputStream = context.assets.open("images/$letter.png")
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }

    private fun drawTitle(canvas: Canvas, text: String, font: Typeface) {
        val titlePaint = Paint().apply {
            this.isAntiAlias = true
            this.textSize = 32f
            this.color = Color.DKGRAY
            this.typeface = font
            this.textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, PAGE_WIDTH / 2f, 60f, titlePaint)
    }
}
