package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.*

object BookmarksPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: BookmarksConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        
        var bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bmp)
        drawPage(canvas, config, context, isBackside = false)
        pages.add(bmp)

        if (config.printBacksides) {
            bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            canvas = Canvas(bmp)
            drawPage(canvas, config, context, isBackside = true)
            pages.add(bmp)
        }

        return pages
    }

    fun drawDocument(document: PdfDocument, config: BookmarksConfig, context: Context) {
        var pageNum = 1
        
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        var page = document.startPage(pageInfo)
        drawPage(page.canvas, config, context, isBackside = false)
        document.finishPage(page)

        if (config.printBacksides) {
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            drawPage(page.canvas, config, context, isBackside = true)
            document.finishPage(page)
        }
    }

    private fun drawPage(canvas: Canvas, config: BookmarksConfig, context: Context, isBackside: Boolean) {
        canvas.drawColor(Color.WHITE)
        
        val margin = 20f
        val spacing = 10f
        val bookmarkWidth = (PAGE_WIDTH - margin * 2 - spacing * 3) / 4f
        val bookmarkHeight = PAGE_HEIGHT - margin * 2

        for (i in 0 until 4) {
            val index = if (isBackside) 3 - i else i
            val settings = config.bookmarks[index]
            
            val left = margin + i * (bookmarkWidth + spacing)
            val top = margin
            val right = left + bookmarkWidth
            val bottom = top + bookmarkHeight

            drawBookmark(canvas, settings, config.isBlackAndWhite, left, top, right, bottom, isBackside, context)
        }
    }

    private fun drawBookmark(
        canvas: Canvas, settings: BookmarkSettings, isBW: Boolean,
        left: Float, top: Float, right: Float, bottom: Float, isBackside: Boolean,
        context: Context
    ) {
        val width = right - left
        val height = bottom - top
        val centerX = left + width / 2f

        val mainColor = if (isBW) Color.BLACK else when(settings.pattern) {
            BookmarkPattern.LEAVES -> Color.parseColor("#4CAF50")
            BookmarkPattern.STARS -> Color.parseColor("#FFC107")
            BookmarkPattern.WAVES -> Color.parseColor("#03A9F4")
            BookmarkPattern.HEARTS -> Color.parseColor("#F44336")
        }

        val textColor = if (isBW) Color.BLACK else Color.DKGRAY

        val bgPaint = Paint().apply { color = if (isBW) Color.WHITE else Color.parseColor("#FAFAFA"); style = Paint.Style.FILL }
        canvas.drawRect(left, top, right, bottom, bgPaint)

        val borderPaint = Paint().apply { color = mainColor; style = Paint.Style.STROKE; strokeWidth = 2f }
        canvas.drawRect(left, top, right, bottom, borderPaint)

        // Узор по краям (оставляем текстовый, чтобы не перегружать память)
        val patternPaint = Paint().apply { color = mainColor; textSize = 16f; textAlign = Paint.Align.CENTER }
        val patternStr = when(settings.pattern) {
            BookmarkPattern.LEAVES -> "🍃"
            BookmarkPattern.STARS -> "★"
            BookmarkPattern.WAVES -> "~"
            BookmarkPattern.HEARTS -> "♥"
        }
        for (y in (top.toInt() + 30) until (bottom.toInt() - 30) step 40) {
            canvas.drawText(patternStr, left + 15f, y.toFloat(), patternPaint)
            canvas.drawText(patternStr, right - 15f, y.toFloat(), patternPaint)
        }

        if (isBackside) {
            val textPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = textColor; textAlign = Paint.Align.CENTER }
            canvas.drawText("Здесь живут истории", centerX, top + 60f, textPaint)
            
            val linePaint = Paint().apply { color = mainColor; strokeWidth = 1f }
            for (i in 1..6) {
                val y = top + 100f + i * 40f
                canvas.drawLine(left + 20f, y, right - 20f, y, linePaint)
            }
            
            canvas.drawText("Книга ждёт — продолжим?", centerX, bottom - 40f, textPaint)
            return
        }

        // ЛИЦЕВАЯ СТОРОНА
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.BLACK; textAlign = Paint.Align.CENTER }
        val linePaint = Paint().apply { color = mainColor; strokeWidth = 1f }

        // --- ЗАГРУЗКА КАРТИНКИ ГЕРОЯ ---
        val heroName = when(settings.character) {
            BookmarkCharacter.GIRL_BOW -> "girl_bow"
            BookmarkCharacter.BOY_SCARF -> "boy_scarf"
            BookmarkCharacter.GIRL_CAT -> "girl_cat"
            BookmarkCharacter.BOY_PLANE -> "boy_plane"
        }
        
        val heroBitmap = loadImageFromAssets(context, heroName)
        
        if (heroBitmap != null) {
            // Рисуем картинку (масштабируем под ширину закладки)
            val imgSize = width * 0.7f
            val leftImg = centerX - imgSize / 2f
            val topImg = top + 30f
            val rightImg = centerX + imgSize / 2f
            val bottomImg = topImg + imgSize
            canvas.drawBitmap(heroBitmap, null, RectF(leftImg, topImg, rightImg, bottomImg), null)
        } else {
            // Если картинки нет, рисуем эмодзи
            val heroEmoji = when(settings.character) {
                BookmarkCharacter.GIRL_BOW -> "👧"
                BookmarkCharacter.BOY_SCARF -> "👦"
                BookmarkCharacter.GIRL_CAT -> "🐱"
                BookmarkCharacter.BOY_PLANE -> "✈️"
            }
            val heroPaint = Paint().apply { textSize = 60f; textAlign = Paint.Align.CENTER }
            canvas.drawText(heroEmoji, centerX, top + 100f, heroPaint)
        }

        var currentY = top + 180f // Сдвигаем текст ниже картинки

        when (settings.type) {
            BookmarkType.COLORING -> {
                canvas.drawText("Моя закладка", centerX, currentY, titlePaint)
                currentY += 20f
                canvas.drawText("Нарисуй свой сюжет", centerX, currentY, bodyPaint)
                currentY += 20f
                
                val boxPaint = Paint().apply { color = mainColor; style = Paint.Style.STROKE; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f) }
                canvas.drawRect(left + 15f, currentY, right - 15f, bottom - 100f, boxPaint)
                
                canvas.drawText("Имя:", centerX, bottom - 60f, titlePaint)
                canvas.drawLine(left + 20f, bottom - 50f, right - 20f, bottom - 50f, linePaint)
            }
            BookmarkType.NAME -> {
                val nameStr = if (settings.name.isNotBlank()) settings.name else "Имя"
                val namePaint = Paint().apply { textSize = 22f; color = mainColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                canvas.drawText(nameStr, centerX, top + 50f, namePaint) // Имя пишем сверху
                
                canvas.drawText("читает книги", centerX, bottom - 180f, titlePaint)
                
                canvas.drawText("Остановился", centerX, bottom - 120f, bodyPaint)
                canvas.drawText("на странице", centerX, bottom - 105f, bodyPaint)
                canvas.drawRect(centerX - 30f, bottom - 90f, centerX + 30f, bottom - 40f, linePaint)
            }
            BookmarkType.CHEAT_SHEET -> {
                if (settings.cheatSheetType == CheatSheetType.MULTIPLICATION) {
                    canvas.drawText("× ${settings.multiplier}", centerX, currentY, titlePaint)
                    currentY += 20f
                    for (i in 1..9) {
                        canvas.drawText("${settings.multiplier} × $i = ${settings.multiplier * i}", centerX, currentY, bodyPaint)
                        currentY += 18f
                        if (currentY > bottom - 100f) break
                    }
                } else if (settings.cheatSheetType == CheatSheetType.ALPHABET) {
                    canvas.drawText("Алфавит", centerX, currentY, titlePaint)
                    currentY += 20f
                    val alphabet = "А Б В Г Д\nЕ Ё Ж З И\nЙ К Л М Н\nО П Р С Т\nУ Ф Х Ц Ч\nШ Щ Ъ Ы Ь\nЭ Ю Я"
                    alphabet.lines().forEach { line ->
                        canvas.drawText(line, centerX, currentY, bodyPaint)
                        currentY += 20f
                    }
                } else {
                    canvas.drawText("Состав числа 10", centerX, currentY, titlePaint)
                    currentY += 25f
                    val pairs = listOf("1 и 9", "2 и 8", "3 и 7", "4 и 6", "5 и 5")
                    pairs.forEach { pair ->
                        canvas.drawText(pair, centerX, currentY, bodyPaint)
                        currentY += 20f
                    }
                }
            }
            BookmarkType.COUNTER -> {
                val counterTitle = when(settings.counterType) {
                    CounterType.BOOKS -> "Книги"
                    CounterType.PAGES -> "Сотни страниц"
                    CounterType.DAYS -> "Дни чтения"
                }
                canvas.drawText(counterTitle, centerX, currentY, titlePaint)
                currentY += 25f
                
                val gridPaint = Paint().apply { color = mainColor; style = Paint.Style.STROKE; strokeWidth = 1f }
                val cols = 3
                val rows = 6
                val cellSize = (width - 40f) / cols
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val cx = left + 20f + c * cellSize
                        val cy = currentY + r * cellSize
                        canvas.drawCircle(cx + cellSize / 2f, cy + cellSize / 2f, cellSize / 2f - 2f, gridPaint)
                    }
                }
                currentY += rows * cellSize + 10f
                if (currentY < bottom - 60f) {
                    canvas.drawText("Прочитано", centerX, currentY, titlePaint)
                }
            }
        }
    }

    private fun loadImageFromAssets(context: Context, name: String): Bitmap? {
        return try {
            val inputStream = context.assets.open("bookmarks/$name.png")
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
}
