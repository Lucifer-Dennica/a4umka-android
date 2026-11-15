package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.*

object PrepositionPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    private val bitmapCache = mutableMapOf<String, Bitmap?>()

    fun clearCache() { bitmapCache.clear() }

    fun generatePreview(config: PrepositionConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val tasks = PrepositionData.generateTasks(config)

        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawMainPage(canvas, config, tasks, context)
        pages.add(bmp)

        if (config.showAnswers) {
            val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.drawColor(Color.WHITE)
            drawAnswersPage(ansCanvas, tasks)
            pages.add(ansBmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: PrepositionConfig, context: Context) {
        val tasks = PrepositionData.generateTasks(config)
        var pageNum = 1

        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
        var page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawMainPage(page.canvas, config, tasks, context)
        document.finishPage(page)

        if (config.showAnswers) {
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.WHITE)
            drawAnswersPage(page.canvas, tasks)
            document.finishPage(page)
        }
    }

    private fun drawMainPage(canvas: Canvas, config: PrepositionConfig, tasks: List<PrepositionTask>, context: Context) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // Шапка (Имя, Дата)
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawText("Имя:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 150f, 42f, linePaint)
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // Заголовок
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 32f; color = textColor; 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Предлоги в картинках", PAGE_WIDTH / 2f, 90f, titlePaint)

        val subPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        val subText = when(config.taskType) {
            PrepositionTaskType.FIND_WORD -> "посмотри на картинку и обведи верные слова"
            PrepositionTaskType.FIND_PICTURE -> "прочитай текст и обведи подходящую картинку"
            PrepositionTaskType.DRAW_SELF -> "прочитай задание и нарисуй предмет сам"
        }
        canvas.drawText(subText, PAGE_WIDTH / 2f, 110f, subPaint)

        // ДИНАМИЧЕСКАЯ СЕТКА (3 колонки для 8 и 9 заданий, иначе 2)
        val cols = if (tasks.size > 6) 3 else 2
        val rows = (tasks.size + cols - 1) / cols
        val tableTop = 140f
        val availableHeight = PAGE_HEIGHT - tableTop - 60f
        val cellWidth = (PAGE_WIDTH - margin * 2) / cols
        val cellHeight = availableHeight / rows

        val isSmall = cols == 3 // Флаг для уменьшения шрифтов и картинок

        val cardBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val numCirclePaint = Paint().apply { color = textColor; style = Paint.Style.FILL }
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }

        for (i in tasks.indices) {
            val col = i % cols
            val row = i / cols
            val left = margin + col * cellWidth + 5f
            val top = tableTop + row * cellHeight + 5f
            val right = left + cellWidth - 10f
            val bottom = top + cellHeight - 10f
            
            val task = tasks[i]

            // Фон карточки
            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, cardBorderPaint)

            // Номер
            canvas.drawCircle(left + 20f, top + 20f, 12f, numCirclePaint)
            canvas.drawText("${i + 1}", left + 20f, top + 25f, numPaint)

            val cx = left + (right - left) / 2f
            val cy = top + (if (isSmall) 50f else 60f)

            when (config.taskType) {
                PrepositionTaskType.FIND_WORD -> {
                    drawAssetImage(canvas, context, task.scene.folderName, task.preposition.fileName, cx, cy, if (isSmall) 60f else 100f, if (isSmall) 60f else 100f)
                    
                    val qPaint = Paint().apply { isAntiAlias = true; textSize = if (isSmall) 12f else 16f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                    canvas.drawText(task.question, cx, cy + (if (isSmall) 45f else 65f), qPaint)

                    val optionTextSize = if (isSmall) 9f else 12f
                    val optionPaint = Paint().apply { isAntiAlias = true; textSize = optionTextSize; color = Color.BLACK; textAlign = Paint.Align.CENTER }
                    val bubblePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
                    val bubbleBorder = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }

                    var optionY = cy + (if (isSmall) 55f else 80f)
                    val boxWidth = right - left - (if (isSmall) 10f else 40f)
                    val boxHeight = if (isSmall) 16f else 22f

                    task.options.forEach { option ->
                        val rect = RectF(cx - boxWidth / 2f, optionY, cx + boxWidth / 2f, optionY + boxHeight)
                        canvas.drawRoundRect(rect, boxHeight / 2f, boxHeight / 2f, bubblePaint)
                        canvas.drawRoundRect(rect, boxHeight / 2f, boxHeight / 2f, bubbleBorder)
                        // Центрируем текст по вертикали
                        val textY = optionY + (boxHeight / 2f) - ((optionPaint.descent() + optionPaint.ascent()) / 2f)
                        canvas.drawText(option, cx, textY, optionPaint)
                        optionY += boxHeight + 4f
                    }
                }
                PrepositionTaskType.FIND_PICTURE -> {
                    val qPaint = Paint().apply { isAntiAlias = true; textSize = if (isSmall) 11f else 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                    canvas.drawText(task.correctAnswer.replaceFirstChar { it.uppercase() }, cx, top + 40f, qPaint)

                    val spacing = (right - left - 10f) / 3f
                    val startX = left + 5f + spacing / 2f

                    val choices = listOf(task.preposition) + task.wrongPrepositions
                    val shuffledChoices = choices.shuffled()

                    for (j in 0 until 3) {
                        val x = startX + j * spacing
                        val rectH = if (isSmall) 45f else 60f
                        val rect = RectF(x - (spacing/2f - 2f), top + 55f, x + (spacing/2f - 2f), top + 55f + rectH)
                        
                        canvas.drawRoundRect(rect, 8f, 8f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
                        canvas.drawRoundRect(rect, 8f, 8f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })

                        drawAssetImage(canvas, context, task.scene.folderName, shuffledChoices[j].fileName, x, top + 55f + rectH/2f, if (isSmall) 40f else 55f, if (isSmall) 40f else 55f)
                    }
                }
                PrepositionTaskType.DRAW_SELF -> {
                    val qPaint = Paint().apply { isAntiAlias = true; textSize = if (isSmall) 11f else 13f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                    canvas.drawText("Нарисуй ${task.scene.targetName} ${task.correctAnswer}", cx, top + 40f, qPaint)

                    val drawRect = RectF(left + 10f, top + 55f, right - 10f, bottom - 10f)
                    val dashPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
                    canvas.drawRoundRect(drawRect, 8f, 8f, dashPaint)
                }
            }
        }
    }

    private fun drawAssetImage(canvas: Canvas, context: Context, folder: String, fileName: String, cx: Float, cy: Float, maxWidth: Float, maxHeight: Float) {
        val assetPath = "prepositions/$folder/$fileName.png"
        var bitmap = bitmapCache[assetPath]
        
        if (bitmap == null && !bitmapCache.containsKey(assetPath)) {
            bitmap = try {
                context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
            } catch (e: Exception) {
                null
            }
            bitmapCache[assetPath] = bitmap
        }

        if (bitmap != null) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            var drawW = maxWidth
            var drawH = maxWidth / ratio
            if (drawH > maxHeight) {
                drawH = maxHeight
                drawW = maxHeight * ratio
            }
            
            val rect = RectF(cx - drawW / 2f, cy - drawH / 2f, cx + drawW / 2f, cy + drawH / 2f)
            canvas.drawBitmap(bitmap, null, rect, Paint().apply { isFilterBitmap = true; isAntiAlias = true })
        } else {
            // Заглушка, если картинку забыли положить
            val errorPaint = Paint().apply { color = Color.RED; style = Paint.Style.STROKE; strokeWidth = 2f }
            val rect = RectF(cx - maxWidth / 2f, cy - maxHeight / 2f, cx + maxWidth / 2f, cy + maxHeight / 2f)
            canvas.drawRect(rect, errorPaint)
            
            val textPaint = Paint().apply { color = Color.RED; textSize = 8f; textAlign = Paint.Align.CENTER }
            canvas.drawText("Нет картинки:", cx, cy - 5f, textPaint)
            canvas.drawText("$folder/$fileName", cx, cy + 5f, textPaint)
        }
    }

    private fun drawAnswersPage(canvas: Canvas, tasks: List<PrepositionTask>) {
        val margin = 40f
        var currentY = 60f
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы для взрослого", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        
        for ((index, task) in tasks.withIndex()) {
            val text = "${index + 1}. ${task.correctAnswer}"
            canvas.drawText(text, margin, currentY, ansPaint)
            currentY += 25f
        }
    }
}
