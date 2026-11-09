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

    // КЭШ КАРТИНОК - чтобы не загружать из assets каждый раз
    private val bitmapCache = mutableMapOf<String, Bitmap?>()

    fun clearCache() {
        bitmapCache.clear()
    }

    fun generatePreview(config: BookmarksConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        // Генерируем предпросмотр в 50% разрешении для скорости
        val scale = 0.5f
        val pw = (PAGE_WIDTH * scale).toInt()
        val ph = (PAGE_HEIGHT * scale).toInt()

        var bmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bmp)
        canvas.scale(scale, scale)
        drawPage(canvas, config, context, isBackside = false)
        pages.add(bmp)

        if (config.printBacksides) {
            bmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bmp)
            canvas.scale(scale, scale)
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
        val centerX = left + width / 2f
        val rect = RectF(left, top, right, bottom)

        val patternName = when(settings.pattern) {
            BookmarkPattern.LEAVES -> "leaves"
            BookmarkPattern.STARS -> "stars"
            BookmarkPattern.WAVES -> "waves"
            BookmarkPattern.HEARTS -> "hearts"
        }
        val suffix = if (isBW) "_bw" else ""
        val backPrefix = if (isBackside) "back_" else ""
        
        val mainColor = if (isBW) Color.BLACK else when(settings.pattern) {
            BookmarkPattern.LEAVES -> Color.parseColor("#2E7D32")
            BookmarkPattern.STARS -> Color.parseColor("#F57F17")
            BookmarkPattern.WAVES -> Color.parseColor("#0277BD")
            BookmarkPattern.HEARTS -> Color.parseColor("#C62828")
        }

        // 1. ФОН
        val bgBitmap = loadImageFromAssets(context, "${backPrefix}${patternName}${suffix}")
        if (bgBitmap != null) {
            canvas.drawBitmap(bgBitmap, null, rect, null)
        } else {
            // Если нет фона, рисуем белый с рамкой
            val bgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
            canvas.drawRect(rect, bgPaint)
        }

        // Рамка поверх фона (всегда)
        val borderPaint = Paint().apply { color = mainColor; style = Paint.Style.STROKE; strokeWidth = 2f }
        canvas.drawRect(rect, borderPaint)

        // 2. ОБОРОТ
        if (isBackside) {
            drawBacksideContent(canvas, settings, mainColor, left, top, right, bottom)
            return
        }

        // 3. ГЕРОЙ (верхняя часть закладки)
        val heroName = when(settings.character) {
            BookmarkCharacter.GIRL_BOW -> "girl_bow"
            BookmarkCharacter.BOY_SCARF -> "boy_scarf"
            BookmarkCharacter.GIRL_CAT -> "girl_cat"
            BookmarkCharacter.BOY_PLANE -> "boy_plane"
        }
        
        val heroBitmap = loadImageFromAssets(context, "${heroName}${suffix}")
        val heroZoneHeight = width * 0.9f
        val heroTop = top + 15f
        
        if (heroBitmap != null) {
            val imgSize = width * 0.7f
            val leftImg = centerX - imgSize / 2f
            val rightImg = centerX + imgSize / 2f
            canvas.drawBitmap(heroBitmap, null, RectF(leftImg, heroTop, rightImg, heroTop + imgSize), null)
        } else {
            val heroEmoji = when(settings.character) {
                BookmarkCharacter.GIRL_BOW -> "👧"
                BookmarkCharacter.BOY_SCARF -> "👦"
                BookmarkCharacter.GIRL_CAT -> "🐱"
                BookmarkCharacter.BOY_PLANE -> "✈️"
            }
            val heroPaint = Paint().apply { textSize = 50f; textAlign = Paint.Align.CENTER }
            canvas.drawText(heroEmoji, centerX, heroTop + 60f, heroPaint)
        }

        // 4. КОНТЕНТ (нижняя часть, на белой плашке)
        val contentTop = top + heroZoneHeight + 10f
        val contentRect = RectF(left + 8f, contentTop, right - 8f, bottom - 8f)
        
        // Полупрозрачная белая плашка для читаемости
        val platePaint = Paint().apply { 
            color = Color.argb(if (isBW) 240 else 220, 255, 255, 255)
            style = Paint.Style.FILL 
        }
        canvas.drawRoundRect(contentRect, 6f, 6f, platePaint)
        
        val plateBorder = Paint().apply { color = mainColor; style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRoundRect(contentRect, 6f, 6f, plateBorder)

        drawFrontContent(canvas, settings, mainColor, contentRect, centerX)
    }

    private fun drawFrontContent(canvas: Canvas, settings: BookmarkSettings, mainColor: Int, rect: RectF, centerX: Float) {
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 13f; color = Color.DKGRAY; 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        val bodyPaint = Paint().apply { 
            isAntiAlias = true; textSize = 11f; color = Color.BLACK; textAlign = Paint.Align.CENTER 
        }
        val linePaint = Paint().apply { color = mainColor; strokeWidth = 1f }

        var y = rect.top + 25f
        val left = rect.left + 10f
        val right = rect.right - 10f

        when (settings.type) {
            BookmarkType.COLORING -> {
                canvas.drawText("Моя закладка", centerX, y, titlePaint)
                y += 18f
                canvas.drawText("Нарисуй сюжет", centerX, y, bodyPaint)
                y += 15f
                
                val boxPaint = Paint().apply { 
                    color = mainColor; style = Paint.Style.STROKE; strokeWidth = 1f; 
                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f, 4f), 0f) 
                }
                val boxRect = RectF(left, y, right, rect.bottom - 60f)
                canvas.drawRect(boxRect, boxPaint)
                
                canvas.drawText("Имя:", centerX, rect.bottom - 35f, titlePaint)
                canvas.drawLine(left, rect.bottom - 20f, right, rect.bottom - 20f, linePaint)
            }
            BookmarkType.NAME -> {
                val namePaint = Paint().apply { 
                    textSize = 20f; color = mainColor; typeface = Typeface.DEFAULT_BOLD; 
                    textAlign = Paint.Align.CENTER; isAntiAlias = true 
                }
                canvas.drawText(if (settings.name.isNotBlank()) settings.name else "Имя", centerX, y + 10f, namePaint)
                y += 40f
                canvas.drawText("читает книги", centerX, y, titlePaint)
                
                canvas.drawText("Остановился", centerX, rect.bottom - 90f, bodyPaint)
                canvas.drawText("на странице", centerX, rect.bottom - 75f, bodyPaint)
                
                val pageBox = RectF(centerX - 30f, rect.bottom - 65f, centerX + 30f, rect.bottom - 20f)
                canvas.drawRect(pageBox, linePaint)
            }
            BookmarkType.CHEAT_SHEET -> {
                if (settings.cheatSheetType == CheatSheetType.MULTIPLICATION) {
                    canvas.drawText("× ${settings.multiplier}", centerX, y, titlePaint)
                    y += 18f
                    for (i in 1..9) {
                        if (y > rect.bottom - 15f) break
                        canvas.drawText("${settings.multiplier}×$i=${settings.multiplier * i}", centerX, y, bodyPaint)
                        y += 15f
                    }
                } else if (settings.cheatSheetType == CheatSheetType.ALPHABET) {
                    canvas.drawText("Алфавит", centerX, y, titlePaint)
                    y += 16f
                    val alphabet = listOf("А Б В Г Д", "Е Ё Ж З И", "Й К Л М Н", "О П Р С Т", "У Ф Х Ц Ч", "Ш Щ Ъ Ы Ь", "Э Ю Я")
                    alphabet.forEach { line ->
                        if (y > rect.bottom - 15f) return@forEach
                        canvas.drawText(line, centerX, y, bodyPaint)
                        y += 15f
                    }
                } else {
                    canvas.drawText("Состав 10", centerX, y, titlePaint)
                    y += 20f
                    listOf("1+9", "2+8", "3+7", "4+6", "5+5").forEach { pair ->
                        if (y > rect.bottom - 15f) return@forEach
                        canvas.drawText(pair, centerX, y, bodyPaint)
                        y += 18f
                    }
                }
            }
            BookmarkType.COUNTER -> {
                val counterTitle = when(settings.counterType) {
                    CounterType.BOOKS -> "Книги"
                    CounterType.PAGES -> "Сотни страниц"
                    CounterType.DAYS -> "Дни чтения"
                }
                canvas.drawText(counterTitle, centerX, y, titlePaint)
                y += 20f
                
                val gridPaint = Paint().apply { color = mainColor; style = Paint.Style.STROKE; strokeWidth = 1f }
                val cols = 3
                val rows = 5
                val cellSize = (right - left) / cols
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val cx = left + c * cellSize
                        val cy = y + r * cellSize
                        if (cy + cellSize > rect.bottom - 10f) break
                        canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, gridPaint)
                    }
                }
            }
        }
    }

    private fun drawBacksideContent(canvas: Canvas, settings: BookmarkSettings, mainColor: Int, left: Float, top: Float, right: Float, bottom: Float) {
        val centerX = (left + right) / 2f
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; 
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        val linePaint = Paint().apply { color = mainColor; strokeWidth = 1f }

        // Заголовок на плашке
        val headerRect = RectF(left + 8f, top + 20f, right - 8f, top + 55f)
        val headerPaint = Paint().apply { color = Color.argb(220, 255, 255, 255); style = Paint.Style.FILL }
        canvas.drawRoundRect(headerRect, 6f, 6f, headerPaint)
        canvas.drawText("Здесь живут истории", centerX, top + 42f, titlePaint)

        // Линии для записей
        val lineStartY = top + 90f
        for (i in 1..8) {
            val y = lineStartY + i * 50f
            if (y > bottom - 80f) break
            canvas.drawLine(left + 15f, y, right - 15f, y, linePaint)
        }

        // Нижняя плашка с вопросом
        val bottomRect = RectF(left + 8f, bottom - 70f, right - 8f, bottom - 30f)
        canvas.drawRoundRect(bottomRect, 6f, 6f, headerPaint)
        canvas.drawText("Продолжим?", centerX, bottom - 45f, titlePaint)
    }

    private fun loadImageFromAssets(context: Context, name: String): Bitmap? {
        // Сначала проверяем кэш
        if (bitmapCache.containsKey(name)) return bitmapCache[name]
        
        return try {
            val inputStream = context.assets.open("bookmarks/$name.png")
            val bmp = BitmapFactory.decodeStream(inputStream)
            // Уменьшаем большие картинки для экономии памяти
            val scaled = if (bmp != null && bmp.width > 800) {
                val ratio = 800f / bmp.width
                Bitmap.createScaledBitmap(bmp, 800, (bmp.height * ratio).toInt(), true)
            } else bmp
            bitmapCache[name] = scaled
            scaled
        } catch (e: Exception) {
            bitmapCache[name] = null
            null
        }
    }
}
