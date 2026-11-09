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

object BookletPdfDrawer {
    // Альбомная ориентация
    const val PAGE_WIDTH = 842f
    const val PAGE_HEIGHT = 595f

    fun generatePreview(config: BookletConfig, context: Context): List<Bitmap> {
        // Генерируем одно превью в 50% разрешении для скорости
        val scale = 0.5f
        val pw = (PAGE_WIDTH * scale).toInt()
        val ph = (PAGE_HEIGHT * scale).toInt()
        
        val bmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.scale(scale, scale)
        drawPage(canvas, config)
        return listOf(bmp)
    }

    fun drawDocument(document: PdfDocument, config: BookletConfig, context: Context) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        drawPage(page.canvas, config)
        document.finishPage(page)
    }

    private fun drawPage(canvas: Canvas, config: BookletConfig) {
        canvas.drawColor(Color.WHITE)
        
        val cellW = PAGE_WIDTH / 4f
        val cellH = PAGE_HEIGHT / 2f
        
        val pages = BookletData.getPages(config)
        
        val isBW = config.isBlackAndWhite
        val mainColor = if (isBW) Color.BLACK else Color.parseColor("#33691E")
        val lineColor = if (isBW) Color.LTGRAY else Color.parseColor("#C8E6C9")

        // --- 1. Линии сгиба и надреза ---
        if (config.showFoldLines) {
            val foldPaint = Paint().apply { 
                color = Color.GRAY; strokeWidth = 1f; style = Paint.Style.STROKE 
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f) 
            }
            // Вертикальные линии
            for (i in 1..3) {
                canvas.drawLine(cellW * i, 0f, cellW * i, PAGE_HEIGHT, foldPaint)
            }
            // Горизонтальная линия (по бокам)
            canvas.drawLine(0f, cellH, cellW, cellH, foldPaint)
            canvas.drawLine(cellW * 3, cellH, PAGE_WIDTH, cellH, foldPaint)

            // Центральная линия разреза (посередине)
            val cutPaint = Paint().apply { color = if(isBW) Color.BLACK else Color.parseColor("#D84315"); strokeWidth = 2f }
            canvas.drawLine(cellW, cellH, cellW * 3, cellH, cutPaint)
        }

        // --- 2. Отрисовка 8 страниц ---
        // Верхний ряд: 8, 1, 4, 5
        // Нижний ряд: 7, 2, 3, 6 (перевернуты на 180)
        
        // Верхний ряд
        drawCell(canvas, pages[7], config, 0, 0, cellW, cellH, mainColor, lineColor, isRotated = false) // 8 (Конец)
        drawCell(canvas, pages[0], config, 1, 0, cellW, cellH, mainColor, lineColor, isRotated = false) // 1 (Обложка)
        drawCell(canvas, pages[3], config, 2, 0, cellW, cellH, mainColor, lineColor, isRotated = false) // 4
        drawCell(canvas, pages[4], config, 3, 0, cellW, cellH, mainColor, lineColor, isRotated = false) // 5

        // Нижний ряд (перевернут)
        drawCell(canvas, pages[6], config, 0, 1, cellW, cellH, mainColor, lineColor, isRotated = true) // 7
        drawCell(canvas, pages[1], config, 1, 1, cellW, cellH, mainColor, lineColor, isRotated = true) // 2
        drawCell(canvas, pages[2], config, 2, 1, cellW, cellH, mainColor, lineColor, isRotated = true) // 3
        drawCell(canvas, pages[5], config, 3, 1, cellW, cellH, mainColor, lineColor, isRotated = true) // 6
        
        // Подпись снизу (полоска с инструкцией)
        val instructionPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY }
        canvas.drawText("✂ Отрежьте нижнюю полоску. Сложите: пополам, еще раз пополам, надрежьте по жирной линии, сложите вдоль и сдвиньте края.", 10f, PAGE_HEIGHT - 10f, instructionPaint)
    }

    private fun drawCell(
        canvas: Canvas, page: PageContent, config: BookletConfig, 
        col: Int, row: Int, cellW: Float, cellH: Float, 
        mainColor: Int, lineColor: Int, isRotated: Boolean
    ) {
        canvas.save()
        
        // Начало ячейки
        val startX = col * cellW
        val startY = row * cellH
        
        // Сдвигаем в центр ячейки для вращения
        if (isRotated) {
            canvas.rotate(180f, startX + cellW / 2f, startY + cellH / 2f)
        }
        
        val margin = 15f
        val left = startX + margin
        val top = startY + margin
        val right = startX + cellW - margin
        val bottom = startY + cellH - margin
        
        val width = right - left
        val height = bottom - top
        val centerX = left + width / 2f
        
        val isBW = config.isBlackAndWhite
        
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = mainColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        val linePaint = Paint().apply { color = lineColor; strokeWidth = 1f }
        
        if (page.isCover) {
            // ОБЛОЖКА
            val coverTitlePaint = Paint().apply { isAntiAlias = true; textSize = 18f; color = mainColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
            canvas.drawText("МОЯ ПЕРВАЯ КНИГА", centerX, top + 20f, Paint().apply { isAntiAlias = true; textSize = 8f; color = Color.GRAY; textAlign = Paint.Align.CENTER })
            
            val titleStr = if (config.title.isNotBlank()) config.title else "Моя книжка"
            // Перенос строк для длинного названия
            val words = titleStr.split(" ")
            var line = ""
            var curY = top + 50f
            for (w in words) {
                val test = if (line.isEmpty()) w else "$line $w"
                if (coverTitlePaint.measureText(test) > width - 20) {
                    canvas.drawText(line, centerX, curY, coverTitlePaint)
                    curY += 20f
                    line = w
                } else { line = test }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, centerX, curY, coverTitlePaint)
                curY += 20f
            }
            
            // Окно для рисунка на обложке
            val boxRect = RectF(left + 10f, curY + 10f, right - 10f, bottom - 50f)
            val boxPaint = Paint().apply { color = if(isBW) Color.LTGRAY else lineColor; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
            canvas.drawRect(boxRect, boxPaint)
            
            val authorStr = if (config.author.isNotBlank()) config.author else "Автор:"
            canvas.drawText(authorStr, centerX, bottom - 20f, bodyPaint)
            canvas.drawLine(left + 20f, bottom - 10f, right - 20f, bottom - 10f, linePaint)
            
        } else if (page.isEnd) {
            // КОНЕЦ
            canvas.drawText("Конец", centerX, startY + cellH / 2f, Paint().apply { isAntiAlias = true; textSize = 20f; color = mainColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER })
            canvas.drawText("a4umka.ru", centerX, bottom - 10f, Paint().apply { isAntiAlias = true; textSize = 8f; color = Color.GRAY; textAlign = Paint.Align.CENTER })
            
        } else {
            // ОБЫЧНАЯ СТРАНИЦА
            var curY = top + 20f
            
            if (page.prompt != null) {
                // Рисуем текст подсказки или загадки
                val promptPaint = if (config.type == BookletType.RIDDLES) Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER } else titlePaint
                
                val words = page.prompt.split(" ")
                var line = ""
                for (w in words) {
                    val test = if (line.isEmpty()) w else "$line $w"
                    if (promptPaint.measureText(test) > width - 10) {
                        canvas.drawText(line, centerX, curY, promptPaint)
                        curY += 15f
                        line = w
                    } else { line = test }
                }
                if (line.isNotEmpty()) {
                    canvas.drawText(line, centerX, curY, promptPaint)
                    curY += 15f
                }
                curY += 10f
            } else if (page.title != null) {
                canvas.drawText(page.title, centerX, curY, titlePaint)
                curY += 20f
            }

            if (page.showDrawingBox) {
                // Окно для рисунка
                val boxHeight = height * 0.4f
                val boxRect = RectF(left, curY, right, curY + boxHeight)
                val boxPaint = Paint().apply { color = if(isBW) Color.LTGRAY else lineColor; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
                canvas.drawRect(boxRect, boxPaint)
                curY += boxHeight + 15f
            }

            if (page.showLines) {
                // Рисуем линии для письма
                val step = when (config.ruling) {
                    BookletRuling.LINE_8MM -> 8f * 2.83f
                    BookletRuling.WIDE_10MM -> 10f * 2.83f
                    BookletRuling.GRID_5MM -> 5f * 2.83f
                    else -> 0f
                }
                
                if (step > 0f) {
                    while (curY < bottom - 10f) {
                        canvas.drawLine(left, curY, right, curY, linePaint)
                        if (config.ruling == BookletRuling.GRID_5MM) {
                            var x = left
                            while (x < right) {
                                canvas.drawLine(x, curY, x, curY + step, linePaint)
                                x += step
                            }
                        }
                        curY += step
                    }
                }
            }
        }
        
        canvas.restore()
    }
}
