package com.example.a4umka.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.a4umka.data.ComprehensionConfig

object ComprehensionPdfDrawer {
    const val PAGE_WIDTH = 595f
    const val PAGE_HEIGHT = 842f

    fun drawDocument(document: PdfDocument, config: ComprehensionConfig, context: Context) {
        val story = config.selectedStory ?: return

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
        var page = document.startPage(pageInfo)
        
        val bgColor = if (config.isBlackAndWhite) Color.WHITE else Color.parseColor("#FAFAFA")
        page.canvas.drawColor(bgColor)
        
        var currentY = 60f
        val margin = 40f
        
        // Шапка (Имя, Класс, Дата)
        val headerPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        
        page.canvas.drawText("Имя:", margin, 40f, headerPaint)
        page.canvas.drawLine(margin + 35f, 42f, margin + 150f, 42f, linePaint)
        
        val gradeText = "${config.grade} класс"
        val gradeWidth = headerPaint.measureText(gradeText)
        page.canvas.drawText(gradeText, PAGE_WIDTH / 2f - gradeWidth / 2f, 40f, headerPaint)
        
        page.canvas.drawText("Дата:", PAGE_WIDTH - 140f, 40f, headerPaint)
        page.canvas.drawLine(PAGE_WIDTH - 100f, 42f, PAGE_WIDTH - margin, 42f, linePaint)
        
        currentY = 90f
        
        // Заголовок рассказа
        val titlePaint = Paint().apply { 
            isAntiAlias = true; textSize = 26f; color = if (config.isBlackAndWhite) Color.BLACK else Color.parseColor("#33691E"); typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER 
        }
        page.canvas.drawText(story.title, PAGE_WIDTH / 2f, currentY, titlePaint)
        currentY += 25f
        
        val subtitlePaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.GRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
        page.canvas.drawText("Читай, замечай, размышляй", PAGE_WIDTH / 2f, currentY, subtitlePaint)
        currentY += 30f
        
        // Инструкция
        val instPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.DKGRAY; typeface = Typeface.DEFAULT }
        page.canvas.drawText("Прочитай рассказ. Затем ответь на вопросы — устно или письменно.", margin, currentY, instPaint)
        currentY += 25f
        
        // Текст с номерами строк
        val bodyPaint = Paint().apply { isAntiAlias = true; textSize = 18f; color = Color.BLACK; typeface = Typeface.DEFAULT }
        val lineNumPaint = Paint().apply { isAntiAlias = true; textSize = 10f; color = Color.GRAY; typeface = Typeface.DEFAULT }
        val lineHeight = bodyPaint.textSize * 1.5f
        
        val paragraphs = story.text.split("\n")
        var globalLineCount = 0
        
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = ""
            val lines = mutableListOf<String>()
            
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (bodyPaint.measureText(testLine) > PAGE_WIDTH - margin * 2 - 30f) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) lines.add(currentLine)
            
            for (line in lines) {
                globalLineCount++
                if (currentY > PAGE_HEIGHT - 100f) {
                    document.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                    page = document.startPage(pageInfo)
                    page.canvas.drawColor(bgColor)
                    currentY = 60f
                }
                
                page.canvas.drawText(line, margin, currentY, bodyPaint)
                
                if (config.showWordCount) {
                    page.canvas.drawText(globalLineCount.toString(), PAGE_WIDTH - margin, currentY, lineNumPaint)
                }
                
                currentY += lineHeight
            }
            currentY += 10f // Отступ между абзацами
        }
        
        currentY += 20f
        
        // Вопросы
        if (config.showAnswerSheet) {
            // Если нужен лист ответов, вопросы не рисуем на этой странице
        } else {
            val qTitlePaint = Paint().apply { isAntiAlias = true; textSize = 20f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD }
            page.canvas.drawText("Вопросы:", margin, currentY, qTitlePaint)
            currentY += 30f
            
            val qPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.BLACK; typeface = Typeface.DEFAULT }
            val boxPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
            
            story.questions.forEachIndexed { index, question ->
                // Проверка на новую страницу для вопросов
                if (currentY > PAGE_HEIGHT - 120f) {
                    document.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
                    page = document.startPage(pageInfo)
                    page.canvas.drawColor(bgColor)
                    currentY = 60f
                }
                
                // Номер вопроса в кружочке
                val circlePaint = Paint().apply { color = if (config.isBlackAndWhite) Color.DKGRAY else Color.parseColor("#A5D6A7"); style = Paint.Style.FILL }
                page.canvas.drawCircle(margin + 10f, currentY - 5f, 10f, circlePaint)
                val numPaint = Paint().apply { isAntiAlias = true; textSize = 12f; color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
                page.canvas.drawText((index + 1).toString(), margin + 10f, currentY, numPaint)
                
                // Текст вопроса
                page.canvas.drawText(question, margin + 30f, currentY, qPaint)
                currentY += 20f
                
                // Рамка для ответа
                val ansBox = RectF(margin + 30f, currentY, PAGE_WIDTH - margin, currentY + 30f)
                page.canvas.drawRoundRect(ansBox, 5f, 5f, boxPaint)
                currentY += 50f
            }
        }
        
        document.finishPage(page)
        
        // Если нужен отдельный лист ответов
        if (config.showAnswerSheet) {
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNum).create()
            page = document.startPage(pageInfo)
            page.canvas.drawColor(bgColor)
            
            val ansTitlePaint = Paint().apply { isAntiAlias = true; textSize = 24f; color = Color.DKGRAY; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
            page.canvas.drawText("Ответы для взрослого", PAGE_WIDTH / 2f, 60f, ansTitlePaint)
            
            val hintText = if (config.showAdultHints) {
                "Проверьте технику чтения: скорость (слов/мин), понимание прочитанного, выразительность."
            } else {
                "Проверьте правильность ответов ребёнка."
            }
            val hintPaint = Paint().apply { isAntiAlias = true; textSize = 14f; color = Color.GRAY; typeface = Typeface.DEFAULT; textAlign = Paint.Align.CENTER }
            page.canvas.drawText(hintText, PAGE_WIDTH / 2f, 90f, hintPaint)
            
            var ansY = 140f
            val ansPaint = Paint().apply { isAntiAlias = true; textSize = 16f; color = Color.BLACK; typeface = Typeface.DEFAULT }
            
            // Примерные ответы (можно оставить пустыми или добавить логику)
            story.questions.forEachIndexed { index, q ->
                page.canvas.drawText("Вопрос ${index + 1}: $q", margin, ansY, ansPaint)
                ansY += 20f
                page.canvas.drawText("Ответ: __________________________________________________", margin, ansY, ansPaint)
                ansY += 40f
            }
            
            document.finishPage(page)
        }
    }
}
