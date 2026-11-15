package com.example.a4umka.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.PrepositionConfig
import com.example.a4umka.data.PrepositionData
import com.example.a4umka.data.PrepositionTask
import com.example.a4umka.data.PrepositionTaskType
import com.example.a4umka.data.SceneObject

object PrepositionPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: PrepositionConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val tasks = PrepositionData.generateTasks(config)

        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawMainPage(canvas, config, tasks)
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
        drawMainPage(page.canvas, config, tasks)
        document.finishPage(page)

        if (config.showAnswers) {
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.WHITE)
            drawAnswersPage(page.canvas, tasks)
            document.finishPage(page)
        }
    }

    private fun drawMainPage(canvas: Canvas, config: PrepositionConfig, tasks: List<PrepositionTask>) {
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

        // Сетка задач (2 колонки)
        val cols = 2
        val rows = (tasks.size + 1) / 2
        val tableTop = 140f
        val availableHeight = PAGE_HEIGHT - tableTop - 60f
        val cellWidth = (PAGE_WIDTH - margin * 2) / cols
        val cellHeight = availableHeight / rows

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
            val cy = top + (bottom - top) * 0.35f

            when (config.taskType) {
                PrepositionTaskType.FIND_WORD -> {
                    drawScene(canvas, task.targetObject, task.baseObject, task.preposition, cx, cy)
                    
                    val qPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                    canvas.drawText(task.question, cx, cy + 45f, qPaint)

                    val optionPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.BLACK; textAlign = Paint.Align.CENTER }
                    val bubblePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
                    val bubbleBorder = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }

                    var optionY = cy + 60f
                    val boxWidth = right - left - 40f
                    val boxHeight = 22f

                    task.options.forEach { option ->
                        val rect = RectF(left + 20f, optionY, left + 20f + boxWidth, optionY + boxHeight)
                        canvas.drawRoundRect(rect, 11f, 11f, bubblePaint)
                        canvas.drawRoundRect(rect, 11f, 11f, bubbleBorder)
                        canvas.drawText(option, cx, optionY + 15f, optionPaint)
                        optionY += boxHeight + 4f
                    }
                }
                PrepositionTaskType.FIND_PICTURE -> {
                    val qPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                    canvas.drawText(task.correctAnswer.replaceFirstChar { it.uppercase() }, cx, top + 45f, qPaint)

                    val spacing = (right - left - 20f) / 3f
                    val startX = left + 10f + spacing / 2f

                    val choices = listOf(task.preposition) + task.wrongPrepositions
                    val shuffledChoices = choices.shuffled()

                    for (j in 0 until 3) {
                        val x = startX + j * spacing
                        val rect = RectF(x - 30f, top + 65f, x + 30f, top + 125f)
                        
                        canvas.drawRoundRect(rect, 8f, 8f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
                        canvas.drawRoundRect(rect, 8f, 8f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })

                        // Уменьшаем масштаб для мини-картинок
                        canvas.save()
                        canvas.translate(x, top + 95f)
                        canvas.scale(0.6f, 0.6f)
                        drawScene(canvas, task.targetObject, task.baseObject, shuffledChoices[j], 0f, 0f)
                        canvas.restore()
                    }
                }
                PrepositionTaskType.DRAW_SELF -> {
                    val qPaint = Paint().apply { isAntiAlias = true; textSize = 13f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                    canvas.drawText("Нарисуй ${task.targetObject.title} ${task.correctAnswer}", cx, top + 45f, qPaint)

                    drawScene(canvas, task.targetObject, task.baseObject, task.preposition, cx, top + 90f, isDrawSelf = true)

                    val drawRect = RectF(left + 20f, top + 120f, right - 20f, bottom - 10f)
                    val dashPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
                    canvas.drawRoundRect(drawRect, 8f, 8f, dashPaint)
                }
            }
        }
    }

    private fun drawScene(canvas: Canvas, target: SceneObject, base: SceneObject, prep: String, cx: Float, cy: Float, isDrawSelf: Boolean = false) {
        // Если рисуем сами - показываем только базу
        if (isDrawSelf) {
            drawObject(canvas, base, cx, cy, isBase = true)
            return
        }

        // Логика наложения слоев
        when (prep) {
            "за" -> {
                // Сначала цель, потом база поверх нее (перекрывает)
                drawObject(canvas, target, cx + 15f, cy - 5f, isBase = false)
                drawObject(canvas, base, cx, cy, isBase = true)
            }
            "перед" -> {
                // Сначала база, потом цель поверх нее
                drawObject(canvas, base, cx - 15f, cy - 5f, isBase = true)
                drawObject(canvas, target, cx + 5f, cy + 5f, isBase = false)
            }
            "на" -> {
                drawObject(canvas, base, cx, cy + 10f, isBase = true)
                drawObject(canvas, target, cx, cy - 15f, isBase = false)
            }
            "под" -> {
                drawObject(canvas, base, cx, cy - 10f, isBase = true)
                drawObject(canvas, target, cx, cy + 15f, isBase = false)
            }
            "в" -> {
                drawObject(canvas, base, cx, cy, isBase = true)
                // Цель рисуем меньше и поверх
                canvas.save()
                canvas.translate(cx, cy)
                canvas.scale(0.5f, 0.5f)
                drawObject(canvas, target, 0f, 0f, isBase = false)
                canvas.restore()
            }
            "над" -> {
                drawObject(canvas, base, cx, cy + 15f, isBase = true)
                drawObject(canvas, target, cx, cy - 25f, isBase = false)
            }
            "слева от" -> {
                drawObject(canvas, base, cx + 25f, cy, isBase = true)
                drawObject(canvas, target, cx - 25f, cy, isBase = false)
            }
            "справа от" -> {
                drawObject(canvas, base, cx - 25f, cy, isBase = true)
                drawObject(canvas, target, cx + 25f, cy, isBase = false)
            }
            "между" -> {
                canvas.save()
                canvas.translate(cx - 35f, cy)
                canvas.scale(0.7f, 0.7f)
                drawObject(canvas, base, 0f, 0f, isBase = true)
                canvas.restore()

                canvas.save()
                canvas.translate(cx + 35f, cy)
                canvas.scale(0.7f, 0.7f)
                drawObject(canvas, base, 0f, 0f, isBase = true)
                canvas.restore()

                drawObject(canvas, target, cx, cy, isBase = false)
            }
            else -> {
                drawObject(canvas, target, cx - 20f, cy, isBase = false)
                drawObject(canvas, base, cx + 20f, cy, isBase = true)
            }
        }
    }

    // --- ВЕКТОРНАЯ ОТРИСОВКА ОБЪЕКТОВ ---

    private fun drawObject(canvas: Canvas, obj: SceneObject, x: Float, y: Float, isBase: Boolean) {
        val fillPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        val strokePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 2f; color = Color.parseColor("#333333") }

        when (obj) {
            SceneObject.TABLE -> {
                fillPaint.color = Color.parseColor("#8D6E63")
                canvas.drawRect(x - 30f, y - 10f, x + 30f, y, fillPaint) // Столешница
                canvas.drawRect(x - 30f, y - 10f, x + 30f, y, strokePaint)
                canvas.drawRect(x - 25f, y, x - 20f, y + 20f, fillPaint) // Ножка левая
                canvas.drawRect(x + 20f, y, x + 25f, y + 20f, fillPaint) // Ножка правая
            }
            SceneObject.CHAIR -> {
                fillPaint.color = Color.parseColor("#A1887F")
                canvas.drawRect(x - 15f, y - 20f, x + 15f, y - 15f, fillPaint) // Спинка
                canvas.drawRect(x - 15f, y - 20f, x + 15f, y - 15f, strokePaint)
                canvas.drawRect(x - 15f, y - 5f, x + 15f, y, fillPaint) // Сиденье
                canvas.drawRect(x - 15f, y - 5f, x + 15f, y, strokePaint)
                canvas.drawRect(x - 12f, y, x - 8f, y + 15f, fillPaint) // Ножки
                canvas.drawRect(x + 8f, y, x + 12f, y + 15f, fillPaint)
            }
            SceneObject.BOX -> {
                fillPaint.color = Color.parseColor("#FFB300")
                canvas.drawRect(x - 25f, y - 15f, x + 25f, y + 15f, fillPaint)
                canvas.drawRect(x - 25f, y - 15f, x + 25f, y + 15f, strokePaint)
                canvas.drawLine(x - 25f, y, x + 25f, y, strokePaint) // Крышка
                canvas.drawLine(x, y - 15f, x, y + 15f, strokePaint) // Стык
            }
            SceneObject.BASKET -> {
                fillPaint.color = Color.parseColor("#FFCA28")
                val path = Path().apply {
                    moveTo(x - 25f, y - 10f)
                    lineTo(x + 25f, y - 10f)
                    lineTo(x + 15f, y + 20f)
                    lineTo(x - 15f, y + 20f)
                    close()
                }
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, strokePaint)
                val handle = RectF(x - 20f, y - 30f, x + 20f, y + 10f)
                canvas.drawArc(handle, 180f, 180f, false, strokePaint) // Ручка
            }
            SceneObject.TREE -> {
                fillPaint.color = Color.parseColor("#795548")
                canvas.drawRect(x - 5f, y - 10f, x + 5f, y + 20f, fillPaint) // Ствол
                fillPaint.color = Color.parseColor("#4CAF50")
                canvas.drawCircle(x, y - 20f, 20f, fillPaint) // Крона
                canvas.drawCircle(x, y - 20f, 20f, strokePaint)
            }
            SceneObject.SHELF -> {
                fillPaint.color = Color.parseColor("#8D6E63")
                canvas.drawRect(x - 30f, y - 5f, x + 30f, y, fillPaint) // Полка
                canvas.drawRect(x - 30f, y - 5f, x + 30f, y, strokePaint)
                canvas.drawRect(x - 25f, y, x - 20f, y + 15f, fillPaint) // Кронштейн
                canvas.drawRect(x + 20f, y, x + 25f, y + 15f, fillPaint)
            }
            SceneObject.BALL -> {
                fillPaint.color = Color.WHITE
                canvas.drawCircle(x, y, 15f, fillPaint)
                canvas.drawCircle(x, y, 15f, strokePaint)
                strokePaint.color = Color.BLACK
                canvas.drawCircle(x, y, 5f, Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }) // Черное пятно
            }
            SceneObject.CAT -> {
                fillPaint.color = Color.parseColor("#FF9800")
                canvas.drawCircle(x, y, 15f, fillPaint) // Голова
                canvas.drawCircle(x, y, 15f, strokePaint)
                // Уши
                val ear1 = Path().apply { moveTo(x - 12f, y - 10f); lineTo(x - 5f, y - 22f); lineTo(x - 2f, y - 10f); close() }
                val ear2 = Path().apply { moveTo(x + 12f, y - 10f); lineTo(x + 5f, y - 22f); lineTo(x + 2f, y - 10f); close() }
                canvas.drawPath(ear1, fillPaint); canvas.drawPath(ear1, strokePaint)
                canvas.drawPath(ear2, fillPaint); canvas.drawPath(ear2, strokePaint)
                // Глаза
                canvas.drawCircle(x - 5f, y - 2f, 2f, Paint().apply { color = Color.BLACK })
                canvas.drawCircle(x + 5f, y - 2f, 2f, Paint().apply { color = Color.BLACK })
            }
            SceneObject.MOUSE -> {
                fillPaint.color = Color.GRAY
                canvas.drawOval(RectF(x - 15f, y - 8f, x + 10f, y + 8f), fillPaint) // Тело
                canvas.drawOval(RectF(x - 15f, y - 8f, x + 10f, y + 8f), strokePaint)
                canvas.drawCircle(x - 10f, y - 8f, 6f, fillPaint) // Ухо
                canvas.drawCircle(x - 10f, y - 8f, 6f, strokePaint)
                canvas.drawLine(x + 10f, y, x + 20f, y + 5f, strokePaint) // Хвост
            }
            SceneObject.BIRD -> {
                fillPaint.color = Color.parseColor("#2196F3")
                canvas.drawOval(RectF(x - 12f, y - 8f, x + 12f, y + 8f), fillPaint) // Тело
                canvas.drawOval(RectF(x - 12f, y - 8f, x + 12f, y + 8f), strokePaint)
                fillPaint.color = Color.parseColor("#FFC107")
                val beak = Path().apply { moveTo(x + 10f, y); lineTo(x + 18f, y - 4f); lineTo(x + 18f, y + 4f); close() }
                canvas.drawPath(beak, fillPaint)
                canvas.drawPath(beak, strokePaint)
            }
            SceneObject.APPLE -> {
                fillPaint.color = Color.RED
                canvas.drawCircle(x, y, 12f, fillPaint)
                canvas.drawCircle(x, y, 12f, strokePaint)
                canvas.drawLine(x, y - 12f, x + 3f, y - 18f, strokePaint) // Хвостик
            }
            SceneObject.DICE -> {
                fillPaint.color = Color.WHITE
                canvas.drawRect(x - 12f, y - 12f, x + 12f, y + 12f, fillPaint)
                canvas.drawRect(x - 12f, y - 12f, x + 12f, y + 12f, strokePaint)
                val dot = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
                canvas.drawCircle(x, y, 3f, dot) // Одна точка
            }
            SceneObject.FLOWER -> {
                fillPaint.color = Color.parseColor("#E91E63")
                for (i in 0 until 5) {
                    val angle = Math.toRadians((i * 72).toDouble())
                    val px = x + (Math.cos(angle) * 10).toFloat()
                    val py = y + (Math.sin(angle) * 10).toFloat()
                    canvas.drawCircle(px, py, 8f, fillPaint)
                    canvas.drawCircle(px, py, 8f, strokePaint)
                }
                fillPaint.color = Color.YELLOW
                canvas.drawCircle(x, y, 5f, fillPaint)
                canvas.drawCircle(x, y, 5f, strokePaint)
            }
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
