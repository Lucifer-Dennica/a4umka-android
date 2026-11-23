package com.example.sovenok.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.sovenok.data.ComprehensionConfig
import com.example.sovenok.data.ComprehensionStory

object ComprehensionPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f
    private const val MARGIN = 40f
    private val GREEN = Color.parseColor("#33691E")
    private val PAGE_BG = Color.parseColor("#FFF8E1")
    private val BOX_STROKE = Color.parseColor("#D7CCC8")

    private fun newBmp(): Bitmap =
        Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)

    fun generatePreviewPages(config: ComprehensionConfig, context: Context): List<Bitmap> {
        val story = config.selectedStory ?: return emptyList()
        val pages = mutableListOf<Bitmap>()
        pages.addAll(buildKidPages(config, story))
        if (config.showAnswerSheet) pages.addAll(buildAdultPages(config, story))
        return pages
    }

    fun drawDocument(document: PdfDocument, config: ComprehensionConfig, context: Context) {
        val story = config.selectedStory ?: return
        var pageNum = 1
        for (bmp in buildKidPages(config, story)) {
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(info); page.canvas.drawBitmap(bmp, 0f, 0f, null); document.finishPage(page)
        }
        if (config.showAnswerSheet) {
            for (bmp in buildAdultPages(config, story)) {
                val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
                val page = document.startPage(info); page.canvas.drawBitmap(bmp, 0f, 0f, null); document.finishPage(page)
            }
        }
    }

    // ===== ЛИСТ РЕБЁНКА: заголовок + инфо-бокс + текст + вопросы, с ПЕРЕНОСОМ страниц =====
    private fun buildKidPages(config: ComprehensionConfig, story: ComprehensionStory): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        var bmp = newBmp(); var canvas = Canvas(bmp)
        var y = drawHeader(canvas, config, story, "Читай, замечай, размышляй")

        // инфо-бокс как на сайте: рамка + симметричный padding + зазор до текста
        y = drawInfoBox(canvas, y) + 26f

        val body = Paint().apply { isAntiAlias = true; textSize = 18f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val num = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.parseColor("#9E9E9E"); typeface = Typeface.DEFAULT }
        val lh = body.textSize * 1.5f
        val maxW = PAGE_WIDTH - MARGIN * 2 - if (config.showWordCount) 34f else 0f
        var cum = 0

        for (para in story.text.split("\n")) {
            for (line in wrap(para, body, maxW)) {
                if (y > PAGE_HEIGHT - 50f) {            // перенос страницы (чинит 2-4 класс)
                    pages.add(bmp); bmp = newBmp(); canvas = Canvas(bmp)
                    y = drawHeader(canvas, config, story, "Читай, замечай, размышляй")
                }
                canvas.drawText(line, MARGIN, y, body)
                if (config.showWordCount) {
                    cum += line.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                    canvas.drawText(cum.toString(), PAGE_WIDTH - MARGIN, y, num)
                }
                y += lh
            }
        }
        y += 22f

        val qBody = Paint().apply { isAntiAlias = true; textSize = 13f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.parseColor("#CFD8DC"); strokeWidth = 1f }
        story.questions.forEachIndexed { i, q ->
            val lines = wrap(q, qBody, PAGE_WIDTH - MARGIN * 2 - 44f)
            val boxH = 22f + lines.size * 16f + 30f
            if (y + boxH > PAGE_HEIGHT - 30f) {
                pages.add(bmp); bmp = newBmp(); canvas = Canvas(bmp)
                y = drawHeader(canvas, config, story, "Читай, замечай, размышляй") + 6f
            }
            drawBox(canvas, y, boxH)
            canvas.drawCircle(MARGIN + 16f, y + 16f, 9f, Paint().apply { color = GREEN; style = Paint.Style.FILL })
            val n = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText((i + 1).toString(), MARGIN + 16f, y + 20f, n)
            var ty = y + 18f
            for (ln in lines) { canvas.drawText(ln, MARGIN + 32f, ty, qBody); ty += 16f }
            canvas.drawLine(MARGIN + 32f, y + boxH - 14f, PAGE_WIDTH - MARGIN - 12f, y + boxH - 14f, linePaint)
            y += boxH + 8f
        }
        pages.add(bmp)
        return pages
    }

    // ===== ЛИСТ ВЗРОСЛОГО =====
    private fun buildAdultPages(config: ComprehensionConfig, story: ComprehensionStory): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        var bmp = newBmp(); var canvas = Canvas(bmp)
        var y = drawHeader(canvas, config, story, "Ответы — для взрослого")
        val qBold = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        val aBody = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.parseColor("#37474F"); typeface = Typeface.DEFAULT }
        story.questions.forEachIndexed { i, q ->
            val qLines = wrap("${i + 1}. $q", qBold, PAGE_WIDTH - MARGIN * 2 - 24f)
            val boxH = 18f + qLines.size * 18f + 6f + 18f + 10f
            if (y + boxH > PAGE_HEIGHT - 30f) {
                pages.add(bmp); bmp = newBmp(); canvas = Canvas(bmp)
                y = drawHeader(canvas, config, story, "Ответы — для взрослого") + 6f
            }
            drawBox(canvas, y, boxH)
            var ty = y + 20f
            for (ln in qLines) { canvas.drawText(ln, MARGIN + 12f, ty, qBold); ty += 18f }
            canvas.drawText("Ответ: ______________________________", MARGIN + 12f, ty + 4f, aBody)
            y += boxH + 8f
        }
        pages.add(bmp)
        return pages
    }

    // ===== helpers =====
    private fun drawHeader(canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory, subtitle: String): Float {
        canvas.drawColor(PAGE_BG)
        val pill = Paint().apply { isAntiAlias = true; color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
        val pillTxt = Paint().apply { isAntiAlias = true; textSize = 13f; color = Color.parseColor("#1B5E20"); textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT }
        val g = "${config.grade} класс"; val gw = pillTxt.measureText(g) + 28f
        canvas.drawRoundRect(RectF(PAGE_WIDTH / 2f - gw / 2f, 22f, PAGE_WIDTH / 2f + gw / 2f, 46f), 8f, 8f, pill)
        canvas.drawText(g, PAGE_WIDTH / 2f, 40f, pillTxt)
        val hp = Paint().apply { isAntiAlias = true; textSize = 13f; color = Color.parseColor("#5D4037"); typeface = Typeface.DEFAULT }
        val hl = Paint().apply { color = Color.parseColor("#BCAAA4"); strokeWidth = 1f }
        canvas.drawText("Имя:", MARGIN, 40f, hp); canvas.drawLine(MARGIN + 36f, 42f, MARGIN + 150f, 42f, hl)
        canvas.drawText("Дата:", PAGE_WIDTH - 130f, 40f, hp); canvas.drawLine(PAGE_WIDTH - 92f, 42f, PAGE_WIDTH - MARGIN, 42f, hl)
        val title = Paint().apply { isAntiAlias = true; textSize = 26f; color = GREEN; typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC); textAlign = Paint.Align.CENTER }
        canvas.drawText(story.title, PAGE_WIDTH / 2f, 78f, title)
        val sub = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.parseColor("#8D6E63"); typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC); textAlign = Paint.Align.CENTER }
        canvas.drawText(subtitle, PAGE_WIDTH / 2f, 98f, sub)
        return 118f
    }

    // Инфо-бокс: рамка + симметричный внутренний отступ (как на сайте)
    private fun drawInfoBox(canvas: Canvas, y: Float): Float {
        val p = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.parseColor("#37474F"); typeface = Typeface.DEFAULT }
        val text = "Прочитай рассказ. Затем ответь на вопросы — устно или письменно."
        val lines = wrap(text, p, PAGE_WIDTH - MARGIN * 2 - 32f)
        val h = 16f + lines.size * 16f + 16f          // padding сверху и снизу по 16
        drawBox(canvas, y, h)
        var ty = y + 16f + 12f                         // baseline первой строки
        for (ln in lines) { canvas.drawText(ln, MARGIN + 16f, ty, p); ty += 16f }
        return y + h
    }

    private fun drawBox(canvas: Canvas, y: Float, h: Float) {
        val r = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + h)
        canvas.drawRoundRect(r, 8f, 8f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        canvas.drawRoundRect(r, 8f, 8f, Paint().apply { isAntiAlias = true; color = BOX_STROKE; style = Paint.Style.STROKE; strokeWidth = 1f })
    }

    private fun wrap(text: String, paint: Paint, maxW: Float): List<String> {
        val out = mutableListOf<String>(); val cur = StringBuilder()
        for (w in text.split(Regex("\\s+")).filter { it.isNotBlank() }) {
            val t = if (cur.isEmpty()) w else "$cur $w"
            if (paint.measureText(t) > maxW && cur.isNotEmpty()) { out.add(cur.toString()); cur.setLength(0); cur.append(w) }
            else cur.append(if (cur.isEmpty()) w else " $w")
        }
        if (cur.isNotEmpty()) out.add(cur.toString())
        return out
    }
}
