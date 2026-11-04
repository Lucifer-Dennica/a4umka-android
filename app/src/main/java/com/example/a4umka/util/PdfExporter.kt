// app/src/main/java/com/example/a4umka/util/PdfExporter.kt
package com.example.a4umka.util

import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    fun createSimplePdf(fileName: String): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)

        // Здесь можно рисовать на page.canvas
        // page.canvas.drawText(...)

        document.finishPage(page)

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        return try {
            document.writeTo(FileOutputStream(file))
            document.close()
            file
        } catch (e: Exception) {
            document.close()
            null
        }
    }
}
