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

object SoundAutomationPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    private val bgColor = Color.parseColor("#FAFAF5")
    private val borderColor = Color.parseColor("#D4D4C8")
    private val textColor = Color.parseColor("#333333")
    private val highlightColor = Color.parseColor("#D84315")
    private val greenCircleColor = Color.parseColor("#4A7A40")
    
    private val safeTypeface = Typeface.DEFAULT
    private val safeTypefaceBold = Typeface.DEFAULT_BOLD
    private val cursiveTypeface = Typeface.create("cursive", Typeface.BOLD)

    fun generatePreview(config: SoundAutomationConfig, context: Context): List<Bitmap> {
        var pages = mutableListOf<Bitmap>()

        if (config.mode == SpeechMode.PAIRS) {
            var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawPairsPage(canvas, config)
            pages.add(bmp)
            if (config.showAnswers) {
                var ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                var ansCanvas = Canvas(ansBmp)
                ansCanvas.drawColor(bgColor)
                drawPairsAnswers(ansCanvas, config)
                pages.add(ansBmp)
            }
            return pages
        }

        var data = SoundAutomationData.getData(config.sound)

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.SYLLABLES) {
            var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawSyllablesPage(canvas, config, data.syllables)
            pages.add(bmp)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WORDS) {
            var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawWordsPage(canvas, config, data)
            pages.add(bmp)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WHERE_IS_SOUND) {
            var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawWhereIsSoundPage(canvas, config, data.whereIsSound, isAnswer = false)
            pages.add(bmp)

            if (config.showAnswers) {
                var ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                var ansCanvas = Canvas(ansBmp)
                ansCanvas.drawColor(bgColor)
                drawWhereIsSoundPage(ansCanvas, config, data.whereIsSound, isAnswer = true)
                pages.add(ansBmp)
            }
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.PHRASES) {
            var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bmp)
            canvas.drawColor(bgColor)
            drawPhrasesPage(canvas, config, data.phrases)
            pages.add(bmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: SoundAutomationConfig, context: Context) {
        var pageNum = 1

        fun startNewPage(): PdfDocument.Page {
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            var page = document.startPage(pageInfo)
            page.canvas.drawColor(bgColor)
            return page
        }

        if (config.mode == SpeechMode.PAIRS) {
            var page = startNewPage()
            drawPairsPage(page.canvas, config)
            document.finishPage(page)
            if (config.showAnswers) {
                var ansPage = startNewPage()
                drawPairsAnswers(ansPage.canvas, config)
                document.finishPage(ansPage)
            }
            return
        }

        var data = SoundAutomationData.getData(config.sound)

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.SYLLABLES) {
            var page = startNewPage()
            drawSyllablesPage(page.canvas, config, data.syllables)
            document.finishPage(page)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WORDS) {
            var page = startNewPage()
            drawWordsPage(page.canvas, config, data)
            document.finishPage(page)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WHERE_IS_SOUND) {
            var page = startNewPage()
            drawWhereIsSoundPage(page.canvas, config, data.whereIsSound, isAnswer = false)
            document.finishPage(page)

            if (config.showAnswers) {
                var ansPage = startNewPage()
                drawWhereIsSoundPage(ansPage.canvas, config, data.whereIsSound, isAnswer = true)
                document.finishPage(ansPage)
            }
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.PHRASES) {
            var page = startNewPage()
            drawPhrasesPage(page.canvas, config, data.phrases)
            document.finishPage(page)
        }
    }

    private fun drawHeader(canvas: Canvas, config: SoundAutomationConfig, title: String, sub: String) {
        var margin = 40f
        var textSize = 10f
        
        var boxPaint = Paint()
        boxPaint.color = Color.WHITE
        boxPaint.style = Paint.Style.FILL
        
        var boxBorder = Paint()
        boxBorder.color = borderColor
        boxBorder.style = Paint.Style.STROKE
        boxBorder.strokeWidth = 1f
        
        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxBorder)
        
        var nameTextPaint = Paint()
        nameTextPaint.textSize = textSize
        nameTextPaint.color = Color.GRAY
        nameTextPaint.isAntiAlias = true
        nameTextPaint.typeface = safeTypeface
        canvas.drawText("Имя:", margin + 5f, 38f, nameTextPaint)
        
        var linePaint = Paint()
        linePaint.color = Color.LTGRAY
        linePaint.strokeWidth = 1f
        canvas.drawLine(margin + 30f, 40f, margin + 110f, 40f, linePaint)

        var centerRect = RectF(PAGE_WIDTH/2f - 60f, 25f, PAGE_WIDTH/2f + 60f, 45f)
        var centerBgPaint = Paint()
        centerBgPaint.color = Color.parseColor("#C8E6C9")
        centerBgPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(centerRect, 5f, 5f, centerBgPaint)
        
        var soundTextPaint = Paint()
        soundTextPaint.textSize = textSize
        soundTextPaint.color = Color.DKGRAY
        soundTextPaint.isAntiAlias = true
        soundTextPaint.typeface = safeTypefaceBold
        canvas.drawText("Звук ${config.sound.letter}", PAGE_WIDTH/2f - 20f, 38f, soundTextPaint)

        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxBorder)
        canvas.drawText("Дата:", PAGE_WIDTH - margin - 115f, 38f, nameTextPaint)
        canvas.drawLine(PAGE_WIDTH - margin - 85f, 40f, PAGE_WIDTH - margin - 10f, 40f, linePaint)

        var titlePaint = Paint()
        titlePaint.isAntiAlias = true
        titlePaint.textSize = 28f
        titlePaint.color = greenCircleColor
        titlePaint.typeface = cursiveTypeface
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText(title, PAGE_WIDTH / 2f, 80f, titlePaint)

        var subPaint = Paint()
        subPaint.isAntiAlias = true
        subPaint.textSize = 10f
        subPaint.color = Color.GRAY
        subPaint.textAlign = Paint.Align.CENTER
        subPaint.typeface = Typeface.create(safeTypeface, Typeface.ITALIC)
        canvas.drawText(sub, PAGE_WIDTH / 2f, 100f, subPaint)
    }

    private fun drawHighlightedText(canvas: Canvas, text: String, highlightIndex: Int, x: Float, y: Float, basePaint: Paint, highlightPaint: Paint) {
        if (highlightIndex < 0 || highlightIndex >= text.length) {
            canvas.drawText(text, x, y, basePaint)
            return
        }
        var prefix = text.substring(0, highlightIndex)
        var target = text.substring(highlightIndex, highlightIndex + 1)
        var suffix = text.substring(highlightIndex + 1)
        
        var w1 = basePaint.measureText(prefix)
        var w2 = highlightPaint.measureText(target)
        var w3 = basePaint.measureText(suffix)
        var totalWidth = w1 + w2 + w3
        
        var startX = x - totalWidth / 2f
        
        canvas.drawText(prefix, startX, y, basePaint)
        canvas.drawText(target, startX + w1, y, highlightPaint)
        canvas.drawText(suffix, startX + w1 + w2, y, basePaint)
    }

    private fun drawSyllablesPage(canvas: Canvas, config: SoundAutomationConfig, syllables: List<Pair<String, Int>>) {
        drawHeader(canvas, config, "Звук ${config.sound.letter}: слоги", "Прочитай или повтори за взрослым каждый слог три раза. После каждого раза закрась кружок")
        
        var margin = 40f
        var currentY = 130f
        
        var blockTitles = listOf("Слог в начале", "Слог в конце", "Между гласными", "С другим согласным", "Цепочки")
        var startIndexes = listOf(0, 4, 8, 12, 16)
        var endIndexes = listOf(4, 8, 12, 16, 18)

        var blockBgPaint = Paint()
        blockBgPaint.color = Color.WHITE
        blockBgPaint.style = Paint.Style.FILL

        var blockBorderPaint = Paint()
        blockBorderPaint.color = borderColor
        blockBorderPaint.style = Paint.Style.STROKE
        blockBorderPaint.strokeWidth = 1.5f
        
        var circleNumBg = Paint()
        circleNumBg.color = greenCircleColor
        circleNumBg.style = Paint.Style.FILL

        var circleNumText = Paint()
        circleNumText.isAntiAlias = true
        circleNumText.textSize = 12f
        circleNumText.color = Color.WHITE
        circleNumText.typeface = safeTypefaceBold
        circleNumText.textAlign = Paint.Align.CENTER

        var blockTitlePaint = Paint()
        blockTitlePaint.isAntiAlias = true
        blockTitlePaint.textSize = 12f
        blockTitlePaint.color = Color.DKGRAY
        blockTitlePaint.typeface = safeTypefaceBold

        var cardBgPaint = Paint()
        cardBgPaint.color = Color.WHITE
        cardBgPaint.style = Paint.Style.FILL

        var cardBorderPaint = Paint()
        cardBorderPaint.color = Color.parseColor("#E0E0E0")
        cardBorderPaint.style = Paint.Style.STROKE
        cardBorderPaint.strokeWidth = 1f
        
        var textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.textSize = 24f
        textPaint.color = textColor
        textPaint.typeface = safeTypefaceBold
        textPaint.textAlign = Paint.Align.CENTER

        var highlightPaint = Paint(textPaint)
        highlightPaint.color = highlightColor

        var circlePaint = Paint()
        circlePaint.color = Color.LTGRAY
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 1.5f

        for (bIndex in blockTitles.indices) {
            var title = blockTitles[bIndex]
            var startIdx = startIndexes[bIndex]
            var endIdx = endIndexes[bIndex]
            
            if (startIdx >= syllables.size) continue
            var actualEndIdx = if (endIdx > syllables.size) syllables.size else endIdx
            var itemsCount = actualEndIdx - startIdx
            if (itemsCount <= 0) continue
            
            var isTwoRows = itemsCount > 4
            var blockHeight = if (isTwoRows) 115f else 80f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText((bIndex + 1).toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            var itemWidth = (PAGE_WIDTH - margin * 2 - 20f) / 4
            
            for (iIdx in 0 until itemsCount) {
                var item = syllables[startIdx + iIdx]
                var col = iIdx % 4
                var row = iIdx / 4
                
                var cardLeft = margin + 10f + col * itemWidth
                var cardTop = currentY + 30f + row * 55f
                var cardRight = cardLeft + itemWidth - 5f
                var cardBottom = cardTop + 45f

                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBgPaint)
                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBorderPaint)
                
                var cx = cardLeft + (cardRight - cardLeft) / 2f
                var cy = cardTop + (cardBottom - cardTop) / 2f

                var currentTextPaint = textPaint
                if (item.first.length > 4) {
                    currentTextPaint = Paint(textPaint)
                    currentTextPaint.textSize = 18f
                }
                var currentHighlightPaint = highlightPaint
                if (item.first.length > 4) {
                    currentHighlightPaint = Paint(highlightPaint)
                    currentHighlightPaint.textSize = 18f
                }

                drawHighlightedText(canvas, item.first, item.second, cx, cy + 8f, currentTextPaint, currentHighlightPaint)
                
                var circleRadius = 4f
                var circleSpacing = 12f
                var startCircleX = cx - circleSpacing
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * circleSpacing, cy + 20f, circleRadius, circlePaint)
                }
            }
            currentY += blockHeight + 10f
        }
    }

    private fun drawWordsPage(canvas: Canvas, config: SoundAutomationConfig, data: SoundData) {
        drawHeader(canvas, config, "Звук ${config.sound.letter} в словах", "Назови картинку чётко, выделяя звук ${config.sound.letter}. Клеточка показывает, где он спрятался")
        
        var margin = 40f
        var currentY = 130f

        var sections = listOf(
            "Звук ${config.sound.letter} в начале слова" to data.wordsStart,
            "Звук ${config.sound.letter} в середине слова" to data.wordsMiddle,
            "Звук ${config.sound.letter} в конце слова" to data.wordsEnd
        )

        var blockBgPaint = Paint()
        blockBgPaint.color = Color.WHITE
        blockBgPaint.style = Paint.Style.FILL

        var blockBorderPaint = Paint()
        blockBorderPaint.color = borderColor
        blockBorderPaint.style = Paint.Style.STROKE
        blockBorderPaint.strokeWidth = 1.5f
        
        var circleNumBg = Paint()
        circleNumBg.color = greenCircleColor
        circleNumBg.style = Paint.Style.FILL

        var circleNumText = Paint()
        circleNumText.isAntiAlias = true
        circleNumText.textSize = 12f
        circleNumText.color = Color.WHITE
        circleNumText.typeface = safeTypefaceBold
        circleNumText.textAlign = Paint.Align.CENTER

        var blockTitlePaint = Paint()
        blockTitlePaint.isAntiAlias = true
        blockTitlePaint.textSize = 12f
        blockTitlePaint.color = Color.DKGRAY
        blockTitlePaint.typeface = safeTypefaceBold

        var cardBgPaint = Paint()
        cardBgPaint.color = Color.WHITE
        cardBgPaint.style = Paint.Style.FILL

        var cardBorderPaint = Paint()
        cardBorderPaint.color = Color.parseColor("#E0E0E0")
        cardBorderPaint.style = Paint.Style.STROKE
        cardBorderPaint.strokeWidth = 1f
        
        var wordPaint = Paint()
        wordPaint.isAntiAlias = true
        wordPaint.textSize = 14f
        wordPaint.color = textColor
        wordPaint.typeface = safeTypefaceBold
        wordPaint.textAlign = Paint.Align.CENTER

        var highlightPaint = Paint(wordPaint)
        highlightPaint.color = highlightColor

        var emojiPaint = Paint()
        emojiPaint.textSize = 36f
        emojiPaint.textAlign = Paint.Align.CENTER
        
        var circlePaint = Paint()
        circlePaint.color = Color.LTGRAY
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 1.5f

        for (sIndex in sections.indices) {
            var title = sections[sIndex].first
            var words = sections[sIndex].second
            if (words.isEmpty()) continue
            var blockHeight = 160f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText((sIndex + 1).toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            var indicatorX = PAGE_WIDTH - margin - 40f
            var indicatorY = currentY + 10f
            for (i in 0 until 3) {
                var rect = RectF(indicatorX + i * 12f, indicatorY, indicatorX + i * 12f + 10f, indicatorY + 10f)
                if (i == sIndex) {
                    var fillPaint = Paint()
                    fillPaint.color = Color.parseColor("#FFB74D")
                    fillPaint.style = Paint.Style.FILL
                    canvas.drawRoundRect(rect, 3f, 3f, fillPaint)
                } else {
                    var strokePaint = Paint()
                    strokePaint.color = Color.WHITE
                    strokePaint.style = Paint.Style.STROKE
                    strokePaint.strokeWidth = 1f
                    canvas.drawRoundRect(rect, 3f, 3f, strokePaint)
                }
            }

            var itemWidth = (PAGE_WIDTH - margin * 2 - 20f) / 4
            
            for (iIndex in words.indices) {
                var item = words[iIndex]
                var col = iIndex % 4
                var cardLeft = margin + 10f + col * itemWidth
                var cardTop = currentY + 30f
                var cardRight = cardLeft + itemWidth - 5f
                var cardBottom = cardTop + 120f

                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBgPaint)
                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBorderPaint)
                
                var cx = cardLeft + (cardRight - cardLeft) / 2f
                
                canvas.drawText(item.emoji, cx, cardTop + 50f, emojiPaint)
                drawHighlightedText(canvas, item.word, item.highlightIndex, cx, cardTop + 75f, wordPaint, highlightPaint)

                var boxW = 16f
                var boxH = 8f
                var startBoxX = cx - (boxW * 3 + 2f) / 2f
                var boxY = cardTop + 85f
                for (i in 0 until 3) {
                    var r = RectF(startBoxX + i * (boxW + 1f), boxY, startBoxX + i * (boxW + 1f) + boxW, boxY + boxH)
                    if (i == item.position) {
                        var fillPaint = Paint()
                        fillPaint.color = Color.parseColor("#FFB74D")
                        fillPaint.style = Paint.Style.FILL
                        canvas.drawRoundRect(r, 2f, 2f, fillPaint)
                    } else {
                        var strokePaint = Paint()
                        strokePaint.color = Color.LTGRAY
                        strokePaint.style = Paint.Style.STROKE
                        strokePaint.strokeWidth = 1f
                        canvas.drawRoundRect(r, 2f, 2f, strokePaint)
                    }
                }

                var startCircleX = cx - 12f
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * 12f, cardTop + 105f, 4f, circlePaint)
                }
            }
            currentY += blockHeight + 10f
        }
    }

    private fun drawWhereIsSoundPage(canvas: Canvas, config: SoundAutomationConfig, words: List<AutoWord>, isAnswer: Boolean) {
        var margin = 40f
        
        if (isAnswer) {
            canvas.drawColor(bgColor)
            var titlePaint = Paint()
            titlePaint.isAntiAlias = true
            titlePaint.textSize = 28f
            titlePaint.color = greenCircleColor
            titlePaint.typeface = cursiveTypeface
            titlePaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Ответы", PAGE_WIDTH / 2f, 60f, titlePaint)
            
            var subPaint = Paint()
            subPaint.isAntiAlias = true
            subPaint.textSize = 11f
            subPaint.color = Color.GRAY
            subPaint.textAlign = Paint.Align.CENTER
            subPaint.typeface = Typeface.create(safeTypeface, Typeface.ITALIC)
            canvas.drawText("Для взрослого", PAGE_WIDTH / 2f, 80f, subPaint)

            var headerTextPaint = Paint()
            headerTextPaint.isAntiAlias = true
            headerTextPaint.textSize = 14f
            headerTextPaint.color = Color.DKGRAY
            headerTextPaint.typeface = safeTypefaceBold
            canvas.drawText("Где живёт звук ${config.sound.letter}?", margin, 120f, headerTextPaint)

            var currentY = 140f
            var rowHeight = 35f
            var textPaint = Paint()
            textPaint.isAntiAlias = true
            textPaint.textSize = 12f
            textPaint.color = textColor
            textPaint.typeface = safeTypeface
            
            var emojiPaint = Paint()
            emojiPaint.textSize = 20f
            emojiPaint.textAlign = Paint.Align.CENTER
            
            var numBg = Paint()
            numBg.color = Color.WHITE
            numBg.style = Paint.Style.FILL
            
            var numBorder = Paint()
            numBorder.color = Color.LTGRAY
            numBorder.style = Paint.Style.STROKE
            numBorder.strokeWidth = 1f
            
            for (index in words.indices) {
                var word = words[index]
                var col = index % 2
                var row = index / 2
                if (col == 0 && row > 0) currentY += rowHeight
                
                var left = margin + col * ((PAGE_WIDTH - margin*2)/2f)
                
                var pillRect = RectF(left, currentY - 12f, left + 250f, currentY + 12f)
                canvas.drawRoundRect(pillRect, 12f, 12f, numBg)
                canvas.drawRoundRect(pillRect, 12f, 12f, numBorder)
                
                canvas.drawText("${index + 1}", left + 10f, currentY + 4f, textPaint)
                canvas.drawText(word.emoji, left + 35f, currentY + 6f, emojiPaint)
                canvas.drawText(word.word.lowercase(), left + 55f, currentY + 4f, textPaint)

                var boxSize = 12f
                var boxX = left + 140f
                var boxY = currentY - 6f
                
                var posText = when(word.position) {
                    0 -> "в начале"
                    1 -> "в середине"
                    else -> "в конце"
                }
                
                for (i in 0 until 3) {
                    var rect = RectF(boxX + i * (boxSize + 2f), boxY, boxX + i * (boxSize + 2f) + boxSize, boxY + boxSize)
                    if (i == word.position) {
                        var pill = RectF(rect.left - 2f, rect.top - 1f, rect.right + 2f, rect.bottom + 1f)
                        var pillPaint = Paint()
                        pillPaint.color = Color.parseColor("#FFB74D")
                        pillPaint.style = Paint.Style.FILL
                        canvas.drawRoundRect(pill, 4f, 4f, pillPaint)
                    } else {
                        var whitePaint = Paint()
                        whitePaint.color = Color.WHITE
                        whitePaint.style = Paint.Style.FILL
                        canvas.drawRoundRect(rect, 2f, 2f, whitePaint)
                        canvas.drawRoundRect(rect, 2f, 2f, numBorder)
                    }
                }
                
                var grayTextPaint = Paint(textPaint)
                grayTextPaint.color = Color.GRAY
                canvas.drawText(posText, boxX + 55f, currentY + 4f, grayTextPaint)
            }
            return
        }

        drawHeader(canvas, config, "Где живёт звук ${config.sound.letter}?", "Назови картинку. Где звук — в начале, в середине или в конце слова? Закрась клетку")

        var cols = 4
        var tableTop = 130f
        var cellWidth = (PAGE_WIDTH - margin * 2) / cols
        var cellHeight = 170f

        var cardBgPaint = Paint()
        cardBgPaint.color = Color.WHITE
        cardBgPaint.style = Paint.Style.FILL
        
        var cardBorderPaint = Paint()
        cardBorderPaint.color = borderColor
        cardBorderPaint.style = Paint.Style.STROKE
        cardBorderPaint.strokeWidth = 1.5f
        
        var numBgPaint = Paint()
        numBgPaint.color = greenCircleColor
        numBgPaint.style = Paint.Style.FILL
        
        var numTextPaint = Paint()
        numTextPaint.isAntiAlias = true
        numTextPaint.textSize = 12f
        numTextPaint.color = Color.WHITE
        numTextPaint.typeface = safeTypefaceBold
        numTextPaint.textAlign = Paint.Align.CENTER
        
        var emojiPaint = Paint()
        emojiPaint.textSize = 46f
        emojiPaint.textAlign = Paint.Align.CENTER
        
        var wordPaint = Paint()
        wordPaint.isAntiAlias = true
        wordPaint.textSize = 12f
        wordPaint.color = Color.DKGRAY
        wordPaint.typeface = safeTypeface
        wordPaint.textAlign = Paint.Align.CENTER
        
        var boxPaint = Paint()
        boxPaint.color = Color.LTGRAY
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 1f

        for (index in words.indices) {
            var word = words[index]
            var col = index % cols
            var row = index / cols
            
            var left = margin + col * cellWidth + 4f
            var top = tableTop + row * cellHeight + 4f
            var right = left + cellWidth - 8f
            var bottom = top + cellHeight - 8f

            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBorderPaint)

            canvas.drawCircle(left + 15f, top + 15f, 10f, numBgPaint)
            canvas.drawText("${index + 1}", left + 15f, top + 19f, numTextPaint)

            var cx = left + (right - left) / 2f
            canvas.drawText(word.emoji, cx, top + 70f, emojiPaint)
            canvas.drawText(word.word.lowercase(), cx, top + 95f, wordPaint)

            var boxSize = 24f
            var boxSpacing = 2f
            var totalWidth = boxSize * 3 + boxSpacing * 2
            var startX = cx - totalWidth / 2f
            var boxY = top + 115f

            var whitePaint = Paint()
            whitePaint.color = Color.WHITE
            whitePaint.style = Paint.Style.FILL

            for (i in 0 until 3) {
                var r = RectF(startX + i * (boxSize + boxSpacing), boxY, startX + i * (boxSize + boxSpacing) + boxSize, boxY + boxSize)
                canvas.drawRoundRect(r, 2f, 2f, whitePaint)
                canvas.drawRoundRect(r, 2f, 2f, boxPaint)
            }
        }

        var legendY = PAGE_HEIGHT - 40f
        var legendBoxX = PAGE_WIDTH / 2f - 60f
        var legendTextPaint = Paint()
        legendTextPaint.isAntiAlias = true
        legendTextPaint.textSize = 10f
        legendTextPaint.color = Color.GRAY
        legendTextPaint.typeface = safeTypeface
        
        for (i in 0 until 3) {
            var r = RectF(legendBoxX + i * 22f, legendY - 10f, legendBoxX + i * 22f + 20f, legendY + 10f)
            var whitePaint = Paint()
            whitePaint.color = Color.WHITE
            whitePaint.style = Paint.Style.FILL
            canvas.drawRoundRect(r, 2f, 2f, whitePaint)
            canvas.drawRoundRect(r, 2f, 2f, boxPaint)
        }
        canvas.drawText("клеточки: начало — середина — конец слова", legendBoxX + 70f, legendY + 4f, legendTextPaint)
    }

    private fun drawPhrasesPage(canvas: Canvas, config: SoundAutomationConfig, phrases: List<Pair<String, Int>>) {
        drawHeader(canvas, config, "Звук ${config.sound.letter} во фразах", "Повтори за взрослым чистоговорку или фразу. После каждого раза закрась кружок")

        var margin = 40f
        var currentY = 130f

        var blockBgPaint = Paint()
        blockBgPaint.color = Color.WHITE
        blockBgPaint.style = Paint.Style.FILL
        
        var blockBorderPaint = Paint()
        blockBorderPaint.color = borderColor
        blockBorderPaint.style = Paint.Style.STROKE
        blockBorderPaint.strokeWidth = 1.5f
        
        var circleNumBg = Paint()
        circleNumBg.color = greenCircleColor
        circleNumBg.style = Paint.Style.FILL
        
        var circleNumText = Paint()
        circleNumText.isAntiAlias = true
        circleNumText.textSize = 12f
        circleNumText.color = Color.WHITE
        circleNumText.typeface = safeTypefaceBold
        circleNumText.textAlign = Paint.Align.CENTER
        
        var blockTitlePaint = Paint()
        blockTitlePaint.isAntiAlias = true
        blockTitlePaint.textSize = 12f
        blockTitlePaint.color = Color.DKGRAY
        blockTitlePaint.typeface = safeTypefaceBold

        var textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.textSize = 16f
        textPaint.color = textColor
        textPaint.typeface = safeTypeface
        
        var highlightPaint = Paint(textPaint)
        highlightPaint.color = highlightColor
        
        var circlePaint = Paint()
        circlePaint.color = Color.LTGRAY
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 1.5f
        
        var dashPaint = Paint()
        dashPaint.color = Color.parseColor("#E0E0E0")
        dashPaint.strokeWidth = 1f
        dashPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 4f), 0f)

        var blocks = listOf(
            "Чистоговорки" to phrases.take(5),
            "Скажи фразу" to phrases.drop(5)
        )

        for (bIndex in blocks.indices) {
            var title = blocks[bIndex].first
            var items = blocks[bIndex].second
            if (items.isEmpty()) continue
            var blockHeight = items.size * 40f + 30f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText((bIndex + 1).toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            for (index in items.indices) {
                var item = items[index]
                var cy = currentY + 45f + index * 40f
                
                var emojiPaint = Paint()
                emojiPaint.textSize = 16f
                canvas.drawText("🦁", margin + 15f, cy, emojiPaint)
                
                drawHighlightedText(canvas, item.first, item.second, margin + 200f, cy, textPaint, highlightPaint)
                canvas.drawLine(margin + 15f, cy + 12f, PAGE_WIDTH - margin - 15f, cy + 12f, dashPaint)

                var startCircleX = PAGE_WIDTH - margin - 60f
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * 15f, cy - 4f, 5f, circlePaint)
                }
            }
            currentY += blockHeight + 10f
        }
    }

    private fun drawPairsPage(canvas: Canvas, config: SoundAutomationConfig) {
        var pairData = SoundAutomationData.getPairData(config.pair)
        drawHeader(canvas, config, "Различаем ${config.pair.title}", "Слушай внимательно: какой звук ты слышишь в слове?")

        var margin = 40f
        var currentY = 130f

        var blockBgPaint = Paint()
        blockBgPaint.color = Color.WHITE
        blockBgPaint.style = Paint.Style.FILL
        
        var blockBorderPaint = Paint()
        blockBorderPaint.color = borderColor
        blockBorderPaint.style = Paint.Style.STROKE
        blockBorderPaint.strokeWidth = 1.5f
        
        var circleNumBg = Paint()
        circleNumBg.color = greenCircleColor
        circleNumBg.style = Paint.Style.FILL
        
        var circleNumText = Paint()
        circleNumText.isAntiAlias = true
        circleNumText.textSize = 12f
        circleNumText.color = Color.WHITE
        circleNumText.typeface = safeTypefaceBold
        circleNumText.textAlign = Paint.Align.CENTER
        
        var blockTitlePaint = Paint()
        blockTitlePaint.isAntiAlias = true
        blockTitlePaint.textSize = 12f
        blockTitlePaint.color = Color.DKGRAY
        blockTitlePaint.typeface = safeTypefaceBold

        var textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.textSize = 20f
        textPaint.color = textColor
        textPaint.typeface = safeTypefaceBold
        textPaint.textAlign = Paint.Align.CENTER
        
        var circlePaint = Paint()
        circlePaint.color = Color.LTGRAY
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 1.5f

        var block1Height = 100f
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block1Height), 12f, 12f, blockBgPaint)
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block1Height), 12f, 12f, blockBorderPaint)
        canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
        canvas.drawText("1", margin + 15f, currentY + 19f, circleNumText)
        canvas.drawText("Повтори пары слогов", margin + 35f, currentY + 19f, blockTitlePaint)

        var sylWidth = (PAGE_WIDTH - margin * 2) / 4
        for (i in 0 until 4) {
            var syl = pairData.syllables[i]
            var cx = margin + i * sylWidth + sylWidth / 2
            var cy = currentY + 45f
            var parts = syl.split("-").map { it.trim() }
            var targetPaint = Paint(textPaint)
            targetPaint.color = highlightColor
            
            if (parts.size == 2) {
                var w1 = targetPaint.measureText(parts[0])
                var dashW = targetPaint.measureText("-")
                var w3 = targetPaint.measureText(parts[1])
                var totalW = w1 + dashW + w3 + 4f
                var startX = cx - totalW / 2f
                
                canvas.drawText(parts[0], startX, cy, targetPaint)
                canvas.drawText("-", startX + w1 + 2f, cy, textPaint)
                canvas.drawText(parts[1], startX + w1 + dashW + 4f, cy, targetPaint)
            }
            
            for (j in 0 until 3) {
                canvas.drawCircle(cx - 15f + j * 15f, cy + 20f, 4f, circlePaint)
            }
        }
        currentY += block1Height + 10f

        var block2Height = 450f
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block2Height), 12f, 12f, blockBgPaint)
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block2Height), 12f, 12f, blockBorderPaint)
        canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
        canvas.drawText("2", margin + 15f, currentY + 19f, circleNumText)
        canvas.drawText("Какой звук в слове? Назови картинку и обведи букву", margin + 35f, currentY + 19f, blockTitlePaint)

        var cellW = (PAGE_WIDTH - margin * 2 - 20f) / 4
        var cellH = 135f
        
        var emojiPaint = Paint()
        emojiPaint.textSize = 36f
        emojiPaint.textAlign = Paint.Align.CENTER
        
        var wordPaint = Paint()
        wordPaint.isAntiAlias = true
        wordPaint.textSize = 11f
        wordPaint.color = Color.DKGRAY
        wordPaint.typeface = safeTypeface
        wordPaint.textAlign = Paint.Align.CENTER
        
        for (index in pairData.words.indices) {
            var item = pairData.words[index]
            var col = index % 4
            var row = index / 4
            
            var left = margin + 10f + col * cellW
            var top = currentY + 30f + row * cellH
            var right = left + cellW - 5f
            var bottom = top + cellH - 10f

            var whitePaint =
