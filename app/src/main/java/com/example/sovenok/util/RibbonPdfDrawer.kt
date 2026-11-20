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

        // Выбор шрифта: обычный или письменный
        val font = if (config.font == RibbonFont.CURSIVE) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

        // Для COLORING — отдельная отрисовка с цветными полосами
        if (config.mode == RibbonMode.COLORING) {
            drawColoringPage(canvas, config, font, isAnswerKey)
            return
        }

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

        // Считаем стартовые X для каждой секции
        val vowelStartX = startX
        val consonantStartX = vowelStartX + 5 * colWidth + 20f
        val signStartX = consonantStartX + 11 * colWidth + 10f

        // Метки секций над лентой
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

        // Подсказка «Эти буквы потерялись» — буквы в отдельных квадратиках
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
        val category: String,  // «гласные» / «согласные» / «знаки»
        val rows: List<List<RibbonLetter>>,  // ряды букв (1 или 2 ряда) — левая колонка
        val showBell: Boolean,  // 🔔 верхний ряд — звонкие
        val showHeadphones: Boolean,  // 🎧 нижний ряд — глухие
        // Для стр. 4: правая колонка со знаками
        val rightRows: List<List<RibbonLetter>>? = null,
        val rightTitle: String? = null
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
        // Стр. 4: Глухие непарные (слева) + Ь, Ъ (справа вертикально)
        val unpairedVoiceless = RibbonData.bottomConsonants.subList(6, 10)  // Х Ц Ч Щ
        // Знаки — вертикально, каждый знак в своей клетке
        val signsVertical = RibbonData.signs.map { listOf(it) }
        val page4 = BoardPage(
            title = "Глухие непарные, Ь и Ъ",
            subtitle = "Х Ц Ч Щ — нет звонкой пары, Ь и Ъ звуков не обозначают",
            category = "согласные",
            rows = listOf(unpairedVoiceless),  // Хх Цц Чч Щщ — один ряд
            showBell = false,
            showHeadphones = true,
            rightRows = signsVertical,  // Ьь и Ъъ — вертикально
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

        // Выбор шрифта: обычный или письменный
        val font = if (config.font == RibbonFont.CURSIVE) {
            ResourcesCompat.getFont(context, R.font.caveat_regular) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }

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
        val rightRows = page.rightRows

        // Адаптивный размер клетки
        // Если есть правая колонка — резервируем под неё место (3 клетки)
        val reservedRight = if (rightRows != null) 3 * 110f + 60f else 0f
        val maxColsLeft = rows.maxOf { it.size }
        val rowCount = rows.size

        // Учитываем правую колонку при расчёте ширины
        val availableWidth = PAGE_WIDTH - 80f - reservedRight
        val maxCellByCols = availableWidth / maxColsLeft - 4f
        val maxCellByRows = (PAGE_HEIGHT - 270f) / rowCount - 4f
        var cellSize = minOf(maxCellByCols, maxCellByRows, 110f)
        // Если правая колонка есть — её клетки могут быть больше
        if (rightRows != null) {
            val rightRowCount = rightRows.size
            val maxCellByRightRows = (PAGE_HEIGHT - 270f) / rightRowCount - 4f
            // Подбираем размер так, чтобы обе колонки смотрелись
            cellSize = minOf(cellSize, maxCellByRightRows, 110f)
        }        val gap = 4f
        val colWidth = cellSize + gap
        val rowHeight = cellSize + gap

        // Центрируем по горизонтали
        val totalWidth = maxColsLeft * colWidth - gap
        val totalHeight = rowCount * rowHeight - gap
        // Если правая колонка есть — базовый X левее, чтобы было место для знаков
        val baseX = if (rightRows != null) {
            60f  // отступ слева
        } else {
            (PAGE_WIDTH - totalWidth) / 2f
        }
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

        // Правая колонка со знаками (для стр. 4)
        if (rightRows != null && page.rightTitle != null) {
            val rightX = PAGE_WIDTH - 60f - cellSize
            val rightTotalHeight = rightRows.size * rowHeight - gap
            val rightStartY = 120f + (PAGE_HEIGHT - 120f - 100f - rightTotalHeight) / 2f

            // Заголовок «знаки» над правой колонкой
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

    // ============ РЕЖИМ «РАСКРАСЬ ЛЕНТУ БУКВ» ============
    // Цветные полосы-заголовки секций + прямоугольные клетки с буквой (контур — для раскраски)
    private fun drawColoringPage(canvas: Canvas, config: RibbonConfig, font: Typeface, isAnswerKey: Boolean) {
        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 32f; color = Color.DKGRAY
            typeface = font; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isAnswerKey) "Ответы" else "Раскрась ленту букв",
            PAGE_WIDTH / 2f, 50f, titlePaint
        )

        // Подзаголовок
        val subtitlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.GRAY
            typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isAnswerKey) "лента букв целиком"
            else "Гласные — красным, всегда твёрдые — синим, всегда мягкие — зелёным, твёрдые или мягкие — пополам",
            PAGE_WIDTH / 2f, 72f, subtitlePaint
        )

        // Поля «Имя» и «Дата»
        val fieldPaint = Paint().apply {
            isAntiAlias = true; textSize = 16f; color = Color.DKGRAY
            typeface = Typeface.DEFAULT
        }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawText("Имя:", 40f, 110f, fieldPaint)
        canvas.drawLine(80f, 112f, 200f, 112f, linePaint)
        canvas.drawText("Дата:", PAGE_WIDTH - 160f, 110f, fieldPaint)
        canvas.drawLine(PAGE_WIDTH - 115f, 112f, PAGE_WIDTH - 40f, 112f, linePaint)

        // === ЛЕНТА КАК ОДИН БЛОК В 2 РЯДА ===
        // Гласные | Согласные | Знаки — все на одной горизонтали
        val margin = 30f
        val cellGap = 2f
        val labelHeight = 18f  // тонкая аккуратная полоса
        val labelGap = 0f

        // Прямоугольные клетки: ширина > высоты
        // Считаем ширину по самой широкой секции (всего 17 клеток + 2 промежутка)
        val sectionGap = 15f
        val totalCols = 5 + 11 + 1  // 17
        val totalGaps = (totalCols - 1) * cellGap + 2 * sectionGap
        val availableWidth = PAGE_WIDTH - margin * 2 - totalGaps
        val cellWidth = availableWidth / totalCols  // ширина клетки
        val cellHeight = 48f  // высота клетки (меньше ширины → прямоугольник)

        // X-координаты начала каждой секции
        val vowelStartX = margin
        val consonantStartX = vowelStartX + 5 * (cellWidth + cellGap) + sectionGap
        val signStartX = consonantStartX + 11 * (cellWidth + cellGap) + sectionGap

        var currentY = 140f

        // === Тонкие цветные полосы-заголовки ===
        val barTextPaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        val iconPaint = Paint().apply {
            isAntiAlias = true; textSize = 11f; textAlign = Paint.Align.CENTER
        }

        // Гласные — красная тонкая полоса
        val vowelBarPaint = Paint().apply { color = Color.parseColor("#EF5350"); style = Paint.Style.FILL }
        val vowelBarWidth = 5 * cellWidth + 4 * cellGap
        canvas.drawRect(vowelStartX, currentY, vowelStartX + vowelBarWidth, currentY + labelHeight, vowelBarPaint)
        canvas.drawText("гласные", vowelStartX + vowelBarWidth / 2f, currentY + 13f, barTextPaint)

        // Согласные — синяя с иконками
        val consBarPaint = Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL }
        val consBarWidth = 11 * cellWidth + 10 * cellGap
        canvas.drawRect(consonantStartX, currentY, consonantStartX + consBarWidth, currentY + labelHeight, consBarPaint)
        canvas.drawText("🔔", consonantStartX + 12f, currentY + 14f, iconPaint)
        canvas.drawText("🎧", consonantStartX + 28f, currentY + 14f, iconPaint)
        canvas.drawText("согласные", consonantStartX + consBarWidth / 2f, currentY + 13f, barTextPaint)

        // Знаки — коричневая тонкая
        val signBarPaint = Paint().apply { color = Color.parseColor("#8D6E63"); style = Paint.Style.FILL }
        canvas.drawRect(signStartX, currentY, signStartX + cellWidth, currentY + labelHeight, signBarPaint)
        canvas.drawText("знаки", signStartX + cellWidth / 2f, currentY + 13f, barTextPaint)

        currentY += labelHeight + labelGap

        // === Прямоугольные клетки ленты (2 ряда) ===
        val emptyMissing = emptySet<String>()

        // Гласные — 2 ряда по 5
        RibbonData.topVowels.forEachIndexed { index, letter ->
            val x = vowelStartX + index * (cellWidth + cellGap)
            drawColoringCell(canvas, letter, config, x, currentY, cellWidth, cellHeight, font, isAnswerKey)
        }
        RibbonData.bottomVowels.forEachIndexed { index, letter ->
            val x = vowelStartX + index * (cellWidth + cellGap)
            drawColoringCell(canvas, letter, config, x, currentY + cellHeight + cellGap, cellWidth, cellHeight, font, isAnswerKey)
        }

        // Согласные — 2 ряда по 11
        RibbonData.topConsonants.forEachIndexed { index, letter ->
            val x = consonantStartX + index * (cellWidth + cellGap)
            drawColoringCell(canvas, letter, config, x, currentY, cellWidth, cellHeight, font, isAnswerKey)
        }
        RibbonData.bottomConsonants.forEachIndexed { index, letter ->
            val x = consonantStartX + index * (cellWidth + cellGap)
            drawColoringCell(canvas, letter, config, x, currentY + cellHeight + cellGap, cellWidth, cellHeight, font, isAnswerKey)
        }

        // Знаки — 2 клетки вертикально (каждая высотой cellHeight)
        RibbonData.signs.forEachIndexed { index, letter ->
            val y = currentY + index * (cellHeight + cellGap)
            drawColoringCell(canvas, letter, config, signStartX, y, cellWidth, cellHeight, font, isAnswerKey)
        }

        // === НИЖНЯЯ ЛЕГЕНДА-СПИСОК ===
        drawColoringLegend(canvas, margin, currentY + 2 * (cellHeight + cellGap) + 25f)
    }

    // ============ Легенда-список для COLORING (как на сайте) ============
    private fun drawColoringLegend(canvas: Canvas, x: Float, y: Float) {
        val textPaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; color = Color.DKGRAY
            typeface = Typeface.DEFAULT
        }
        val iconPaint = Paint().apply { style = Paint.Style.FILL }
        val iconSize = 14f
        val iconGap = 8f
        val lineH = 22f

        var currentY = y

        // Красный квадрат — гласные
        iconPaint.color = Color.parseColor("#EF5350")
        canvas.drawRect(x, currentY - 10f, x + iconSize, currentY + 4f, iconPaint)
        canvas.drawText("гласные — А О У Ы Э — после твёрдых, Я Ё Ю И Е — после мягких", x + iconSize + iconGap, currentY, textPaint)
        currentY += lineH

        // Синий квадрат — всегда твёрдые
        iconPaint.color = Color.parseColor("#42A5F5")
        canvas.drawRect(x, currentY - 10f, x + iconSize, currentY + 4f, iconPaint)
        canvas.drawText("всегда твёрдые — Ж Ш Ц", x + iconSize + iconGap, currentY, textPaint)
        currentY += lineH

        // Зелёный квадрат — всегда мягкие
        iconPaint.color = Color.parseColor("#66BB6A")
        canvas.drawRect(x, currentY - 10f, x + iconSize, currentY + 4f, iconPaint)
        canvas.drawText("всегда мягкие — Й Ч Щ", x + iconSize + iconGap, currentY, textPaint)
        currentY += lineH

        // Сине-зелёный квадрат (2 треугольника) — твёрдые или мягкие
        val p1 = android.graphics.Path().apply { moveTo(x, currentY - 10f); lineTo(x + iconSize, currentY - 10f); lineTo(x, currentY + 4f); close() }
        canvas.drawPath(p1, Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
        val p2 = android.graphics.Path().apply { moveTo(x + iconSize, currentY - 10f); lineTo(x + iconSize, currentY + 4f); lineTo(x, currentY + 4f); close() }
        canvas.drawPath(p2, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
        canvas.drawText("твёрдые или мягкие — остальные согласные", x + iconSize + iconGap, currentY, textPaint)
        currentY += lineH

        // Белый квадрат с контуром — Ь и Ъ
        iconPaint.color = Color.WHITE
        canvas.drawRect(x, currentY - 10f, x + iconSize, currentY + 4f, iconPaint)
        val borderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRect(x, currentY - 10f, x + iconSize, currentY + 4f, borderPaint)
        canvas.drawText("звука не обозначают — Ь и Ъ", x + iconSize + iconGap, currentY, textPaint)
        currentY += lineH

        // Звонкие / глухие — две строки с иконками
        val emojiPaint = Paint().apply {
            isAntiAlias = true; textSize = 13f; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🔔", x + iconSize / 2f, currentY + 4f, emojiPaint)
        canvas.drawText("верхний ряд согласных — звонкие", x + iconSize + iconGap, currentY, textPaint)
        currentY += lineH

        canvas.drawText("🎧", x + iconSize / 2f, currentY + 4f, emojiPaint)
        canvas.drawText("нижний ряд согласных — глухие", x + iconSize + iconGap, currentY, textPaint)
    }

    // Отрисовка ряда клеток (row указывает сколько строк: 1 — горизонтально, 2 — для гласных 5+5)
    private fun drawColoringRow(
        canvas: Canvas, letters: List<RibbonLetter>, config: RibbonConfig,
        startX: Float, startY: Float, cellWidth: Float, cellHeight: Float, gap: Float,
        font: Typeface, isAnswerKey: Boolean, rows: Int
    ) {
        if (rows == 1) {
            // Все в один ряд
            letters.forEachIndexed { index, letter ->
                val x = startX + index * (cellWidth + gap)
                drawColoringCell(canvas, letter, config, x, startY, cellWidth, cellHeight, font, isAnswerKey)
            }
        } else {
            // 2 ряда: первая половина сверху, вторая снизу
            val half = (letters.size + 1) / 2
            val topRow = letters.subList(0, half)
            val botRow = letters.subList(half, letters.size)
            topRow.forEachIndexed { index, letter ->
                val x = startX + index * (cellWidth + gap)
                drawColoringCell(canvas, letter, config, x, startY, cellWidth, cellHeight, font, isAnswerKey)
            }
            botRow.forEachIndexed { index, letter ->
                val x = startX + index * (cellWidth + gap)
                drawColoringCell(canvas, letter, config, x, startY + cellHeight + gap, cellWidth, cellHeight, font, isAnswerKey)
            }
        }
    }

    // Контурная клетка (или с цветом — если ответы)
    private fun drawColoringCell(
        canvas: Canvas, letter: RibbonLetter, config: RibbonConfig,
        x: Float, y: Float, w: Float, h: Float, font: Typeface, isAnswerKey: Boolean
    ) {
        // Цвет клетки по типу буквы
        val fillColor = when (letter.color) {
            RibbonColor.RED -> Color.parseColor("#EF5350")
            RibbonColor.BLUE -> Color.parseColor("#42A5F5")
            RibbonColor.GREEN -> Color.parseColor("#66BB6A")
            RibbonColor.WHITE -> Color.WHITE
            RibbonColor.BOTH -> Color.WHITE
        }
        if (isAnswerKey) {
            // Ответы — закрашиваем клетку соответствующим цветом
            val fillPaint = Paint().apply { color = fillColor; style = Paint.Style.FILL }
            canvas.drawRect(x, y, x + w, y + h, fillPaint)
            // Для BOTH — два треугольника
            if (letter.color == RibbonColor.BOTH) {
                val p1 = android.graphics.Path().apply { moveTo(x, y); lineTo(x + w, y); lineTo(x, y + h); close() }
                canvas.drawPath(p1, Paint().apply { color = Color.parseColor("#42A5F5"); style = Paint.Style.FILL })
                val p2 = android.graphics.Path().apply { moveTo(x + w, y); lineTo(x + w, y + h); lineTo(x, y + h); close() }
                canvas.drawPath(p2, Paint().apply { color = Color.parseColor("#66BB6A"); style = Paint.Style.FILL })
            }
        }
        // Контур клетки (серый прямоугольник)
        val borderPaint = Paint().apply {
            color = Color.DKGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f
        }
        canvas.drawRect(x, y, x + w, y + h, borderPaint)

        // Буква внутри
        val text = when (config.case) {
            RibbonCase.BOTH -> letter.char
            RibbonCase.UPPERCASE -> letter.char.substring(0, 1)
            RibbonCase.LOWERCASE -> letter.char.substring(letter.char.length - 1)
        }
        val textPaint = Paint().apply {
            isAntiAlias = true; textSize = h * 0.5f; typeface = font; textAlign = Paint.Align.CENTER
            color = if (isAnswerKey) Color.WHITE else Color.DKGRAY
        }
        val baselineY = y + h / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(text, x + w / 2f, baselineY, textPaint)
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

    // ============ Подсказка «Эти буквы потерялись» — буквы в отдельных квадратиках ============
    private fun drawLostLettersBox(
        canvas: Canvas, left: Float, top: Float, right: Float,
        letters: Set<String>, font: Typeface
    ) {
        val padding = 14f
        val boxWidth = right - left
        val boxHeight = 100f
        val bottom = top + boxHeight

        // Рамка с заливкой (бледно-жёлтая)
        val bgPaint = Paint().apply { color = Color.parseColor("#FFFBF0"); style = Paint.Style.FILL }
        canvas.drawRoundRect(android.graphics.RectF(left, top, right, bottom), 8f, 8f, bgPaint)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#FFB74D"); style = Paint.Style.STROKE; strokeWidth = 1.5f
        }
        canvas.drawRoundRect(android.graphics.RectF(left, top, right, bottom), 8f, 8f, borderPaint)

        // Заголовок
        val titlePaint = Paint().apply {
            isAntiAlias = true; textSize = 14f; color = Color.parseColor("#E65100")
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Эти буквы потерялись:", left + padding, top + 22f, titlePaint)

        // Квадратики с буквами (как на сайте)
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
                // Квадрат с пунктиром
                canvas.drawRoundRect(
                    android.graphics.RectF(x, yRow, x + letterSize, yRow + letterSize),
                    4f, 4f, dashedPaint
                )
                // Буква
                val baselineY = yRow + letterSize / 2f - ((letterPaint.descent() + letterPaint.ascent()) / 2f)
                canvas.drawText(lettersList[index], x + letterSize / 2f, baselineY, letterPaint)
                index++
            }
            row++
        }
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
