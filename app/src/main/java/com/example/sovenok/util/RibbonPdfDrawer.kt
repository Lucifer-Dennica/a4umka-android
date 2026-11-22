package com.example.sovenok.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.example.sovenok.R
import com.example.sovenok.data.*
import kotlin.random.Random

object RibbonPdfDrawer {

    const val PAGE_WIDTH = 842f
    const val PAGE_HEIGHT = 595f
    private const val PREVIEW_SCALE = 3

    private val COL_RED = Color.parseColor("#E05A4E")
    private val COL_BLUE = Color.parseColor("#4A80D0")
    private val COL_GREEN = Color.parseColor("#43A047")
    private val COL_TITLE = Color.parseColor("#33691E")
    private val COL_SUB = Color.parseColor("#8A8A8A")
    private val COL_BORDER = Color.parseColor("#9AA0A6")
    private val COL_TEXT = Color.parseColor("#212121")
    private val PEACH_TOP = Color.parseColor("#FFF6E8")
    private val PEACH_BOTTOM = Color.parseColor("#EAF2E0")

    fun getPreview(config: RibbonConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()

        if (config.mode == RibbonMode.BOARD) {
            val boardPages = getBoardPages()
            for ((index, page) in boardPages.withIndex()) {
                val bmp = newPreviewBitmap()
                val canvas = Canvas(bmp)
                canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
                drawBoardPage(canvas, config, page, index + 1, boardPages.size, context)
                pages.add(bmp)
            }
            return pages
        }

        val bmp = newPreviewBitmap()
        val canvas = Canvas(bmp)
        canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
        drawRibbon(canvas, config, context, isAnswerKey = false)
        pages.add(bmp)

        if (config.showAnswers && (config.mode == RibbonMode.MISSING || config.mode == RibbonMode.COLORING)) {
            val ansBmp = newPreviewBitmap()
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
            drawRibbon(ansCanvas, config, context, isAnswerKey = true)
            pages.add(ansBmp)
        }
        return pages
    }

    private fun newPreviewBitmap(): Bitmap = Bitmap.createBitmap(
        (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
        (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
        Bitmap.Config.ARGB_8888
    )

    fun drawDocument(document: PdfDocument, config: RibbonConfig, context: Context) {
        var pageNum = 1

        if (config.mode == RibbonMode.BOARD) {
            val boardPages = getBoardPages()
            boardPages.forEachIndexed { index, page ->
                val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
                val docPage = document.startPage(info)
                drawBoardPage(docPage.canvas, config, page, index + 1, boardPages.size, context)
                document.finishPage(docPage)
            }
            return
        }

        val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
        val page = document.startPage(info)
        drawRibbon(page.canvas, config, context, isAnswerKey = false)
        document.finishPage(page)

        if (config.showAnswers && (config.mode == RibbonMode.MISSING || config.mode == RibbonMode.COLORING)) {
            val ansInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val ansPage = document.startPage(ansInfo)
            drawRibbon(ansPage.canvas, config, context, isAnswerKey = true)
            document.finishPage(ansPage)
        }
    }

    fun drawRibbon(canvas: Canvas, config: RibbonConfig, context: Context, isAnswerKey: Boolean = false) {
        drawBackground(canvas, config)

        val font = Typeface.DEFAULT
        val titleFont = ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT

        if (config.mode == RibbonMode.COLORING) {
            drawColoringPage(canvas, config, font, titleFont, isAnswerKey)
        } else {
            drawRibbonColored(canvas, config, font, titleFont, isAnswerKey)
        }
    }

    private fun drawBackground(canvas: Canvas, config: RibbonConfig) {
        if (config.bgColor == RibbonBgColor.PEACH) {
            canvas.drawRect(0f, 0f, PAGE_WIDTH, PAGE_HEIGHT, Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, PAGE_HEIGHT, PEACH_TOP, PEACH_BOTTOM, Shader.TileMode.CLAMP)
            })
        } else {
            canvas.drawColor(Color.WHITE)
        }
    }

    private fun drawRibbonColored(canvas: Canvas, config: RibbonConfig, font: Typeface, titleFont: Typeface, isAnswerKey: Boolean) {
        val isMissing = config.mode == RibbonMode.MISSING

        if (isMissing) {
            drawHeaderBox(canvas, "Имя:", 40f, 26f, 175f, 56f)
            drawHeaderBox(canvas, "Дата:", PAGE_WIDTH - 175f, 26f, PAGE_WIDTH - 40f, 56f)
            drawBadge(canvas, if (isAnswerKey) "✅ Ответы" else "✏️ Впиши")
        }

        val title = when {
            isAnswerKey -> "Ответы"
            isMissing -> "Впиши пропущенные буквы"
            else -> "Лента букв"
        }
        val subtitle = when {
            isAnswerKey -> "лента букв целиком"
            isMissing -> "Впиши буквы в пустые клетки: цвет клетки и соседи — подсказка"
            else -> "гласные и согласные, звонкие и глухие, твёрдые и мягкие"
        }
        drawTitle(canvas, title, subtitle, titleFont)

        val missingSet = mutableSetOf<String>()
        if (isMissing && !isAnswerKey) {
            val allLetters = (RibbonData.topVowels + RibbonData.bottomVowels +
                RibbonData.topConsonants + RibbonData.bottomConsonants).map { it.char }
            val countToHide = when (config.missingCount) {
                MissingCount.EIGHT -> 8
                MissingCount.SIXTEEN -> 16
                MissingCount.ALL -> allLetters.size
            }
            val random = Random(config.seed)
            missingSet.addAll(allLetters.shuffled(random).take(countToHide))
        }

        val cellW = 34f; val cellH = 44f; val gap = 3f
        val colW = cellW + gap; val rowH = cellH + gap
        val vowelStartX = 40f
        val consonantStartX = vowelStartX + 5 * colW + 26f
        val signX = PAGE_WIDTH - 40f - cellW
        val startY = 214f

        drawSectionLabel(canvas, "гласные", vowelStartX, 5 * colW - gap, startY - 26f, startY - 14f, COL_RED)
        drawSectionLabel(canvas, "согласные", consonantStartX, 11 * colW - gap, startY - 26f, startY - 14f, COL_BLUE)
        drawSectionLabel(canvas, "знаки", signX, cellW, startY - 26f, startY - 14f, Color.parseColor("#8D6E63"))

        val iconPaint = Paint().apply { isAntiAlias = true; textSize = 18f; textAlign = Paint.Align.CENTER }
        canvas.drawText("🔔", consonantStartX - 14f, startY + cellH / 2f + 6f, iconPaint)
        canvas.drawText("🎧", consonantStartX - 14f, startY + rowH + cellH / 2f + 6f, iconPaint)

        RibbonData.topVowels.forEachIndexed { i, letter ->
            drawColoredCell(canvas, letter, config, vowelStartX + i * colW, startY, cellW, cellH, font, missing = letter.char in missingSet)
            drawColoredCell(canvas, RibbonData.bottomVowels[i], config, vowelStartX + i * colW, startY + rowH, cellW, cellH, font, missing = RibbonData.bottomVowels[i].char in missingSet)
        }
        RibbonData.topConsonants.forEachIndexed { i, letter ->
            drawColoredCell(canvas, letter, config, consonantStartX + i * colW, startY, cellW, cellH, font, missing = letter.char in missingSet)
        }
        RibbonData.bottomConsonants.forEachIndexed { i, letter ->
            drawColoredCell(canvas, letter, config, consonantStartX + (i + 5) * colW, startY + rowH, cellW, cellH, font, missing = letter.char in missingSet)
        }
        RibbonData.signs.forEachIndexed { i, letter ->
            drawColoredCell(canvas, letter, config, signX, startY + i * rowH, cellW, cellH, font, missing = false)
        }

        var cardTop = 352f
        if (isMissing && !isAnswerKey && config.showHint && missingSet.isNotEmpty()) {
            drawLostLettersBox(canvas, 352f, missingSet, font)
            cardTop = 394f
        }
        drawLegendCard(canvas, cardTop, colored = true)
    }

    private fun drawColoringPage(canvas: Canvas, config: RibbonConfig, font: Typeface, titleFont: Typeface, isAnswerKey: Boolean) {
        drawHeaderBox(canvas, "Имя:", 40f, 26f, 175f, 56f)
        drawHeaderBox(canvas, "Дата:", PAGE_WIDTH - 175f, 26f, PAGE_WIDTH - 40f, 56f)
        drawBadge(canvas, if (isAnswerKey) "✅ Ответы" else "🎨 Раскрась")

        val title = if (isAnswerKey) "Ответы" else "Раскрась ленту букв"
        val subtitle = if (isAnswerKey) "так выглядит раскрашенная лента букв"
        else "Гласные — красным, всегда твёрдые — синим, всегда мягкие — зелёным, твёрдые или мягкие — пополам: синим и зелёным"
        drawTitle(canvas, title, subtitle, titleFont)

        val cellW = 34f; val cellH = 44f; val gap = 3f
        val colW = cellW + gap; val rowH = cellH + gap
        val vowelStartX = 40f
        val consonantStartX = vowelStartX + 5 * colW + 26f
        val signX = PAGE_WIDTH - 40f - cellW
        val startY = 214f

        drawSectionLabel(canvas, "гласные", vowelStartX, 5 * colW - gap, startY - 26f, startY - 14f, COL_RED)
        drawSectionLabel(canvas, "согласные", consonantStartX, 11 * colW - gap, startY - 26f, startY - 14f, COL_BLUE)
        drawSectionLabel(canvas, "знаки", signX, cellW, startY - 26f, startY - 14f, Color.parseColor("#8D6E63"))

        val iconPaint = Paint().apply { isAntiAlias = true; textSize = 18f; textAlign = Paint.Align.CENTER }
        canvas.drawText("🔔", consonantStartX - 14f, startY + cellH / 2f + 6f, iconPaint)
        canvas.drawText("🎧", consonantStartX - 14f, startY + rowH + cellH / 2f + 6f, iconPaint)

        RibbonData.topVowels.forEachIndexed { i, letter ->
            drawColoredCell(canvas, letter, config, vowelStartX + i * colW, startY, cellW, cellH, font, filled = isAnswerKey)
            drawColoredCell(canvas, RibbonData.bottomVowels[i], config, vowelStartX + i * colW, startY + rowH, cellW, cellH, font, filled = isAnswerKey)
        }
        RibbonData.topConsonants.forEachIndexed { i, letter ->
            drawColoredCell(canvas, letter, config, consonantStartX + i * colW, startY, cellW, cellH, font, filled = isAnswerKey)
        }
        RibbonData.bottomConsonants.forEachIndexed { i, letter ->
            drawColoredCell(canvas, letter, config, consonantStartX + (i + 5) * colW, startY + rowH, cellW, cellH, font, filled = isAnswerKey)
        }
        RibbonData.signs.forEachIndexed { i, letter ->
            drawColoredCell(canvas, letter, config, signX, startY + i * rowH, cellW, cellH, font, filled = isAnswerKey)
        }

        drawLegendCard(canvas, 352f, colored = isAnswerKey)
    }

    private fun drawColoredCell(
        canvas: Canvas, letter: RibbonLetter, config: RibbonConfig,
        x: Float, y: Float, w: Float, h: Float, font: Typeface,
        filled: Boolean = true, missing: Boolean = false,
        radius: Float = 10f, textRatio: Float = 0.45f
    ) {
        val rect = RectF(x, y, x + w, y + h)
        val fillColor = when (letter.color) {
            RibbonColor.RED -> COL_RED
            RibbonColor.BLUE -> COL_BLUE
            RibbonColor.GREEN -> COL_GREEN
            else -> Color.WHITE
        }

        if (missing) {
            canvas.drawRoundRect(rect, radius, radius, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
            canvas.drawRoundRect(rect, radius, radius, Paint().apply {
                color = if (letter.color == RibbonColor.BOTH) COL_BLUE else fillColor
                style = Paint.Style.STROKE; strokeWidth = 2f
                pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
            })
            canvas.drawLine(x + w / 2f - 8f, y + h / 2f, x + w / 2f + 8f, y + h / 2f,
                Paint().apply { color = COL_BORDER; strokeWidth = 2f; isAntiAlias = true })
            return
        }

        if (filled) {
            when (letter.color) {
                RibbonColor.BOTH -> {
                    canvas.drawRoundRect(rect, radius, radius, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
                    val clip = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
                    canvas.save(); canvas.clipPath(clip)
                    canvas.drawPath(Path().apply { moveTo(x, y); lineTo(x + w, y); lineTo(x, y + h); close() },
                        Paint().apply { color = COL_BLUE; style = Paint.Style.FILL })
                    canvas.drawPath(Path().apply { moveTo(x + w, y); lineTo(x + w, y + h); lineTo(x, y + h); close() },
                        Paint().apply { color = COL_GREEN; style = Paint.Style.FILL })
                    canvas.restore()
                }
                RibbonColor.WHITE -> canvas.drawRoundRect(rect, radius, radius, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
                else -> canvas.drawRoundRect(rect, radius, radius, Paint().apply { color = fillColor; style = Paint.Style.FILL })
            }
        } else {
            canvas.drawRoundRect(rect, radius, radius, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        }

        canvas.drawRoundRect(rect, radius, radius, Paint().apply {
            color = if (filled && letter.color != RibbonColor.WHITE) Color.argb(70, 0, 0, 0) else COL_BORDER
            style = Paint.Style.STROKE; strokeWidth = 1.5f
        })

        val text = when (config.case) {
            RibbonCase.BOTH -> letter.char
            RibbonCase.UPPERCASE -> letter.char.substring(0, 1)
            RibbonCase.LOWERCASE -> letter.char.substring(letter.char.length - 1)
        }
        val textPaint = Paint().apply {
            isAntiAlias = true; textSize = h * textRatio; typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            color = if (filled && letter.color != RibbonColor.WHITE) Color.WHITE else COL_TEXT
        }
        canvas.drawText(text, x + w / 2f, y + h / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f), textPaint)
    }

    private fun drawTitle(canvas: Canvas, title: String, subtitle: String, titleFont: Typeface) {
        canvas.drawText(title, PAGE_WIDTH / 2f, 112f, Paint().apply {
            isAntiAlias = true; textSize = 40f; color = COL_TITLE; typeface = titleFont; textAlign = Paint.Align.CENTER
        })
        canvas.drawText(subtitle, PAGE_WIDTH / 2f, 134f, Paint().apply {
            isAntiAlias = true; textSize = 11f; color = COL_SUB
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER
        })
    }

    private fun drawBadge(canvas: Canvas, text: String) {
        val w = 130f; val h = 32f; val left = (PAGE_WIDTH - w) / 2f
        val rect = RectF(left, 24f, left + w, 24f + h)
        canvas.drawRoundRect(rect, 16f, 16f, Paint().apply { color = Color.parseColor("#C5E1A5"); style = Paint.Style.FILL })
        canvas.drawRoundRect(rect, 16f, 16f, Paint().apply { color = Color.parseColor("#9CCC65"); style = Paint.Style.STROKE; strokeWidth = 1f })
        canvas.drawText(text, PAGE_WIDTH / 2f, 24f + h / 2f + 5f, Paint().apply {
            isAntiAlias = true; textSize = 13f; color = COL_TITLE; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        })
    }

    private fun drawHeaderBox(canvas: Canvas, label: String, left: Float, top: Float, right: Float, bottom: Float) {
        val rect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, 10f, 10f, Paint().apply { color = Color.parseColor("#FDF3DC"); style = Paint.Style.FILL })
        canvas.drawRoundRect(rect, 10f, 10f, Paint().apply { color = Color.parseColor("#D9C9A3"); style = Paint.Style.STROKE; strokeWidth = 1.5f })
        val p = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        val midY = (top + bottom) / 2f + 4f
        canvas.drawText(label, left + 10f, midY, p)
        canvas.drawLine(left + 10f + p.measureText(label) + 6f, midY - 2f, right - 10f, midY - 2f,
            Paint().apply { color = Color.parseColor("#C9B98F"); strokeWidth = 1f })
    }

    private fun drawSectionLabel(canvas: Canvas, text: String, left: Float, width: Float, textY: Float, lineY: Float, color: Int) {
        canvas.drawText(text, left + width / 2f, textY, Paint().apply {
            isAntiAlias = true; this.color = color; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        })
        canvas.drawLine(left, lineY, left + width, lineY, Paint().apply { this.color = color; strokeWidth = 2f })
    }

    private fun drawLostLettersBox(canvas: Canvas, top: Float, letters: Set<String>, font: Typeface) {
        val left = 40f; val right = PAGE_WIDTH - 40f; val bottom = top + 46f
        val rect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, 10f, 10f, Paint().apply { color = Color.parseColor("#FFF8E7"); style = Paint.Style.FILL })
        canvas.drawRoundRect(rect, 10f, 10f, Paint().apply { color = Color.parseColor("#E6B450"); style = Paint.Style.STROKE; strokeWidth = 1.5f })

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = Color.parseColor("#B45309"); typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Эти буквы потерялись:", left + 14f, top + 28f, titlePaint)

        val size = 26f; val gap = 6f
        var x = left + 14f + titlePaint.measureText("Эти буквы потерялись:") + 16f
        val y = top + 10f
        val letterPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = COL_TEXT; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        val dashed = Paint().apply {
            color = Color.parseColor("#E6B450"); style = Paint.Style.STROKE; strokeWidth = 1.5f
            pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
        }
        for (l in letters) {
            if (x + size > right - 14f) break
            canvas.drawRoundRect(RectF(x, y, x + size, y + size), 6f, 6f, dashed)
            canvas.drawText(l, x + size / 2f, y + size / 2f - ((letterPaint.descent() + letterPaint.ascent()) / 2f), letterPaint)
            x += size + gap
        }
    }

    private fun drawLegendCard(canvas: Canvas, top: Float, colored: Boolean) {
        val left = 40f; val right = PAGE_WIDTH - 40f; val bottom = 560f
        val card = RectF(left, top, right, bottom)
        canvas.drawRoundRect(card, 14f, 14f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        canvas.drawRoundRect(card, 14f, 14f, Paint().apply { color = Color.parseColor("#E3E3E3"); style = Paint.Style.STROKE; strokeWidth = 1f })

        val col1 = left + 26f
        val col2 = left + (right - left) * 0.52f
        val lh = 30f
        var y = top + 34f

        legendItem(canvas, col1, y, colored, LegendKind.RED, "гласные", " — А О У Ы Э — после твёрдых,", "Я Ё Ю И Е — после мягких"); y += lh + 16f
        legendItem(canvas, col1, y, colored, LegendKind.GREEN, "всегда мягкие", " — Й Ч Щ"); y += lh
        legendItem(canvas, col1, y, colored, LegendKind.WHITE, "звука не обозначают", " — Ь и Ъ"); y += lh
        legendItem(canvas, col1, y, colored, LegendKind.ICON_HEADPHONES, "", "нижний ряд согласных — глухие")

        y = top + 34f
        legendItem(canvas, col2, y, colored, LegendKind.BLUE, "всегда твёрдые", " — Ж Ш Ц"); y += lh
        legendItem(canvas, col2, y, colored, LegendKind.BOTH, "твёрдые или мягкие", " — остальные согласные"); y += lh
        legendItem(canvas, col2, y, colored, LegendKind.ICON_BELL, "", "верхний ряд согласных — звонкие")
    }

    private enum class LegendKind { RED, BLUE, GREEN, WHITE, BOTH, ICON_BELL, ICON_HEADPHONES }

    private fun legendItem(canvas: Canvas, x: Float, y: Float, colored: Boolean, kind: LegendKind, bold: String, rest: String, secondLine: String? = null) {
        val box = 16f
        val r = RectF(x, y - 12f, x + box, y + 4f)
        when (kind) {
            LegendKind.ICON_BELL, LegendKind.ICON_HEADPHONES -> {
                canvas.drawText(if (kind == LegendKind.ICON_BELL) "🔔" else "🎧", x + box / 2f, y + 3f,
                    Paint().apply { isAntiAlias = true; textSize = 16f; textAlign = Paint.Align.CENTER })
            }
            LegendKind.BOTH -> {
                if (colored) {
                    canvas.drawRoundRect(r, 4f, 4f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
                    val clip = Path().apply { addRoundRect(r, 4f, 4f, Path.Direction.CW) }
                    canvas.save(); canvas.clipPath(clip)
                    canvas.drawPath(Path().apply { moveTo(r.left, r.top); lineTo(r.right, r.top); lineTo(r.left, r.bottom); close() },
                        Paint().apply { color = COL_BLUE; style = Paint.Style.FILL })
                    canvas.drawPath(Path().apply { moveTo(r.right, r.top); lineTo(r.right, r.bottom); lineTo(r.left, r.bottom); close() },
                        Paint().apply { color = COL_GREEN; style = Paint.Style.FILL })
                    canvas.restore()
                } else drawCheckbox(canvas, r)
            }
            else -> {
                val c = when (kind) {
                    LegendKind.RED -> COL_RED
                    LegendKind.BLUE -> COL_BLUE
                    LegendKind.GREEN -> COL_GREEN
                    else -> Color.WHITE
                }
                if (colored) {
                    canvas.drawRoundRect(r, 4f, 4f, Paint().apply { color = c; style = Paint.Style.FILL })
                    if (kind == LegendKind.WHITE) {
                        canvas.drawRoundRect(r, 4f, 4f, Paint().apply { color = COL_BORDER; style = Paint.Style.STROKE; strokeWidth = 1.5f })
                    }
                } else drawCheckbox(canvas, r)
            }
        }

        val textX = x + box + 8f
        val boldP = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.parseColor("#333333"); typeface = Typeface.DEFAULT_BOLD }
        val restP = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.parseColor("#555555"); typeface = Typeface.DEFAULT }
        canvas.drawText(bold, textX, y, boldP)
        canvas.drawText(rest, textX + boldP.measureText(bold), y, restP)
        if (secondLine != null) canvas.drawText(secondLine, textX, y + 16f, restP)
    }

    private fun drawCheckbox(canvas: Canvas, r: RectF) {
        canvas.drawRoundRect(r, 4f, 4f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        canvas.drawRoundRect(r, 4f, 4f, Paint().apply { color = COL_BORDER; style = Paint.Style.STROKE; strokeWidth = 1.5f })
    }

    data class BoardPage(
        val title: String,
        val subtitle: String,
        val category: String,
        val categoryColor: Int,
        val rows: List<List<RibbonLetter>>,
        val showBell: Boolean,
        val showHeadphones: Boolean,
        val rowAtBottom: Boolean = false,
        val rightRows: List<List<RibbonLetter>>? = null,
        val rightTitle: String? = null
    )

    private fun getBoardPages(): List<BoardPage> {
        val sonorants = RibbonData.topConsonants.subList(0, 5)
        val pairedVoiced = RibbonData.topConsonants.subList(5, 11)
        val pairedVoiceless = RibbonData.bottomConsonants.subList(0, 6)
        val unpairedVoiceless = RibbonData.bottomConsonants.subList(6, 10)
        val signsVertical = RibbonData.signs.map { listOf(it) }

        return listOf(
            BoardPage(
                title = "Гласные",
                subtitle = "верхний ряд — после твёрдых согласных, нижний — после мягких",
                category = "гласные", categoryColor = COL_RED,
                rows = listOf(RibbonData.topVowels, RibbonData.bottomVowels),
                showBell = false, showHeadphones = false
            ),
            BoardPage(
                title = "Звонкие непарные",
                subtitle = "звонкие согласные без глухой пары",
                category = "согласные", categoryColor = COL_BLUE,
                rows = listOf(sonorants),
                showBell = true, showHeadphones = true, rowAtBottom = false
            ),
            BoardPage(
                title = "Звонкие и глухие пары",
                subtitle = "звонкий звук сверху, его глухая пара снизу",
                category = "согласные", categoryColor = COL_BLUE,
                rows = listOf(pairedVoiced, pairedVoiceless),
                showBell = true, showHeadphones = true
            ),
            BoardPage(
                title = "Глухие непарные, ь и ъ",
                subtitle = "глухие согласные без звонкой пары, ь и ъ звуков не обозначает",
                category = "согласные", categoryColor = COL_BLUE,
                rows = listOf(unpairedVoiceless),
                showBell = true, showHeadphones = true, rowAtBottom = true,
                rightRows = signsVertical, rightTitle = "знаки"
            )
        )
    }

    private fun drawBoardPage(canvas: Canvas, config: RibbonConfig, page: BoardPage, pageNum: Int, totalPages: Int, context: Context) {
        drawBackground(canvas, config)

        val titleFont = ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        val font = Typeface.DEFAULT

        canvas.drawText(page.title, PAGE_WIDTH / 2f, 62f, Paint().apply {
            isAntiAlias = true; textSize = 34f; color = COL_TITLE; typeface = titleFont; textAlign = Paint.Align.CENTER
        })
        canvas.drawText(page.subtitle, PAGE_WIDTH / 2f, 82f, Paint().apply {
            isAntiAlias = true; textSize = 11f; color = COL_SUB
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER
        })

        val gap = 10f
        val maxColsLeft = page.rows.maxOf { it.size }
        val rowCount = page.rows.size
        val reservedRight = if (page.rightRows != null) 190f else 0f
        val availableWidth = PAGE_WIDTH - 100f - reservedRight
        val gridTop = 115f; val gridBottom = 395f
        
        val cell = (availableWidth / maxColsLeft - gap)
            .coerceAtMost((gridBottom - gridTop) / rowCount - gap)
            .coerceAtMost(125f)
            
        val colW = cell + gap
        val rowH = cell + gap

        val totalWidth = maxColsLeft * colW - gap
        val baseX = if (page.rightRows != null) 50f else (PAGE_WIDTH - totalWidth) / 2f

        val rowY = if (rowCount == 2) gridTop
        else if (page.rowAtBottom) gridBottom - cell else gridTop

        val rowWidth = page.rows[0].size * colW - gap
        val rowStartX = baseX + (totalWidth - rowWidth) / 2f
        drawSectionLabel(canvas, page.category, rowStartX, rowWidth, rowY - 22f, rowY - 12f, page.categoryColor)

        val iconPaint = Paint().apply { isAntiAlias = true; textSize = 24f; textAlign = Paint.Align.CENTER }
        if (page.showBell) canvas.drawText("🔔", rowStartX - 30f, gridTop + cell / 2f + 8f, iconPaint)
        if (page.showHeadphones) canvas.drawText("🎧", rowStartX - 30f, gridBottom - cell / 2f + 8f, iconPaint)

        page.rows.forEachIndexed { rowIndex, rowLetters ->
            val y = if (rowCount == 2) gridTop + rowIndex * rowH else rowY
            rowLetters.forEachIndexed { colIndex, letter ->
                drawColoredCell(canvas, letter, config, rowStartX + colIndex * colW, y, cell, cell, font,
                    filled = true, radius = 18f, textRatio = 0.38f)
            }
        }

        if (page.rightRows != null && page.rightTitle != null) {
            val rightX = PAGE_WIDTH - 50f - cell
            val rightTotalH = page.rightRows.size * rowH - gap
            val rightStartY = gridTop + (gridBottom - gridTop - rightTotalH) / 2f
            drawSectionLabel(canvas, page.rightTitle, rightX, cell, rightStartY - 22f, rightStartY - 12f, Color.parseColor("#8D6E63"))
            page.rightRows.forEachIndexed { i, rowLetters ->
                rowLetters.forEach { letter ->
                    drawColoredCell(canvas, letter, config, rightX, rightStartY + i * rowH, cell, cell, font,
                        filled = true, radius = 18f, textRatio = 0.38f)
                }
            }
        }

        canvas.drawText(
            "Лист $pageNum из $totalPages. Повесьте листы в ряд по порядку — получится лента букв во всю доску",
            PAGE_WIDTH / 2f, 428f, Paint().apply {
                isAntiAlias = true; textSize = 11f; color = COL_SUB
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER
            })
    }
}
