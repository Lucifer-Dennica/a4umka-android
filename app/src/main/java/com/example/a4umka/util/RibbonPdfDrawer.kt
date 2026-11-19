package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.example.a4umka.R
import com.example.a4umka.data.*
import kotlin.math.ceil
import kotlin.random.Random

object RibbonPdfDrawer {
    // Альбомная ориентация: 842 × 595 (A4 landscape)
    const val PAGE_WIDTH = 842f
    const val PAGE_HEIGHT = 595f
    private const val PREVIEW_SCALE = 6  // 4K: 5052×3570 px

    private val PEACH_COLOR = Color.parseColor("#FFF3E0")

    // ============ ПРЕДПРОСМОТР (1 лист) ============
    fun getPreview(config: RibbonConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()

        // Для BOARD — 4 страницы
        if (config.mode == RibbonMode.BOARD) {
            val boardPages = listOf(
                Triple("Гласные", "лента на доску — лист 1 из 4", RibbonData.topVowels + RibbonData.bottomVowels),
                Triple("Звонкие согласные", "лента на доску — лист 2 из 4", RibbonData.topConsonants),
                Triple("Глухие согласные", "лента на доску — лист 3 из 4", RibbonData.bottomConsonants),
                Triple("Ъ и Ь знаки", "лента на доску — лист 4 из 4", RibbonData.signs)
            )
            for ((title, sub, letters) in boardPages) {
                val bmp = Bitmap.createBitmap(
                    (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
                    (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bmp)
                canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
                drawBoardPage(canvas, config, title, sub, letters, context)
                pages.add(bmp)
            }
            return pages
        }

        // Для остальных — 1 страница + страница ответов
        val bmp = Bitmap.createBitmap(
            (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
            (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
        drawRibbon(canvas, config, context, isAnswerKey = false)
        pages.add(bmp)

        if (config.showAnswers && (config.mode == RibbonMode.MISSING || config.mode == RibbonMode.COLORING)) {
            val ansBmp = Bitmap.createBitmap(
                (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
                (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
                Bitmap.Config.ARGB_8888
            )
            val ansCanvas = Canvas(ansBmp)
            ansCanvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
            drawRibbon(ansCanvas, config, context, isAnswerKey = true)
            pages.add(ansBmp)
        }

        return pages
    }

    // ============ ОБЩАЯ ОТРИСОВКА (DESK / MISSING / COLORING) ============
    fun drawRibbon(canvas: Canvas, config: RibbonConfig, context: Context, isAnswerKey: Boolean = false) {
        val bg = if (config.bgColor == RibbonBgColor.PEACH) PEACH_COLOR else Color.WHITE
        canvas.drawColor(bg)

        val font = ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT

        val titleText = when {
            isAnswerKey -> "Ответы"
            config.mode == RibbonMode.DESK -> "Лента букв"
            config.mode == RibbonMode.MISSING -> "Впиши пропущенные буквы"
            config.mode == RibbonMode.COLORING -> "Раскрась ленту букв"
            else -> "Лента букв"
        }

        val subtitleText = when {
            isAnswerKey -> "лента букв целиком"
            config.mode == RibbonMode.DESK -> "Гласные и согласные, звонкие и глухие, твердые и мягкие"
            config.mode == RibbonMode.MISSING -> "Впиши буквы в пустые клетки: цвет клетки и соседи — подсказка"
            config.mode == RibbonMode.COLORING -> "Раскрась клетки по правилам"
            else -> ""
        }

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = Color.DKGRAY
            typeface = font; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(titleText, PAGE_WIDTH / 2f, 50f, titlePaint)

        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.GRAY
            typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitleText, PAGE_WIDTH / 2f, 70f, subtitlePaint)

        if (config.mode != RibbonMode.DESK || isAnswerKey) {
            val fieldPaint = Paint().apply {
                isAntiAlias = true; textSize = 16f; color = Color.DKGRAY
                typeface = Typeface.DEFAULT
            }
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

            canvas.drawText("Имя:", 40f, 110f, fieldPaint)
            canvas.drawLine(80f, 112f, 200f, 112f, linePaint)
            canvas.drawText("Дата:", PAGE_WIDTH - 160f, 110f, fieldPaint)
            canvas.drawLine(PAGE_WIDTH - 115f, 112f, PAGE_WIDTH - 40f, 112f, linePaint)
        }

        // Лента центрирована по горизонтали
        val cellSize = 36f
        val gap = 2f
        val colWidth = cellSize + gap
        val rowHeight = cellSize + gap

        // Считаем общую ширину ленты для центрирования
        // 5 гласных + 11 согласных + 2 знака (в столбик) = 17 столбцов + 2 промежутка
        val totalWidth = 5 * colWidth + 20f + 11 * colWidth + 10f + colWidth
        val startX = (PAGE_WIDTH - totalWidth) / 2f
        val startY = 150f

        val missingSet = mutableSetOf<String>()
        if (config.mode == RibbonMode.MISSING && !isAnswerKey) {
            val allLetters = (RibbonData.topVowels + RibbonData.bottomVowels + RibbonData.topConsonants + RibbonData.bottomConsonants).map { it.char }
            val countToHide = when (config.missingCount) {
                MissingCount.EIGHT -> 8
                MissingCount.SIXTEEN -> 16
                MissingCount.ALL -> allLetters.size
            }
            val random = Random(config.seed)
            missingSet.addAll(allLetters.shuffled(random).take(countToHide))
        }

        // Гласные
        val vowelStartX = startX
        RibbonData.topVowels.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, vowelStartX + index * colWidth, startY, cellSize, missingSet, font, isAnswerKey)
            drawCell(canvas, RibbonData.bottomVowels[index], config, vowelStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        // Согласные
        val consonantStartX = vowelStartX + 5 * colWidth + 20f
        RibbonData.topConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY, cellSize, missingSet, font, isAnswerKey)
        }
        RibbonData.bottomConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        // Знаки (вертикально)
        val signStartX = consonantStartX + 11 * colWidth + 10f
        RibbonData.signs.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, signStartX, startY + index * rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        if (config.mode == RibbonMode.MISSING && config.showHint && missingSet.isNotEmpty()) {
            val hintBoxTop = startY + 2 * rowHeight + 30f
            val hintBoxBottom = hintBoxTop + 60f
            drawHintBox(
                canvas,
                startX, hintBoxTop, PAGE_WIDTH - startX, hintBoxBottom,
                "Эти буквы потерялись",
                missingSet.joinToString(", ")
            )
        }

        if (config.mode == RibbonMode.COLORING && !isAnswerKey) {
            val hintBoxTop = startY + 2 * rowHeight + 30f
            val hintBoxBottom = hintBoxTop + 60f
            drawHintBox(
                canvas,
                startX, hintBoxTop, PAGE_WIDTH - startX, hintBoxBottom,
                "Как раскрасить",
                "красные — гласные • синие — Ж Ш Ц (твердые) • зеленые — Й Ч Щ (мягкие) • сине-зеленые — остальные • белые — Ь Ъ"
            )
        }

        drawLegend(canvas, 40f, PAGE_HEIGHT - 110f)
    }

    // ============ ЛЕНТА НА ДОСКУ — 4 СТРАНИЦЫ С КРУПНЫМИ БУКВАМИ ============
    fun drawBoardPage(
        canvas: Canvas, config: RibbonConfig,
        title: String, subtitle: String,
        letters: List<RibbonLetter>, context: Context
    ) {
        val bg = if (config.bgColor == RibbonBgColor.PEACH) PEACH_COLOR else Color.WHITE
        canvas.drawColor(bg)

        val font = ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 40f; color = Color.DKGRAY
            typeface = font; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, PAGE_WIDTH / 2f, 60f, titlePaint)

        val subPaint = Paint().apply {
            isAntiAlias = true; textSize = 16f; color = Color.GRAY
            typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle, PAGE_WIDTH / 2f, 85f, subPaint)

        // Крупные клетки в 2 ряда (или 1, если букв мало)
        val count = letters.size
        val rows = when {
            count <= 2 -> 1  // знаки — 1 ряд
            count <= 6 -> 1  // если меньше 6 — в 1 ряд
            else -> 2
        }
        val cols = ceil(count.toFloat() / rows).toInt()

        // Адаптивный размер клетки: чем меньше букв, тем больше клетка
        val maxCellByCols = (PAGE_WIDTH - 80f) / cols - 4f
        val maxCellByRows = (PAGE_HEIGHT - 200f) / rows - 4f
        val cellSize = minOf(maxCellByCols, maxCellByRows, 110f)  // максимум 110pt
        val gap = 4f
        val colWidth = cellSize + gap
        val rowHeight = cellSize + gap

        val totalWidth = cols * colWidth - gap
        val totalHeight = rows * rowHeight - gap
        val startX = (PAGE_WIDTH - totalWidth) / 2f
        val startY = 130f + (PAGE_HEIGHT - 130f - 80f - totalHeight) / 2f  // центр по вертикали

        val emptyMissing = emptySet<String>()
        letters.forEachIndexed { index, letter ->
            val col = index % cols
            val row = index / cols
            val x = startX + col * colWidth
            val y = startY + row * rowHeight
            drawCell(canvas, letter, config, x, y, cellSize, emptyMissing, font, false)
        }

        drawLegend(canvas, 40f, PAGE_HEIGHT - 80f)
    }

    // ============ ОТРИСОВКА ДОКУМЕНТА (PDF) ============
    fun drawDocument(document: PdfDocument, config: RibbonConfig, context: Context) {
        var pageNum = 1

        if (config.mode == RibbonMode.BOARD) {
            val boardPages = listOf(
                Triple("Гласные", "лента на доску — лист 1 из 4", RibbonData.topVowels + RibbonData.bottomVowels),
                Triple("Звонкие согласные", "лента на доску — лист 2 из 4", RibbonData.topConsonants),
                Triple("Глухие согласные", "лента на доску — лист 3 из 4", RibbonData.bottomConsonants),
                Triple("Ъ и Ь знаки", "лента на доску — лист 4 из 4", RibbonData.signs)
            )
            for ((title, sub, letters) in boardPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
                val page = document.startPage(pageInfo)
                drawBoardPage(page.canvas, config, title, sub, letters, context)
                document.finishPage(page)
            }
            return
        }

        // Основной лист
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
        val page = document.startPage(pageInfo)
        drawRibbon(page.canvas, config, context, isAnswerKey = false)
        document.finishPage(page)

        // Лист ответов
        if (config.showAnswers && (config.mode == RibbonMode.MISSING || config.mode == RibbonMode.COLORING)) {
            val ansPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val ansPage = document.startPage(ansPageInfo)
            drawRibbon(ansPage.canvas, config, context, isAnswerKey = true)
            document.finishPage(ansPage)
        }
    }

    // ============ ОТРИСОВКА КЛЕТКИ ============
    private fun drawCell(
        canvas: Canvas, letter: RibbonLetter, config: RibbonConfig,
        x: Float, y: Float, size: Float,
        missing: Set<String>, font: Typeface, isAnswerKey: Boolean
    ) {
        val rectPaint = Paint().apply { style = Paint.Style.FILL }

        val cellColor = when (letter.color) {
            RibbonColor.RED -> Color.parseColor("#EF5350")
            RibbonColor.BLUE -> Color.parseColor("#42A5F5")
            RibbonColor.GREEN -> Color.parseColor("#66BB6A")
            RibbonColor.WHITE -> Color.WHITE
            RibbonColor.BOTH -> Color.WHITE
        }

        if (config.mode == RibbonMode.COLORING) {
            rectPaint.color = Color.WHITE
            rectPaint.style = Paint.Style.STROKE
            rectPaint.strokeWidth = 2f
            canvas.drawRect(x, y, x + size, y + size, rectPaint)

            val outlinePaint = Paint().apply {
                color = if (letter.color == RibbonColor.WHITE) Color.GRAY else cellColor
                style = Paint.Style.STROKE; strokeWidth = 2f
            }
            canvas.drawRect(x, y, x + size, y + size, outlinePaint)
        } else {
            rectPaint.color = cellColor
            canvas.drawRect(x, y, x + size, y + size, rectPaint)

            if (letter.color == RibbonColor.BOTH) {
                // Нижний правый треугольник — зелёный (мягкий)
                val path = Path().apply { moveTo(x, y + size); lineTo(x + size, y); lineTo(x + size, y + size); close() }
                canvas.drawPath(path, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
                // Верхний левый треугольник — синий (твёрдый)
                val pathBlue = Path().apply { moveTo(x, y); lineTo(x + size, y); lineTo(x, y + size); close() }
                canvas.drawPath(pathBlue, Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
            }

            val borderPaint = Paint().apply {
                color = if (letter.color == RibbonColor.WHITE) Color.LTGRAY else Color.TRANSPARENT
                style = Paint.Style.STROKE; strokeWidth = 1f
            }
            canvas.drawRect(x, y, x + size, y + size, borderPaint)
        }

        if (config.mode != RibbonMode.COLORING) {
            val text = when (config.case) {
                RibbonCase.BOTH -> letter.char
                RibbonCase.UPPERCASE -> letter.char.substring(0, 1)
                RibbonCase.LOWERCASE -> letter.char.substring(letter.char.length - 1)
            }

            // Размер шрифта пропорционален размеру клетки
            val fontScale = size * 0.6f
            val textPaint = Paint().apply {
                isAntiAlias = true; textSize = fontScale; typeface = font; textAlign = Paint.Align.CENTER
            }

            if (letter.char in missing && !isAnswerKey) {
                // Пустая клетка с пунктиром
                canvas.drawRect(
                    x + 2, y + 2, x + size - 2, y + size - 2,
                    Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
                )
                canvas.drawRect(
                    x, y, x + size, y + size,
                    Paint().apply {
                        color = Color.GRAY; style = Paint.Style.STROKE; strokeWidth = 2f
                        pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
                    }
                )
            } else {
                textPaint.color = if (letter.color == RibbonColor.WHITE) Color.DKGRAY else Color.WHITE

                if (isAnswerKey && letter.char in missing) {
                    textPaint.color = Color.RED
                    textPaint.isFakeBoldText = true
                }

                val baselineY = y + (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
                canvas.drawText(text, x + size / 2f, baselineY, textPaint)
            }
        }
    }

    private fun drawHintBox(
        canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float,
        title: String, content: String
    ) {
        // Рамка с заливкой
        val bgPaint = Paint().apply { color = Color.parseColor("#FFFBF0"); style = Paint.Style.FILL }
        canvas.drawRoundRect(android.graphics.RectF(left, top, right, bottom), 8f, 8f, bgPaint)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#FFB74D"); style = Paint.Style.STROKE; strokeWidth = 1.5f
        }
        canvas.drawRoundRect(android.graphics.RectF(left, top, right, bottom), 8f, 8f, borderPaint)

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.parseColor("#E65100")
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(title, left + 14f, top + 22f, titlePaint)

        val contentPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.DKGRAY
            typeface = Typeface.DEFAULT
        }
        // Перенос по длине
        val maxWidth = right - left - 28f
        val words = content.split(" ")
        var line = ""
        var y = top + 45f
        for (word in words) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (contentPaint.measureText(test) > maxWidth) {
                canvas.drawText(line, left + 14f, y, contentPaint)
                line = word
                y += 18f
            } else {
                line = test
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line, left + 14f, y, contentPaint)
    }

    // ============ Легенда в красивой рамке (5 пунктов + звонкие/глухие) ============
    private fun drawLegend(canvas: Canvas, x: Float, y: Float) {
        // Рамка
        val padding = 12f
        val boxLeft = x
        val boxTop = y
        val boxRight = PAGE_WIDTH - 40f
        val boxBottom = y + 95f

        val bgPaint = Paint().apply { color = Color.parseColor("#FAFAFA"); style = Paint.Style.FILL }
        canvas.drawRoundRect(android.graphics.RectF(boxLeft, boxTop, boxRight, boxBottom), 8f, 8f, bgPaint)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#BDBDBD"); style = Paint.Style.STROKE; strokeWidth = 1f
        }
        canvas.drawRoundRect(android.graphics.RectF(boxLeft, boxTop, boxRight, boxBottom), 8f, 8f, borderPaint)

        val textPaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = Color.DKGRAY
            typeface = Typeface.DEFAULT
        }
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.DKGRAY
            typeface = Typeface.DEFAULT_BOLD
        }

        canvas.drawText("Обозначения", boxLeft + padding, boxTop + 22f, titlePaint)

        // Две колонки, чтобы влезло
        val col1X = boxLeft + padding
        val col2X = boxLeft + (boxRight - boxLeft) / 2f + 10f
        var currentY = boxTop + 45f
        val lineH = 18f

        // Колонка 1
        // Красный — гласные
        canvas.drawRect(col1X, currentY - 9f, col1X + 14f, currentY + 5f, Paint().apply { color = Color.parseColor("#EF5350"); style = Paint.Style.FILL })
        canvas.drawText("Гласные: А О У Ы Э, Я Ё Ю И Е", col1X + 22f, currentY, textPaint)
        currentY += lineH

        // Синий — всегда твёрдые
        canvas.drawRect(col1X, currentY - 9f, col1X + 14f, currentY + 5f, Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
        canvas.drawText("Всегда твёрдые: Ж Ш Ц", col1X + 22f, currentY, textPaint)
        currentY += lineH

        // Зелёный — всегда мягкие
        canvas.drawRect(col1X, currentY - 9f, col1X + 14f, currentY + 5f, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
        canvas.drawText("Всегда мягкие: Й Ч Щ", col1X + 22f, currentY, textPaint)
        currentY += lineH

        // Колонка 2
        currentY = boxTop + 45f
        // Сине-зелёный — твёрдые или мягкие (два треугольника)
        val p = android.graphics.Path().apply { moveTo(col2X, currentY); lineTo(col2X + 14f, currentY); lineTo(col2X, currentY + 14f); close() }
        canvas.drawPath(p, Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
        val p2 = android.graphics.Path().apply { moveTo(col2X + 14f, currentY); lineTo(col2X + 14f, currentY + 14f); lineTo(col2X, currentY + 14f); close() }
        canvas.drawPath(p2, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
        canvas.drawText("Твёрдые или мягкие (остальные)", col2X + 22f, currentY, textPaint)
        currentY += lineH

        // Белый пустой — Ь, Ъ
        canvas.drawRect(col2X, currentY - 9f, col2X + 14f, currentY + 5f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        canvas.drawRect(col2X, currentY - 9f, col2X + 14f, currentY + 5f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })
        canvas.drawText("Ь и Ъ — не обозначают звуков", col2X + 22f, currentY, textPaint)
        currentY += lineH

        // Звонкие / глухие
        canvas.drawText("🔊 Верхний ряд — звонкие    🔈 Нижний ряд — глухие", col2X, currentY, textPaint)
    }
}
