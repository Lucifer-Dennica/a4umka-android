package com.example.a4umka.util

import android.content.ContentValues
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.a4umka.data.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfExporter {

    // ... (старые методы exportWorksheet, exportAlphabetPoster, exportRibbon остаются без изменений) ...

    fun exportWorksheet(context: Context, letter: String, config: PdfExportConfig): String? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PdfDrawer.PAGE_WIDTH.toInt(), PdfDrawer.PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        PdfDrawer.drawWorksheet(page.canvas, letter, config, context)
        document.finishPage(page)
        return saveDocument(context, document, "worksheet_${letter}_${System.currentTimeMillis()}.pdf")
    }

    fun exportAlphabetPoster(context: Context, config: AlphabetConfig): String? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(AlphabetPdfDrawer.PAGE_WIDTH.toInt(), AlphabetPdfDrawer.PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        AlphabetPdfDrawer.drawLayout(page.canvas, config, context)
        document.finishPage(page)
        return saveDocument(context, document, "alphabet_${System.currentTimeMillis()}.pdf")
    }

    fun exportRibbon(context: Context, config: RibbonConfig): String? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(RibbonPdfDrawer.PAGE_WIDTH.toInt(), RibbonPdfDrawer.PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        RibbonPdfDrawer.drawRibbon(page.canvas, config, context, isAnswerKey = false)
        document.finishPage(page)
        return saveDocument(context, document, "ribbon_${System.currentTimeMillis()}.pdf")
    }

    // НОВЫЙ МЕТОД ДЛЯ МНОГОСТРАНИЧНОГО PDF
    fun exportSyllableText(context: Context, config: SyllableConfig): String? {
        val document = PdfDocument()
        
        // Определяем список текстов для печати
        val textsToPrint = if (config.source == SyllableSource.READY) {
            config.selectedTexts
        } else {
            if (config.customText.isNotBlank()) {
                listOf(SyllableText(config.customTitle, config.customText))
            } else {
                emptyList()
            }
        }

        if (textsToPrint.isEmpty()) return null

        textsToPrint.forEachIndexed { index, textObj ->
            val pageInfo = PdfDocument.PageInfo.Builder(
                SyllablePdfDrawer.PAGE_WIDTH.toInt(),
                SyllablePdfDrawer.PAGE_HEIGHT.toInt(),
                index + 1
            ).create()
            val page = document.startPage(pageInfo)
            
            SyllablePdfDrawer.drawSinglePage(
                canvas = page.canvas,
                title = textObj.title,
                text = textObj.text,
                questions = textObj.questions,
                config = config,
                context = context
            )
            document.finishPage(page)
        }

        return saveDocument(context, document, "syllables_${System.currentTimeMillis()}.pdf")
    }

    private fun saveDocument(context: Context, document: PdfDocument, fileName: String): String? {
        return try {
            val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
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
