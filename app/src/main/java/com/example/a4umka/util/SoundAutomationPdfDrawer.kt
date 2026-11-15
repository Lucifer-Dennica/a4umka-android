package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.SoundAutomationConfig
import com.example.a4umka.data.SoundAutomationData
import com.example.a4umka.data.SoundItem
import com.example.a4umka.data.SpeechMode

object SoundAutomationPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: SoundAutomationConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val data = SoundAutomationData.getData(config.sound)

        // В зависимости от режима генерируем нужные листы
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
        val data = SoundAutomationData.getData(config.sound)
        var pageNum = 1

        fun startNewPage(): Canvas {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.parseColor("#FAFAFA"))
            return page.canvas
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.SYLLABLES) {
            val canvas = startNewPage()
            drawSyllablesPage(canvas, config, data.syllables)
            document.finishPage(document.pages.last())
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.WORDS) {
            val canvas = startNewPage()
            drawWordsPage(canvas, config, data)
            document.finishPage(document.pages.last())
        }

        if (config.mode == SpeechMode.KIT || config.mode == SpeechMode.PHRASES) {
            val canvas = startNewPage()
            drawPhrasesPage(canvas, config, data.phrases)
            document.finishPage(document.pages.last())
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
        
        // Разбиваем на блоки (примерно как на скрине)
        val blocks = listOf(
            "Слог в начале" to syllables.subList(0, 4),
            "Слог в конце" to syllables.subList(4, 8),
            "Между гласными" to syllables.subList(8, 12),
            "С другим согласным" to syllables.subList(12, 16),
            "Цепочки" to syllables.subList(16, 18)
        )

        val blockBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val blockBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
        val blockTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 22f; color = Color.parseColor("#D84315"); typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val circlePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }

        var currentY = startY
        for ((title, items) in blocks) {
            if (items.isEmpty()) continue
            val blockHeight = if (items.size > 4) 100f else 70f // Упрощенно
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 10f, 10f, blockBgPaint)
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 10f, 10f, blockBorderPaint)
            
            canvas.drawText(title, margin + 10f, currentY + 20f, blockTitlePaint)

            val cols = if (items.size > 4) 4 else items.size
            val itemWidth = (PAGE_WIDTH - margin * 2) / cols
            val itemStartY = currentY + 35f

            items.forEachIndexed { index, item ->
                val col = index % cols
                val row = index / cols
                val cx = margin + col * itemWidth + itemWidth / 2f
                val cy = itemStartY + row * 45f

                canvas.drawText(item.text, cx, cy + 15f, textPaint)
                
                // 3 кружочка под слогом
                val circleRadius = 4f
                val circleSpacing = 12f
                val startCircleX = cx - circleSpacing
                for (i in 0 until 3) {
                    canvas.drawCircle(startCircleX + i * circleSpacing, cy + 28f, circleRadius, circlePaint)
                }
            }
            currentY += blockHeight + 10f
        }
    }

    private fun drawWordsPage(canvas: Canvas, config: SoundAutomationConfig, data: com.example.a4umka.data.SoundData) {
        drawHeader(canvas, config, "Звук ${config.sound.letter}: слова")
        val margin = 40f
        var currentY = 130f

        val sections = listOf(
            "Звук в начале" to data.wordsStart,
            "Звук в середине" to data.wordsMiddle,
            "Звук в конце" to data.wordsEnd
        )

        val blockBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val textPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val emojiPaint = Paint().apply { textSize = 40f; textAlign = Paint.Align.CENTER }

        for ((title, words) in sections) {
            if (words.isEmpty()) continue
            val rows = (words.size + 1) / 2
            val blockHeight = rows * 80f + 30f
            
            canvas.drawRoundRect(RectF(margin, currentY, PAGE_WIDTH - margin, currentY + blockHeight), 10f, 10f, blockBgPaint)
            canvas.drawText(title, margin + 10f, currentY + 20f, Paint().apply { textSize = 12f; typeface = Typeface.DEFAULT_BOLD; color = Color.DKGRAY })

            words.forEachIndexed { index, word ->
                val col = index % 2
                val row = index / 2
                val cx = margin + col * ((PAGE_WIDTH - margin*2)/2) + (PAGE_WIDTH - margin*2)/4
                val cy = currentY + 50f + row * 80f

                canvas.drawText(word.emoji, cx, cy, emojiPaint)
                canvas.drawText(word.text, cx, cy + 20f, textPaint)
            }
            currentY += blockHeight + 10f
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
            
            // Кружочки
            val circleRadius = 6f
            val circleSpacing = 20f
            val startX = PAGE_WIDTH / 2f - circleSpacing
            for (i in 0 until 3) {
                canvas.drawCircle(startX + i * circleSpacing, currentY + 30f, circleRadius, circlePaint)
            }
            currentY += 90f
        }
    }
}
