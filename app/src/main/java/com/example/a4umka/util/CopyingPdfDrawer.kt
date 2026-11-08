package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.CopyingConfig
import com.example.a4umka.data.CopyingLineType
import com.example.a4umka.data.CopyingMode

object CopyingPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    // Превью для экрана
    fun generatePreview(config: CopyingConfig, context: Context): Bitmap {
        val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawPage(canvas, config)
        return bmp
    }

    // Генерация PDF
    fun drawDocument(document: PdfDocument, config: CopyingConfig, context: Context) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        drawPage(page.canvas, config)
        document.finishPage(page)
    }

    private fun drawPage(canvas: Canvas, config: CopyingConfig) {
        canvas.drawColor(Color.WHITE)

        val margin = 40f
        val startY = 80f

        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 24f
            color = Color.DKGRAY
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(config.title, PAGE_WIDTH / 2f, startY, titlePaint)

        // Если списывание - печатаем текст
        var currentY = startY + 40f
        if (config.mode == CopyingMode.COPYING) {
            val bodyPaint = Paint().apply {
                isAntiAlias = true
                textSize = if (config.isLargeFont) 18f else 14f
                color = Color.BLACK
                typeface = Typeface.DEFAULT
            }
            // Простой перенос текста по словам
            val words = config.text.split(" ")
            var line = ""
            for (word in words) {
                val test = if (line.isEmpty()) word else "$line $word"
                if (bodyPaint.measureText(test) > PAGE_WIDTH - margin * 2) {
                    canvas.drawText(line, margin, currentY, bodyPaint)
                    currentY += 25f
                    line = word
                } else {
                    line = test
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, margin, currentY, bodyPaint)
                currentY += 25f
            }
        }

        // Рисуем линии для письма
        val linePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#B0BEC5") // Серый цвет линий
            strokeWidth = 1.5f
        }
        val slantedPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 1f
        }

        currentY += 40f // Отступ перед линиями
        val lineHeight = if (config.isLargeFont) 40f else 30f

        for (i in 0 until config.linesCount) {
            if (currentY > PAGE_HEIGHT - 40f) break

            // Основная линия
            canvas.drawLine(margin, currentY, PAGE_WIDTH - margin, currentY, linePaint)

            // Наклонные линии (косая линейка)
            if (config.lineType == CopyingLineType.SLANTED) {
                var slantX = margin
                while (slantX < PAGE_WIDTH - margin) {
                    canvas.drawLine(slantX, currentY - lineHeight, slantX + lineHeight, currentY, slantedPaint)
                    slantX += lineHeight
                }
            }

            currentY += lineHeight
        }
    }
}
