package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.CopyingConfig
import com.example.a4umka.data.CopyingMode
import com.example.a4umka.data.CopyingStory

object CopyingPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    // Общая функция для рендеринга в Bitmap (для превью) и в PDF
    private fun renderDocument(
        config: CopyingConfig,
        canvasProvider: (Int) -> Canvas,
        onPageFinished: () -> Unit
    ) {
        var pageIndex = 0
        var canvas = canvasProvider(pageIndex)
        var currentY = 50f
        val margin = 40f
        val lineSpacingPx = config.lineStep * 3.78f

        // Рисуем шапку на первой странице (или на каждой, если нужно)
        fun drawHeader() {
            val headerInfo = "${if (config.mode == CopyingMode.COPYING) "СПИСЫВАНИЕ" else "ДИКТАНТ"} • ${config.grade} КЛАСС • ОРИЕНТИР"
            val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = Typeface.DEFAULT }
            canvas.drawText(headerInfo, margin, currentY, headerPaint)
            
            currentY += 30f
            
            val fieldPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
            
            canvas.drawText("Имя:", margin, currentY, fieldPaint)
            canvas.drawLine(margin + 35f, currentY + 2f, margin + 150f, currentY + 2f, linePaint)
            
            if (config.signature.isNotEmpty()) {
                canvas.drawText(config.signature, PAGE_WIDTH / 2f - fieldPaint.measureText(config.signature) / 2f, currentY, fieldPaint)
            }
            
            canvas.drawText("Дата:", PAGE_WIDTH - 140f, currentY, fieldPaint)
            canvas.drawLine(PAGE_WIDTH - 100f, currentY + 2f, PAGE_WIDTH - margin, currentY + 2f, linePaint)
            
            currentY += 30f
        }

        drawHeader()

        if (config.combineOnOnePage) {
            // РЕЖИМ "НЕ РАЗДЕЛЯТЬ ЛИСТЫ"
            for (story in config.selectedStories) {
                // Оцениваем высоту текста
                val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
                val words = story.text.split(" ")
                var line = ""
                val lines = mutableListOf<String>()
                for (word in words) {
                    val testLine = if (line.isEmpty()) word else "$line $word"
                    if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2) {
                        lines.add(line); line = word
                    } else { line = testLine }
                }
                if (line.isNotEmpty()) lines.add(line)
                
                val textHeight = lines.size * bodyPaint.textSize * 1.5f
                val titleHeight = 30f
                val boxHeight = 60f
                val linesForThisStory = 6 // Даем 6 строк на переписывание
                val writeAreaHeight = linesForThisStory * lineSpacingPx
                
                // Если не влезает, начинаем новый лист
                if (currentY + titleHeight + textHeight + boxHeight + writeAreaHeight > PAGE_HEIGHT - 40f) {
                    onPageFinished()
                    pageIndex++
                    canvas = canvasProvider(pageIndex)
                    currentY = 50f
                    drawHeader()
                }

                // Рисуем Заголовок
                val titlePaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
                canvas.drawText(story.title, margin, currentY, titlePaint)
                currentY += 25f

                // Рисуем Текст
                for (l in lines) {
                    canvas.drawText(l, margin, currentY, bodyPaint)
                    currentY += bodyPaint.textSize * 1.5f
                }
                currentY += 10f

                // Рамка с правилами
                val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
                val boxRect = RectF(margin, currentY, PAGE_WIDTH - margin, currentY + 40f)
                canvas.drawRect(boxRect, boxPaint)
                val hintPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
                canvas.drawText("Прочитай текст. Списывай по одному предложению.", margin + 5f, currentY + 15f, hintPaint)
                canvas.drawText("Проверь: начало предложений, буквы в словах, пробелы, знаки в конце.", margin + 5f, currentY + 30f, hintPaint)
                currentY += 55f

                // Рисуем фиксированное количество строк
                val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
                for (i in 0 until linesForThisStory) {
                    canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
                    currentY += lineSpacingPx
                }
                currentY += 20f // Отступ между историями
            }
        } else {
            // РЕЖИМ "РАЗДЕЛЬНЫЕ ЛИСТЫ" (старый)
            for (story in config.selectedStories) {
                for (copy in 0 until config.childrenCount) {
                    if (currentY > 100f) { // Если это не первая страница
                        onPageFinished()
                        pageIndex++
                        canvas = canvasProvider(pageIndex)
                        currentY = 50f
                        drawHeader()
                    }
                    
                    // Заголовок
                    val titlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
                    canvas.drawText(story.title, margin, currentY, titlePaint)
                    currentY += 30f

                    // Текст
                    if (config.mode == CopyingMode.COPYING) {
                        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
                        val words = story.text.split(" ")
                        var line = ""
                        for (word in words) {
                            val testLine = if (line.isEmpty()) word else "$line $word"
                            if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2) {
                                canvas.drawText(line, margin, currentY, bodyPaint)
                                currentY += bodyPaint.textSize * 1.5f
                                line = word
                            } else { line = testLine }
                        }
                        if (line.isNotEmpty()) {
                            canvas.drawText(line, margin, currentY, bodyPaint)
                            currentY += bodyPaint.textSize * 1.5f
                        }
                        currentY += 15f
                    }

                    // Рамка с правилами
                    val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
                    val boxRect = RectF(margin, currentY, PAGE_WIDTH - margin, currentY + 40f)
                    canvas.drawRect(boxRect, boxPaint)
                    val hintPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
                    if (config.mode == CopyingMode.COPYING) {
                        canvas.drawText("Прочитай текст. Списывай по одному предложению.", margin + 5f, currentY + 15f, hintPaint)
                    } else {
                        canvas.drawText("Взрослый читает текст. Ребёнок записывает под диктовку.", margin + 5f, currentY + 15f, hintPaint)
                    }
                    canvas.drawText("Проверь: начало предложений, буквы в словах, пробелы, знаки в конце.", margin + 5f, currentY + 30f, hintPaint)
                    currentY += 60f

                    // Линии до конца страницы
                    val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
                    while (currentY < PAGE_HEIGHT - 40f) {
                        canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
                        currentY += lineSpacingPx
                    }
                }
            }
        }

        // Если нужен дополнительный пустой лист
        if (config.addBlankPage) {
            onPageFinished()
            pageIndex++
            canvas = canvasProvider(pageIndex)
            currentY = 60f
            val headerPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
            canvas.drawText("Дополнительный лист для письма", PAGE_WIDTH / 2f - 100f, currentY, headerPaint)
            currentY += 40f
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
            while (currentY < PAGE_HEIGHT - 40f) {
                canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)
                currentY += lineSpacingPx
            }
        }

        onPageFinished()
    }

    fun generatePreview(config: CopyingConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        var currentBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        var currentCanvas = Canvas(currentBmp)
        currentCanvas.drawColor(if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA"))

        renderDocument(config,
            canvasProvider = { 
                if (it > 0) {
                    currentBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
                    currentCanvas = Canvas(currentBmp)
                }
                currentCanvas.drawColor(if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA"))
                currentCanvas
            },
            onPageFinished = {
                pages.add(currentBmp)
            }
        )
        return if (pages.isEmpty()) listOf(currentBmp) else pages
    }

    fun drawDocument(document: PdfDocument, config: CopyingConfig, context: Context) {
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        var page = document.startPage(pageInfo)

        renderDocument(config,
            canvasProvider = { 
                if (it > 0) {
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                    page = document.startPage(pageInfo)
                }
                page.canvas.drawColor(if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA"))
                page.canvas
            },
            onPageFinished = {
                document.finishPage(page)
            }
        )
    }
}
