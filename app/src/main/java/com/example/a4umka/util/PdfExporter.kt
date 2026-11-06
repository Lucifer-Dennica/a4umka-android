package com.example.a4umka.util

import android.content.ContentValues
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.a4umka.data.AlphabetConfig
import com.example.a4umka.data.PdfExportConfig
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfExporter {

    fun exportWorksheet(
        context: Context,
        letter: String,
        config: PdfExportConfig
    ): String? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(
            PdfDrawer.PAGE_WIDTH.toInt(),
            PdfDrawer.PAGE_HEIGHT.toInt(),
            1
        ).create()
        val page = document.startPage(pageInfo)

        PdfDrawer.drawWorksheet(page.canvas, letter, config, context)
        document.finishPage(page)

        val fileName = "worksheet_${letter}_${System.currentTimeMillis()}.pdf"
        return saveDocument(context, document, fileName)
    }

    fun exportAlphabetPoster(
        context: Context,
        config: AlphabetConfig
    ): String? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(
            AlphabetPdfDrawer.PAGE_WIDTH.toInt(),
            AlphabetPdfDrawer.PAGE_HEIGHT.toInt(),
            1
        ).create()
        val page = document.startPage(pageInfo)

        AlphabetPdfDrawer.drawPoster(page.canvas, config, context)
        document.finishPage(page)

        val fileName = "alphabet_poster_${System.currentTimeMillis()}.pdf"
        return saveDocument(context, document, fileName)
    }

    private fun saveDocument(context: Context, document: PdfDocument, fileName: String): String? {
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
