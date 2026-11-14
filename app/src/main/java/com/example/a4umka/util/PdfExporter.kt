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

    fun exportSyllableTable(context: Context, config: SyllableTableConfig): String? {
        val document = PdfDocument()
        SyllableTablePdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "syllable_table_${System.currentTimeMillis()}.pdf")
    }

    fun exportAssembleWord(context: Context, config: AssembleWordConfig): String? {
        val document = PdfDocument()
        AssembleWordPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "assemble_word_${System.currentTimeMillis()}.pdf")
    }

    fun exportFillword(context: Context, config: FillwordConfig): String? {
        val document = PdfDocument()
        FillwordPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "fillword_${System.currentTimeMillis()}.pdf")
    }

    fun exportFillwordKids(context: Context, config: FillwordKidsConfig): String? {
        val document = PdfDocument()
        FillwordKidsPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "fillword_kids_${System.currentTimeMillis()}.pdf")
    }

    fun exportFunnyStory(context: Context, config: FunnyStoryConfig): String? {
        val document = PdfDocument()
        FunnyStoryPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "funny_story_${System.currentTimeMillis()}.pdf")
    }

    fun exportRebus(context: Context, config: RebusConfig): String? {
        val document = PdfDocument()
        RebusPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "rebus_${System.currentTimeMillis()}.pdf")
    }

    fun exportSimpleRebus(context: Context, config: SimpleRebusConfig): String? {
        val document = PdfDocument()
        SimpleRebusPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "simple_rebus_${System.currentTimeMillis()}.pdf")
    }

    fun exportLetterMath(context: Context, config: LetterMathConfig): String? {
        val document = PdfDocument()
        LetterMathPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "letter_math_${System.currentTimeMillis()}.pdf")
    }

    fun exportProverb(context: Context, config: ProverbConfig): String? {
        val document = PdfDocument()
        ProverbPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "proverb_${System.currentTimeMillis()}.pdf")
    }

    fun exportSyllableText(context: Context, config: SyllableConfig): String? {
        val document = PdfDocument()
        SyllablePdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "syllables_${System.currentTimeMillis()}.pdf")
    }

    fun exportComprehension(context: Context, config: ComprehensionConfig): String? {
        val document = PdfDocument()
        ComprehensionPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "comprehension_${System.currentTimeMillis()}.pdf")
    }

    fun exportCopying(context: Context, config: CopyingConfig): String? {
        val document = PdfDocument()
        CopyingPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "copying_${System.currentTimeMillis()}.pdf")
    }

    fun exportDiary(context: Context, config: DiaryConfig): String? {
        val document = PdfDocument()
        DiaryPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "diary_${System.currentTimeMillis()}.pdf")
    }

    fun exportBooklet(context: Context, config: BookletConfig): String? {
        val document = PdfDocument()
        BookletPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "booklet_${System.currentTimeMillis()}.pdf")
    }

    fun exportFairyTale(context: Context, config: FairyTaleConfig): String? {
        val document = PdfDocument()
        FairyTalePdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "fairy_tales_${System.currentTimeMillis()}.pdf")
    }

    fun exportAnagram(context: Context, config: AnagramConfig): String? {
        val document = PdfDocument()
        AnagramPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "anagrams_${System.currentTimeMillis()}.pdf")
    }

    fun exportWordInWord(context: Context, config: WordInWordConfig): String? {
        val document = PdfDocument()
        WordInWordPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "word_in_word_${System.currentTimeMillis()}.pdf")
    }

    fun exportCrossword(context: Context, config: CrosswordConfig): String? {
        val document = PdfDocument()
        CrosswordPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "crossword_${System.currentTimeMillis()}.pdf")
    }

    fun exportCustomCrossword(context: Context, config: CustomCrosswordConfig): String? {
        val document = PdfDocument()
        CustomCrosswordPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "custom_crossword_${System.currentTimeMillis()}.pdf")
    }

    fun exportPreposition(context: Context, config: PrepositionConfig): String? {
        val document = PdfDocument()
        PrepositionPdfDrawer.drawDocument(document, config, context)
        return saveDocument(context, document, "prepositions_${System.currentTimeMillis()}.pdf")
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
