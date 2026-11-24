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
    const val MARGIN = 40f
    const val BODY_START_FIRST = 150f   // после шапки+заголовка+инструкции
    const val BODY_START_OTHER = 70f    // на续ных страницах только шапка
    const val BOTTOM_LIMIT = 792f

    private val GREEN = Color.parseColor("#33691E")
    private val GREEN_LIGHT = Color.parseColor("#A5D6A7")

    data class LineData(val text: String, val cumulativeWords: Int)
    data class LayoutPage(val lines: List<LineData>, val hasQuestions: Boolean)

    fun generatePreviewPages(config: ComprehensionConfig, context: Context): List<Bitmap> {
        val story = config.selectedStory ?: return emptyList()
        val layout = calculateLayout(config, story)
        val pages = mutableListOf<Bitmap>()
        for (i in layout.indices) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            drawPage(Canvas(bmp), config, story, layout[i], i + 1, layout.size)
            pages.add(bmp)
        }
        if (config.showAnswerSheet) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            drawAnswerSheet(Canvas(bmp), config, story)
            pages.add(bmp)
        }
        return pages
    }

    fun drawDocument(document: PdfDocument, config: ComprehensionConfig, context: Context) {
        val story = config.selectedStory ?: return
        val layout = calculateLayout(config, story)
        var pageNum = 1
        for (i in layout.indices) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val page = document.startPage(pageInfo)
            drawPage(page.canvas, config, story, layout[i], pageNum, layout.size)
            document.finishPage(page); pageNum++
        }
        if (config.showAnswerSheet) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            val page = document.startPage(pageInfo)
            drawAnswerSheet(page.canvas, config, story)
            document.finishPage(page)
        }
    }

    private fun questionBlockHeight(story: ComprehensionStory) = 34f + story.questions.size * 54f

    private fun calculateLayout(config: ComprehensionConfig, story: ComprehensionStory): List<LayoutPage> {
        val paint = Paint().apply { textSize = config.fontSize; typeface = Typeface.DEFAULT }
        val lineHeight = config.fontSize * 1.4f
        val maxWidth = PAGE_WIDTH - MARGIN * 2

        // строки текста с накопительным счётчиком слов
        val allLines = mutableListOf<LineData>()
        var running = 0
        for (para in story.text.split("\n")) {
            var line = ""; var cnt = 0
            for (word in para.split(" ")) {
                val test = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(test) > maxWidth) {
                    if (line.isNotEmpty()) { running += cnt; allLines.add(LineData(line, running)) }
                    line = word; cnt = 1
                } else { line = test; cnt++ }
            }
            if (line.isNotEmpty()) { running += cnt; allLines.add(LineData(line, running)) }
            allLines.add(LineData("", running))
        }

        val pages = mutableListOf<LayoutPage>()
        var cur = mutableListOf<LineData>()
        var y = BODY_START_FIRST
        fun savePage(q: Boolean = false) {
            if (cur.isNotEmpty() || q) { pages.add(LayoutPage(cur.toList(), q)); cur.clear(); y = BODY_START_OTHER }
        }
        for (l in allLines) {
            if (y + lineHeight > BOTTOM_LIMIT) savePage()
            cur.add(l); y += if (l.text.isEmpty()) lineHeight * 0.6f else lineHeight
        }
        val qH = questionBlockHeight(story)
        if (story.questions.isNotEmpty() && !config.showAnswerSheet) {
            if (y + qH > BOTTOM_LIMIT) savePage(true) else savePage(true)
        } else if (cur.isNotEmpty()) savePage()
        return pages.ifEmpty { listOf(LayoutPage(emptyList(), false)) }
    }

    private fun drawPage(canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory,
                         page: LayoutPage, pageNum: Int, totalPages: Int) {
        canvas.drawColor(Color.WHITE)
        val b = config.isBlackAndWhite
        val accent = if (b) Color.DKGRAY else GREEN
        val header = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY }
        val ln = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; isAntiAlias = true }

        canvas.drawText("Имя:", MARGIN, 40f, header)
        canvas.drawLine(MARGIN + 35f, 42f, MARGIN + 150f, 42f, ln)
        drawPill(canvas, "${config.grade} класс", PAGE_WIDTH / 2f, 38f, accent)
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, header)
        canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - MARGIN, 42f, ln)

        var y = BODY_START_OTHER
        if (pageNum == 1) {
            val title = Paint().apply { isAntiAlias = true; textSize = 26f; color = accent; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
            canvas.drawText(story.title, PAGE_WIDTH / 2f, y, title); y += 25f
            val sub = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }
            canvas.drawText("Читай, замечай, размышляй", PAGE_WIDTH / 2f, y, sub); y += 28f
            val inst = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY }
            canvas.drawText("Прочитай рассказ. Затем ответь на вопросы — устно или письменно.", MARGIN, y, inst); y += 30f
        }

        val body = Paint().apply { isAntiAlias = true; textSize = config.fontSize; color = Color.BLACK }
        val num = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.RIGHT }
        val lh = config.fontSize * 1.4f
        for (l in page.lines) {
            if (l.text.isNotEmpty()) {
                canvas.drawText(l.text, MARGIN, y, body)
                if (config.showWordCount) canvas.drawText(l.cumulativeWords.toString(), PAGE_WIDTH - MARGIN, y, num)
            }
            y += if (l.text.isEmpty()) lh * 0.6f else lh
        }

        if (page.hasQuestions) {
            y += 6
            val qt = Paint().apply { isAntiAlias = true; textSize = 18f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            canvas.drawText("Вопросы:", MARGIN, y, qt); y += 24f
            val qp = Paint().apply { isAntiAlias = true; textSize = if (config.grade >= 4) 12f else 14f; color = Color.BLACK }
            val box = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f; isAntiAlias = true }
            val circ = Paint().apply { color = accent; style = Paint.Style.FILL; isAntiAlias = true }
            val cn = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
            story.questions.forEachIndexed { i, q ->
                val top = y; val bot = y + 46f
                canvas.drawRoundRect(RectF(MARGIN, top, PAGE_WIDTH - MARGIN, bot), 8f, 8f, box)
                canvas.drawCircle(MARGIN + 18f, top + 18f, 11f, circ)
                canvas.drawText((i + 1).toString(), MARGIN + 18f, top + 22f, cn)
                canvas.drawText(q, MARGIN + 38f, top + 22f, qp)
                val ul = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; isAntiAlias = true }
                canvas.drawLine(MARGIN + 38f, bot - 12f, PAGE_WIDTH - MARGIN - 12f, bot - 12f, ul)
                y = bot + 8f
            }
        }

        if (totalPages > 1) {
            val pp = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
            canvas.drawText("Страница $pageNum", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, pp)
        }
    }

    private fun drawAnswerSheet(canvas: Canvas, config: ComprehensionConfig, story: ComprehensionStory) {
        canvas.drawColor(Color.WHITE)
        val b = config.isBlackAndWhite
        val accent = if (b) Color.DKGRAY else GREEN
        val header = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY }
        val ln = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; isAntiAlias = true }
        canvas.drawText("Имя:", MARGIN, 40f, header); canvas.drawLine(MARGIN + 35f, 42f, MARGIN + 150f, 42f, ln)
        drawPill(canvas, "${config.grade} класс", PAGE_WIDTH / 2f, 38f, accent)
        canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, header); canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - MARGIN, 42f, ln)

        var y = 90f
        val title = Paint().apply { isAntiAlias = true; textSize = 26f; color = accent; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText(story.title, PAGE_WIDTH / 2f, y, title); y += 22f
        val sub = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }
        canvas.drawText("Ответы — для взрослого", PAGE_WIDTH / 2f, y, sub); y += 30f

        val box = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f; isAntiAlias = true }

        if (config.showAdultHints) {
            val hTop = y
            y += 22f
            val hTitle = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            y = drawWrapped(canvas, "Как проверять", MARGIN + 12f, y, PAGE_WIDTH - MARGIN * 2 - 24f, hTitle, 18f)
            val hBody = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY }
            y = drawWrapped(canvas, "Ребёнок может ответить своими словами. Ниже — смысловые ориентиры, а не обязательные формулировки.",
                MARGIN + 12f, y + 4f, PAGE_WIDTH - MARGIN * 2 - 24f, hBody, 16f)
            canvas.drawRoundRect(RectF(MARGIN, hTop, PAGE_WIDTH - MARGIN, y + 10f), 8f, 8f, box); y += 24f
        }

        val qBold = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        val qNorm = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY }
        story.questions.forEachIndexed { i, q ->
            val top = y
            y += 20f
            y = drawWrapped(canvas, "${i + 1}. $q", MARGIN + 12f, y, PAGE_WIDTH - MARGIN * 2 - 24f, qBold, 18f)
            val ans = if (i < story.answers.size && story.answers[i].isNotEmpty()) story.answers[i] else "_______________________________"
            y = drawWrapped(canvas, ans, MARGIN + 12f, y + 4f, PAGE_WIDTH - MARGIN * 2 - 24f, qNorm, 18f)
            canvas.drawRoundRect(RectF(MARGIN, top, PAGE_WIDTH - MARGIN, y + 10f), 8f, 8f, box); y += 22f
        }

        if (config.showAdultHints) {
            val top = y; y += 22f
            val wTitle = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            y = drawWrapped(canvas, "В тексте ${story.totalWords} ${wordForm(story.totalWords)}", MARGIN + 12f, y, PAGE_WIDTH - MARGIN * 2 - 24f, wTitle, 18f)
            val wBody = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.DKGRAY }
            val tech = "Заголовков не учитывается. Слова с дефисом считаются одним словом, отдельное тире словом не считается.\n" +
                "За одну минуту: прочитано _____ слов, ошибок _____. Отметьте последнее полностью прочитанное слово. Число справа — сумма слов до конца строки; для неполной строки прибавьте прочитанные слова к числу предыдущей.\n" +
                "Если текст закончился раньше минуты, запишите общее время: _____ секунд. Не пересчитывайте его автоматически в минутный норматив. Для следующей проверки выберите незнакомый текст. Обсудите содержание после чтения."
            y = drawWrapped(canvas, tech, MARGIN + 12f, y + 4f, PAGE_WIDTH - MARGIN * 2 - 24f, wBody, 15f)
            canvas.drawRoundRect(RectF(MARGIN, top, PAGE_WIDTH - MARGIN, y + 10f), 8f, 8f, box)
        }
    }

    private fun drawPill(canvas: Canvas, text: String, cx: Float, cy: Float, fill: Color) {
        val meas = Paint().apply { isAntiAlias = true; textSize = 14f; typeface = Typeface.DEFAULT_BOLD }
        val w = meas.measureText(text); val padH = 14f; val h = 24f
        canvas.drawRoundRect(RectF(cx - w / 2 - padH, cy - h / 2, cx + w / 2 + padH, cy + h / 2), 12f, 12f,
            Paint().apply { color = fill; style = Paint.Style.FILL; isAntiAlias = true })
        val tp = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText(text, cx, cy + 5f, tp)
    }

    private fun drawWrapped(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint, lineH: Float): Float {
        var cy = y
        for (par in text.split("\n")) {
            if (par.isEmpty()) { cy += lineH * 0.5f; continue }
            var line = ""
            for (w in par.split(" ")) {
                val t = if (line.isEmpty()) w else "$line $w"
                if (paint.measureText(t) > maxWidth) { canvas.drawText(line, x, cy, paint); cy += lineH; line = w } else line = t
            }
            if (line.isNotEmpty()) { canvas.drawText(line, x, cy, paint); cy += lineH }
        }
        return cy
    }

    private fun wordForm(count: Int): String {
        val lastTwo = count % 100; val last = count % 10
        return when { lastTwo in 11..19 -> "слов"; last == 1 -> "слово"; last in 2..4 -> "слова"; else -> "слов" }
    }
}
