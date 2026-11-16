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
        
        val boxPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val boxBorder = Paint().apply { color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1f }
        
        // Имя
        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(margin, 25f, margin + 120f, 45f), 5f, 5f, boxBorder)
        canvas.drawText("Имя:", margin + 5f, 38f, Paint().apply { textSize = textSize; color = Color.GRAY; isAntiAlias = true })
        canvas.drawLine(margin + 30f, 40f, margin + 110f, 40f, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })

        // Звук
        val centerRect = RectF(PAGE_WIDTH/2f - 60f, 25f, PAGE_WIDTH/2f + 60f, 45f)
        canvas.drawRoundRect(centerRect, 5f, 5f, Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL })
        canvas.drawText("Звук ${config.sound.letter}", PAGE_WIDTH/2f - 20f, 38f, Paint().apply { textSize = textSize; color = Color.DKGRAY; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD })

        // Дата
        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxPaint)
        canvas.drawRoundRect(RectF(PAGE_WIDTH - margin - 120f, 25f, PAGE_WIDTH - margin, 45f), 5f, 5f, boxBorder)
        canvas.drawText("Дата:", PAGE_WIDTH - margin - 115f, 38f, Paint().apply { textSize = textSize; color = Color.GRAY; isAntiAlias = true })
        canvas.drawLine(PAGE_WIDTH - margin - 85f, 40f, PAGE_WIDTH - margin - 10f, 40f, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 28f; color = greenCircleColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(title, PAGE_WIDTH / 2f, 80f, titlePaint)

        // Подзаголовок
        val subPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }
        canvas.drawText(sub, PAGE_WIDTH / 2f, 100f, subPaint)
    }

    private fun drawHighlightedText(canvas: Canvas, text: String, highlightIndex: Int, x: Float, y: Float, basePaint: Paint, highlightPaint: Paint) {
        if (highlightIndex < 0 || highlightIndex >= text.length) {
            canvas.drawText(text, x, y, basePaint)
            return
        }
        val prefix = text.substring(0, highlightIndex)
        val target = text.substring(highlightIndex, highlightIndex + 1)
        val suffix = text.substring(highlightIndex + 1)
        
        val w1 = basePaint.measureText(prefix)
        val w2 = highlightPaint.measureText(target)
        val w3 = basePaint.measureText(suffix)
        val totalWidth = w1 + w2 + w3
        
        val startX = x - totalWidth / 2f
        
        canvas.drawText(prefix, startX, y, basePaint)
        canvas.drawText(target, startX + w1, y, highlightPaint)
        canvas.drawText(suffix, startX + w1 + w2, y, basePaint)
    }

    private fun drawSyllablesPage(canvas: Canvas, config: SoundAutomationConfig, syllables: List<Pair<String, Int>>) {
        drawHeader(canvas, config, "Звук ${config.sound.letter}: слоги", "Прочитай или повтори за взрослым каждый слог три раза. После каждого раза закрась кружок")
        
        val margin = 40f
        val startY = 130f
        
        val blocks = listOf(
            "Слог в начале" to syllables.subList(0, 4),
            "Слог в конце" to syllables.subList(4, 8),
            "Между гласными" to syllables.subList(8, 12),
            "С другим согласным" to syllables.subList(12, 16),
            "Цепочки" to syllables.subList(16, 18)
        )

        val blockBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val blockBorderPaint = Paint().apply { color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        
        val circleNumBg = Paint().apply { color = greenCircleColor; style = Paint.Style.FILL }
        val circleNumText = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val blockTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }

        val cardBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = Color.parseColor("#E0E0E0"); style = Paint.Style.STROKE; strokeWidth = 1f }
        
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val highlightPaint = Paint(textPaint).apply { color = highlightColor }
        val circlePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }

        var currentY = startY
        var blockIndex = 1

        for ((title, items) in blocks) {
            if (items.isEmpty()) continue
            val isTwoRows = items.size > 4
            val blockHeight = if (isTwoRows) 115f else 80f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText(blockIndex.toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            val itemWidth = (PAGE_WIDTH - margin * 2 - 20f) / 4
            
            items.forEachIndexed { index, item ->
                val col = index % 4
                val row = index / 4
                
                val cardLeft = margin + 10f + col * itemWidth
                val cardTop = currentY + 30f + row * 55f
                val cardRight = cardLeft + itemWidth - 5f
                val cardBottom = cardTop + 45f

                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBgPaint)
                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBorderPaint)
                
                val cx = cardLeft + (cardRight - cardLeft) / 2f
                val cy = cardTop + (cardBottom - cardTop) / 2f

                drawHighlightedText(canvas, item.first, item.second, cx, cy + 8f, textPaint, highlightPaint)
                
                val circleRadius = 4f
                val circleSpacing = 12f
                val startCircleX = cx - circleSpacing
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * circleSpacing, cy + 20f, circleRadius, circlePaint)
                }
            }
            currentY += blockHeight + 10f
            blockIndex++
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

        val blockBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val blockBorderPaint = Paint().apply { color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        
        val circleNumBg = Paint().apply { color = greenCircleColor; style = Paint.Style.FILL }
        val circleNumText = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val blockTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }

        val cardBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = Color.parseColor("#E0E0E0"); style = Paint.Style.STROKE; strokeWidth = 1f }
        
        val wordPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val highlightPaint = Paint(wordPaint).apply { color = highlightColor }
        val emojiPaint = Paint().apply { textSize = 36f; textAlign = Paint.Align.CENTER }
        
        val miniBoxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        val circlePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }

        var blockIndex = 1

        for ((title, words) in sections) {
            if (words.isEmpty()) continue
            val blockHeight = 160f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText(blockIndex.toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            // Верхний правый индикатор
            val indicatorX = PAGE_WIDTH - margin - 40f
            val indicatorY = currentY + 10f
            for (i in 0 until 3) {
                val rect = RectF(indicatorX + i * 12f, indicatorY, indicatorX + i * 12f + 10f, indicatorY + 10f)
                if (i == (blockIndex - 1)) {
                    canvas.drawRoundRect(rect, 3f, 3f, Paint().apply { color = Color.parseColor("#FFB74D"); style = Paint.Style.FILL })
                } else {
                    canvas.drawRoundRect(rect, 3f, 3f, Paint().apply { color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 1f })
                }
            }

            val itemWidth = (PAGE_WIDTH - margin * 2 - 20f) / 4
            
            words.forEachIndexed { index, item ->
                val col = index % 4
                val cardLeft = margin + 10f + col * itemWidth
                val cardTop = currentY + 30f
                val cardRight = cardLeft + itemWidth - 5f
                val cardBottom = cardTop + 120f

                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBgPaint)
                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBorderPaint)
                
                val cx = cardLeft + (cardRight - cardLeft) / 2f
                
                canvas.drawText(item.emoji, cx, cardTop + 50f, emojiPaint)
                drawHighlightedText(canvas, item.word, item.highlightIndex, cx, cardTop + 75f, wordPaint, highlightPaint)

                // Мини-сетка позиции звука
                val boxW = 16f
                val boxH = 8f
                val startBoxX = cx - (boxW * 3 + 2f) / 2f
                val boxY = cardTop + 85f
                for (i in 0 until 3) {
                    val r = RectF(startBoxX + i * (boxW + 1f), boxY, startBoxX + i * (boxW + 1f) + boxW, boxY + boxH)
                    if (i == item.position) {
                        canvas.drawRoundRect(r, 2f, 2f, Paint().apply { color = Color.parseColor("#FFB74D"); style = Paint.Style.FILL })
                    } else {
                        canvas.drawRoundRect(r, 2f, 2f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })
                    }
                }

                // Кружочки
                val startCircleX = cx - 12f
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * 12f, cardTop + 105f, 4f, circlePaint)
                }
            }
            currentY += blockHeight + 10f
            blockIndex++
        }
    }

    private fun drawWhereIsSoundPage(canvas: Canvas, config: SoundAutomationConfig, words: List<SoundWord>, isAnswer: Boolean) {
        val margin = 40f
        
        if (isAnswer) {
            canvas.drawColor(bgColor)
            val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = greenCircleColor; typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER }
            canvas.drawText("Ответы", PAGE_WIDTH / 2f, 60f, titlePaint)
            val subPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }
            canvas.drawText("Для взрослого", PAGE_WIDTH / 2f, 80f, subPaint)

            val headerTextPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText("Где живёт звук ${config.sound.letter}?", margin, 120f, headerTextPaint)

            var currentY = 140f
            val rowHeight = 35f
            val textPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = textColor; typeface = Typeface.DEFAULT }
            val emojiPaint = Paint().apply { textSize = 20f; textAlign = Paint.Align.CENTER }
            val numBg = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
            val numBorder = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
            
            words.forEachIndexed { index, word ->
                val col = index % 2
                val row = index / 2
                if (col == 0 && row > 0) currentY += rowHeight
                
                val left = margin + col * ((PAGE_WIDTH - margin*2)/2f)
                
                // Белая капсула
                val pillRect = RectF(left, currentY - 12f, left + 230f, currentY + 12f)
                canvas.drawRoundRect(pillRect, 12f, 12f, numBg)
                canvas.drawRoundRect(pillRect, 12f, 12f, numBorder)
                
                canvas.drawText("${index + 1}", left + 10f, currentY + 4f, textPaint)
                canvas.drawText(word.emoji, left + 35f, currentY + 6f, emojiPaint)
                canvas.drawText(word.word.lowercase(), left + 55f, currentY + 4f, textPaint)

                // Мини-схема (оранжевая таблетка)
                val boxSize = 12f
                val boxX = left + 150f
                val boxY = currentY - 6f
                
                val posText = when(word.position) {
                    0 -> "в начале"
                    1 -> "в середине"
                    else -> "в конце"
                }
                
                for (i in 0 until 3) {
                    val rect = RectF(boxX + i * (boxSize + 2f), boxY, boxX + i * (boxSize + 2f) + boxSize, boxY + boxSize)
                    if (i == word.position) {
                        val pill = RectF(rect.left - 2f, rect.top - 1f, rect.right + 2f, rect.bottom + 1f)
                        canvas.drawRoundRect(pill, 4f, 4f, Paint().apply { color = Color.parseColor("#FFB74D"); style = Paint.Style.FILL })
                    } else {
                        canvas.drawRoundRect(rect, 2f, 2f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
                        canvas.drawRoundRect(rect, 2f, 2f, numBorder)
                    }
                }
                canvas.drawText(posText, boxX + 50f, currentY + 4f, Paint(textPaint).apply { color = Color.GRAY })
            }
            return
        }

        drawHeader(canvas, config, "Где живёт звук ${config.sound.letter}?", "Назови картинку. Где звук — в начале, в середине или в конце слова? Закрась клетку")

        val cols = 4
        val tableTop = 130f
        val cellWidth = (PAGE_WIDTH - margin * 2) / cols
        val cellHeight = 170f

        val cardBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val numBgPaint = Paint().apply { color = greenCircleColor; style = Paint.Style.FILL }
        val numTextPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        
        val emojiPaint = Paint().apply { textSize = 46f; textAlign = Paint.Align.CENTER }
        val wordPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }

        words.forEachIndexed { index, word ->
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

            for (i in 0 until 3) {
                val r = RectF(startX + i * (boxSize + boxSpacing), boxY, startX + i * (boxSize + boxSpacing) + boxSize, boxY + boxSize)
                canvas.drawRoundRect(r, 2f, 2f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
                canvas.drawRoundRect(r, 2f, 2f, boxPaint)
            }
        }

        // Подсказка внизу
        val legendY = PAGE_HEIGHT - 40f
        val legendBoxX = PAGE_WIDTH / 2f - 60f
        val legendTextPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY }
        
        for (i in 0 until 3) {
            val r = RectF(legendBoxX + i * 22f, legendY - 10f, legendBoxX + i * 22f + 20f, legendY + 10f)
            canvas.drawRoundRect(r, 2f, 2f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
            canvas.drawRoundRect(r, 2f, 2f, boxPaint)
        }
        canvas.drawText("клеточки: начало — середина — конец слова", legendBoxX + 70f, legendY + 4f, legendTextPaint)
    }

    private fun drawPhrasesPage(canvas: Canvas, config: SoundAutomationConfig, phrases: List<Pair<String, Int>>) {
        drawHeader(canvas, config, "Звук ${config.sound.letter} во фразах", "Повтори за взрослым чистоговорку или фразу. После каждого раза закрась кружок")

        val margin = 40f
        var currentY = 130f

        val blockBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val blockBorderPaint = Paint().apply { color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        
        val circleNumBg = Paint().apply { color = greenCircleColor; style = Paint.Style.FILL }
        val circleNumText = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val blockTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }

        val textPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = textColor; typeface = Typeface.DEFAULT }
        val highlightPaint = Paint(textPaint).apply { color = highlightColor }
        val circlePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val dashPaint = Paint().apply { color = Color.parseColor("#E0E0E0"); strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 4f), 0f) }

        val blocks = listOf(
            "Чистоговорки" to phrases.take(5),
            "Скажи фразу" to phrases.drop(5)
        )

        var blockIndex = 1

        for ((title, items) in blocks) {
            if (items.isEmpty()) continue
            val blockHeight = items.size * 40f + 30f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText(blockIndex.toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            items.forEachIndexed { index, item ->
                val cy = currentY + 45f + index * 40f
                
                // Эмодзи-маркер
                canvas.drawText("🦁", margin + 15f, cy, Paint().apply { textSize = 16f })
                
                // Фраза (слева)
                drawHighlightedText(canvas, item.first, item.second, margin + 200f, cy, textPaint, highlightPaint)
                
                // Линия
                canvas.drawLine(margin + 15f, cy + 12f, PAGE_WIDTH - margin - 15f, cy + 12f, dashPaint)

                // 3 кружочка (справа)
                val startCircleX = PAGE_WIDTH - margin - 60f
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * 15f, cy - 4f, 5f, circlePaint)
                }
            }
            currentY += blockHeight + 10f
            blockIndex++
        }
    }

    private fun drawPairsPage(canvas: Canvas, config: SoundAutomationConfig) {
        val pairData = SoundAutomationData.getPairData(config.pair)
        drawHeader(canvas, config, "Различаем ${config.pair.title}", "Слушай внимательно: какой звук ты слышишь в слове?")

        val margin = 40f
        var currentY = 130f

        val blockBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val blockBorderPaint = Paint().apply { color = borderColor; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        
        val circleNumBg = Paint().apply { color = greenCircleColor; style = Paint.Style.FILL }
        val circleNumText = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val blockTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }

        val textPaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val highlightPaint = Paint(textPaint).apply { color = highlightColor }
        val circlePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }

        // Блок 1: Слоги
        val block1Height = 100f
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block1Height), 12f, 12f, blockBgPaint)
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block1Height), 12f, 12f, blockBorderPaint)
        canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
        canvas.drawText("1", margin + 15f, currentY + 19f, circleNumText)
        canvas.drawText("Повтори пары слогов", margin + 35f, currentY + 19f, blockTitlePaint)

        val sylWidth = (PAGE_WIDTH - margin * 2) / 4
        pairData.syllables.take(4).forEachIndexed { i, syl ->
            val cx = margin + i * sylWidth + sylWidth / 2
            val cy = currentY + 45f
            // СА - ША. Выделяем цветом (упрощенно)
            val parts = syl.split(" — ")
            val targetPaint = Paint(textPaint).apply { color = highlightColor }
            if (parts.size == 2) {
                canvas.drawText(parts[0], cx - 20f, cy, targetPaint)
                canvas.drawText("—", cx, cy, textPaint)
                canvas.drawText(parts[1], cx + 20f, cy, targetPaint)
            }
            
            for (j in 0 until 3) {
                canvas.drawCircle(cx - 15f + j * 15f, cy + 20f, 4f, circlePaint)
            }
        }
        currentY += block1Height + 10f

        // Блок 2: Слова (12 карточек)
        val block2Height = 450f
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block2Height), 12f, 12f, blockBgPaint)
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + block2Height), 12f, 12f, blockBorderPaint)
        canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
        canvas.drawText("2", margin + 15f, currentY + 19f, circleNumText)
        canvas.drawText("Какой звук в слове? Назови картинку и обведи букву", margin + 35f, currentY + 19f, blockTitlePaint)

        val cellW = (PAGE_WIDTH - margin * 2 - 20f) / 4
        val cellH = 135f
        val emojiPaint = Paint().apply { textSize = 36f; textAlign = Paint.Align.CENTER }
        val wordPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.DKGRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        
        pairData.words.forEachIndexed { index, item ->
            val col = index % 4
            val row = index / 4
            
            val left = margin + 10f + col * cellW
            val top = currentY + 30f + row * cellH
            val right = left + cellW - 5f
            val bottom = top + cellH - 10f

            // Белая карточка
            canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
            canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })

            // Номер
            canvas.drawCircle(left + 12f, top + 12f, 8f, Paint().apply { color = greenCircleColor; style = Paint.Style.FILL })
            canvas.drawText("${index + 1}", left + 12f, top + 15f, Paint().apply { textSize = 10f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD })

            val cx = left + (right - left) / 2f
            canvas.drawText(item.emoji, cx, top + 55f, emojiPaint)
            canvas.drawText(item.word.lowercase(), cx, top + 75f, wordPaint)

            // Кнопки выбора букв
            val btnW = 30f
            val btnH = 16f
            val btnY = top + 85f
            
            // Левая буква
            val leftBtn = RectF(cx - btnW - 5f, btnY, cx - 5f, btnY + btnH)
            canvas.drawRoundRect(leftBtn, 4f, 4f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
            canvas.drawRoundRect(leftBtn, 4f, 4f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })
            canvas.drawText(config.pair.first.letter, cx - btnW / 2f - 5f, btnY + 12f, Paint().apply { textSize = 10f; color = textColor; textAlign = Paint.Align.CENTER })
            canvas.drawText("💧", cx - btnW + 2f, btnY + 12f, Paint().apply { textSize = 8f; textAlign = Paint.Align.CENTER })

            // Правая буква
            val rightBtn = RectF(cx + 5f, btnY, cx + btnW + 5f, btnY + btnH)
            canvas.drawRoundRect(rightBtn, 4f, 4f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
            canvas.drawRoundRect(rightBtn, 4f, 4f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })
            canvas.drawText(config.pair.second.letter, cx + btnW / 2f + 5f, btnY + 12f, Paint().apply { textSize = 10f; color = textColor; textAlign = Paint.Align.CENTER })
            canvas.drawText("💧", cx + 5f, btnY + 12f, Paint().apply { textSize = 8f; textAlign = Paint.Align.CENTER })
        }
    }

    private fun drawPairsAnswers(canvas: Canvas, config: SoundAutomationConfig) {
        val pairData = SoundAutomationData.getPairData(config.pair)
        
        val margin = 40f
        
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = greenCircleColor; typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы", PAGE_WIDTH / 2f, 60f, titlePaint)
        val subPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }
        canvas.drawText("Для взрослого", PAGE_WIDTH / 2f, 80f, subPaint)

        val headerTextPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("Различаем ${config.pair.title}", margin, 120f, headerTextPaint)

        var currentY = 140f
        val rowHeight = 35f
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = textColor; typeface = Typeface.DEFAULT }
        val emojiPaint = Paint().apply { textSize = 20f; textAlign = Paint.Align.CENTER }
        val numBg = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val numBorder = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        
        val greenBoxBg = Paint().apply { color = greenCircleColor; style = Paint.Style.FILL }
        val greenBoxText = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }

        pairData.words.forEachIndexed { index, word ->
            val col = index % 2
            val row = index / 2
            if (col == 0 && row > 0) currentY += rowHeight
            
            val left = margin + col * ((PAGE_WIDTH - margin*2)/2f)
            
            // Белая капсула
            val pillRect = RectF(left, currentY - 12f, left + 230f, currentY + 12f)
            canvas.drawRoundRect(pillRect, 12f, 12f, numBg)
            canvas.drawRoundRect(pillRect, 12f, 12f, numBorder)
            
            canvas.drawText("${index + 1}", left + 10f, currentY + 4f, textPaint)
            canvas.drawText(word.emoji, left + 35f, currentY + 6f, emojiPaint)
            canvas.drawText(word.word.lowercase(), left + 55f, currentY + 4f, textPaint)

            // Квадрат с буквой
            val boxRect = RectF(left + 180f, currentY - 10f, left + 210f, currentY + 10f)
            canvas.drawRoundRect(boxRect, 8f, 8f, greenBoxBg)
            canvas.drawText(word.correctSound, left + 195f, currentY + 4f, greenBoxText)
        }
    }
}
