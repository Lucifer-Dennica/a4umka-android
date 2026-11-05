package com.example.a4umka.util

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.unit.IntSize
import androidx.core.content.res.ResourcesCompat
import com.example.a4umka.R
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

        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 26f
            color = android.graphics.Color.DKGRAY
        }
        canvas.drawText("Пропись буквы «$letter»", 40f, 55f, titlePaint)

        // Верхняя секция — печатная
        drawRow(
            context = context,
            canvas = canvas,
            letter = letter,
            isCursive = false,
            paths = printPaths,
            viewSize = printViewSize,
            top = 80f,
            height = 330f,
            label = "Печатная"
        )

        val dividerPaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(40f, 420f, PAGE_WIDTH - 40f, 420f, dividerPaint)

        // Нижняя секция — пропись
        drawRow(
            context = context,
            canvas = canvas,
            letter = letter,
            isCursive = true,
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
        context: Context,
        canvas: Canvas,
        letter: String,
        isCursive: Boolean,
        paths: List<Path>,
        viewSize: IntSize,
        top: Float,
        height: Float,
        label: String
    ) {
        val labelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            color = android.graphics.Color.GRAY
        }
        canvas.drawText(label, 40f, top + 20f, labelPaint)

        // Загрузка шрифта Caveat
        val typeface = if (isCursive) {
            ResourcesCompat.getFont(context, R.font.caveat_regular)
                ?: Typeface.create("cursive", Typeface.NORMAL)
        } else {
            Typeface.DEFAULT
        }

        val textToDraw = if (isCursive) "$letter ${letter.lowercase()}" else letter
        val letterSize = minOf(PAGE_WIDTH * 0.55f, height * 0.85f)

        val letterTextPaint = Paint().apply {
            isAntiAlias = true
            textSize = if (isCursive) letterSize * 0.8f else letterSize
            color = android.graphics.Color.parseColor("#FF7043")
            this.typeface = typeface
        }

        val letterPath = Path()
        letterTextPaint.getTextPath(textToDraw, 0, textToDraw.length, 0f, 0f, letterPath)

        // Центрирование в PDF
        val bounds = RectF()
        letterPath.computeBounds(bounds, true)
        val centerX = PAGE_WIDTH / 2f
        val centerY = top + height / 2f
        val offsetX = centerX - bounds.centerX()
        val offsetY = centerY - bounds.centerY()
        
        val matrix = Matrix()
        matrix.postTranslate(offsetX, offsetY)
        letterPath.transform(matrix)

        val outlinePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = android.graphics.Color.parseColor("#FF7043")
            strokeWidth = 5f
            pathEffect = DashPathEffect(floatArrayOf(18f, 18f), 0f)
        }
        canvas.drawPath(letterPath, outlinePaint)

        // Рисуем штрихи пользователя
        if (viewSize.width > 0 && viewSize.height > 0) {
            val screenLetterSize = if (isCursive) {
                minOf(viewSize.width, viewSize.height) * 0.45f
            } else {
                minOf(viewSize.width, viewSize.height) * 0.7f
            }
            
            if (screenLetterSize > 0f) {
                val scale = (if (isCursive) letterSize * 0.8f else letterSize) / screenLetterSize
                
                val screenCenterX = viewSize.width / 2f
                val screenCenterY = viewSize.height / 2f
                
                val userPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    color = android.graphics.Color.parseColor("#2E1F1A")
                    strokeWidth = 4f / scale.coerceAtLeast(0.1f)
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }

                canvas.save()
                // Точная привязка координат рисования к центру
                canvas.translate(centerX, centerY)
                canvas.scale(scale, scale)
                canvas.translate(-screenCenterX, -screenCenterY)
                
                paths.forEach { canvas.drawPath(it, userPaint) }
                canvas.restore()
            }
        }
    }
}
