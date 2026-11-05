package com.example.a4umka.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.unit.IntSize
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun exportLetter(
        context: Context,
        letter: String,
        printPaths: List<Path>,
        cursivePaths: List<Path>,
        printViewSize: IntSize,
        cursiveViewSize: IntSize
    ): String? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 26f
            color = android.graphics.Color.DKGRAY
        }
        canvas.drawText("Пропись буквы «$letter»", 40f, 55f, titlePaint)

        // Верхняя секция — печатная буква
        drawRow(
            canvas = canvas,
            letter = letter,
            typeface = Typeface.DEFAULT,
            paths = printPaths,
            viewSize = printViewSize,
            top = 80f,
            height = 330f,
            label = "Печатная"
        )

        // Разделитель
        val dividerPaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(40f, 420f, PAGE_WIDTH - 40f, 420f, dividerPaint)

        // Нижняя секция — пропись
        drawRow(
            canvas = canvas,
            letter = letter,
            typeface = Typeface.create("cursive", Typeface.NORMAL),
            paths = cursivePaths,
            viewSize = cursiveViewSize,
            top = 450f,
            height = 330f,
            label = "Пропись"
        )

        document.finishPage(page)

        val fileName = "propis_${letter}_${System.currentTimeMillis()}.pdf"
        return try {
            val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                )
                uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(dir, fileName)
                FileOutputStream(file)
            }

            outputStream?.use { document.writeTo(it) }
            document.close()
            "Downloads/$fileName"
        } catch (e: Exception) {
            document.close()
            null
        }
    }

    private fun drawRow(
        canvas: Canvas,
        letter: String,
        typeface: Typeface,
        paths: List<Path>,
        viewSize: IntSize,
        top: Float,
        height: Float,
        label: String
    ) {
        // Подпись секции
        val labelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            color = android.graphics.Color.GRAY
        }
        canvas.drawText(label, 40f, top + 20f, labelPaint)

        // Размер буквы в PDF
        val letterSize = minOf(PAGE_WIDTH * 0.55f, height * 0.85f)
        // Позиция baseline буквы в PDF
        val letterX = (PAGE_WIDTH - letterSize) / 2f
        val letterY = top + height * 0.95f

        // Пунктирный образец
        val outlinePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = android.graphics.Color.parseColor("#FF7043")
            strokeWidth = 5f
            pathEffect = DashPathEffect(floatArrayOf(18f, 18f), 0f)
        }
        val letterTextPaint = Paint().apply {
            isAntiAlias = true
            textSize = letterSize
            color = android.graphics.Color.parseColor("#FF7043")
            this.typeface = typeface
        }
        val letterPath = Path()
        letterTextPaint.getTextPath(letter, 0, letter.length, letterX, letterY, letterPath)
        canvas.drawPath(letterPath, outlinePaint)

        // Штрихи пользователя: масштабируем и сдвигаем так, чтобы
        // координаты совпали с координатами буквы в PDF.
        if (viewSize.width > 0 && viewSize.height > 0) {
            // На экране буква нарисована с textSize = min(w,h)*0.7,
            // с baseline в (w*0.15, h*0.8).
            val screenLetterSize = minOf(viewSize.width, viewSize.height) * 0.7f
            if (screenLetterSize > 0f) {
                val scale = letterSize / screenLetterSize
                val offsetX = letterX - viewSize.width * 0.15f * scale
                val offsetY = letterY - viewSize.height * 0.8f * scale

                val userPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    color = android.graphics.Color.parseColor("#2E1F1A")
                    strokeWidth = 4f / scale.coerceAtLeast(0.1f)
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }

                canvas.save()
                canvas.translate(offsetX, offsetY)
                canvas.scale(scale, scale)
                paths.forEach { canvas.drawPath(it, userPaint) }
                canvas.restore()
            }
        }
    }
}
