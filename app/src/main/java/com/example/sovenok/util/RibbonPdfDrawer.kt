package com.example.sovenok.util

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
import com.example.sovenok.R
import com.example.sovenok.data.*
import kotlin.math.ceil
import kotlin.random.Random

object RibbonPdfDrawer {
    // Альбомная ориентация: 842 × 595 (A4 landscape)
    const val PAGE_WIDTH = 842f
    const val PAGE_HEIGHT = 595f
    
    // ИЗМЕНЕНО: уменьшено с 6 до 3, чтобы избежать OutOfMemoryError (OOM). 
    // 3 даёт 2526×1785 px (~18 МБ на лист), что достаточно для чёткого превью и безопасно для памяти.
    private const val PREVIEW_SCALE = 3

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

        // Выбор шрифта: обычный или письменный
        val font = if (config.font == RibbonFont.CURSIVE) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

        // Для COLORING — отдельная отрисовка
        if (config.mode == RibbonMode.COLORING) {
            drawColoringPage(canvas, config, font, isAnswerKey)
            return
        }

        val titleText = when {
            isAnswerKey -> "Ответы"
            config.mode == RibbonMode.DESK -> "Лента букв"
            config.mode == RibbonMode.MISSING -> "Впиши пропущенные буквы"
            else -> "Лента букв"
        }

        val subtitleText = when {
            isAnswerKey -> "лента букв целиком"
            config.mode == RibbonMode.DESK -> "Гласные и согласные, звонкие и глухие, твердые и мягкие"
            config.mode == RibbonMode.MISSING -> "Впиши буквы в пустые клетки: цвет клетки и соседи — подсказка"
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

        val vowelStartX = startX
        val consonantStartX = vowelStartX + 5 * colWidth + 20f
        val signStartX = consonantStartX + 11 * colWidth + 10f

        val sectionLabelPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = Color.parseColor("#9E9E9E")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER
        }
        val labelY = startY - 8f
        canvas.drawText("гласные", vowelStartX + 2.5f * colWidth, labelY, sectionLabelPaint)
        canvas.drawText("согласные", consonantStartX + 5.5f * colWidth, labelY, sectionLabelPaint)
        canvas.drawText("знаки", signStartX + 0.5f * colWidth, labelY, sectionLabelPaint)

        // Гласные
        RibbonData.topVowels.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, vowelStartX + index * colWidth, startY, cellSize, missingSet, font, isAnswerKey)
            drawCell(canvas, RibbonData.bottomVowels[index], config, vowelStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        // Согласные
        RibbonData.topConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY, cellSize, missingSet, font, isAnswerKey)
        }
        RibbonData.bottomConsonants.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, consonantStartX + index * colWidth, startY + rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        // Знаки (вертикально)
        RibbonData.signs.forEachIndexed { index, letter ->
            drawCell(canvas, letter, config, signStartX, startY + index * rowHeight, cellSize, missingSet, font, isAnswerKey)
        }

        // Подсказка «Эти буквы потерялись»
        if (config.mode == RibbonMode.MISSING && config.showHint && missingSet.isNotEmpty()) {
            val hintBoxTop = startY + 2 * rowHeight + 30f
            drawLostLettersBox(canvas, startX, hintBoxTop, PAGE_WIDTH - startX, missingSet, font)
        }

        drawLegend(canvas, 40f, PAGE_HEIGHT - 110f)
    }

    // Структура страницы «Лента на доску»
    data class BoardPage(
        val title: String,
        val subtitle: String,
        val category: String,
        val rows: List<List<RibbonLetter>>,
        val showBell: Boolean,
        val showHeadphones: Boolean,
        val rightRows: List<List<RibbonLetter>>? = null,
        val rightTitle: String? = null
    )

    // ============ 4 страницы BOARD ============
    private fun getBoardPages(): List<BoardPage> {
        val page1 = BoardPage(
            title = "Гласные",
            subtitle = "вверху — после твёрдых, внизу — после мягких",
            category = "гласные",
            rows = listOf(RibbonData.topVowels, RibbonData.bottomVowels),
            showBell = false,
            showHeadphones = false
        )
        val sonorants = RibbonData.topConsonants.subList(0, 5)
        val page2 = BoardPage(
            title = "Звонкие непарные",
            subtitle = "нет пары среди глухих",
            category = "согласные",
            rows = listOf(sonorants),
            showBell = true,
            showHeadphones = false
        )
        val pairedVoiced = RibbonData.topConsonants.subList(5, 11)
        val pairedVoiceless = RibbonData.bottomConsonants.subList(0, 6)
        val page3 = BoardPage(
            title = "Звонкие и глухие пары",
            subtitle = "звонкие вверху, глухие под ними",
            category = "согласные",
            rows = listOf(pairedVoiced, pairedVoiceless),
            showBell = true,
            showHeadphones = true
        )
        val unpairedVoiceless = RibbonData.bottomConsonants.subList(6, 10)
        val signsVertical = RibbonData.signs.map { listOf(it) }
        val page4 = BoardPage(
            title = "Глухие непарные, Ь и Ъ",
            subtitle = "Х Ц Ч Щ — нет звонкой пары, Ь и Ъ звуков не обозначают",
            category = "согласные",
            rows = listOf(unpairedVoiceless),
            showBell = false,
            showHeadphones = true,
            rightRows = signsVertical,
            rightTitle = "знаки"
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

        val font = if (config.font == RibbonFont.CURSIVE) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 40f; color = Color.DKGRAY
            typeface = font; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(page.title, PAGE_WIDTH / 2f, 55f, titlePaint)

        val subPaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.GRAY
            typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(page.subtitle, PAGE_WIDTH / 2f, 78f, subPaint)

        val catPaint = Paint().apply {
            isAntiAlias = true; textSize = 12f; color = Color.parseColor("#9E9E9E")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(page.category, PAGE_WIDTH - 40f, 55f, catPaint)

        val rows = page.rows
        val rightRows = page.rightRows

        val reservedRight = if (rightRows != null) 3 * 110f + 60f else 0f
        val maxColsLeft = rows.maxOf { it.size }
        val rowCount = rows.size

        val availableWidth = PAGE_WIDTH - 80f - reservedRight
        val maxCellByCols = availableWidth / maxColsLeft - 4f
        val maxCellByRows = (PAGE_HEIGHT - 270f) / rowCount - 4f
        var cellSize = minOf(maxCellByCols, maxCellByRows, 110f)
        
        if (rightRows != null) {
            val rightRowCount = rightRows.size
            val maxCellByRightRows = (PAGE_HEIGHT - 270f) / rightRowCount - 4f
            cellSize = minOf(cellSize, maxCellByRightRows, 110f)
        }
        
        val gap = 4f
        val colWidth = cellSize + gap
        val rowHeight = cellSize + gap

        val totalWidth = maxColsLeft * colWidth - gap
        val totalHeight = rowCount * rowHeight - gap
        
        val baseX = if (rightRows != null) 60f else (PAGE_WIDTH - totalWidth) / 2f
        val startY = 120f + (PAGE_HEIGHT - 120f - 100f - totalHeight) / 2f

        val emptyMissing = emptySet<String>()
        rows.forEachIndexed { rowIndex, rowLetters ->
            val rowWidth = rowLetters.size * colWidth - gap
            val rowStartX = baseX + (totalWidth - rowWidth) / 2f
            val rowY = startY + rowIndex * rowHeight

            if (page.showBell && rowIndex == 0) {
                val iconPaint = Paint().apply { isAntiAlias = true; textSize = 24f; textAlign = Paint.Align.CENTER }
                canvas.drawText("🔔", rowStartX - 25f, rowY + cellSize / 2f + 8f, iconPaint)
            }
            if (page.showHeadphones && rowIndex == rows.size - 1 && rowCount > 1) {
                val iconPaint = Paint().apply { isAntiAlias = true; textSize = 24f; textAlign = Paint.Align.CENTER }
                canvas.drawText("🎧", rowStartX - 25f, rowY + cellSize / 2f + 8f, iconPaint)
            }
            if (page.showHeadphones && rowCount == 1) {
                val iconPaint = Paint().apply { isAntiAlias = true; textSize = 24f; textAlign = Paint.Align.CENTER }
                canvas.drawText("🎧", rowStartX - 25f, rowY + cellSize / 2f + 8f, iconPaint)
            }

            rowLetters.forEachIndexed { colIndex, letter ->
                val x = rowStartX + colIndex * colWidth
                drawCell(canvas, letter, config, x, rowY, cellSize, emptyMissing, font, false)
            }
        }

        if (rightRows != null && page.rightTitle != null) {
            val rightX = PAGE_WIDTH - 60f - cellSize
            val rightTotalHeight = rightRows.size * rowHeight - gap
            val rightStartY = 120f + (PAGE_HEIGHT - 120f - 100f - rightTotalHeight) / 2f

            val rightTitlePaint = Paint().apply {
                isAntiAlias = true; textSize = 12f; color = Color.parseColor("#9E9E9E")
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER
            }
            canvas.drawText(page.rightTitle, rightX + cellSize / 2f, rightStartY - 8f, rightTitlePaint)

            rightRows.forEachIndexed { rowIndex, rowLetters ->
                val rowY = rightStartY + rowIndex * rowHeight
                rowLetters.forEachIndexed { colIndex, letter ->
                    val x = rightX + colIndex * colWidth
                    drawCell(canvas, letter, config, x, rowY, cellSize, emptyMissing, font, false)
                }
            }
        }

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

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
        val page = document.startPage(pageInfo)
        drawRibbon(page.canvas, config, context, isAnswerKey = false)
        document.finishPage(page)

        if (config.showAnswers && (config.mode == RibbonMode.MISSING || config.mode == RibbonMode.COLORING)) {
            val ansPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val ansPage = document.startPage(ansPageInfo)
            drawRibbon(ansPage.canvas, config, context, isAnswerKey = true)
            document.finishPage(ansPage)
        }
    }

    // ============ ОТРИСОВКА КЛЕТКИ (DESK / MISSING) ============
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
                val path = Path().apply { moveTo(x, y + size); lineTo(x + size, y); lineTo(x + size, y + size); close() }
                canvas.drawPath(path, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
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

            val fontScale = size * 0.6f
            val textPaint = Paint().apply {
                isAntiAlias = true; textSize = fontScale; typeface = font; textAlign = Paint.Align.CENTER
            }

            if (letter.char in missing && !isAnswerKey) {
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

    // ========================================================================
    // ============ РЕЖИМ «РАСКРАСЬ ЛЕНТУ БУКВ» (НОВЫЙ ДИЗАЙН) ============
    // ========================================================================

    private fun drawColoringPage(canvas: Canvas, config: RibbonConfig, font: Typeface, isAnswerKey: Boolean) {
        // Фон — мягкий градиент персик → светло-зелёный
        canvas.drawRect(0f, 0f, PAGE_WIDTH, PAGE_HEIGHT, Paint().apply {
            shader = android.graphics.LinearGradient(
                0f, 0f, 0f, PAGE_HEIGHT,
                Color.parseColor("#FFF6E8"), Color.parseColor("#EAF2E0"),
                android.graphics.Shader.TileMode.CLAMP
            )
        })

        // ===== Шапка: боксы Имя / Дата + бейдж =====
        drawHeaderBox(canvas, "Имя:", 40f, 26f, 175f, 56f)
        drawHeaderBox(canvas, "Дата:", PAGE_WIDTH - 175f, 26f, PAGE_WIDTH - 40f, 56f)

        val badgeW = 130f
        val badgeH = 32f
        val badgeLeft = (PAGE_WIDTH - badgeW) / 2f
        canvas.drawRoundRect(android.graphics.RectF(badgeLeft, 24f, badgeLeft + badgeW, 24f + badgeH), 16f, 16f,
            Paint().apply { color = Color.parseColor("#C5E1A5"); style = Paint.Style.FILL })
        canvas.drawText(if (isAnswerKey) "✅ Ответы" else "🎨 Раскрась",
            PAGE_WIDTH / 2f, 24f + badgeH / 2f + 5f,
            Paint().apply { isAntiAlias = true; textSize = 13f; color = Color.parseColor("#33691E"); typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER })

        // ===== Заголовок + подзаголовок =====
        canvas.drawText(if (isAnswerKey) "Ответы" else "Раскрась ленту букв", PAGE_WIDTH / 2f, 118f,
            Paint().apply { isAntiAlias = true; textSize = 40f; color = Color.parseColor("#33691E"); typeface = font; textAlign = Paint.Align.CENTER })

        canvas.drawText(
            if (isAnswerKey) "лента букв целиком"
            else "Гласные — красным, всегда твёрдые — синим, всегда мягкие — зелёным, твёрдые или мягкие — пополам: синим и зелёным",
            PAGE_WIDTH / 2f, 142f,
            Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.parseColor("#8A8A8A"); typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); textAlign = Paint.Align.CENTER })

        // ===== Лента =====
        val cellW = 34f
        val cellH = 44f
        val gap = 3f
        val colW = cellW + gap
        val rowH = cellH + gap
        val startX = 40f
        val startY = 212f

        val vowelStartX = startX
        val consonantStartX = startX + 5 * colW + 26f      // зазор под иконки 🔔/🎧
        val signX = PAGE_WIDTH - 40f - cellW                // знаки — у правого края

        // Подписи секций + цветные линии
        drawSectionLabel(canvas, "гласные", vowelStartX, 5 * colW - gap, startY - 26f, startY - 14f, Color.parseColor("#EF5350"))
        drawSectionLabel(canvas, "согласные", consonantStartX, 11 * colW - gap, startY - 26f, startY - 14f, Color.parseColor("#42A5F5"))
        drawSectionLabel(canvas, "знаки", signX, cellW, startY - 26f, startY - 14f, Color.parseColor("#8D6E63"))

        // Иконки звонкие/глухие
        val iconPaint = Paint().apply { isAntiAlias = true; textSize = 18f; textAlign = Paint.Align.CENTER }
        canvas.drawText("🔔", consonantStartX - 14f, startY + cellH / 2f + 6f, iconPaint)
        canvas.drawText("🎧", consonantStartX - 14f, startY + rowH + cellH / 2f + 6f, iconPaint)

        // Гласные — 2 ряда по 5
        RibbonData.topVowels.forEachIndexed { i, letter ->
            drawColoringCell(canvas, letter, config, vowelStartX + i * colW, startY, cellW, cellH, font, isAnswerKey)
            drawColoringCell(canvas, RibbonData.bottomVowels[i], config, vowelStartX + i * colW, startY + rowH, cellW, cellH, font, isAnswerKey)
        }

        // Согласные — верхний ряд (звонкие, 11 клеток)
        RibbonData.topConsonants.forEachIndexed { i, letter ->
            drawColoringCell(canvas, letter, config, consonantStartX + i * colW, startY, cellW, cellH, font, isAnswerKey)
        }

        // Согласные — нижний ряд (глухие), СО СДВИГОМ НА 5 КЛЕТОК (под парными)
        RibbonData.bottomConsonants.forEachIndexed { i, letter ->
            drawColoringCell(canvas, letter, config, consonantStartX + (i + 5) * colW, startY + rowH, cellW, cellH, font, isAnswerKey)
        }

        // Знаки — вертикально у правого края
        RibbonData.signs.forEachIndexed { i, letter ->
            drawColoringCell(canvas, letter, config, signX, startY + i * rowH, cellW, cellH, font, isAnswerKey)
        }

        // ===== Легенда-чеклист =====
        drawColoringLegend(canvas)
    }

    private fun drawHeaderBox(canvas: Canvas, label: String, left: Float, top: Float, right: Float, bottom: Float) {
        val rect = android.graphics.RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, 10f, 10f, Paint().apply { color = Color.parseColor("#FDF3DC"); style = Paint.Style.FILL })
        canvas.drawRoundRect(rect, 10f, 10f, Paint().apply { color = Color.parseColor("#D9C9A3"); style = Paint.Style.STROKE; strokeWidth = 1.5f })
        val p = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
        val midY = (top + bottom) / 2f + 4f
        canvas.drawText(label, left + 10f, midY, p)
        canvas.drawLine(left + 10f + p.measureText(label) + 6f, midY - 2f, right - 10f, midY - 2f,
            Paint().apply { color = Color.parseColor("#C9B98F"); strokeWidth = 1f })
    }

    private fun drawSectionLabel(canvas: Canvas, text: String, left: Float, width: Float, textY: Float, lineY: Float, color: Int) {
        canvas.drawText(text, left + width / 2f, textY,
            Paint().apply { isAntiAlias = true; this.color = color; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER })
        canvas.drawLine(left, lineY, left + width, lineY, Paint().apply { this.color = color; strokeWidth = 2f })
    }

    private fun drawColoringCell(
        canvas: Canvas, letter: RibbonLetter, config: RibbonConfig,
        x: Float, y: Float, w: Float, h: Float, font: Typeface, isAnswerKey: Boolean
    ) {
        val rect = android.graphics.RectF(x, y, x + w, y + h)

        // Заливка (только в ответах)
        if (isAnswerKey) {
            when (letter.color) {
                RibbonColor.RED -> canvas.drawRoundRect(rect, 9f, 9f, Paint().apply { color = Color.parseColor("#EF5350"); style = Paint.Style.FILL })
                RibbonColor.BLUE -> canvas.drawRoundRect(rect, 9f, 9f, Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
                RibbonColor.GREEN -> canvas.drawRoundRect(rect, 9f, 9f, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
                RibbonColor.BOTH -> {
                    canvas.drawRoundRect(rect, 9f, 9f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
                    canvas.drawPath(android.graphics.Path().apply { moveTo(x, y); lineTo(x + w, y); lineTo(x, y + h); close() },
                        Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
                    canvas.drawPath(android.graphics.Path().apply { moveTo(x + w, y); lineTo(x + w, y + h); lineTo(x, y + h); close() },
                        Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
                }
                RibbonColor.WHITE -> canvas.drawRoundRect(rect, 9f, 9f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
            }
        } else {
            canvas.drawRoundRect(rect, 9f, 9f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        }

        // Рамка
        canvas.drawRoundRect(rect, 9f, 9f, Paint().apply { color = Color.parseColor("#9AA0A6"); style = Paint.Style.STROKE; strokeWidth = 1.5f })

        // Буква
        val text = when (config.case) {
            RibbonCase.BOTH -> letter.char
            RibbonCase.UPPERCASE -> letter.char.substring(0, 1)
            RibbonCase.LOWERCASE -> letter.char.substring(letter.char.length - 1)
        }
        val colored = isAnswerKey && letter.color != RibbonColor.WHITE
        val textPaint = Paint().apply {
            isAntiAlias = true; textSize = h * 0.45f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            color = if (colored) Color.WHITE else Color.parseColor("#212121")
        }
        canvas.drawText(text, x + w / 2f, y + h / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f), textPaint)
    }

    private fun drawColoringLegend(canvas: Canvas) {
        val left = 40f
        val top = 332f
        val right = PAGE_WIDTH - 40f
        val bottom = 562f
        val card = android.graphics.RectF(left, top, right, bottom)
        canvas.drawRoundRect(card, 16f, 16f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        canvas.drawRoundRect(card, 16f, 16f, Paint().apply { color = Color.parseColor("#E3E3E3"); style = Paint.Style.STROKE; strokeWidth = 1f })

        val col1 = left + 26f
        val col2 = left + (right - left) * 0.52f
        val lh = 30f

        var y = top + 34f
        drawCheckItem(canvas, col1, y, null, "гласные", " — А О У Ы Э — после твёрдых,", "Я Ё Ю И Е — после мягких"); y += lh + 16f
        drawCheckItem(canvas, col1, y, null, "всегда мягкие", " — Й Ч Щ"); y += lh
        drawCheckItem(canvas, col1, y, null, "звука не обозначают", " — Ь и Ъ"); y += lh
        drawCheckItem(canvas, col1, y, "🎧", "", "нижний ряд согласных — глухие")

        y = top + 34f
        drawCheckItem(canvas, col2, y, null, "всегда твёрдые", " — Ж Ш Ц"); y += lh
        drawCheckItem(canvas, col2, y, null, "твёрдые или мягкие", " — остальные согласные"); y += lh
        drawCheckItem(canvas, col2, y, "🔔", "", "верхний ряд согласных — звонкие")
    }

    private fun drawCheckItem(canvas: Canvas, x: Float, y: Float, icon: String?, bold: String, rest: String, secondLine: String? = null) {
        val box = 15f
        if (icon != null) {
            canvas.drawText(icon, x + box / 2f, y + 3f, Paint().apply { isAntiAlias = true; textSize = 15f; textAlign = Paint.Align.CENTER })
        } else {
            val r = android.graphics.RectF(x, y - 12f, x + box, y + 3f)
            canvas.drawRoundRect(r, 4f, 4f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
            canvas.drawRoundRect(r, 4f, 4f, Paint().apply { color = Color.parseColor("#9AA0A6"); style = Paint.Style.STROKE; strokeWidth = 1.5f })
        }
        val textX = x + box + 8f
        val boldP = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.parseColor("#333333"); typeface = Typeface.DEFAULT_BOLD }
        val restP = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.parseColor("#555555"); typeface = Typeface.DEFAULT }
        canvas.drawText(bold, textX, y, boldP)
        canvas.drawText(rest, textX + boldP.measureText(bold), y, restP)
        if (secondLine != null) canvas.drawText(secondLine, textX, y + 16f, restP)
    }

    // ============ Подсказка «Эти буквы потерялись» ============
    private fun drawLostLettersBox(
        canvas: Canvas, left: Float, top: Float, right: Float,
        letters: Set<String>, font: Typeface
    ) {
        val padding = 14f
        val boxWidth = right - left
        val boxHeight = 100f
        val bottom = top + boxHeight

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
        canvas.drawText("Эти буквы потерялись:", left + padding, top + 22f, titlePaint)

        val letterSize = 28f
        val letterGap = 6f
        val lettersList = letters.toList()
        val rowWidth = boxWidth - padding * 2
        val maxPerRow = (rowWidth / (letterSize + letterGap)).toInt()

        val letterPaint = Paint().apply {
            isAntiAlias = true; textSize = letterSize * 0.55f; color = Color.DKGRAY
            typeface = font; textAlign = Paint.Align.CENTER
        }
        val dashedPaint = Paint().apply {
            color = Color.parseColor("#FFB74D"); style = Paint.Style.STROKE; strokeWidth = 1.5f
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        }

        var index = 0
        var row = 0
        while (index < lettersList.size) {
            val yRow = top + 38f + row * (letterSize + letterGap)
            val inRow = minOf(maxPerRow, lettersList.size - index)
            val rowStartX = left + padding + (rowWidth - inRow * (letterSize + letterGap) + letterGap) / 2f
            for (i in 0 until inRow) {
                val x = rowStartX + i * (letterSize + letterGap)
                canvas.drawRoundRect(
                    android.graphics.RectF(x, yRow, x + letterSize, yRow + letterSize),
                    4f, 4f, dashedPaint
                )
                val baselineY = yRow + letterSize / 2f - ((letterPaint.descent() + letterPaint.ascent()) / 2f)
                canvas.drawText(lettersList[index], x + letterSize / 2f, baselineY, letterPaint)
                index++
            }
            row++
        }
    }

    // ============ Легенда в красивой рамке (для DESK / MISSING) ============
    private fun drawLegend(canvas: Canvas, x: Float, y: Float) {
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

        val col1X = boxLeft + padding
        val col2X = boxLeft + (boxRight - boxLeft) / 2f + 10f
        var currentY = boxTop + 45f
        val lineH = 18f

        canvas.drawRect(col1X, currentY - 9f, col1X + 14f, currentY + 5f, Paint().apply { color = Color.parseColor("#EF5350"); style = Paint.Style.FILL })
        canvas.drawText("Гласные: А О У Ы Э, Я Ё Ю И Е", col1X + 22f, currentY, textPaint)
        currentY += lineH

        canvas.drawRect(col1X, currentY - 9f, col1X + 14f, currentY + 5f, Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
        canvas.drawText("Всегда твёрдые: Ж Ш Ц", col1X + 22f, currentY, textPaint)
        currentY += lineH

        canvas.drawRect(col1X, currentY - 9f, col1X + 14f, currentY + 5f, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
        canvas.drawText("Всегда мягкие: Й Ч Щ", col1X + 22f, currentY, textPaint)
        currentY += lineH

        currentY = boxTop + 45f
        val p = android.graphics.Path().apply { moveTo(col2X, currentY); lineTo(col2X + 14f, currentY); lineTo(col2X, currentY + 14f); close() }
        canvas.drawPath(p, Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
        val p2 = android.graphics.Path().apply { moveTo(col2X + 14f, currentY); lineTo(col2X + 14f, currentY + 14f); lineTo(col2X, currentY + 14f); close() }
        canvas.drawPath(p2, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
        canvas.drawText("Твёрдые или мягкие (остальные)", col2X + 22f, currentY, textPaint)
        currentY += lineH

        canvas.drawRect(col2X, currentY - 9f, col2X + 14f, currentY + 5f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        canvas.drawRect(col2X, currentY - 9f, col2X + 14f, currentY + 5f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })
        canvas.drawText("Ь и Ъ — не обозначают звуков", col2X + 22f, currentY, textPaint)
        currentY += lineH

        canvas.drawText("🔊 Верхний ряд — звонкие    🔈 Нижний ряд — глухие", col2X, currentY, textPaint)
    }
}
