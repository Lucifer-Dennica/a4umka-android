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
    private val PEACH = Color.parseColor("#FFF3E0")
    private val BOX_STROKE = Color.parseColor("#D7CCC8")
    private val BOX_FILL = Color.WHITE

    private fun newBmp(): Bitmap = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)

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

    // ===== ЛИСТ РЕБЁНКА =====
    private fun buildKidPages(config: ComprehensionConfig, story: ComprehensionStory): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        var bmp = newBmp(); var canvas = Canvas(bmp)
        var y = drawHeader(canvas, config, story, "Читай, замечай, размышляй")

        // инфо-бокс + увеличенный зазор снизу до текста (было 14f, стало 24f)
        y = drawInfoBox(canvas, y, "Прочитай рассказ. Затем ответь на вопросы — устно или письменно.") + 24f

        val body = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val num = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.parseColor("#9E9E9E"); typeface = Typeface.DEFAULT }
        val lh = body.textSize * 1.5f
        val maxW = PAGE_WIDTH - MARGIN * 2 - if (config.showWordCount) 34f else 0f
        var cum = 0
        for (para in story.text.split("\n")) {
            for (line in wrap(para, body, maxW)) {
                if (y > PAGE_HEIGHT - 40f) {
                    bmp = newBmp(); canvas = Canvas(bmp); pages.add(bmp)
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
        y += 18f

        val qBody = Paint().apply { isAntiAlias = true; textSize = 13f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.parseColor("#CFD8DC"); strokeWidth = 1f }
        story.questions.forEachIndexed { i, q ->
            val lines = wrap(q, qBody, PAGE_WIDTH - MARGIN * 2 - 44f)
            val boxH = 22f + lines.size * 16f + 30f
            if (y + boxH > PAGE_HEIGHT - 30f) {
                bmp = newBmp(); canvas = Canvas(bmp); pages.add(bmp)
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

        y = drawLabeledBox(canvas, y, "Как проверять",
            "Ребёнок может ответить своими словами. Ниже — смысловые ориентиры, а не обязательные формулировки.") + 12f

        val qBold = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        val aBody = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.parseColor("#37474F"); typeface = Typeface.DEFAULT }
        story.questions.forEachIndexed { i, q ->
            val a = story.answers.getOrElse(i) { "Ответ не указан" }
            val qLines = wrap("${i + 1}. $q", qBold, PAGE_WIDTH - MARGIN * 2 - 24f)
            val aLines = wrap(a, aBody, PAGE_WIDTH - MARGIN * 2 - 24f)
            val boxH = 18f + qLines.size * 18f + 6f + aLines.size * 18f + 10f
            if (y + boxH > PAGE_HEIGHT - 30f) {
                bmp = newBmp(); canvas = Canvas(bmp); pages.add(bmp)
                y = drawHeader(canvas, config, story, "Ответы — для взрослого") + 6f
            }
            drawBox(canvas, y, boxH)
            var ty = y + 20f
            for (ln in qLines) { canvas.drawText(ln, MARGIN + 12f, ty, qBold); ty += 18f }
            ty += 4f
            for (ln in aLines) { canvas.drawText(ln, MARGIN + 12f, ty, aBody); ty += 18f }
            y += boxH + 8f
        }

        val wc = if (story.totalWords > 0) story.totalWords else story.text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val instr = listOf(
            "Заголовок не учитывается. Слова с дефисом считаются одним словом, отдельное тире словом не считается.",
            "За одну минуту: прочитано _____ слов, ошибок _____. Отметьте последнее полностью прочитанное слово. Число справа — сумма слов до конца строки; для неполной строки прибавьте прочитанные слова к числу предыдущей.",
            "Если текст закончился раньше минуты, запишите общее время: _____ секунд. Не пересчитывайте его автоматически в минутный норматив. Для следующей проверки выберите незнакомый текст. Обсудите содержание после чтения."
        )
        val title = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        val small = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.parseColor("#37474F"); typeface = Typeface.DEFAULT }
        var boxH = 22f
        val allLines = mutableListOf<Pair<Paint, List<String>>>()
        allLines.add(title to listOf("В тексте $wc слов"))
        for (p in instr) allLines.add(small to wrap(p, small, PAGE_WIDTH - MARGIN * 2 - 24f))
        for ((_, ls) in allLines) boxH += ls.size * 15f + 2f
        boxH += 10f
        if (y + boxH > PAGE_HEIGHT - 30f) {
            bmp = newBmp(); canvas = Canvas(bmp); pages.add(bmp)
            y = drawHeader(canvas, config, story, "Ответы — для взрослого") + 6f
        }
        drawBox(canvas, y, boxH)
        var ty = y + 20f
        for ((paint, ls) in allLines) { for (ln in ls) { canvas.drawText(ln, MARGIN + 12f, ty, paint); ty += 15f }; ty += 2f }
        pages.add(bmp)
        return pages
    }

    // ===== helpers =====
    private fun drawHeader(canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory, subtitle: String): Float {
        canvas.drawColor(if (config.isPeachBackground) PEACH else Color.WHITE)
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

    // ИЗМЕНЁН: симметричный внутренний отступ (было 16/10, стало 18/16) — текст не прилипает к рамке
    private fun drawInfoBox(canvas: Canvas, y: Float, text: String): Float {
        val p = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.parseColor("#37474F"); typeface = Typeface.DEFAULT }
        val lines = wrap(text, p, PAGE_WIDTH - MARGIN * 2 - 24f)
        val h = 18f + lines.size * 16f + 16f
        drawBox(canvas, y, h)
        var ty = y + 22f
        for (ln in lines) { canvas.drawText(ln, MARGIN + 12f, ty, p); ty += 16f }
        return y + h
    }

    private fun drawLabeledBox(canvas: Canvas, y: Float, label: String, text: String): Float {
        val lb = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        val p = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.parseColor("#37474F"); typeface = Typeface.DEFAULT }
        val lines = wrap(text, p, PAGE_WIDTH - MARGIN * 2 - 24f)
        val h = 18f + 16f + lines.size * 16f + 10f
        drawBox(canvas, y, h)
        canvas.drawText(label, MARGIN + 12f, y + 18f, lb)
        var ty = y + 36f
        for (ln in lines) { canvas.drawText(ln, MARGIN + 12f, ty, p); ty += 16f }
        return y + h
    }

    private fun drawBox(canvas: Canvas, y: Float, h: Float) {
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + h), 8f, 8f, Paint().apply { color = BOX_FILL; style = Paint.Style.FILL })
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + h), 8f, 8f, Paint().apply { isAntiAlias = true; color = BOX_STROKE; style = Paint.Style.STROKE; strokeWidth = 1f })
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
