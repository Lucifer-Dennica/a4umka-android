package com.example.sovenok.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.example.sovenok.R
import com.example.sovenok.data.*

object BookletPdfDrawer {
    const val PAGE_WIDTH = 842f
    const val PAGE_HEIGHT = 595f
    private const val INSTRUCTION_HEIGHT = 30f
    private val bitmapCache = mutableMapOf<String, Bitmap?>()
    private var cachedCaveat: Typeface? = null

    fun clearCache() { bitmapCache.clear() }

    private fun caveatTypeface(context: Context): Typeface {
        if (cachedCaveat == null) {
            cachedCaveat = try {
                ResourcesCompat.getFont(context, R.font.caveat)
            } catch (e: Exception) { null }
        }
        return cachedCaveat ?: Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
    }

    private fun titleTypeface(context: Context, font: BookletFont): Typeface =
        if (font == BookletFont.CAVEAT) caveatTypeface(context) else Typeface.DEFAULT_BOLD

    private fun ts(font: BookletFont, baseSize: Float): Float =
        if (font == BookletFont.CAVEAT) baseSize * 1.15f else baseSize

    fun generatePreview(config: BookletConfig, context: Context): List<Bitmap> {
        val scale = 1f
        val pw = (PAGE_WIDTH * scale).toInt()
        val ph = (PAGE_HEIGHT * scale).toInt()
        val bmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.scale(scale, scale)
        drawPage(canvas, config, context)
        return listOf(bmp)
    }

    fun drawDocument(document: PdfDocument, config: BookletConfig, context: Context) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        drawPage(page.canvas, config, context)
        document.finishPage(page)
    }

    private fun drawPage(canvas: Canvas, config: BookletConfig, context: Context) {
        canvas.drawColor(Color.WHITE)
        val gridHeight = PAGE_HEIGHT - INSTRUCTION_HEIGHT
        val cellW = PAGE_WIDTH / 4f
        val cellH = gridHeight / 2f
        val pages = BookletData.getPages(config)
        val isBW = config.isBlackAndWhite
        val mainColor = if (isBW) Color.BLACK else Color.parseColor("#33691E")
        val lineColor = if (isBW) Color.LTGRAY else Color.parseColor("#C8E6C9")

        if (config.showFoldLines) {
            val foldPaint = Paint().apply {
                color = Color.GRAY; strokeWidth = 1f; style = Paint.Style.STROKE
                pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
            }
            for (i in 1..3) canvas.drawLine(cellW * i, 0f, cellW * i, gridHeight, foldPaint)
            canvas.drawLine(0f, cellH, cellW, cellH, foldPaint)
            canvas.drawLine(cellW * 3, cellH, PAGE_WIDTH, cellH, foldPaint)
            val cutPaint = Paint().apply { color = if (isBW) Color.BLACK else Color.parseColor("#D84315"); strokeWidth = 2f }
            canvas.drawLine(cellW, cellH, cellW * 3, cellH, cutPaint)
        }

        drawCell(canvas, pages[7], config, 0, 0, cellW, cellH, mainColor, lineColor, false, context, 8)
        drawCell(canvas, pages[0], config, 1, 0, cellW, cellH, mainColor, lineColor, false, context, 1)
        drawCell(canvas, pages[3], config, 2, 0, cellW, cellH, mainColor, lineColor, false, context, 4)
        drawCell(canvas, pages[4], config, 3, 0, cellW, cellH, mainColor, lineColor, false, context, 5)
        drawCell(canvas, pages[6], config, 0, 1, cellW, cellH, mainColor, lineColor, true, context, 7)
        drawCell(canvas, pages[1], config, 1, 1, cellW, cellH, mainColor, lineColor, true, context, 2)
        drawCell(canvas, pages[2], config, 2, 1, cellW, cellH, mainColor, lineColor, true, context, 3)
        drawCell(canvas, pages[5], config, 3, 1, cellW, cellH, mainColor, lineColor, true, context, 6)

        val cutLineY = gridHeight + 5f
        val cutLinePaint = Paint().apply { color = Color.GRAY; strokeWidth = 1f; style = Paint.Style.STROKE; pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f) }
        canvas.drawLine(0f, cutLineY, PAGE_WIDTH, cutLineY, cutLinePaint)
        val instructionPaint = Paint().apply { isAntiAlias = true; textSize = 8f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        canvas.drawText("✂ Отрежьте полоску. Сложите пополам 3 раза, надрежьте по жирной линии, разверните, сложите вдоль и сдвиньте края — книжка соберётся сама.", 10f, cutLineY + 12f, instructionPaint)
    }

    private fun drawCell(
        canvas: Canvas, page: PageContent, config: BookletConfig,
        col: Int, row: Int, cellW: Float, cellH: Float,
        mainColor: Int, lineColor: Int, isRotated: Boolean,
        context: Context, pageNum: Int
    ) {
        canvas.save()
        val startX = col * cellW
        val startY = row * cellH
        if (isRotated) canvas.rotate(180f, startX + cellW / 2f, startY + cellH / 2f)
        val margin = 15f
        val left = startX + margin
        val top = startY + margin
        val right = startX + cellW - margin
        val bottom = startY + cellH - margin
        val width = right - left
        val height = bottom - top
        val centerX = left + width / 2f
        val isBW = config.isBlackAndWhite
        val tf = titleTypeface(context, config.titleFont)
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = mainColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        val linePaint = Paint().apply { color = lineColor; strokeWidth = 1f }

        if (page.isCover) {
            val coverTitlePaint = Paint().apply { isAntiAlias = true; textSize = ts(config.titleFont, 18f); color = mainColor; typeface = tf; textAlign = Paint.Align.CENTER }
            canvas.drawText("МОЯ ПЕРВАЯ КНИГА", centerX, top + 20f, Paint().apply { isAntiAlias = true; textSize = 8f; color = Color.GRAY; textAlign = Paint.Align.CENTER })
            val titleStr = if (config.title.isNotBlank()) config.title else "Моя книжка"
            val words = titleStr.split(" ")
            var line = ""
            var curY = top + 50f
            for (w in words) {
                val test = if (line.isEmpty()) w else "$line $w"
                if (coverTitlePaint.measureText(test) > width - 20) {
                    canvas.drawText(line, centerX, curY, coverTitlePaint)
                    curY += 20f
                    line = w
                } else { line = test }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, centerX, curY, coverTitlePaint)
                curY += 20f
            }
            val charFile = when (config.character) {
                BookletCharacter.BOY -> "1"
                BookletCharacter.GIRL -> "2"
                else -> null
            }
            if (charFile == null) {
                val boxRect = RectF(left + 10f, curY + 10f, right - 10f, bottom - 50f)
                val boxPaint = Paint().apply { color = if (isBW) Color.LTGRAY else lineColor; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f) }
                canvas.drawRect(boxRect, boxPaint)
            }
            if (charFile != null) {
                val bmp = loadImageFromAssets(context, charFile)
                if (bmp != null) {
                    val imgSize = width * 0.6f
                    val imgLeft = centerX - imgSize / 2f
                    val imgTop = bottom - 50f - imgSize
                    canvas.drawBitmap(bmp, null, RectF(imgLeft, imgTop, imgLeft + imgSize, bottom - 50f), null)
                }
            }
            val authorStr = if (config.author.isNotBlank()) config.author else "Автор:"
            canvas.drawText(authorStr, centerX, bottom - 20f, bodyPaint)
            canvas.drawLine(left + 20f, bottom - 10f, right - 20f, bottom - 10f, linePaint)
        } else if (page.isEnd) {
            if (page.riddleAnswers != null) {
                val tableTitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = mainColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                canvas.drawText("Ответы на загадки:", centerX, startY + 25f, tableTitlePaint)
                val tableTop = startY + 35f
                val tableBottom = startY + cellH / 2f - 30f
                val rowHeight = (tableBottom - tableTop) / page.riddleAnswers.size
                val answerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
                for (i in page.riddleAnswers.indices) {
                    val y = tableTop + i * rowHeight + rowHeight / 2f + 3f
                    canvas.drawText("${i + 1}. ${page.riddleAnswers[i]}", centerX, y, answerPaint)
                }
                canvas.drawText("Конец", centerX, startY + cellH / 2f + 20f, Paint().apply { isAntiAlias = true; textSize = ts(config.titleFont, 20f); color = mainColor; typeface = tf; textAlign = Paint.Align.CENTER })
            } else {
                canvas.drawText("Конец", centerX, startY + cellH / 2f, Paint().apply { isAntiAlias = true; textSize = ts(config.titleFont, 20f); color = mainColor; typeface = tf; textAlign = Paint.Align.CENTER })
            }
        } else {
            var curY = top + 20f
            if (page.prompt != null) {
                val promptPaint = if (config.type == BookletType.RIDDLES) Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER } else titlePaint
                val words = page.prompt.split(" ")
                var line = ""
                for (w in words) {
                    val test = if (line.isEmpty()) w else "$line $w"
                    if (promptPaint.measureText(test) > width - 10) {
                        canvas.drawText(line, centerX, curY, promptPaint)
                        curY += 15f
                        line = w
                    } else { line = test }
                }
                if (line.isNotEmpty()) {
                    canvas.drawText(line, centerX, curY, promptPaint)
                    curY += 15f
                }
                curY += 10f
            } else if (page.title != null) {
                canvas.drawText(page.title, centerX, curY, titlePaint)
                curY += 20f
            }

            if (page.showDrawingBox) {
                val boxHeight = height * 0.4f
                val boxRect = RectF(left, curY, right, curY + boxHeight)
                val boxPaint = Paint().apply { color = if (isBW) Color.LTGRAY else lineColor; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f) }
                canvas.drawRect(boxRect, boxPaint)
                curY += boxHeight + 15f
            }

            if (page.showLines) {
                // ★ ГЛАВНАЯ ПРАВКА: нижняя граница разлиновки — выше номера страницы
                val numberReserve = 16f
                val linesBottom = bottom - numberReserve

                if (page.fullPageLines) {
                    val step = when (config.ruling) {
                        BookletRuling.LINE_8MM -> 8f * 2.83f
                        BookletRuling.WIDE_10MM -> 10f * 2.83f
                        BookletRuling.GRID_5MM -> 5f * 2.83f
                        BookletRuling.NONE -> 0f
                    }
                    if (step > 0f) {
                        if (config.ruling == BookletRuling.GRID_5MM) {
                            var y = curY
                            while (y + step <= linesBottom) {
                                canvas.drawLine(left, y, right, y, linePaint)
                                var x = left
                                while (x <= right) {
                                    canvas.drawLine(x, y, x, y + step, linePaint)
                                    x += step
                                }
                                y += step
                            }
                        } else {
                            var y = curY
                            while (y + step <= linesBottom) {
                                y += step
                                canvas.drawLine(left, y, right, y, linePaint)
                            }
                        }
                    }
                } else {
                    when (config.ruling) {
                        BookletRuling.LINE_8MM -> {
                            val linesCount = if (page.prompt != null || page.title != null) 3 else 4
                            val step = 8f * 2.83f
                            for (i in 0 until linesCount) {
                                curY += step
                                if (curY > linesBottom) break   // ★ не лезем под номер страницы
                                canvas.drawLine(left, curY, right, curY, linePaint)
                            }
                        }
                        BookletRuling.WIDE_10MM -> {
                            val linesCount = if (page.prompt != null || page.title != null) 2 else 3
                            val step = 10f * 2.83f
                            for (i in 0 until linesCount) {
                                curY += step
                                if (curY > linesBottom) break   // ★ не лезем под номер страницы
                                canvas.drawLine(left, curY, right, curY, linePaint)
                            }
                        }
                        BookletRuling.GRID_5MM -> {
                            // ★ ПРАВКА: клетка начинается сразу после поля рисунка (curY),
                            // а не привязана к низу листа — больше не налезает на рамку рисунка,
                            // и обрывается выше номера страницы
                            val step = 5f * 2.83f
                            var y = curY
                            while (y <= linesBottom) {
                                canvas.drawLine(left, y, right, y, linePaint)
                                var x = left
                                while (x <= right) {
                                    canvas.drawLine(x, y, x, (y + step).coerceAtMost(linesBottom), linePaint)
                                    x += step
                                }
                                y += step
                            }
                        }
                        BookletRuling.NONE -> {}
                    }
                }
            }
        }

        if (!page.isCover && !page.isEnd) {
            val pageNumPaint = Paint().apply {
                isAntiAlias = true; textSize = 10f; color = Color.GRAY;
                typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER
            }
            canvas.drawText("$pageNum", centerX, bottom - 3f, pageNumPaint)
        }

        canvas.restore()
    }

    private fun loadImageFromAssets(context: Context, name: String): Bitmap? {
        if (bitmapCache.containsKey(name)) return bitmapCache[name]
        return try {
            val inputStream = context.assets.open("booklet/$name.png")
            val bmp = BitmapFactory.decodeStream(inputStream)
            bitmapCache[name] = bmp
            bmp
        } catch (e: Exception) {
            bitmapCache[name] = null
            null
        }
    }
}
