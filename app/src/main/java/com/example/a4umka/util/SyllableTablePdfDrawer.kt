package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.*
import kotlin.random.Random

object SyllableTablePdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: SyllableTableConfig, context: Context): List<Bitmap> {
        val scale = 0.5f
        val pw = (PAGE_WIDTH * scale).toInt()
        val ph = (PAGE_HEIGHT * scale).toInt()

        val bmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.scale(scale, scale)
        canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(canvas, config)
        return listOf(bmp)
    }

    fun drawDocument(document: PdfDocument, config: SyllableTableConfig, context: Context) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawColor(Color.parseColor("#FAFAFA"))
        drawPage(page.canvas, config)
        document.finishPage(page)
    }

    private fun drawPage(canvas: Canvas, config: SyllableTableConfig) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")
        val redColor = Color.parseColor("#D32F2F")
        val blueColor = Color.parseColor("#1976D2")

        if (config.workType == SyllableWorkType.TABLE) {
            // ШАПКА
            val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
            
            canvas.drawText("Дата:", margin, 40f, headerPaint)
            canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)
            
            val lettersStr = "Буквы: " + config.consonants.joinToString(" ")
            val lettersWidth = headerPaint.measureText(lettersStr)
            val rectPaint = Paint().apply { color = Color.parseColor("#C8E6C9"); style = Paint.Style.FILL }
            canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - lettersWidth/2f - 10f, 25f, PAGE_WIDTH/2f + lettersWidth/2f + 10f, 45f), 5f, 5f, rectPaint)
            canvas.drawText(lettersStr, PAGE_WIDTH/2f - lettersWidth/2f, 40f, headerPaint)
            
            canvas.drawText("Время:", PAGE_WIDTH - 120f, 40f, headerPaint)
            canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

            // ЗАГОЛОВОК
            val titlePaint = Paint().apply { 
                isAntiAlias = true; textSize = 28f; color = textColor; 
                typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
            }
            canvas.drawText("Слоговая таблица", PAGE_WIDTH / 2f, 90f, titlePaint)
            
            val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
            canvas.drawText("читай слоги по строкам • прочитал строку — закрась кружочек", PAGE_WIDTH / 2f, 110f, subtitlePaint)

            // СЕТКА
            val vowels = SyllableTableData.getVowels(config.vowelStep)
            val consonants = config.consonants.toList().sorted()
            
            val cols = vowels.size
            val rows = consonants.size
            
            val tableTop = 160f
            val tableLeft = margin + 30f
            val tableWidth = PAGE_WIDTH - margin * 2 - 30f
            val cellW = tableWidth / cols
            val cellH = (PAGE_HEIGHT - tableTop - 150f) / rows.coerceAtLeast(1)

            val gridPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }
            val consPaint = Paint().apply { isAntiAlias = true; textSize = 22f; color = blueColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
            val vowelPaint = Paint().apply { isAntiAlias = true; textSize = 22f; color = redColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
            val circlePaint = Paint().apply { color = Color.GRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }

            // Заголовки столбцов (гласные)
            for (c in 0 until cols) {
                val x = tableLeft + c * cellW + cellW / 2f
                canvas.drawText(vowels[c], x, tableTop - 10f, vowelPaint)
            }

            var randomSyllable = ""
            
            for (r in 0 until rows) {
                val y = tableTop + r * cellH
                val cons = consonants[r]
                
                // Заголовок строки (согласная)
                canvas.drawText(cons, tableLeft - 15f, y + cellH / 2f + 8f, consPaint)
                
                // Кружочек прогресса
                canvas.drawCircle(PAGE_WIDTH - margin + 10f, y + cellH / 2f, 8f, circlePaint)

                for (c in 0 until cols) {
                    val x = tableLeft + c * cellW
                    // Рамка ячейки
                    canvas.drawRect(x, y, x + cellW, y + cellH, gridPaint)
                    
                    val vowel = vowels[c]
                    
                    // Формируем слог
                    var syllable = ""
                    var isValid = true
                    
                    when (config.syllableType) {
                        SyllableType.OPEN -> {
                            if (SyllableTableData.isValidSyllable(cons, vowel)) {
                                syllable = cons + vowel.lowercase()
                            } else isValid = false
                        }
                        SyllableType.REVERSE -> {
                            if (SyllableTableData.isValidSyllable(cons, vowel)) {
                                syllable = vowel + cons.lowercase()
                            } else isValid = false
                        }
                        SyllableType.CLOSED -> {
                            if (SyllableTableData.isValidSyllable(cons, vowel)) {
                                syllable = cons + vowel.lowercase() + config.closedSuffix.lowercase()
                            } else isValid = false
                        }
                        SyllableType.CLUSTER -> {
                            // Для стечения берем первую согласную из списка
                            val cluster = config.clusterList.firstOrNull() ?: "СТ"
                            syllable = cluster.lowercase() + vowel.lowercase()
                        }
                    }

                    if (isValid && syllable.isNotEmpty()) {
                        if (randomSyllable.isEmpty()) randomSyllable = syllable // Запоминаем для игры
                        
                        val textColor = if (config.syllableType == SyllableType.REVERSE) redColor else blueColor
                        val textPaint = Paint(consPaint).apply { this.color = textColor }
                        
                        // Рисуем слог (упрощенно, без разделения на цвета внутри ячейки)
                        canvas.drawText(syllable.uppercase(), x + cellW / 2f, y + cellH / 2f + 8f, textPaint)
                        
                        // Слог дня (рамка)
                        if (config.syllableOfTheDay && r == 1 && c == 2) {
                            val starPaint = Paint().apply { color = Color.parseColor("#FFC107"); style = Paint.Style.STROKE; strokeWidth = 2f }
                            canvas.drawRect(x + 2f, y + 2f, x + cellW - 2f, y + cellH - 2f, starPaint)
                        }
                    }
                }
            }

            // ПОДВАЛ (Игра "Найди слог")
            if (config.findGame) {
                val bottomY = PAGE_HEIGHT - 80f
                val findPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
                canvas.drawText("🔍 Найди и обведи в таблице слоги:", margin, bottomY, findPaint)
                
                // Генерируем 3 случайных слога
                val r1 = (0 until rows).random()
                val r2 = (0 until rows).random()
                val r3 = (0 until rows).random()
                val c1 = (0 until cols).random()
                val c2 = (0 until cols).random()
                val c3 = (0 until cols).random()
                
                val syllables = listOf(
                    consonants[r1] + vowels[c1].lowercase(),
                    consonants[r2] + vowels[c2].lowercase(),
                    consonants[r3] + vowels[c3].lowercase()
                )

                var xOffset = margin + 260f
                val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
                syllables.forEach { syl ->
                    canvas.drawRoundRect(RectF(xOffset, bottomY - 15f, xOffset + 50f, bottomY + 5f), 5f, 5f, boxPaint)
                    val sylPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
                    canvas.drawText(syl.uppercase(), xOffset + 25f, bottomY, sylPaint)
                    xOffset += 60f
                }
            }

        } else {
            // РЕЖИМ КАРТОЧЕК
            val titlePaint = Paint().apply { 
                isAntiAlias = true; textSize = 24f; color = textColor; 
                typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
            }
            canvas.drawText("Слоговые карточки", PAGE_WIDTH / 2f, 50f, titlePaint)
            
            val vowels = SyllableTableData.getVowels(config.vowelStep)
            val consonants = config.consonants.toList().sorted()
            val allSyllables = mutableListOf<String>()
            
            for (cons in consonants) {
                for (vowel in vowels) {
                    if (SyllableTableData.isValidSyllable(cons, vowel)) {
                        allSyllables.add((cons + vowel.lowercase()).uppercase())
                    }
                }
            }
            
            if (config.shuffle) allSyllables.shuffle()

            val margin = 40f
            val cardW = (PAGE_WIDTH - margin * 2 - 20f) / 2f
            val cardH = (PAGE_HEIGHT - 80f - 20f) / 2f
            
            val borderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 2f }
            val textPaint = Paint().apply { isAntiAlias = true; textSize = 48f; color = textColor; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }

            for (i in 0 until 4) {
                val col = i % 2
                val row = i / 2
                val left = margin + col * (cardW + 20f)
                val top = 80f + row * (cardH + 20f)
                
                canvas.drawRect(left, top, left + cardW, top + cardH, borderPaint)
                
                if (i < allSyllables.size) {
                    canvas.drawText(allSyllables[i], left + cardW / 2f, top + cardH / 2f + 15f, textPaint)
                }
            }
        }
    }
}
