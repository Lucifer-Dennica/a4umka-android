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
            val boardPages = getBoardPages()
            for ((index, page) in boardPages.withIndex()) {
                val bmp = Bitmap.createBitmap(
                    (PAGE_WIDTH * PREVIEW_SCALE).toInt(),
                    (PAGE_HEIGHT * PREVIEW_SCALE).toInt(),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bmp)
                canvas.scale(PREVIEW_SCALE.toFloat(), PREVIEW_SCALE.toFloat())
                drawBoardPage(canvas, config, page, index + 1, boardPages.size, context)
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

    // Структура страницы «Лента на доску»
    data class BoardPage(
        val title: String,
        val subtitle: String,
        val category: String,  // «гласные» / «согласные» / «знаки»
        val rows: List<List<RibbonLetter>>,  // ряды букв (1 или 2 ряда)
        val showBell: Boolean,  // 🔔 верхний ряд — звонкие
        val showHeadphones: Boolean  // 🎧 нижний ряд — глухие
    )

    // ============ 4 страницы BOARD согласно сайту ============
    private fun getBoardPages(): List<BoardPage> {
        // Стр. 1: Гласные (5+5 в 2 ряда)
        val page1 = BoardPage(
            title = "Гласные",
            subtitle = "вверху — после твёрдых, внизу — после мягких",
            category = "гласные",
            rows = listOf(RibbonData.topVowels, RibbonData.bottomVowels),
            showBell = false,
            showHeadphones = false
        )
        // Стр. 2: Звонкие непарные (Л М Н Р Й) — 1 ряд
        val sonorants = RibbonData.topConsonants.subList(0, 5)  // Л М Н Р Й
        val page2 = BoardPage(
            title = "Звонкие непарные",
            subtitle = "нет пары среди глухих",
            category = "согласные",
            rows = listOf(sonorants),
            showBell = true,
            showHeadphones = false
        )
        // Стр. 3: Звонкие и глухие пары (6+6 в 2 ряда)
        val pairedVoiced = RibbonData.topConsonants.subList(5, 11)  // Б В Г Д Ж З
        val pairedVoiceless = RibbonData.bottomConsonants.subList(0, 6)  // П Ф К Т Ш С
        val page3 = BoardPage(
            title = "Звонкие и глухие пары",
            subtitle = "звонкие вверху, глухие под ними",
            category = "согласные",
            rows = listOf(pairedVoiced, pairedVoiceless),
            showBell = true,
            showHeadphones = true
        )
        // Стр. 4: Глухие непарные + Ь, Ъ (4 в 1 ряд + 2 знака)
        val unpairedVoiceless = RibbonData.bottomConsonants.subList(6, 10)  // Х Ц Ч Щ
        val combinedRow = unpairedVoiceless + RibbonData.signs
        val page4 = BoardPage(
            title = "Глухие непарные, Ь и Ъ",
            subtitle = "Х Ц Ч Щ — нет пары, Ь и Ъ не обозначают звуков",
            category = "согласные и знаки",
            rows = listOf(combinedRow),
            showBell = false,
            showHeadphones = true
        )
        return listOf(page1, page2, page3, page4)
    }

    // ============ ЛЕНТА НА ДОСКУ — ОДНА СТРАНИЦА ============
    fun drawBoardPage(
        canvas: Canvas, config: RibbonConfig,
        page: BoardPage, pageNum: Int, totalPages: Int, context: Context
    ) {
        val bg = if (config.bgColor == RibbonBgColor.PEACH) PEACH_COLOR else Color.WHITE
        canvas.drawColor(bg)

        val font = ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT

        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 40f; color = Color.DKGRAY
            typeface = font; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(page.title, PAGE_WIDTH / 2f, 55f, titlePaint)

        // Подзаголовок
        val subPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.GRAY
            typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(page.subtitle, PAGE_WIDTH / 2f, 78f, subPaint)

        // Категория (мелкая надпись справа сверху)
        val catPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = Color.parseColor("#9E9E9E")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(page.category, PAGE_WIDTH - 40f, 55f, catPaint)

        // Клетки: 1 или 2 ряда
        val rows = page.rows
        val maxCols = rows.maxOf { it.size }
        val rowCount = rows.size

        // Адаптивный размер клетки
        val maxCellByCols = (PAGE_WIDTH - 80f) / maxCols - 4f
        val maxCellByRows = (PAGE_HEIGHT - 270f) / rowCount - 4f
        val cellSize = minOf(maxCellByCols, maxCellByRows, 110f)
        val gap = 4f
        val colWidth = cellSize + gap
        val rowHeight = cellSize + gap

        // Центрируем по горизонтали (каждый ряд отдельно, если их длина разная)
        // Общая ширина определяется самым длинным рядом
        val totalWidth = maxCols * colWidth - gap
        val totalHeight = rowCount * rowHeight - gap
        val baseX = (PAGE_WIDTH - totalWidth) / 2f
        val startY = 120f + (PAGE_HEIGHT - 120f - 100f - totalHeight) / 2f

        val emptyMissing = emptySet<String>()
        rows.forEachIndexed { rowIndex, rowLetters ->
            // Центрируем каждый ряд внутри общей ширины
            val rowWidth = rowLetters.size * colWidth - gap
            val rowStartX = baseX + (totalWidth - rowWidth) / 2f
            val rowY = startY + rowIndex * rowHeight

            // Иконка 🔔 слева от верхнего ряда (звонкие)
            if (page.showBell && rowIndex == 0) {
                val iconPaint = Paint().apply {
                    isAntiAlias = true; textSize = 24f; textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🔔", rowStartX - 25f, rowY + cellSize / 2f + 8f, iconPaint)
            }
            // Иконка 🎧 слева от нижнего ряда (глухие)
            if (page.showHeadphones && rowIndex == rows.size - 1 && rowCount > 1) {
                val iconPaint = Paint().apply {
                    isAntiAlias = true; textSize = 24f; textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🎧", rowStartX - 25f, rowY + cellSize / 2f + 8f, iconPaint)
            }
            // Для одного ряда — тоже показываем иконку, если надо
            if (page.showHeadphones && rowCount == 1) {
                val iconPaint = Paint().apply {
                    isAntiAlias = true; textSize = 24f; textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🎧", rowStartX - 25f, rowY + cellSize / 2f + 8f, iconPaint)
            }

            rowLetters.forEachIndexed { colIndex, letter ->
                val x = rowStartX + colIndex * colWidth
                drawCell(canvas, letter, config, x, rowY, cellSize, emptyMissing, font, false)
            }
        }

        // Нижняя заметка
        val notePaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = Color.GRAY
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Лист $pageNum из $totalPages. Повесьте листы в ряд по порядку — получится лента букв во всю доску",
            PAGE_WIDTH / 2f, PAGE_HEIGHT - 30f, notePaint
        )
    }

    // ============ ОТРИСОВКА ДОКУМЕНТА (PDF) ============
    fun drawDocument(document: PdfDocument, config: RibbonConfig, context: Context) {
        var pageNum = 1

        if (config.mode == RibbonMode.BOARD) {
            val boardPages = getBoardPages()
            boardPages.forEachIndexed { index, page ->
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
                val docPage = document.startPage(pageInfo)
                drawBoardPage(docPage.canvas, config, page, index + 1, boardPages.size, context)
                document.finishPage(docPage)
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
