package com.example.sovenok.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.sovenok.data.*

object SoundAutomationPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    private val bgColor = Color.parseColor("#FAFAF5")
    private val borderColor = Color.parseColor("#D4D4C8")
    private val textColor = Color.parseColor("#333333")
    private val highlightColor = Color.parseColor("#D84315")
    private val greenCircleColor = Color.parseColor("#4A7A40")
    
    private val safeTypeface = Typeface.create("sans-serif", Typeface.NORMAL)
    private val safeTypefaceBold = Typeface.create("sans-serif", Typeface.BOLD)
    private val cursiveTypeface = Typeface.create("cursive", Typeface.BOLD)

    fun generatePreview(config: SoundAutomationConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()

        if (config.mode == SpeechMode.PAIRS) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawPairsPage(canvas, config)
            pages.add(bmp)
            if (config.showAnswers) {
                val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                val ansCanvas = Canvas(ansBmp)
                ansCanvas.drawColor(bgColor)
                drawPairsAnswers(ansCanvas, config)
                pages.add(ansBmp)
            }
            return pages
        }

        val data = SoundAutomationData.getData(config.sound)

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.SYLLABLES) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawSyllablesPage(canvas, config, data.syllables)
            pages.add(bmp)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WORDS) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawWordsPage(canvas, config, data)
            pages.add(bmp)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WHERE_IS_SOUND) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawWhereIsSoundPage(canvas, config, data.whereIsSound, isAnswer = false)
            pages.add(bmp)

            if (config.showAnswers) {
                val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                val ansCanvas = Canvas(ansBmp)
                ansCanvas.drawColor(bgColor)
                drawWhereIsSoundPage(ansCanvas, config, data.whereIsSound, isAnswer = true)
                pages.add(ansBmp)
            }
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.PHRASES) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawPhrasesPage(canvas, config, data.phrases)
            pages.add(bmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: SoundAutomationConfig, context: Context) {
        var pageNum = 1

        fun startNewPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(bgColor)
            return page
        }

        if (config.mode == SpeechMode.PAIRS) {
            val page = startNewPage()
            drawPairsPage(page.canvas, config)
            document.finishPage(page)
            if (config.showAnswers) {
                val ansPage = startNewPage()
                drawPairsAnswers(ansPage.canvas, config)
                document.finishPage(ansPage)
            }
            return
        }

        val data = SoundAutomationData.getData(config.sound)

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.SYLLABLES) {
            val page = startNewPage()
            drawSyllablesPage(page.canvas, config, data.syllables)
            document.finishPage(page)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WORDS) {
            val page = startNewPage()
            drawWordsPage(page.canvas, config, data)
            document.finishPage(page)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WHERE_IS_SOUND) {
            val page = startNewPage()
            drawWhereIsSoundPage(page.canvas, config, data.whereIsSound, isAnswer = false)
            document.finishPage(page)

            if (config.showAnswers) {
                val ansPage = startNewPage()
                drawWhereIsSoundPage(ansPage.canvas, config, data.whereIsSound, isAnswer = true)
                document.finishPage(ansPage)
            }
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.PHRASES) {
            val page = startNewPage()
            drawPhrasesPage(page.canvas, config, data.phrases)
            document.finishPage(page)
        }
    }

    private fun drawHeader(canvas: Canvas, config: SoundAutomationConfig, title: String, sub: String) {
        val margin = 40f
        val textSize = 10f
        
        val boxPaint = Paint()
        boxPaint.color = Color.WHITE
        boxPaint.style = Paint.Style.FILL
        
        val boxBorder = Paint()
        boxBorder.color = borderColor
        boxBorder.style = Paint.Style.STROKE
        boxBorder.strokeWidth = 1f
        
        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxBorder)
        
        val nameTextPaint = Paint()
        nameTextPaint.textSize = textSize
        nameTextPaint.color = Color.GRAY
        nameTextPaint.isAntiAlias = true
        nameTextPaint.typeface = safeTypeface
        canvas.drawText("Имя:", margin + 5f, 38f, nameTextPaint)
        
        val linePaint = Paint()
        linePaint.color = Color.LTGRAY
        linePaint.strokeWidth = 1f
        canvas.drawLine(margin + 30f, 40f, margin + 110f, 40f, linePaint)

        val centerRect = RectF(PAGE_WIDTH/2f - 60f, 25f, PAGE_WIDTH/2f + 60f, 45f)
        val centerBgPaint = Paint()
        centerBgPaint.color = Color.parseColor("#C8E6C9")
        centerBgPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(centerRect, 5f, 5f, centerBgPaint)
        
        val soundTextPaint = Paint()
        soundTextPaint.textSize = textSize
        soundTextPaint.color = Color.DKGRAY
        soundTextPaint.isAntiAlias = true
        soundTextPaint.typeface = safeTypefaceBold
        canvas.drawText("Звук ${config.sound.letter}", PAGE_WIDTH/2f - 20f, 38f, soundTextPaint)

        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxBorder)
        canvas.drawText("Дата:", PAGE_WIDTH - margin - 115f, 38f, nameTextPaint)
        canvas.drawLine(PAGE_WIDTH - margin - 85f, 40f, PAGE_WIDTH - margin - 10f, 40f, linePaint)

        val titlePaint = Paint()
        titlePaint.isAntiAlias = true
        titlePaint.textSize = 28f
        titlePaint.color = greenCircleColor
        titlePaint.typeface = cursiveTypeface
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText(title, PAGE_WIDTH / 2f, 80f, titlePaint)

        val subPaint = Paint()
        subPaint.isAntiAlias = true
        subPaint.textSize = 10f
        subPaint.color = Color.GRAY
        subPaint.textAlign = Paint.Align.CENTER
        subPaint.typeface = Typeface.create(safeTypeface, Typeface.ITALIC)
        canvas.drawText(sub, PAGE_WIDTH / 2f, 100f, subPaint)
    }

    // Вспомогательная функция для отрисовки текста с выделенной буквой (без пробелов).
    // ИСПРАВЛЕНО: всегда используем LEFT-align внутри функции, иначе при CENTER-align
    // каждая часть (prefix/target/suffix) рисуется ЦЕНТРОМ вокруг своей точки x,
    // и узкие соседние буквы (Т, К, Б, В) «наезжают» на выделенную букву Р.
    private fun drawHighlightedText(canvas: Canvas, text: String, highlightIndex: Int, x: Float, y: Float, basePaint: Paint, highlightPaint: Paint) {
        // Локальные копии с LEFT-align — не трогаем выравнивание у внешнего paint.
        val leftBase = Paint(basePaint).apply { textAlign = Paint.Align.LEFT }
        val leftHighlight = Paint(highlightPaint).apply { textAlign = Paint.Align.LEFT }

        if (highlightIndex < 0 || highlightIndex >= text.length) {
            val w = leftBase.measureText(text)
            canvas.drawText(text, x - w / 2f, y, leftBase)
            return
        }
        val prefix = text.substring(0, highlightIndex)
        val target = text.substring(highlightIndex, highlightIndex + 1)
        val suffix = text.substring(highlightIndex + 1)

        val w1 = leftBase.measureText(prefix)
        val w2 = leftHighlight.measureText(target)
        val w3 = leftBase.measureText(suffix)
        val totalWidth = w1 + w2 + w3

        val startX = x - totalWidth / 2f

        canvas.drawText(prefix, startX, y, leftBase)
        canvas.drawText(target, startX + w1, y, leftHighlight)
        canvas.drawText(suffix, startX + w1 + w2, y, leftBase)
    }

    private fun drawSyllablesPage(canvas: Canvas, config: SoundAutomationConfig, syllables: List<Pair<String, Int>>) {
        drawHeader(canvas, config, "Звук ${config.sound.letter}: слоги", "Прочитай или повтори за взрослым каждый слог три раза. После каждого раза закрась кружок")
        
        val margin = 40f
        var currentY = 130f
        
        val blockTitles = listOf("Слог в начале", "Слог в конце", "Между гласными", "С другим согласным", "Цепочки")
        val startIndexes = listOf(0, 4, 8, 12, 16)
        val endIndexes = listOf(4, 8, 12, 16, 18)

        val blockBgPaint = Paint()
        blockBgPaint.color = Color.WHITE
        blockBgPaint.style = Paint.Style.FILL

        val blockBorderPaint = Paint()
        blockBorderPaint.color = borderColor
        blockBorderPaint.style = Paint.Style.STROKE
        blockBorderPaint.strokeWidth = 1.5f
        
        val circleNumBg = Paint()
        circleNumBg.color = greenCircleColor
        circleNumBg.style = Paint.Style.FILL

        val circleNumText = Paint()
        circleNumText.isAntiAlias = true
        circleNumText.textSize = 12f
        circleNumText.color = Color.WHITE
        circleNumText.typeface = safeTypefaceBold
        circleNumText.textAlign = Paint.Align.CENTER

        val blockTitlePaint = Paint()
        blockTitlePaint.isAntiAlias = true
        blockTitlePaint.textSize = 12f
        blockTitlePaint.color = Color.DKGRAY
        blockTitlePaint.typeface = safeTypefaceBold

        val cardBgPaint = Paint()
        cardBgPaint.color = Color.WHITE
        cardBgPaint.style = Paint.Style.FILL

        val cardBorderPaint = Paint()
        cardBorderPaint.color = Color.parseColor("#E0E0E0")
        cardBorderPaint.style = Paint.Style.STROKE
        cardBorderPaint.strokeWidth = 1f
        
        // УМЕНЬШИЛИ ШРИФТ СЛОГОВ, ЧТОБЫ ОНИ НЕ СЛИПАЛИСЬ
        val textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.textSize = 20f // Было 24f, теперь влезает ровно
        textPaint.color = textColor
        textPaint.typeface = safeTypefaceBold
        textPaint.textAlign = Paint.Align.CENTER

        val highlightPaint = Paint(textPaint)
        highlightPaint.color = highlightColor

        val circlePaint = Paint()
        circlePaint.color = Color.LTGRAY
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 1.5f

        for (bIndex in blockTitles.indices) {
            val title = blockTitles[bIndex]
            val startIdx = startIndexes[bIndex]
            val endIdx = endIndexes[bIndex]
            
            if (startIdx >= syllables.size) continue
            val actualEndIdx = if (endIdx > syllables.size) syllables.size else endIdx
            val itemsCount = actualEndIdx - startIdx
            if (itemsCount <= 0) continue
            
            val isTwoRows = itemsCount > 4
            val blockHeight = if (bIndex == 4) 80f else if (isTwoRows) 115f else 80f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText((bIndex + 1).toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            val cols = if (bIndex == 4) 2 else 4
            val itemWidth = (PAGE_WIDTH - margin * 2 - 20f) / cols
            
            for (iIdx in 0 until itemsCount) {
                val item = syllables[startIdx + iIdx]
                val col = iIdx % cols
                val row = iIdx / cols
                
                val cardLeft = margin + 10f + col * itemWidth
                val cardTop = currentY + 30f + row * 55f
                val cardRight = cardLeft + itemWidth - 5f
                val cardBottom = cardTop + 45f

                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBgPaint)
                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBorderPaint)
                
                val cx = cardLeft + (cardRight - cardLeft) / 2f
                val cy = cardTop + (cardBottom - cardTop) / 2f

                val currentTextPaint = if (bIndex == 4) Paint(textPaint).also { it.textSize = 16f } else textPaint
                val currentHighlightPaint = if (bIndex == 4) Paint(highlightPaint).also { it.textSize = 16f } else highlightPaint

                drawHighlightedText(canvas, item.first, item.second, cx, cy + 6f, currentTextPaint, currentHighlightPaint)
                
                val circleRadius = 4f
                val circleSpacing = 12f
                val startCircleX = cx - circleSpacing
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * circleSpacing, cy + 20f, circleRadius, circlePaint)
                }
            }
            currentY += blockHeight + 10f
        }
    }

    private fun drawWordsPage(canvas: Canvas, config: SoundAutomationConfig, data: SoundData) {
        drawHeader(canvas, config, "Звук ${config.sound.letter} в словах", "Назови картинку чётко, выделяя звук ${config.sound.letter}. Клеточка показывает, где он спрятался")
        
        val margin = 40f
        var currentY = 130f

        val sections = listOf(
            "Звук ${config.sound.letter} в начале слова" to data.wordsStart,
            "Звук ${config.sound.letter} в середине слова" to data.wordsMiddle,
            "Звук ${config.sound.letter} в конце слова" to data.wordsEnd
        )

        val blockBgPaint = Paint()
        blockBgPaint.color = Color.WHITE
        blockBgPaint.style = Paint.Style.FILL

        val blockBorderPaint = Paint()
        blockBorderPaint.color = borderColor
        blockBorderPaint.style = Paint.Style.STROKE
        blockBorderPaint.strokeWidth = 1.5f
        
        val circleNumBg = Paint()
        circleNumBg.color = greenCircleColor
        circleNumBg.style = Paint.Style.FILL

        val circleNumText = Paint()
        circleNumText.isAntiAlias = true
        circleNumText.textSize = 12f
        circleNumText.color = Color.WHITE
        circleNumText.typeface = safeTypefaceBold
        circleNumText.textAlign = Paint.Align.CENTER

        val blockTitlePaint = Paint()
        blockTitlePaint.isAntiAlias = true
        blockTitlePaint.textSize = 12f
        blockTitlePaint.color = Color.DKGRAY
        blockTitlePaint.typeface = safeTypefaceBold

        val cardBgPaint = Paint()
        cardBgPaint.color = Color.WHITE
        cardBgPaint.style = Paint.Style.FILL

        val cardBorderPaint = Paint()
        cardBorderPaint.color = Color.parseColor("#E0E0E0")
        cardBorderPaint.style = Paint.Style.STROKE
        cardBorderPaint.strokeWidth = 1f
        
        val wordPaint = Paint()
        wordPaint.isAntiAlias = true
        wordPaint.textSize = 12f 
        wordPaint.color = textColor
        wordPaint.typeface = safeTypefaceBold
        wordPaint.textAlign = Paint.Align.CENTER

        val highlightPaint = Paint(wordPaint)
        highlightPaint.color = highlightColor

        val emojiPaint = Paint()
        emojiPaint.textSize = 36f
        emojiPaint.textAlign = Paint.Align.CENTER
        
        val circlePaint = Paint()
        circlePaint.color = Color.LTGRAY
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 1.5f

        for (sIndex in sections.indices) {
            val title = sections[sIndex].first
            val words = sections[sIndex].second
            if (words.isEmpty()) continue
            val blockHeight = 160f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText((sIndex + 1).toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            val indicatorX = PAGE_WIDTH - margin - 40f
            val indicatorY = currentY + 10f
            for (i in 0 until 3) {
                val rect = RectF(indicatorX + i * 12f, indicatorY, indicatorX + i * 12f + 10f, indicatorY + 10f)
                if (i == sIndex) {
                    val fillPaint = Paint()
                    fillPaint.color = Color.parseColor("#FFB74D")
                    fillPaint.style = Paint.Style.FILL
                    canvas.drawRoundRect(rect, 3f, 3f, fillPaint)
                } else {
                    val strokePaint = Paint()
                    strokePaint.color = Color.WHITE
                    strokePaint.style = Paint.Style.STROKE
                    strokePaint.strokeWidth = 1f
                    canvas.drawRoundRect(rect, 3f, 3f, strokePaint)
                }
            }

            val itemWidth = (PAGE_WIDTH - margin * 2 - 20f) / 4
            
            for (iIndex in words.indices) {
                val item = words[iIndex]
                val col = iIndex % 4
                val cardLeft = margin + 10f + col * itemWidth
                val cardTop = currentY + 30f
                val cardRight = cardLeft + itemWidth - 5f
                val cardBottom = cardTop + 120f

                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBgPaint)
                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBorderPaint)
                
                val cx = cardLeft + (cardRight - cardLeft) / 2f
                
                canvas.drawText(item.emoji, cx, cardTop + 50f, emojiPaint)
                drawHighlightedText(canvas, item.word, item.highlightIndex, cx, cardTop + 75f, wordPaint, highlightPaint)

                val boxW = 16f
                val boxH = 8f
                val startBoxX = cx - (boxW * 3 + 2f) / 2f
                val boxY = cardTop + 85f
                for (i in 0 until 3) {
                    val r = RectF(startBoxX + i * (boxW + 1f), boxY, startBoxX + i * (boxW + 1f) + boxW, boxY + boxH)
                    if (i == item.position) {
                        val fillPaint = Paint()
                        fillPaint.color = Color.parseColor("#FFB74D")
                        fillPaint.style = Paint.Style.FILL
                        canvas.drawRoundRect(r, 2f, 2f, fillPaint)
                    } else {
                        val strokePaint = Paint()
                        strokePaint.color = Color.LTGRAY
                        strokePaint.style = Paint.Style.STROKE
                        strokePaint.strokeWidth = 1f
                        canvas.drawRoundRect(r, 2f, 2f, strokePaint)
                    }
                }

                val startCircleX = cx - 12f
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * 12f, cardTop + 105f, 4f, circlePaint)
                }
            }
            currentY += blockHeight + 10f
        }
    }

    private fun drawWhereIsSoundPage(canvas: Canvas, config: SoundAutomationConfig, words: List<AutoWord>, isAnswer: Boolean) {
        val margin = 40f
        
        if (isAnswer) {
            canvas.drawColor(bgColor)
            val titlePaint = Paint()
            titlePaint.isAntiAlias = true
            titlePaint.textSize = 28f
            titlePaint.color = greenCircleColor
            titlePaint.typeface = cursiveTypeface
            titlePaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Ответы", PAGE_WIDTH / 2f, 60f, titlePaint)
            
            val subPaint = Paint()
            subPaint.isAntiAlias = true
            subPaint.textSize = 11f
            subPaint.color = Color.GRAY
            subPaint.textAlign = Paint.Align.CENTER
            subPaint.typeface = Typeface.create(safeTypeface, Typeface.ITALIC)
            canvas.drawText("Для взрослого", PAGE_WIDTH / 2f, 80f, subPaint)

            val headerTextPaint = Paint()
            headerTextPaint.isAntiAlias = true
            headerTextPaint.textSize = 14f
            headerTextPaint.color = Color.DKGRAY
            headerTextPaint.typeface = safeTypefaceBold
            canvas.drawText("Где живёт звук ${config.sound.letter}?", margin, 120f, headerTextPaint)

            // === НОВОЕ: поддержка 1 или 2 столбцов ===
            val columns = if (config.answerColumns >= 2) 2 else 1
            val gap = 10f
            val usableWidth = PAGE_WIDTH - margin * 2
            val colWidth = if (columns == 2) (usableWidth - gap) / 2 else usableWidth
            val itemsCount = words.size
            val rowsInCol = if (columns == 2) (itemsCount + 1) / 2 else itemsCount
            val rowHeight = if (columns == 2) 60f else 42f

            val textPaint = Paint()
            textPaint.isAntiAlias = true
            textPaint.textSize = if (columns == 2) 12f else 13f
            textPaint.color = textColor
            textPaint.typeface = safeTypeface

            val emojiPaint = Paint()
            emojiPaint.textSize = if (columns == 2) 20f else 22f
            emojiPaint.textAlign = Paint.Align.CENTER

            val numBg = Paint()
            numBg.color = greenCircleColor
            numBg.style = Paint.Style.FILL

            val numTextPaint = Paint()
            numTextPaint.isAntiAlias = true
            numTextPaint.textSize = 11f
            numTextPaint.color = Color.WHITE
            numTextPaint.typeface = safeTypefaceBold
            numTextPaint.textAlign = Paint.Align.CENTER

            val pillBg = Paint()
            pillBg.color = Color.WHITE
            pillBg.style = Paint.Style.FILL

            val pillBorder = Paint()
            pillBorder.color = borderColor
            pillBorder.style = Paint.Style.STROKE
            pillBorder.strokeWidth = 1f

            val activePaint = Paint()
            activePaint.color = Color.parseColor("#FFB74D")
            activePaint.style = Paint.Style.FILL

            val boxStroke = Paint()
            boxStroke.color = Color.LTGRAY
            boxStroke.style = Paint.Style.STROKE
            boxStroke.strokeWidth = 1f

            val grayTextPaint = Paint(textPaint)
            grayTextPaint.color = Color.GRAY
            grayTextPaint.textSize = if (columns == 2) 10f else 11f

            val startY = 145f

            for (index in words.indices) {
                val word = words[index]
                val col: Int
                val row: Int
                if (columns == 2) {
                    col = index / rowsInCol
                    row = index % rowsInCol
                } else {
                    col = 0
                    row = index
                }

                val left = margin + col * (colWidth + gap)
                val top = startY + row * rowHeight
                val pillHeight = rowHeight - 6f

                // Фон-пилюля на всю ширину столбца
                val pillRect = RectF(left, top, left + colWidth, top + pillHeight)
                canvas.drawRoundRect(pillRect, 10f, 10f, pillBg)
                canvas.drawRoundRect(pillRect, 10f, 10f, pillBorder)

                val posText = when (word.position) {
                    0 -> "в начале"
                    1 -> "в середине"
                    else -> "в конце"
                }

                if (columns == 1) {
                    // ОДНА строка: номер + emoji + слово + 3 бокса + текст позиции
                    val cy = top + pillHeight / 2f
                    val numCx = left + 18f

                    canvas.drawCircle(numCx, cy, 11f, numBg)
                    canvas.drawText("${index + 1}", numCx, cy + 4f, numTextPaint)

                    canvas.drawText(word.emoji, left + 50f, cy + emojiPaint.textSize / 3f, emojiPaint)
                    canvas.drawText(word.word.lowercase(), left + 72f, cy + 4f, textPaint)

                    // 3 бокса с подсветкой нужного + текст позиции (прижаты вправо)
                    val boxSize = 22f
                    val boxGap = 3f
                    val boxesWidth = boxSize * 3 + boxGap * 2
                    val posTextWidth = grayTextPaint.measureText(posText)
                    val rightBlockWidth = boxesWidth + 8f + posTextWidth
                    val rightBlockStart = left + colWidth - rightBlockWidth - 14f
                    val boxY = cy - boxSize / 2f

                    for (i in 0 until 3) {
                        val r = RectF(
                            rightBlockStart + i * (boxSize + boxGap), boxY,
                            rightBlockStart + i * (boxSize + boxGap) + boxSize, boxY + boxSize
                        )
                        if (i == word.position) {
                            canvas.drawRoundRect(r, 3f, 3f, activePaint)
                        } else {
                            canvas.drawRoundRect(r, 3f, 3f, pillBg)
                            canvas.drawRoundRect(r, 3f, 3f, boxStroke)
                        }
                    }

                    canvas.drawText(posText, rightBlockStart + boxesWidth + 8f, cy + 4f, grayTextPaint)
                } else {
                    // ДВЕ строки: номер + emoji + слово сверху; 3 бокса + текст позиции снизу
                    val line1Y = top + 19f
                    val line2Y = top + 41f
                    val numCx = left + 14f

                    canvas.drawCircle(numCx, line1Y, 9f, numBg)
                    canvas.drawText("${index + 1}", numCx, line1Y + 3f, numTextPaint)

                    canvas.drawText(word.emoji, left + 38f, line1Y + 7f, emojiPaint)
                    canvas.drawText(word.word.lowercase(), left + 58f, line1Y + 4f, textPaint)

                    val boxSize = 18f
                    val boxGap = 3f
                    val boxesWidth = boxSize * 3 + boxGap * 2
                    val posTextWidth = grayTextPaint.measureText(posText)
                    val rightBlockWidth = boxesWidth + 6f + posTextWidth
                    val rightBlockStart = left + colWidth - rightBlockWidth - 10f
                    val boxY = line2Y - boxSize / 2f

                    for (i in 0 until 3) {
                        val r = RectF(
                            rightBlockStart + i * (boxSize + boxGap), boxY,
                            rightBlockStart + i * (boxSize + boxGap) + boxSize, boxY + boxSize
                        )
                        if (i == word.position) {
                            canvas.drawRoundRect(r, 3f, 3f, activePaint)
                        } else {
                            canvas.drawRoundRect(r, 3f, 3f, pillBg)
                            canvas.drawRoundRect(r, 3f, 3f, boxStroke)
                        }
                    }

                    canvas.drawText(posText, rightBlockStart + boxesWidth + 6f, line2Y + 4f, grayTextPaint)
                }
            }
            return
        }

        drawHeader(canvas, config, "Где живёт звук ${config.sound.letter}?", "Назови картинку. Где звук — в начале, в середине или в конце слова? Закрась клетку")

        val cols = 4
        val tableTop = 130f
        val cellWidth = (PAGE_WIDTH - margin * 2) / cols
        val cellHeight = 170f

        val cardBgPaint = Paint()
        cardBgPaint.color = Color.WHITE
        cardBgPaint.style = Paint.Style.FILL
        
        val cardBorderPaint = Paint()
        cardBorderPaint.color = borderColor
        cardBorderPaint.style = Paint.Style.STROKE
        cardBorderPaint.strokeWidth = 1.5f
        
        val numBgPaint = Paint()
        numBgPaint.color = greenCircleColor
        numBgPaint.style = Paint.Style.FILL
        
        val numTextPaint = Paint()
        numTextPaint.isAntiAlias = true
        numTextPaint.textSize = 12f
        numTextPaint.color = Color.WHITE
        numTextPaint.typeface = safeTypefaceBold
        numTextPaint.textAlign = Paint.Align.CENTER
        
        val emojiPaint = Paint()
        emojiPaint.textSize = 46f
        emojiPaint.textAlign = Paint.Align.CENTER
        
        val wordPaint = Paint()
        wordPaint.isAntiAlias = true
        wordPaint.textSize = 12f
        wordPaint.color = Color.DKGRAY
        wordPaint.typeface = safeTypeface
        wordPaint.textAlign = Paint.Align.CENTER
        
        val boxPaint = Paint()
        boxPaint.color = Color.LTGRAY
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 1f

        for (index in words.indices) {
            val word = words[index]
            val col = index % cols
            val row = index / cols
            
            val left = margin + col * cellWidth + 4f
            val top = tableTop + row * cellHeight + 4f
            val right = left + cellWidth - 8f
            val bottom = top + cellHeight - 8f

            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBorderPaint)

            canvas.drawCircle(left + 15f, top + 15f, 10f, numBgPaint)
            canvas.drawText("${index + 1}", left + 15f, top + 19f, numTextPaint)

            val cx = left + (right - left) / 2f
            canvas.drawText(word.emoji, cx, top + 70f, emojiPaint)
            canvas.drawText(word.word.lowercase(), cx, top + 95f, wordPaint)

            val boxSize = 24f
            val boxSpacing = 2f
            val totalWidth = boxSize * 3 + boxSpacing * 2
            val startX = cx - totalWidth / 2f
            val boxY = top + 115f

            val whitePaint = Paint()
            whitePaint.color = Color.WHITE
            whitePaint.style = Paint.Style.FILL

            for (i in 0 until 3) {
                val r = RectF(startX + i * (boxSize + boxSpacing), boxY, startX + i * (boxSize + boxSpacing) + boxSize, boxY + boxSize)
                canvas.drawRoundRect(r, 2f, 2f, whitePaint)
                canvas.drawRoundRect(r, 2f, 2f, boxPaint)
            }
        }

        val legendY = PAGE_HEIGHT - 40f
        val legendBoxX = PAGE_WIDTH / 2f - 60f
        val legendTextPaint = Paint()
        legendTextPaint.isAntiAlias = true
        legendTextPaint.textSize = 10f
        legendTextPaint.color = Color.GRAY
        legendTextPaint.typeface = safeTypeface
        
        for (i in 0 until 3) {
            val r = RectF(legendBoxX + i * 22f, legendY - 10f, legendBoxX + i * 22f + 20f, legendY + 10f)
            val whitePaint = Paint()
            whitePaint.color = Color.WHITE
            whitePaint.style = Paint.Style.FILL
            canvas.drawRoundRect(r, 2f, 2f, whitePaint)
            canvas.drawRoundRect(r, 2f, 2f, boxPaint)
        }
        canvas.drawText("клеточки: начало — середина — конец слова", legendBoxX + 70f, legendY + 4f, legendTextPaint)
    }

    private fun drawPhrasesPage(canvas: Canvas, config: SoundAutomationConfig, phrases: List<Pair<String, Int>>) {
        drawHeader(canvas, config, "Звук ${config.sound.letter} во фразах", "Повтори за взрослым чистоговорку или фразу. После каждого раза закрась кружок")

        val margin = 40f
        var currentY = 130f

        val blockBgPaint = Paint()
        blockBgPaint.color = Color.WHITE
        blockBgPaint.style = Paint.Style.FILL
        
        val blockBorderPaint = Paint()
        blockBorderPaint.color = borderColor
        blockBorderPaint.style = Paint.Style.STROKE
        blockBorderPaint.strokeWidth = 1.5f
        
        val circleNumBg = Paint()
        circleNumBg.color = greenCircleColor
        circleNumBg.style = Paint.Style.FILL
        
        val circleNumText = Paint()
        circleNumText.isAntiAlias = true
        circleNumText.textSize = 12f
        circleNumText.color = Color.WHITE
        circleNumText.typeface = safeTypefaceBold
        circleNumText.textAlign = Paint.Align.CENTER
        
        val blockTitlePaint = Paint()
        blockTitlePaint.isAntiAlias = true
        blockTitlePaint.textSize = 12f
        blockTitlePaint.color = Color.DKGRAY
        blockTitlePaint.typeface = safeTypefaceBold

        val textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.textSize = 16f
        textPaint.color = textColor
        textPaint.typeface = safeTypeface
        
        val highlightPaint = Paint(textPaint)
        highlightPaint.color = highlightColor
        
        val circlePaint = Paint()
        circlePaint.color = Color.LTGRAY
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 1.5f
        
        val dashPaint = Paint()
        dashPaint.color = Color.parseColor("#E0E0E0")
        dashPaint.strokeWidth = 1f
        dashPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 4f), 0f)

        val blocks = listOf(
            "Чистоговорки" to phrases.take(5),
            "Скажи фразу" to phrases.drop(5)
        )

        for (bIndex in blocks.indices) {
            val title = blocks[bIndex].first
            val items = blocks[bIndex].second
            if (items.isEmpty()) continue
            val blockHeight = items.size * 40f + 30f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText((bIndex + 1).toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            for (index in items.indices) {
                val item = items[index]
                val cy = currentY + 45f + index * 40f
                
                val emojiPaint = Paint()
                emojiPaint.textSize = 16f
                canvas.drawText("🦁", margin + 15f, cy, emojiPaint)
                
                drawHighlightedText(canvas, item.first, item.second, margin + 200f, cy, textPaint, highlightPaint)
                canvas.drawLine(margin + 15f, cy + 12f, PAGE_WIDTH - margin - 15f, cy + 12f, dashPaint)

                val startCircleX = PAGE_WIDTH - margin - 60f
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * 15f, cy - 4f, 5f, circlePaint)
                }
            }
            currentY += blockHeight + 10f
        }
    }

    private fun drawPairsPage(canvas: Canvas, config: SoundAutomationConfig) {
        val pairData = SoundAutomationData.getPairData(config.pair)
        drawHeader(canvas, config, "Различаем ${config.pair.title}", "Слушай внимательно: какой звук ты слышишь в слове?")

        val margin = 40f
        var currentY = 130f

        val blockBgPaint = Paint()
        blockBgPaint.color = Color.WHITE
        blockBgPaint.style = Paint.Style.FILL
        
        val blockBorderPaint = Paint()
        blockBorderPaint.color = borderColor
        blockBorderPaint.style = Paint.Style.STROKE
        blockBorderPaint.strokeWidth = 1.5f
        
        val circleNumBg = Paint()
        circleNumBg.color = greenCircleColor
        circleNumBg.style = Paint.Style.FILL
        
        val circleNumText = Paint()
        circleNumText.isAntiAlias = true
        circleNumText.textSize = 12f
        circleNumText.color = Color.WHITE
        circleNumText.typeface = safeTypefaceBold
        circleNumText.textAlign = Paint.Align.CENTER
        
        val blockTitlePaint = Paint()
        blockTitlePaint.isAntiAlias = true
        blockTitlePaint.textSize = 12f
        blockTitlePaint.color = Color.DKGRAY
        blockTitlePaint.typeface = safeTypefaceBold

        val textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.textSize = 18f
        textPaint.color = textColor
        textPaint.typeface = safeTypefaceBold
        textPaint.textAlign = Paint.Align.CENTER
        
        val circlePaint = Paint()
        circlePaint.color = Color.LTGRAY
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 1.5f

        val block1Height = 100f
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block1Height), 12f, 12f, blockBgPaint)
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block1Height), 12f, 12f, blockBorderPaint)
        canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
        canvas.drawText("1", margin + 15f, currentY + 19f, circleNumText)
        canvas.drawText("Повтори пары слогов", margin + 35f, currentY + 19f, blockTitlePaint)

        val sylWidth = (PAGE_WIDTH - margin * 2) / 4
        for (i in 0 until 4) {
            val syl = pairData.syllables[i]
            val cx = margin + i * sylWidth + sylWidth / 2
            val cy = currentY + 45f
            val parts = syl.split("-").map { it.trim() }
            val targetPaint = Paint(textPaint)
            targetPaint.color = highlightColor
            
            if (parts.size == 2) {
                // ИСПРАВЛЕНО: используем LEFT-align, иначе при CENTER-align тире
                // рисуется ЦЕНТРОМ вокруг своей точки x и «уезжает» в следующий слог.
                val leftTarget = Paint(targetPaint).apply { textAlign = Paint.Align.LEFT }
                val leftText = Paint(textPaint).apply { textAlign = Paint.Align.LEFT }

                val w1 = leftTarget.measureText(parts[0])
                val dashW = leftText.measureText("-")
                val w3 = leftTarget.measureText(parts[1])
                val totalW = w1 + dashW + w3 + 4f
                val startX = cx - totalW / 2f

                canvas.drawText(parts[0], startX, cy, leftTarget)
                canvas.drawText("-", startX + w1 + 2f, cy, leftText)
                canvas.drawText(parts[1], startX + w1 + dashW + 4f, cy, leftTarget)
            }
            
            for (j in 0 until 3) {
                canvas.drawCircle(cx - 15f + j * 15f, cy + 20f, 4f, circlePaint)
            }
        }
        currentY += block1Height + 10f

        val block2Height = 450f
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block2Height), 12f, 12f, blockBgPaint)
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block2Height), 12f, 12f, blockBorderPaint)
        canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
        canvas.drawText("2", margin + 15f, currentY + 19f, circleNumText)
        canvas.drawText("Какой звук в слове? Назови картинку и обведи букву", margin + 35f, currentY + 19f, blockTitlePaint)

        val cellW = (PAGE_WIDTH - margin * 2 - 20f) / 4
        val cellH = 135f
        
        val emojiPaint = Paint()
        emojiPaint.textSize = 36f
        emojiPaint.textAlign = Paint.Align.CENTER
        
        val wordPaint = Paint()
        wordPaint.isAntiAlias = true
        wordPaint.textSize = 11f
        wordPaint.color = Color.DKGRAY
        wordPaint.typeface = safeTypeface
        wordPaint.textAlign = Paint.Align.CENTER
        
        for (index in pairData.words.indices) {
            val item = pairData.words[index]
            val col = index % 4
            val row = index / 4
            
            val left = margin + 10f + col * cellW
            val top = currentY + 30f + row * cellH
            val right = left + cellW - 5f
            val bottom = top + cellH - 10f

            val whitePaint = Paint()
            whitePaint.color = Color.WHITE
            whitePaint.style = Paint.Style.FILL
            
            val borderPaint = Paint()
            borderPaint.color = Color.LTGRAY
            borderPaint.style = Paint.Style.STROKE
            borderPaint.strokeWidth = 1f

            canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, whitePaint)
            canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, borderPaint)

            canvas.drawCircle(left + 12f, top + 12f, 8f, circleNumBg)
            
            val numPaint = Paint()
            numPaint.textSize = 10f
            numPaint.color = Color.WHITE
            numPaint.textAlign = Paint.Align.CENTER
            numPaint.typeface = safeTypefaceBold
            canvas.drawText("${index + 1}", left + 12f, top + 15f, numPaint)

            val cx = left + (right - left) / 2f
            canvas.drawText(item.emoji, cx, top + 55f, emojiPaint)
            canvas.drawText(item.word.lowercase(), cx, top + 75f, wordPaint)

            val btnW = 30f
            val btnH = 16f
            val btnY = top + 85f
            
            val leftBtn = RectF(cx - btnW - 5f, btnY, cx - 5f, btnY + btnH)
            canvas.drawRoundRect(leftBtn, 4f, 4f, whitePaint)
            canvas.drawRoundRect(leftBtn, 4f, 4f, borderPaint)
            
            val btnTextPaint = Paint()
            btnTextPaint.textSize = 12f
            btnTextPaint.color = textColor
            btnTextPaint.textAlign = Paint.Align.CENTER
            btnTextPaint.typeface = safeTypefaceBold
            canvas.drawText(config.pair.first.letter, cx - btnW / 2f - 5f, btnY + 12f, btnTextPaint)
            
            val dropPaint = Paint()
            dropPaint.textSize = 8f
            dropPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("💧", cx - btnW + 2f, btnY + 12f, dropPaint)

            val rightBtn = RectF(cx + 5f, btnY, cx + btnW + 5f, btnY + btnH)
            canvas.drawRoundRect(rightBtn, 4f, 4f, whitePaint)
            canvas.drawRoundRect(rightBtn, 4f, 4f, borderPaint)
            
            canvas.drawText(config.pair.second.letter, cx + btnW / 2f + 5f, btnY + 12f, btnTextPaint)
            canvas.drawText("💧", cx + 5f, btnY + 12f, dropPaint)
        }
    }

    private fun drawPairsAnswers(canvas: Canvas, config: SoundAutomationConfig) {
        val pairData = SoundAutomationData.getPairData(config.pair)
        
        val margin = 40f
        
        val titlePaint = Paint()
        titlePaint.isAntiAlias = true
        titlePaint.textSize = 28f
        titlePaint.color = greenCircleColor
        titlePaint.typeface = cursiveTypeface
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Ответы", PAGE_WIDTH / 2f, 60f, titlePaint)
        
        val subPaint = Paint()
        subPaint.isAntiAlias = true
        subPaint.textSize = 11f
        subPaint.color = Color.GRAY
        subPaint.textAlign = Paint.Align.CENTER
        subPaint.typeface = Typeface.create(safeTypeface, Typeface.ITALIC)
        canvas.drawText("Для взрослого", PAGE_WIDTH / 2f, 80f, subPaint)

        val headerTextPaint = Paint()
        headerTextPaint.isAntiAlias = true
        headerTextPaint.textSize = 14f
        headerTextPaint.color = Color.DKGRAY
        headerTextPaint.typeface = safeTypefaceBold
        canvas.drawText("Различаем ${config.pair.title}", margin, 120f, headerTextPaint)

        // === НОВОЕ: поддержка 1 или 2 столбцов ===
        val columns = if (config.answerColumns >= 2) 2 else 1
        val gap = 10f
        val usableWidth = PAGE_WIDTH - margin * 2
        val colWidth = if (columns == 2) (usableWidth - gap) / 2 else usableWidth
        val itemsCount = pairData.words.size
        val rowsInCol = if (columns == 2) (itemsCount + 1) / 2 else itemsCount
        val rowHeight = if (columns == 2) 55f else 42f
        
        val textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.textSize = if (columns == 2) 12f else 13f
        textPaint.color = textColor
        textPaint.typeface = safeTypeface
        
        val emojiPaint = Paint()
        emojiPaint.textSize = if (columns == 2) 20f else 22f
        emojiPaint.textAlign = Paint.Align.CENTER

        val numBg = Paint()
        numBg.color = greenCircleColor
        numBg.style = Paint.Style.FILL

        val numTextPaint = Paint()
        numTextPaint.isAntiAlias = true
        numTextPaint.textSize = 11f
        numTextPaint.color = Color.WHITE
        numTextPaint.typeface = safeTypefaceBold
        numTextPaint.textAlign = Paint.Align.CENTER
        
        val pillBg = Paint()
        pillBg.color = Color.WHITE
        pillBg.style = Paint.Style.FILL

        val pillBorder = Paint()
        pillBorder.color = borderColor
        pillBorder.style = Paint.Style.STROKE
        pillBorder.strokeWidth = 1f
        
        val greenBoxBg = Paint()
        greenBoxBg.color = greenCircleColor
        greenBoxBg.style = Paint.Style.FILL
        
        val greenBoxText = Paint()
        greenBoxText.isAntiAlias = true
        greenBoxText.textSize = if (columns == 2) 13f else 15f
        greenBoxText.color = Color.WHITE
        greenBoxText.typeface = safeTypefaceBold
        greenBoxText.textAlign = Paint.Align.CENTER

        val grayLabelPaint = Paint(textPaint)
        grayLabelPaint.color = Color.GRAY
        grayLabelPaint.textSize = if (columns == 2) 10f else 11f

        val startY = 145f

        for (index in pairData.words.indices) {
            val word = pairData.words[index]
            val col: Int
            val row: Int
            if (columns == 2) {
                col = index / rowsInCol
                row = index % rowsInCol
            } else {
                col = 0
                row = index
            }
            
            val left = margin + col * (colWidth + gap)
            val top = startY + row * rowHeight
            val pillHeight = rowHeight - 6f

            // Фон-пилюля на всю ширину столбца
            val pillRect = RectF(left, top, left + colWidth, top + pillHeight)
            canvas.drawRoundRect(pillRect, 10f, 10f, pillBg)
            canvas.drawRoundRect(pillRect, 10f, 10f, pillBorder)

            if (columns == 1) {
                // ОДНА строка: номер + emoji + слово + бокс с буквой справа
                val cy = top + pillHeight / 2f
                val numCx = left + 18f

                canvas.drawCircle(numCx, cy, 11f, numBg)
                canvas.drawText("${index + 1}", numCx, cy + 4f, numTextPaint)

                canvas.drawText(word.emoji, left + 50f, cy + emojiPaint.textSize / 3f, emojiPaint)
                canvas.drawText(word.word.lowercase(), left + 72f, cy + 4f, textPaint)

                // Подпись "звук:" + зелёный бокс с буквой прижаты вправо
                val boxW = 36f
                val boxH = 26f
                val boxX = left + colWidth - boxW - 14f
                val boxY = cy - boxH / 2f
                canvas.drawText("звук:", boxX - 35f, cy + 4f, grayLabelPaint)
                val boxRect = RectF(boxX, boxY, boxX + boxW, boxY + boxH)
                canvas.drawRoundRect(boxRect, 6f, 6f, greenBoxBg)
                canvas.drawText(word.correctSound, boxX + boxW / 2f, cy + 5f, greenBoxText)
            } else {
                // ДВЕ строки: номер + emoji + слово сверху; бокс с буквой справа снизу
                val line1Y = top + 19f
                val line2Y = top + 41f
                val numCx = left + 14f

                canvas.drawCircle(numCx, line1Y, 9f, numBg)
                canvas.drawText("${index + 1}", numCx, line1Y + 3f, numTextPaint)

                canvas.drawText(word.emoji, left + 38f, line1Y + 7f, emojiPaint)
                canvas.drawText(word.word.lowercase(), left + 58f, line1Y + 4f, textPaint)

                val boxW = 30f
                val boxH = 22f
                val boxX = left + colWidth - boxW - 10f
                val boxY = line2Y - boxH / 2f
                canvas.drawText("звук:", boxX - 32f, line2Y + 4f, grayLabelPaint)
                val boxRect = RectF(boxX, boxY, boxX + boxW, boxY + boxH)
                canvas.drawRoundRect(boxRect, 5f, 5f, greenBoxBg)
                canvas.drawText(word.correctSound, boxX + boxW / 2f, line2Y + 5f, greenBoxText)
            }
        }
    }
}
