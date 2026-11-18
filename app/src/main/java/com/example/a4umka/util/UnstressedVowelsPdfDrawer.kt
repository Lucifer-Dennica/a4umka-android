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
 * Генератор листов «Безударные гласные в корне».
 *
 * Лист 1 (задание): правило + слова с пропуском + линия для проверочного слова.
 * Лист 2 (ответы, опционально): то же, но вставленная буква выделена красным + проверочное слово.
 *
 * Layout: 2 колонки, как на сайте-эталоне.
 */
object UnstressedVowelsPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    private val bgColor = Color.WHITE  // белый фон — экономия краски
    private val cardColor = Color.WHITE
    private val borderColor = Color.parseColor("#CCCCCC")
    private val textColor = Color.parseColor("#333333")
    private val numBgColor = Color.parseColor("#4A7A40")
    private val titleColor = Color.parseColor("#2E5C8A")
    private val answerColor = Color.parseColor("#D32F2F")  // красный для вставленной буквы
    private val checkWordColor = Color.parseColor("#1976D2")  // синий для проверочного слова
    private val ruleBgColor = Color.parseColor("#FFF8E1")  // светло-жёлтый для правила

    private val sans = Typeface.create("sans-serif", Typeface.NORMAL)
    private val sansBold = Typeface.create("sans-serif", Typeface.BOLD)
    private val cursive = Typeface.create("cursive", Typeface.BOLD)

    fun generatePreview(config: UnstressedVowelsConfig, context: Context): List<Bitmap> {
        val words = UnstressedVowelsData.getRandomWords(config.wordsCount, config.groups, config.randomSeed)
        val pages = mutableListOf<Bitmap>()

        val bmp1 = newBitmap()
        val canvas1 = Canvas(bmp1)
        drawWorksheet(canvas1, words, config, isAnswer = false)
        pages.add(bmp1)

        if (config.showAnswers) {
            val bmp2 = newBitmap()
            val canvas2 = Canvas(bmp2)
            drawWorksheet(canvas2, words, config, isAnswer = true)
            pages.add(bmp2)
        }
        return pages
    }

    fun drawDocument(document: PdfDocument, config: UnstressedVowelsConfig, context: Context) {
        var pageNum = 1
        val pages = generatePreview(config, context)
        // ИСПРАВЛЕНО: рендерим в 3× разрешении для высокого качества PDF
        val scale = 3
        pages.forEach { bmp ->
            val scaledBmp = Bitmap.createScaledBitmap(
                bmp,
                PAGE_WIDTH.toInt() * scale,
                PAGE_HEIGHT.toInt() * scale,
                true
            )
            val pageInfo = PdfDocument.PageInfo.Builder(
                PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++
            ).create()
            val page = document.startPage(pageInfo)
            // Рисуем масштабированный bitmap на canvas страницы, масштабируя обратно
            val srcRect = android.graphics.Rect(0, 0, scaledBmp.width, scaledBmp.height)
            val dstRect = android.graphics.RectF(0f, 0f, PAGE_WIDTH, PAGE_HEIGHT)
            page.canvas.drawBitmap(scaledBmp, srcRect, dstRect, null)
            document.finishPage(page)
        }
    }

    private fun newBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(bgColor)
        return bmp
    }

    private fun drawWorksheet(canvas: Canvas, words: List<UnstressedWord>, config: UnstressedVowelsConfig, isAnswer: Boolean) {
        val margin = 40f

        drawHeader(canvas, margin, isAnswer)

        // === Блок с правилом (только на листе задания) ===
        if (!isAnswer) {
            drawRuleBox(canvas, margin, 120f, PAGE_WIDTH - margin * 2, config)
        }

        // === Слова ===
        val usableWidth = PAGE_WIDTH - margin * 2
        val startY = if (!isAnswer) 205f else 130f
        val cols = 2
        val gap = 10f
        val cellW = (usableWidth - gap * (cols - 1)) / cols
        val cellH = 55f

        val cardBg = Paint().apply { color = cardColor; style = Paint.Style.FILL }
        val cardBorder = Paint().apply {
            color = borderColor; style = Paint.Style.STROKE; strokeWidth = 0.8f
        }
        val numBgPaint = Paint().apply { color = numBgColor; style = Paint.Style.FILL }
        val numCircleText = Paint().apply {
            isAntiAlias = true; textSize = 9f; color = Color.WHITE
            typeface = sansBold; textAlign = Paint.Align.CENTER
        }
        val wordPaint = Paint().apply {
            isAntiAlias = true; textSize = 16f; color = textColor; typeface = sansBold
            textAlign = Paint.Align.LEFT
        }
        val answerLetterPaint = Paint().apply {
            isAntiAlias = true; textSize = 16f; color = answerColor; typeface = sansBold
            textAlign = Paint.Align.LEFT
        }
        val checkWordPaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = checkWordColor; typeface = sans
            textAlign = Paint.Align.LEFT
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#CCCCCC"); strokeWidth = 1f
        }
        val labelPaint = Paint().apply {
            isAntiAlias = true; textSize = 9f; color = Color.GRAY; typeface = sans
            textAlign = Paint.Align.LEFT
        }

        words.forEachIndexed { index, word ->
            val col = index % cols
            val row = index / cols
            val left = margin + col * (cellW + gap)
            val top = startY + row * (cellH + gap)
            val rect = RectF(left, top, left + cellW, top + cellH)

            canvas.drawRoundRect(rect, 6f, 6f, cardBg)
            canvas.drawRoundRect(rect, 6f, 6f, cardBorder)

            // Номер
            val nCx = left + 14f
            val nCy = top + 16f
            canvas.drawCircle(nCx, nCy, 9f, numBgPaint)
            canvas.drawText("${index + 1}", nCx, nCy + 3f, numCircleText)

            // Слово с пропуском
            val wordX = left + 30f
            val wordY = top + 22f
            drawWordWithGap(canvas, word, wordX, wordY, wordPaint, answerLetterPaint, isAnswer)

            // Линия для проверочного слова
            val lineY = top + cellH - 14f
            canvas.drawLine(wordX, lineY, left + cellW - 10f, lineY, linePaint)
            if (isAnswer && config.taskType == VowelTaskType.INSERT_AND_CHECK) {
                canvas.drawText(word.checkWord, wordX, lineY - 3f, checkWordPaint)
            } else if (!isAnswer && config.taskType == VowelTaskType.INSERT_AND_CHECK) {
                canvas.drawText("проверочное:", wordX, lineY - 3f, labelPaint)
            }
        }
    }

    private fun drawWordWithGap(
        canvas: Canvas,
        word: UnstressedWord,
        x: Float, y: Float,
        wordPaint: Paint,
        answerPaint: Paint,
        isAnswer: Boolean
    ) {
        val fullWord = word.word
        var currentX = x
        // Размер клетки для пропущенной буквы
        val cellSize = 14f
        val cellGap = 1f

        for (i in fullWord.indices) {
            val ch = fullWord[i]
            if (i == word.missingIndex) {
                if (isAnswer) {
                    // В ответах: рисуем правильную букву красным в клетке
                    val cellRect = RectF(currentX, y - cellSize + 2f, currentX + cellSize, y + 2f)
                    val cellPaint = Paint().apply {
                        color = Color.parseColor("#FFF3E0"); style = Paint.Style.FILL
                    }
                    val cellBorder = Paint().apply {
                        color = answerColor; style = Paint.Style.STROKE; strokeWidth = 1f
                    }
                    canvas.drawRect(cellRect, cellPaint)
                    canvas.drawRect(cellRect, cellBorder)
                    // Буква по центру клетки
                    val letterPaint = Paint(answerPaint).apply { textAlign = Paint.Align.CENTER }
                    canvas.drawText(ch.toString(), currentX + cellSize / 2f, y, letterPaint)
                    currentX += cellSize + cellGap
                } else {
                    // В задании: рисуем красивый прямоугольник-клетку
                    val cellRect = RectF(currentX, y - cellSize + 2f, currentX + cellSize, y + 2f)
                    val cellPaint = Paint().apply {
                        color = Color.parseColor("#FAFAF5"); style = Paint.Style.FILL
                    }
                    val cellBorder = Paint().apply {
                        color = Color.parseColor("#888888"); style = Paint.Style.STROKE; strokeWidth = 1f
                    }
                    canvas.drawRect(cellRect, cellPaint)
                    canvas.drawRect(cellRect, cellBorder)
                    currentX += cellSize + cellGap
                }
            } else {
                canvas.drawText(ch.toString(), currentX, y, wordPaint)
                currentX += wordPaint.measureText(ch.toString())
            }
        }
    }

    private fun drawRuleBox(canvas: Canvas, x: Float, y: Float, width: Float, config: UnstressedVowelsConfig) {
        val ruleBg = Paint().apply { color = ruleBgColor; style = Paint.Style.FILL }
        val ruleBorder = Paint().apply {
            color = Color.parseColor("#FFD54F"); style = Paint.Style.STROKE; strokeWidth = 1f
        }
        val rect = RectF(x, y, x + width, y + 70f)
        canvas.drawRoundRect(rect, 8f, 8f, ruleBg)
        canvas.drawRoundRect(rect, 8f, 8f, ruleBorder)

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = Color.parseColor("#E65100"); typeface = sansBold
        }
        canvas.drawText("Как проверить:", x + 12f, y + 20f, titlePaint)

        val textPaint = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = textColor; typeface = sans
        }
        canvas.drawText("Измени слово или подбери однокоренное, чтобы гласная стала ударной:", x + 12f, y + 38f, textPaint)

        val examplePaint = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = checkWordColor; typeface = sansBold
        }
        canvas.drawText("вода — во́ды    леса — ле́с    кричать — кри́к", x + 12f, y + 56f, examplePaint)
    }

    private fun drawHeader(canvas: Canvas, margin: Float, isAnswer: Boolean) {
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

        val centerBg = Paint().apply {
            color = Color.parseColor("#FFF3E0"); style = Paint.Style.FILL
        }
        val centerRect = RectF(PAGE_WIDTH / 2f - 80f, 25f, PAGE_WIDTH / 2f + 80f, 45f)
        canvas.drawRoundRect(centerRect, 5f, 5f, centerBg)
        val centerText = Paint().apply {
            isAntiAlias = true; textSize = 10f; color = answerColor; typeface = sansBold
            textAlign = Paint.Align.CENTER
        }
        val label = if (isAnswer) "Ответы" else "Безударные гласные"
        canvas.drawText(label, PAGE_WIDTH / 2f, 38f, centerText)

        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxBorder)
        canvas.drawText("Дата:", PAGE_WIDTH - margin - 115f, 38f, labelPaint)
        canvas.drawLine(PAGE_WIDTH - margin - 85f, 40f, PAGE_WIDTH - margin - 10f, 40f, linePaint)

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 22f; color = titleColor; typeface = cursive
            textAlign = Paint.Align.CENTER
        }
        val title = if (isAnswer) "Ответы" else "Безударные гласные в корне"
        canvas.drawText(title, PAGE_WIDTH / 2f, 72f, titlePaint)

        if (!isAnswer) {
            val subPaint = Paint().apply {
                isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = sans
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Вставь пропущенную букву и запиши проверочное слово", PAGE_WIDTH / 2f, 90f, subPaint)
        } else {
            val subPaint = Paint().apply {
                isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = sans
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Для взрослого", PAGE_WIDTH / 2f, 90f, subPaint)
        }
    }
}
