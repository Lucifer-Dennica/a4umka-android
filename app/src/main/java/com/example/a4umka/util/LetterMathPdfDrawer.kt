package com.example.a4umka.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.LetterMathConfig
import com.example.a4umka.data.LetterMathData
import com.example.a4umka.data.LetterMathDifficulty
import com.example.a4umka.data.LetterMathHint
import com.example.a4umka.data.MathProblem

object LetterMathPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun generatePreview(config: LetterMathConfig, context: Context): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        
        // Генерируем 8 задач на каждый лист
        val problemsPerPage = 8
        val totalProblems = config.pagesCount * problemsPerPage
        val problems = LetterMathData.generateProblems(config, totalProblems)

        for (p in 0 until config.pagesCount) {
            val bmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.parseColor("#FAFAFA"))
            
            val pageProblems = problems.subList(p * problemsPerPage, minOf((p + 1) * problemsPerPage, problems.size))
            drawPage(canvas, config, pageProblems, p + 1)
            pages.add(bmp)
        }

        // Лист ответов
        val ansBmp = Bitmap.createBitmap(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val ansCanvas = Canvas(ansBmp)
        ansCanvas.drawColor(Color.WHITE)
        drawAnswersPage(ansCanvas, problems)
        pages.add(ansBmp)

        return pages
    }

    fun drawDocument(document: PdfDocument, config: LetterMathConfig, context: Context) {
        val problemsPerPage = 8
        val totalProblems = config.pagesCount * problemsPerPage
        val problems = LetterMathData.generateProblems(config, totalProblems)
        var pageNum = 1

        for (p in 0 until config.pagesCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum++).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(Color.parseColor("#FAFAFA"))
            val pageProblems = problems.subList(p * problemsPerPage, minOf((p + 1) * problemsPerPage, problems.size))
            drawPage(page.canvas, config, pageProblems, p + 1)
            document.finishPage(page)
        }

        val ansPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        val ansPage = document.startPage(ansPageInfo)
        ansPage.canvas.drawColor(Color.WHITE)
        drawAnswersPage(ansPage.canvas, problems)
        document.finishPage(ansPage)
    }

    private fun drawPage(canvas: Canvas, config: LetterMathConfig, problems: List<MathProblem>, pageNum: Int) {
        val margin = 40f
        val textColor = Color.parseColor("#33691E")

        // ШАПКА
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        canvas.drawText("Имя:", margin, 40f, headerPaint)
        canvas.drawLine(margin + 30f, 42f, margin + 100f, 42f, linePaint)
        
        val diffStr = when(config.difficulty) {
            LetterMathDifficulty.EASY -> "лёгкие"
            LetterMathDifficulty.MEDIUM -> "средние"
            LetterMathDifficulty.HARD -> "трудные"
        }
        val rectPaint = Paint().apply { color = Color.parseColor("#FFE082"); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(PAGE_WIDTH/2f - 40f, 25f, PAGE_WIDTH/2f + 40f, 45f), 5f, 5f, rectPaint)
        val centerTextPaint = Paint(headerPaint).apply { textAlign = Paint.Align.CENTER; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("✏️ $diffStr", PAGE_WIDTH/2f, 40f, centerTextPaint)
        
        canvas.drawText("Дата:", PAGE_WIDTH - 120f, 40f, headerPaint)
        canvas.drawLine(PAGE_WIDTH - 85f, 42f, PAGE_WIDTH - margin, 42f, linePaint)

        // ЗАГОЛОВОК
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 32f; color = textColor; 
            typeface = Typeface.create("cursive", Typeface.BOLD); textAlign = Paint.Align.CENTER 
        }
        canvas.drawText("Сложи слово", PAGE_WIDTH / 2f, 90f, titlePaint)

        val subPaint = Paint().apply { isAntiAlias = true; textSize = 11f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("«+» — допиши буквы в конец, «−» — вычеркни их. Должно получиться настоящее слово!", PAGE_WIDTH / 2f, 110f, subPaint)

        // ЗАДАЧИ
        val startY = 140f
        val cardHeight = 80f
        val cardSpacing = 8f
        val cardWidth = PAGE_WIDTH - margin * 2

        val cardBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        
        val formulaPaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        val draftLinePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; pathEffect = android.graphics.DashPathEffect(floatArrayOf(4f, 4f), 0f) }
        val numPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
        val numCirclePaint = Paint().apply { color = Color.parseColor("#4CAF50"); style = Paint.Style.FILL }
        val emojiPaint = Paint().apply { textSize = 24f }

        for (i in problems.indices) {
            val problem = problems[i]
            val top = startY + i * (cardHeight + cardSpacing)
            
            // Карточка
            val cardRect = RectF(margin, top, margin + cardWidth, top + cardHeight)
            canvas.drawRoundRect(cardRect, 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(cardRect, 10f, 10f, cardBorderPaint)

            // Номер
            canvas.drawCircle(margin + 20f, top + 25f, 12f, numCirclePaint)
            canvas.drawText("${i + 1}", margin + 20f, top + 30f, numPaint)

            // Пример
            canvas.drawText(problem.formula + " =", margin + 45f, top + 32f, formulaPaint)

            // Подсказка (если есть)
            if (config.hint == LetterMathHint.PICTURE) {
                canvas.drawText(problem.emoji, margin + cardWidth - 40f, top + 35f, emojiPaint)
            }

            // Клеточки для ответа
            val boxSize = 20f
            val boxSpacing = 3f
            var boxX = margin + 45f
            val boxY = top + 45f

            for (j in problem.targetWord.indices) {
                canvas.drawRect(boxX, boxY, boxX + boxSize, boxY + boxSize, boxPaint)
                boxX += boxSize + boxSpacing
            }

            // Черновик
            canvas.drawText("черновик", boxX + 10f, boxY + 15f, Paint().apply { textSize = 9f; color = Color.LTGRAY })
            canvas.drawLine(boxX + 60f, boxY + 18f, margin + cardWidth - 10f, boxY + 18f, draftLinePaint)
        }

        // Подпись внизу
        val footerPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
        canvas.drawText("a4umka.ru — задания и игры для детей • лист $pageNum", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, footerPaint)
    }

    private fun drawAnswersPage(canvas: Canvas, problems: List<MathProblem>) {
        val margin = 40f
        var currentY = 60f
        
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 28f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText("Ответы для взрослого", PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 40f
        
        val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val emojiPaint = Paint().apply { textSize = 24f }
        
        for (problem in problems) {
            canvas.drawText(problem.formula + " = ${problem.targetWord}", margin, currentY, ansPaint)
            canvas.drawText(problem.emoji, PAGE_WIDTH - margin - 30f, currentY, emojiPaint)
            currentY += 30f
        }
    }
}
