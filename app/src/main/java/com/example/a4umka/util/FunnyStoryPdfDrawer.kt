package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.FunnyStory
import com.example.a4umka.data.FunnyStoryConfig
import com.example.a4umka.data.FunnyStoryWriter

object FunnyStoryPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: FunnyStoryConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val stories = config.currentStories

        for (story in stories) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA"))
            drawPage(canvas, config, story)
            pages.add(bmp)
        }
        return pages
    }

    fun drawDocument(document: PdfDocument, config: FunnyStoryConfig, context: Context) {
        var pageNum = 1
        for (story in config.currentStories) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA"))
            drawPage(page.canvas, config, story)
            document.finishPage(page)
        }
    }

    private fun drawPage(canvas: Canvas, config: FunnyStoryConfig, story: FunnyStory) {
        val margin = 40f
        val textColor = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#33691E")
        val isChild = config.writer == FunnyStoryWriter.CHILD

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 28f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText(story.title, PAGE_WIDTH / 2f, 60f, titlePaint)
        
        val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("весёлая история — приключения", PAGE_WIDTH / 2f, 80f, subtitlePaint)

        // Рамка с предупреждением
        val boxPaint = Paint().apply { color = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#E57373"); style = Paint.Style.STROKE; strokeWidth = 1.5f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
        val boxRect = RectF(margin, 100f, PAGE_WIDTH - margin, 145f)
        canvas.drawRoundRect(boxRect, 8f, 8f, boxPaint)
        
        val warnPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("Сначала спросите слова — и не показывайте ребёнку нижнюю половину листа.", margin + 10f, 120f, warnPaint)
        canvas.drawText("В этом вся соль: слова придумываются вслепую.", margin + 10f, 138f, warnPaint)

        // Вопросы
        val stepPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("1. Спросите слова", margin, 180f, stepPaint)

        var currentY = 210f
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        val lineLength = if (isChild) 300f else 250f
        val spacing = if (isChild) 40f else 30f

        for (i in story.questions.indices) {
            val qText = "${i + 1}. ${story.questions[i]}"
            canvas.drawText(qText, margin, currentY, numPaint)
            canvas.drawLine(PAGE_WIDTH - margin - lineLength, currentY - 4f, PAGE_WIDTH - margin, currentY - 4f, linePaint)
            currentY += spacing
        }

        // Текст истории
        currentY += 20f
        canvas.drawText("2. Впишите их в историю и прочитайте вслух", margin, currentY, stepPaint)
        currentY += 30f

        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val lineHeight = 25f
        
        // Разбиваем шаблон на слова для переноса
        val words = story.template.split(" ")
        var line = ""
        val lines = mutableListOf<String>()
        for (word in words) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (bodyPaint.measureText(test) > PAGE_WIDTH - margin * 2) {
                lines.add(line)
                line = word
            } else {
                line = test
            }
        }
        if (line.isNotEmpty()) lines.add(line)

        for (l in lines) {
            canvas.drawText(l, margin, currentY, bodyPaint)
            currentY += lineHeight
        }

        // Рамка для рисования
        currentY += 20f
        if (currentY < PAGE_HEIGHT - 120f) {
            val drawFrame = RectF(margin, currentY, PAGE_WIDTH - margin, PAGE_HEIGHT - 60f)
            canvas.drawRoundRect(drawFrame, 10f, 10f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) })
            val hintPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
            canvas.drawText("Нарисуй самый смешной момент этой истории", PAGE_WIDTH / 2f, currentY + 20f, hintPaint)
        }
    }
}
