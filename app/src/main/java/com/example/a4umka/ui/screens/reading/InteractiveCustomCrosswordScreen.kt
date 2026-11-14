package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.CrosswordMode
import com.example.a4umka.data.CustomCrosswordConfig
import com.example.a4umka.data.CustomCrosswordData
import com.example.a4umka.data.PlacedWord

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun InteractiveCustomCrosswordScreen(
    config: CustomCrosswordConfig,
    onBack: () -> Unit
) {
    val pairs = remember { CustomCrosswordData.parseInput(config.wordsInput) }
    val grid = remember(config.seed) { CustomCrosswordData.generateGrid(pairs, config.seed) }
    
    val userInputs = remember { mutableStateMapOf<Pair<Int, Int>, String>() }
    var isSolved by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    
    // Для автоперехода
    val focusRequesters = remember { mutableMapOf<Pair<Int, Int>, FocusRequester>() }
    var focusedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var currentWord by remember { mutableStateOf<PlacedWord?>(null) }

    // Инициализация FocusRequester для всех ячеек
    LaunchedEffect(grid) {
        val minX = grid.cells.keys.minOfOrNull { it.first } ?: 0
        val maxX = grid.cells.keys.maxOfOrNull { it.first } ?: 0
        val minY = grid.cells.keys.minOfOrNull { it.second } ?: 0
        val maxY = grid.cells.keys.maxOfOrNull { it.second } ?: 0
        
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val pos = Pair(x, y)
                if (grid.cells.containsKey(pos)) {
                    if (!focusRequesters.containsKey(pos)) {
                        focusRequesters[pos] = FocusRequester()
                    }
                }
            }
        }
        
        // Ставим фокус на первую клетку
        if (focusedCell == null) {
            val firstCell = grid.cells.keys.firstOrNull()
            if (firstCell != null) {
                focusedCell = firstCell
                currentWord = grid.words.firstOrNull()
            }
        }
    }

    LaunchedEffect(focusedCell) {
        focusedCell?.let { pos ->
            if (focusRequesters.containsKey(pos)) {
                focusRequesters[pos]?.requestFocus()
            }
        }
    }
    
    fun checkSolution() {
        var allCorrect = true
        for ((pos, char) in grid.cells) {
            if (userInputs[pos]?.uppercase() != char.toString()) {
                allCorrect = false
                break
            }
        }
        if (allCorrect) {
            isSolved = true
            showError = false
        } else {
            showError = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(config.title.ifEmpty { "Кроссворд" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        userInputs.clear()
                        isSolved = false 
                        showError = false
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Очистить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (grid.cells.isEmpty()) {
                Text("Нет слов для отображения. Проверьте ввод.")
                return@Column
            }

            val minX = grid.cells.keys.minOfOrNull { it.first } ?: 0
            val maxX = grid.cells.keys.maxOfOrNull { it.first } ?: 0
            val minY = grid.cells.keys.minOfOrNull { it.second } ?: 0
            val maxY = grid.cells.keys.maxOfOrNull { it.second } ?: 0

            // Показываем текущий вопрос для сканворда
            if (config.mode == CrosswordMode.SCANWORD && currentWord != null) {
                val question = pairs.find { it.word == currentWord!!.word }?.question ?: ""
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        text = "${currentWord!!.num} ${if (currentWord!!.isAcross) "→" else "↓"} $question",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Отрисовка сетки
            Column(modifier = Modifier.border(1.dp, Color.Gray)) {
                for (y in minY..maxY) {
                    Row {
                        for (x in minX..maxX) {
                            val pos = Pair(x, y)
                            val isCell = grid.cells.containsKey(pos)
                            
                            if (isCell) {
                                val wordNum = grid.words.find { it.x == x && it.y == y }?.num
                                val currentText = userInputs[pos] ?: ""
                                val isCorrect = currentText.uppercase() == grid.cells[pos].toString()
                                
                                // Логика подсветки
                                val bgColor = when {
                                    showError && !isCorrect -> Color(0xFFFFCDD2) // Красный при ошибке
                                    isSolved && isCorrect -> Color(0xFFC8E6C9) // Зеленый при успехе
                                    else -> Color.White
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(0.5.dp, Color.Gray)
                                        .background(bgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (wordNum != null) {
                                        Text(
                                            text = wordNum.toString(),
                                            fontSize = 8.sp,
                                            color = Color.Red,
                                            modifier = Modifier.align(Alignment.TopStart).padding(2.dp)
                                        )
                                    }
                                    
                                    // Стрелочки для сканворда
                                    if (config.mode == CrosswordMode.SCANWORD) {
                                        if (grid.words.any { it.isAcross && it.x == x && it.y == y }) {
                                            Text("→", fontSize = 8.sp, color = Color.Blue, modifier = Modifier.align(Alignment.BottomEnd))
                                        }
                                        if (grid.words.any { !it.isAcross && it.x == x && it.y == y }) {
                                            Text("↓", fontSize = 8.sp, color = Color.Blue, modifier = Modifier.align(Alignment.TopEnd))
                                        }
                                    }

                                    BasicTextField(
                                        value = currentText,
                                        onValueChange = { newValue ->
                                            val oldValue = currentText
                                            val filtered = newValue.take(1).uppercase()
                                            userInputs[pos] = filtered
                                            isSolved = false
                                            showError = false

                                            if (currentWord != null) {
                                                val word = currentWord!!
                                                val index = if (word.isAcross) pos.first - word.x else pos.second - word.y
                                                
                                                if (filtered.length > oldValue.length) {
                                                    // Ввод буквы -> вперед
                                                    if (index < word.word.length - 1) {
                                                        val nextPos = if (word.isAcross) Pair(pos.first + 1, pos.second) else Pair(pos.first, pos.second + 1)
                                                        focusedCell = nextPos
                                                    }
                                                } else if (filtered.isEmpty() && oldValue.isNotEmpty()) {
                                                    // Удаление буквы -> назад
                                                    if (index > 0) {
                                                        val prevPos = if (word.isAcross) Pair(pos.first - 1, pos.second) else Pair(pos.first, pos.second - 1)
                                                        focusedCell = prevPos
                                                    }
                                                }
                                            }
                                        },
                                        textStyle = TextStyle(
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        ),
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .wrapContentHeight(Alignment.CenterVertically)
                                            .focusRequester(focusRequesters.getOrPut(pos) { FocusRequester() })
                                            .onFocusChanged { state ->
                                                if (state.isFocused) {
                                                    focusedCell = pos
                                                    val wordsHere = grid.words.filter { w ->
                                                        if (w.isAcross) pos.second == w.y && pos.first >= w.x && pos.first < w.x + w.word.length
                                                        else pos.first == w.x && pos.second >= w.y && pos.second < w.y + w.word.length
                                                    }
                                                    
                                                    if (wordsHere.isNotEmpty()) {
                                                        // Если текущее слово здесь, оставляем его, иначе берем первое
                                                        if (!wordsHere.contains(currentWord)) {
                                                            currentWord = wordsHere.firstOrNull { it.x == pos.first && it.y == pos.second } ?: wordsHere.first()
                                                        }
                                                    }
                                                }
                                            }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showError) {
                Text("Есть ошибки, проверь выделенные красным клетки!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isSolved) {
                Text("🎉 Молодец! Кроссворд решен!", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Button(onClick = { checkSolution() }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Проверить")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Список вопросов внизу только для классического кроссворда
            if (config.mode == CrosswordMode.STANDARD) {
                Text("Вопросы:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                
                grid.words.forEach { word ->
                    val question = pairs.find { it.word == word.word }?.question ?: ""
                    Text(
                        text = "${word.num} ${if (word.isAcross) "→" else "↓"} $question", 
                        fontSize = 14.sp, 
                        modifier = Modifier.padding(vertical = 4.dp).align(Alignment.Start)
                    )
                }
            }
        }
    }
}
