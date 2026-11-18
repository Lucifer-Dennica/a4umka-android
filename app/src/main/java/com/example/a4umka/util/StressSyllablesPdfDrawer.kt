package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.*

/**
 * Генератор листов «Ударение и слоги».
 *
 * Лист 1 (задание): эмодзи + слово с дефисами между слогами + пустой кружок + «слогов».
 * Лист 2 (ответы, опционально): то же, но ударение отмечено и кружок заполнен.
 */
object StressSyllablesPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    private val bgColor = Color.parseColor("#FAFAF5")
    private val cardColor = Color.WHITE
    private val borderColor = Color.parseColor("#D4D4C8")
    private val textColor = Color.parseColor("#333333")
    private val numBgColor = Color.parseColor("#4A7A40")
    private val titleColor = Color.parseColor("#2E5C8A")
    private val stressColor = Color.parseColor("#D84315")
    private val circleColor = Color.parseColor("#888888")

    private val sans = Typeface.create("sans-serif", Typeface.NORMAL)
    private val sansBold = Typeface.create("sans-serif", Typeface.BOLD)
    private val cursive = Typeface.create("cursive", Typeface.BOLD)

    fun generatePreview(config: StressSyllablesConfig, context: Context): List<Bitmap> {
        val words = StressSyllablesData.getRandomWords(config.wordsCount, config.randomSeed)
        val pages = mutableListOf<Bitmap>()

        // Лист с заданием
        val bmp1 = newBitmap()
        val canvas1 = Canvas(bmp1)
        drawWorksheet(canvas1, words, isAnswer = false)
        pages.add(bmp1)

        // Лист с ответами
        if (config.showAnswers) {
            val bmp2 = newBitmap()
            val canvas2 = Canvas(bmp2)
            drawWorksheet(canvas2, words, isAnswer = true)
            pages.add(bmp2)
        }
        return pages
    }

    fun drawDocument(document: PdfDocument, config: StressSyllablesConfig, context: Context) {
        var pageNum = 1
        val pages = generatePreview(config, context)
        pages.forEach { bmp ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            document.finishPage(page)
        }
    }

    private fun newBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(bgColor)
        return bmp
    }

    private fun drawWorksheet(canvas: Canvas, words: List<StressWord>, isAnswer: Boolean) {
        val margin = 40f

        // === Шапка ===
        drawHeader(canvas, margin, isAnswer)

        // === Карточки со словами ===
        val usableWidth = PAGE_WIDTH - margin * 2
        val startY = 150f
        // По 2 слова в ряд
        val cols = 2
        val gap = 12f
        val cardW = (usableWidth - gap * (cols - 1)) / cols
        val cardH = 85f

        val cardBg = Paint().apply { color = cardColor; style = Paint.Style.FILL }
        val cardBorder = Paint().apply {
            color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1f
        }
        val emojiPaint = Paint().apply { textSize = 36f; textAlign = Paint.Align.CENTER }
        val wordPaint = Paint().apply {
            isAntiAlias = true; textSize = 20f; color = textColor; typeface = sansBold
            textAlign = Paint.Align.LEFT
        }
        val stressMarkPaint = Paint().apply {
            isAntiAlias = true; textSize = 20f; color = stressColor; typeface = sansBold
            textAlign = Paint.Align.LEFT
        }
        val circlePaint = Paint().apply {
            color = circleColor; style = Paint.Style.STROKE; strokeWidth = 1.5f
        }
        val circleFillPaint = Paint().apply {
            color = numBgColor; style = Paint.Style.FILL
        }
        val numTextPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }
        val labelPaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = Color.GRAY; typeface = sans
            textAlign = Paint.Align.LEFT
        }
        val numBgPaint = Paint().apply { color = numBgColor; style = Paint.Style.FILL }
        val numCircleText = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }

        words.forEachIndexed { index, word ->
            val col = index % cols
            val row = index / cols
            val left = margin + col * (cardW + gap)
            val top = startY + row * (cardH + gap)
            val rect = RectF(left, top, left + cardW, top + cardH)

            canvas.drawRoundRect(rect, 10f, 10f, cardBg)
            canvas.drawRoundRect(rect, 10f, 10f, cardBorder)

            // Номер слова в кружке слева сверху
            val nCx = left + 16f
            val nCy = top + 16f
            canvas.drawCircle(nCx, nCy, 11f, numBgPaint)
            canvas.drawText("${index + 1}", nCx, nCy + 4f, numCircleText)

            // Эмодзи слева
            val emojiCx = left + 50f
            val emojiCy = top + cardH / 2f
            canvas.drawText(word.emoji, emojiCx, emojiCy + emojiPaint.textSize / 3f, emojiPaint)

            // Слово с дефисами между слогами — по центру карточки
            val syllableText = word.syllables.joinToString("-")
            val wordX = left + 80f
            val wordY = top + cardH / 2f + 7f

            if (isAnswer) {
                // В ответах: рисуем слоги по одному, над ударным слогом ставим знак ´
                drawWordWithStress(canvas, word, wordX, wordY, wordPaint, stressMarkPaint)
            } else {
                // В задании: просто слово с дефисами
                canvas.drawText(syllableText, wordX, wordY, wordPaint)
            }

            // Справа: кружок для количества слогов + "слогов"
            val circleCx = left + cardW - 55f
            val circleCy = top + cardH / 2f
            val circleR = 16f

            canvas.drawCircle(circleCx, circleCy, circleR, circlePaint)

            if (isAnswer) {
                // Заполненный кружок с числом
                canvas.drawCircle(circleCx, circleCy, circleR, circleFillPaint)
                canvas.drawText("${word.syllablesCount}", circleCx, circleCy + 5f, numTextPaint)
            }

            // Подпись "слогов"
            canvas.drawText("слогов", circleCx + circleR + 6f, circleCy + 4f, labelPaint)
        }
    }

    /**
     * Рисует слово по слогам с дефисами, над ударным слогом ставит знак ударения ´.
     */
    private fun drawWordWithStress(
        canvas: Canvas,
        word: StressWord,
        x: Float, y: Float,
        wordPaint: Paint,
        stressPaint: Paint
    ) {
        var currentX = x
        for (i in word.syllables.indices) {
            if (i > 0) {
                canvas.drawText("-", currentX, y, wordPaint)
                currentX += wordPaint.measureText("-")
            }
            val syllable = word.syllables[i]
            canvas.drawText(syllable, currentX, y, wordPaint)

            // Над ударным слогом рисуем ´
            if (i == word.stressedSyllable) {
                val syllableWidth = wordPaint.measureText(syllable)
                // Ставим знак ударения примерно над серединой слога
                val markX = currentX + syllableWidth / 2f - 4f
                val markY = y - 18f
                canvas.drawText("´", markX, markY, stressPaint)
            }

            currentX += wordPaint.measureText(syllable)
        }
    }

    private fun drawHeader(canvas: Canvas, margin: Float, isAnswer: Boolean) {
        // Имя / Дата
        val boxPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val boxBorder = Paint().apply {
            color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1f
        }
        val labelPaint = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = sans
        }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxBorder)
        canvas.drawText("Имя:", margin + 5f, 38f, labelPaint)
        canvas.drawLine(margin + 30f, 40f, margin + 110f, 40f, linePaint)

        // Центральная метка
        val centerBg = Paint().apply {
            color = Color.parseColor("#FFF3E0"); style = Paint.Style.FILL
        }
        val centerRect = RectF(PAGE_WIDTH / 2f - 80f, 25f, PAGE_WIDTH / 2f + 80f, 45f)
        canvas.drawRoundRect(centerRect, 5f, 5f, centerBg)
        val centerText = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = stressColor; typeface = sansBold
            textAlign = Paint.Align.CENTER
        }
        val label = if (isAnswer) "Ответы" else "Ударение и слоги"
        canvas.drawText(label, PAGE_WIDTH / 2f, 38f, centerText)

        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxBorder)
        canvas.drawText("Дата:", PAGE_WIDTH - margin - 115f, 38f, labelPaint)
        canvas.drawLine(PAGE_WIDTH - margin - 85f, 40f, PAGE_WIDTH - margin - 10f, 40f, linePaint)

        // Заголовок листа
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 26f; color = titleColor; typeface = cursive
            textAlign = Paint.Align.CENTER
        }
        val title = if (isAnswer) "Ответы" else "Ударение и слоги"
        canvas.drawText(title, PAGE_WIDTH / 2f, 80f, titlePaint)

        // Инструкция
        val subPaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = Color.GRAY; typeface = sans
            textAlign = Paint.Align.CENTER
        }
        val sub = if (isAnswer) "Для взрослого" else "Раздели на слоги, посчитай и поставь ударение"
        canvas.drawText(sub, PAGE_WIDTH / 2f, 100f, subPaint)

        // В ответах — вторая строка инструкции
        if (isAnswer) {
            canvas.drawText("Раздели на слоги, посчитай и поставь ударение", PAGE_WIDTH / 2f, 116f, subPaint)
        }
    }
}
