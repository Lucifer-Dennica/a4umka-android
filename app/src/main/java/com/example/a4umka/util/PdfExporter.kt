package com.example.a4umka.util

import android.content.ContentValues
import android.content.Context
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfExporter {

    fun exportLetter(
        context: Context,
        letter: String,
        userPaths: List<Path>
    ): String? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 28f
            color = android.graphics.Color.DKGRAY
        }
        canvas.drawText("Пропись буквы «$letter»", 40f, 60f, titlePaint)

        // Пунктирная буква
        val letterPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = android.graphics.Color.parseColor("#FF7043")
            strokeWidth = 6f
            pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
        }
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 420f
            color = android.graphics.Color.parseColor("#FF7043")
        }
        val letterPath = Path()
        textPaint.getTextPath(letter, 0, letter.length, 80f, 650f, letterPath)
        canvas.drawPath(letterPath, letterPaint)

        // Штрихи ребёнка (масштабируем из экранных координат примерно)
        val userPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = android.graphics.Color.parseColor("#2E1F1A")
            strokeWidth = 4f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.save()
        canvas.translate(80f, 100f)
        canvas.scale(0.5f, 0.5f)
        userPaths.forEach { canvas.drawPath(it, userPaint) }
        canvas.restore()

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
}
