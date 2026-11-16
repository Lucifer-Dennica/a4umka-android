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

    fun generatePreview(config: SoundAutomationConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()

        if (config.mode == SpeechMode.PAIRS) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawPairsPage(canvas, config)
            pages.add(bmp)
            return pages
        }

        val data = SoundAutomationData.getData(config.sound)

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.SYLLABLES) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawSyllablesPage(canvas, config, data.syllables)
            pages.add(bmp)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WORDS) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawWordsPage(canvas, config, data)
            pages.add(bmp)
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WHERE_IS_SOUND) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            drawWhereIsSoundPage(canvas, config, data.whereIsSound, isAnswer = false)
            pages.add(bmp)

            if (config.showAnswers) {
                val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                val ansCanvas = Canvas(ansBmp)
                ansCanvas.drawColor(Color.WHITE)
                drawWhereIsSoundPage(ansCanvas, config, data.whereIsSound, isAnswer = true)
                pages.add(ansBmp)
            }
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.PHRASES) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
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
            page.canvas.drawColor(Color.parseColor("#FAFAFA"))
            return page
        }

        if (config.mode == SpeechMode.PAIRS) {
            val page = startNewPage()
            drawPairsPage(page.canvas, config)
            document.finishPage(page)
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
                ansPage.canvas.drawColor(Color.WHITE)
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

    private fun drawHeader(canvas: Canvas, config: SoundAutomationConfig, title: String) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")
        
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        canvas.drawText("Имя:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 150f, 42f, linePaint)
        
        val centerRect = RectF(PAGE_WIDTH/2f - 50f, 25f, PAGE_WIDTH/2f + 50f, 45f)
        canvas.drawRoundRect(centerRect, 5f, 5f, Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL })
        canvas.drawText("Звук ${config.sound.letter}", PAGE_WIDTH/2f - 20f, 40f, headerPaint)

        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 28f; color = textColor; 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(title, PAGE_WIDTH / 2f, 80f, titlePaint)

        val subPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("Прочитай или повтори за взрослым 3 раза. После каждого раза закрась кружок", PAGE_WIDTH / 2f, 100f, subPaint)
    }

    private fun drawSyllablesPage(canvas: Canvas, config: SoundAutomationConfig, syllables: List<SoundItem>) {
        drawHeader(canvas, config, "Звук ${config.sound.letter}: слоги")
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
        val blockBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
        
        val circleNumBg = Paint().apply { color = Color.parseColor("#4CAF50"); style = Paint.Style.FILL }
        val circleNumText = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val blockTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }

        val cardBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = Color.parseColor("#E0E0E0"); style = Paint.Style.STROKE; strokeWidth = 1f }
        
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 22f; color = Color.parseColor("#D84315"); typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val circlePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }

        var currentY = startY
        var blockIndex = 1

        for ((title, items) in blocks) {
            if (items.isEmpty()) continue
            val isTwoRows = items.size > 4
            val blockHeight = if (isTwoRows) 110f else 75f
            
            // Рамка блока
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 12f, 12f, blockBorderPaint)
            
            // Номер блока и заголовок
            canvas.drawCircle(margin + 15f, currentY + 15f, 10f, circleNumBg)
            canvas.drawText(blockIndex.toString(), margin + 15f, currentY + 19f, circleNumText)
            canvas.drawText(title, margin + 35f, currentY + 19f, blockTitlePaint)

            val cols = if (isTwoRows) 4 else items.size
            val itemWidth = (PAGE_WIDTH - margin * 2 - 20f) / 4 // Фиксируем ширину под 4 колонки
            
            items.forEachIndexed { index, item ->
                val col = index % 4
                val row = index / 4
                
                val cardLeft = margin + 10f + col * itemWidth
                val cardTop = currentY + 30f + row * 50f
                val cardRight = cardLeft + itemWidth - 5f
                val cardBottom = cardTop + 40f

                // Карточка со слогом
                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBgPaint)
                canvas.drawRoundRect(RectF(cardLeft, cardTop, cardRight, cardBottom), 8f, 8f, cardBorderPaint)
                
                val cx = cardLeft + (cardRight - cardLeft) / 2f
                val cy = cardTop + (cardBottom - cardTop) / 2f

                canvas.drawText(item.text, cx, cy + 5f, textPaint)
                
                // Кружочки
                val circleRadius = 3.5f
                val circleSpacing = 10f
                val startCircleX = cx - circleSpacing
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * circleSpacing, cy + 15f, circleRadius, circlePaint)
                }
            }
            currentY += blockHeight + 10f
            blockIndex++
        }
    }

    private fun drawWordsPage(canvas: Canvas, config: SoundAutomationConfig, data: SoundData) {
        drawHeader(canvas, config, "Звук ${config.sound.letter}: слова")
        val margin = 40f
        var currentY = 130f

        val sections = listOf(
            "Звук в начале" to data.wordsStart,
            "Звук в середине" to data.wordsMiddle,
            "Звук в конце" to data.wordsEnd
        )

        val blockBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val blockBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
        val blockTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val emojiPaint = Paint().apply { textSize = 36f; textAlign = Paint.Align.CENTER }

        for ((title, words) in sections) {
            if (words.isEmpty()) continue
            val rows = (words.size + 1) / 2
            val blockHeight = rows * 70f + 30f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 10f, 10f, blockBgPaint)
            canvas.drawText(title, margin + 10f, currentY + 20f, blockTitlePaint)

            words.forEachIndexed { index, word ->
                val col = index % 2
                val row = index / 2
                val cx = margin + col * ((PAGE_WIDTH - margin*2)/2) + (PAGE_WIDTH - margin*2)/4
                val cy = currentY + 50f + row * 70f

                canvas.drawText(word.emoji, cx, cy, emojiPaint)
                canvas.drawText(word.text, cx, cy + 20f, textPaint)
            }
            currentY += blockHeight + 10f
        }
    }

    private fun drawWhereIsSoundPage(canvas: Canvas, config: SoundAutomationConfig, words: List<SoundItemWithPos>, isAnswer: Boolean) {
        val margin = 40f
        
        if (isAnswer) {
            canvas.drawColor(Color.WHITE)
            val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
            canvas.drawText("Ответы для взрослого", PAGE_WIDTH / 2f, 60f, titlePaint)
            val subPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
            canvas.drawText("Где живёт звук ${config.sound.letter}?", PAGE_WIDTH / 2f, 80f, subPaint)

            var currentY = 120f
            val itemHeight = 40f
            val textPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val emojiPaint = Paint().apply { textSize = 24f; textAlign = Paint.Align.CENTER }

            words.forEachIndexed { index, word ->
                val col = index % 2
                val row = index / 2
                if (col == 0 && row > 0) currentY += itemHeight
                
                val left = margin + col * ((PAGE_WIDTH - margin*2)/2)
                
                canvas.drawText("${index + 1}", left, currentY, textPaint)
                canvas.drawText(word.emoji, left + 30f, currentY, emojiPaint)
                canvas.drawText(word.text, left + 50f, currentY, textPaint)

                // Мини-схема
                val boxSize = 10f
                val boxX = left + 170f
                val boxY = currentY - 10f
                
                val posText = when(word.position) {
                    0 -> "в начале"
                    1 -> "в середине"
                    else -> "в конце"
                }
                
                for (i in 0 until 3) {
                    val rect = RectF(boxX + i * (boxSize + 2f), boxY, boxX + i * (boxSize + 2f) + boxSize, boxY + boxSize)
                    if (i == word.position) {
                        canvas.drawRect(rect, Paint().apply { color = Color.parseColor("#FFB74D"); style = Paint.Style.FILL })
                    } else {
                        canvas.drawRect(rect, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })
                    }
                }
                canvas.drawText(posText, boxX + 45f, currentY, Paint(textPaint).apply { color = Color.GRAY })
            }
            return
        }

        drawHeader(canvas, config, "Где живёт звук ${config.sound.letter}?")
        
        val subPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("Назови картинку. Где звук — в начале, в середине или в конце слова? Закрась клетку", PAGE_WIDTH / 2f, 100f, subPaint)

        val cols = 4
        val rows = 3
        val tableTop = 130f
        val cellWidth = (PAGE_WIDTH - margin * 2) / cols
        val cellHeight = (PAGE_HEIGHT - tableTop - 80f) / rows

        val cardBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        val numBgPaint = Paint().apply { color = Color.parseColor("#4CAF50"); style = Paint.Style.FILL }
        val numTextPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        
        val emojiPaint = Paint().apply { textSize = 36f; textAlign = Paint.Align.CENTER }
        val wordPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.DKGRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }

        words.forEachIndexed { index, word ->
            val col = index % cols
            val row = index / cols
            
            val left = margin + col * cellWidth + 5f
            val top = tableTop + row * cellHeight + 5f
            val right = left + cellWidth - 10f
            val bottom = top + cellHeight - 10f

            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBorderPaint)

            canvas.drawCircle(left + 15f, top + 15f, 10f, numBgPaint)
            canvas.drawText("${index + 1}", left + 15f, top + 19f, numTextPaint)

            val cx = left + (right - left) / 2f
            canvas.drawText(word.emoji, cx, top + 60f, emojiPaint)
            canvas.drawText(word.text, cx, top + 80f, wordPaint)

            // 3 клеточки
            val boxSize = 20f
            val boxSpacing = 2f
            val totalWidth = boxSize * 3 + boxSpacing * 2
            val startX = cx - totalWidth / 2f
            val boxY = top + 95f

            for (i in 0 until 3) {
                canvas.drawRect(startX + i * (boxSize + boxSpacing), boxY, startX + i * (boxSize + boxSpacing) + boxSize, boxY + boxSize, boxPaint)
            }
        }
    }

    private fun drawPhrasesPage(canvas: Canvas, config: SoundAutomationConfig, phrases: List<String>) {
        drawHeader(canvas, config, "Звук ${config.sound.letter}: чистоговорки")
        val margin = 40f
        var currentY = 150f

        val textPaint = Paint().apply { isAntiAlias = true; textSize = 22f; color = Color.parseColor("#33691E"); typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val circlePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 2f }

        for (phrase in phrases) {
            canvas.drawText("«$phrase»", PAGE_WIDTH / 2f, currentY, textPaint)
            
            val circleRadius = 6f
            val circleSpacing = 20f
            val startX = PAGE_WIDTH / 2f - circleSpacing
            for (i in 0 until 3) {
                canvas.drawCircle(startX + i * circleSpacing, currentY + 30f, circleRadius, circlePaint)
            }
            currentY += 90f
        }
    }

    private fun drawPairsPage(canvas: Canvas, config: SoundAutomationConfig) {
        val pairData = SoundAutomationData.getPairData(config.pair)
        val title = "Различаем: ${config.pair.title}"
        
        val margin = 40f
        val textColor = Color.parseColor("#33691E")
        
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawText("Имя:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 150f, 42f, linePaint)
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 28f; color = textColor; 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(title, PAGE_WIDTH / 2f, 80f, titlePaint)

        val subPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("Назови пары слов и обведи тот звук, который услышал", PAGE_WIDTH / 2f, 100f, subPaint)

        val blockBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val blockBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
        val blockTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        
        var currentY = 140f

        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + 80f), 10f, 10f, blockBgPaint)
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + 80f), 10f, 10f, blockBorderPaint)
        canvas.drawText("1. Слоги", margin + 10f, currentY + 20f, blockTitlePaint)

        val sylTextPaint = Paint().apply { isAntiAlias = true; textSize = 22f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val circlePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val sylWidth = (PAGE_WIDTH - margin * 2) / 4
        pairData.syllables.forEachIndexed { i, pair ->
            val cx = margin + i * sylWidth + sylWidth / 2
            canvas.drawText("${pair.first} - ${pair.second}", cx, currentY + 45f, sylTextPaint)
            canvas.drawCircle(cx - 10f, currentY + 65f, 4f, circlePaint)
            canvas.drawCircle(cx + 10f, currentY + 65f, 4f, circlePaint)
        }
        currentY += 100f

        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + 180f), 10f, 10f, blockBgPaint)
        canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + 180f), 10f, 10f, blockBorderPaint)
        canvas.drawText("2. Слова", margin + 10f, currentY + 20f, blockTitlePaint)

        val wordTextPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val emojiPaint = Paint().apply { textSize = 30f; textAlign = Paint.Align.CENTER }
        
        pairData.words.forEachIndexed { i, triple ->
            val col = i % 2
            val row = i / 2
            val cx = margin + col * ((PAGE_WIDTH - margin*2)/2) + (PAGE_WIDTH - margin*2)/4
            val cy = currentY + 60f + row * 60f

            canvas.drawText(triple.third, cx, cy - 10f, emojiPaint)
            canvas.drawText("${triple.first} - ${triple.second}", cx, cy + 15f, wordTextPaint)
            
            canvas.drawCircle(cx - 25f, cy + 30f, 4f, circlePaint)
            canvas.drawCircle(cx + 25f, cy + 30f, 4f, circlePaint)
        }
    }
}
