package com.example.sovenok.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.example.sovenok.R
import com.example.sovenok.data.*
import kotlin.math.ceil
import kotlin.math.sqrt

object AlphabetPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f
    private const val PREVIEW_SCALE = 6  // 4K: 3570×5052 px

    // ============ ПРЕДПРОСМОТР (только ОДНА страница для FULL_PAGE_LETTER, чтобы не вешать UI) ============
    fun getPreviewPages(config: AlphabetConfig, context: Context): List<Bitmap> {
        val letters = AlphabetData.getLettersByRange(config.startLetterIndex, config.endLetterIndex, config.filter)
        if (letters.isEmpty()) return emptyList()

        // Для FULL_PAGE_LETTER — только первый лист как образец (PDF всё равно соберёт все буквы)
        if (config.layoutType == AlphabetLayoutType.FULL_PAGE_LETTER) {
            val firstLetter = letters.first()
            val bmp = Bitmap.createBitmap(
                (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
                (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bmp)
            canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
            drawLayoutForLetter(canvas, config, firstLetter, context)
            return listOf(bmp)
        }

        // Для остальных — одна страница
        val bmp = Bitmap.createBitmap(
            (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
            (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
        drawLayout(canvas, config, context)
        return listOf(bmp)
    }

    // ============ ОБЩАЯ ОТРИСОВКА ОДНОЙ СТРАНИЦЫ ============
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
            AlphabetLayoutType.FULL_PAGE_LETTER -> {
                // На одну страницу — только первая буква (для предпросмотра одной страницей)
                drawFullPageLetter(canvas, config, letters.first(), font, mainTextColor, isBW, context)
            }
        }
    }

    // ============ ОТРИСОВКА ОДНОЙ КОНКРЕТНОЙ БУКВЫ (для FULL_PAGE_LETTER) ============
    fun drawLayoutForLetter(canvas: Canvas, config: AlphabetConfig, letter: AlphabetLetter, context: Context) {
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

        drawFullPageLetter(canvas, config, letter, font, mainTextColor, isBW, context)
    }

    // ============ ПЛЕНАЯ БУКВА НА ВЕСЬ ЛИСТ (контурная) ============
    private fun drawFullPageLetter(
        canvas: Canvas, config: AlphabetConfig, item: AlphabetLetter,
        font: Typeface, textColor: Int, isBW: Boolean, context: Context
    ) {
        val centerX = PAGE_WIDTH / 2f

        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 28f; color = if (isBW) Color.BLACK else Color.DKGRAY
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Буква на весь лист", centerX, 60f, titlePaint)

        // Подзаголовок
        val subPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.GRAY
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER
        }
        canvas.drawText("обведи, раскрась или вырежи", centerX, 82f, subPaint)

        // ОГРОМНАЯ КОНТУРНАЯ БУКВА
        // Учитываем выбранный регистр: UPPERCASE → заглавная, LOWERCASE → строчная, BOTH → заглавная (контурная)
        val bigLetter = when (config.case) {
            AlphabetCase.UPPERCASE -> item.uppercase
            AlphabetCase.LOWERCASE -> item.lowercase
            AlphabetCase.BOTH -> item.uppercase
        }

        val letterPaint = Paint().apply {
            isAntiAlias = true
            textSize = 460f
            color = if (isBW) Color.BLACK else textColor
            typeface = font
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        // Центр контурной буквы (учитываем её высоту)
        val fontMetrics = letterPaint.fontMetrics
        val letterCenterY = 380f
        val baselineY = letterCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(bigLetter, centerX, baselineY, letterPaint)

        // НИЖНЯЯ ЗОНА: маленькая строчная буква (если BOTH), картинка и слово
        val bottomCenterY = 720f

        // Строчная буква (только в режиме BOTH)
        var sideOffset = 0f
        if (config.case == AlphabetCase.BOTH) {
            val smallLetterPaint = Paint().apply {
                isAntiAlias = true; textSize = 60f
                color = if (isBW) Color.BLACK else textColor
                typeface = font; textAlign = Paint.Align.CENTER
                style = Paint.Style.FILL
            }
            canvas.drawText(item.lowercase, centerX - 110f, bottomCenterY + 20f, smallLetterPaint)
            sideOffset = 80f
        }

        // Картинка
        var imageWidth = 0f
        if (config.showImage) {
            val picSize = 120f
            val bitmap = loadImageFromAssets(context, item.uppercase.lowercase())
            val picCenterX = if (config.case == AlphabetCase.BOTH) centerX + sideOffset - 20f else centerX - 60f
            if (bitmap != null) {
                val rect = RectF(
                    picCenterX - picSize / 2f, bottomCenterY - picSize / 2f,
                    picCenterX + picSize / 2f, bottomCenterY + picSize / 2f
                )
                canvas.drawBitmap(bitmap, null, rect, null)
            } else {
                val picPaint = Paint().apply { color = if (isBW) Color.LTGRAY else Color.parseColor("#FFD4C2") }
                canvas.drawCircle(picCenterX, bottomCenterY, picSize / 2f, picPaint)
            }
            imageWidth = picSize
        }

        // Слово под картинкой
        if (config.showWord) {
            val wordPaint = Paint().apply {
                isAntiAlias = true; textSize = 24f
                color = if (isBW) Color.BLACK else Color.DKGRAY
                typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
            }
            val wordX = if (config.case == AlphabetCase.BOTH) centerX + sideOffset - 20f else centerX - 60f
            // Если нет картинки — слово по центру
            val finalWordX = if (config.showImage) wordX else centerX
            canvas.drawText(item.word.lowercase(), finalWordX, bottomCenterY + 100f, wordPaint)
        }
    }

    // ============ ПЛЕНАЯ СТРАНИЦА — ПЛЕНАЯ БУКВА (для PDF) ============
    fun drawFullPageLetterDocument(document: PdfDocument, config: AlphabetConfig, context: Context) {
        val letters = AlphabetData.getLettersByRange(config.startLetterIndex, config.endLetterIndex, config.filter)
        if (letters.isEmpty()) return

        val isBW = config.color == PosterColor.BLACK_WHITE
        val mainTextColor = if (isBW) Color.BLACK else Color.parseColor("#FF7043")
        val font = if (config.isCursive) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

        var pageNum = 1
        for (letter in letters) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            drawFullPageLetter(page.canvas, config, letter, font, mainTextColor, isBW, context)
            document.finishPage(page)
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

        // АДАПТИВНЫЙ РАЗМЕР БУКВ
        val letterSizeMultiplier = if (!config.showImage && !config.showWord) 0.6f else 0.4f

        val letterPaint = Paint().apply {
            this.isAntiAlias = true
            this.textSize = minOf(cellWidth, cellHeight) * letterSizeMultiplier
            this.color = if (isBW) Color.BLACK else textColor
            this.typeface = font
            this.textAlign = Paint.Align.CENTER
            this.style = Paint.Style.FILL
        }

        val text = when (config.case) {
            AlphabetCase.UPPERCASE -> item.uppercase
            AlphabetCase.LOWERCASE -> item.lowercase
            AlphabetCase.BOTH -> "${item.uppercase} ${item.lowercase}"
        }

        val letterCenterY = if (!config.showImage && !config.showWord) {
            top + cellHeight / 2f
        } else {
            top + cellHeight * 0.32f
        }

        val fontMetrics = letterPaint.fontMetrics
        val baselineY = letterCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(text, centerX, baselineY, letterPaint)

        // КАРТИНКА И СЛОВО — с увеличенной картинкой и уменьшенным словом
        if (config.showImage || config.showWord) {
            // УМЕНЬШЕННОЕ СЛОВО: было 0.15f/0.25f → стало 0.09f/0.13f
            val wordPaint = Paint().apply {
                this.isAntiAlias = true
                this.textSize = minOf(cellWidth, cellHeight) * (if (!config.showImage) 0.13f else 0.09f)
                this.color = Color.DKGRAY
                this.typeface = Typeface.DEFAULT
                this.textAlign = Paint.Align.CENTER
            }

            // УВЕЛИЧЕННАЯ КАРТИНКА: было 0.25f → стало 0.42f (≈ размер заглавной буквы)
            if (config.showImage) {
                val picSize = minOf(cellWidth, cellHeight) * 0.42f
                val picCenterY = top + cellHeight * 0.62f
                val bitmap = loadImageFromAssets(context, item.uppercase.lowercase())

                if (bitmap != null) {
                    val rect = RectF(centerX - picSize / 2f, picCenterY - picSize / 2f, centerX + picSize / 2f, picCenterY + picSize / 2f)
                    canvas.drawBitmap(bitmap, null, rect, null)
                } else {
                    val picPaint = Paint().apply { this.color = if (isBW) Color.LTGRAY else Color.parseColor("#FFD4C2") }
                    canvas.drawCircle(centerX, picCenterY, picSize / 2f, picPaint)
                }
            }

            // Слово внизу
            if (config.showWord) {
                val wordY = top + cellHeight * 0.92f
                // Для длинных слов проверяем ширину, при необходимости ещё уменьшаем
                val wordWidth = wordPaint.measureText(item.word)
                val maxWidth = cellWidth * 0.85f
                if (wordWidth > maxWidth) {
                    wordPaint.textSize = wordPaint.textSize * (maxWidth / wordWidth)
                }
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
