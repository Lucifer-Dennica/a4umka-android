package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveRibbonScreen(
    onBack: () -> Unit
) {
    // Для игры всегда ставим 8 пропусков для начала
    var config by remember { mutableStateOf(RibbonConfig(mode = RibbonMode.MISSING, missingCount = MissingCount.EIGHT)) }
    
    val userAnswers = remember { mutableStateMapOf<String, Boolean>() }
    
    // Диалог выбора буквы
    var activeLetter by remember { mutableStateOf<RibbonLetter?>(null) }
    var options by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorOption by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    // Вычисляем пропущенные буквы
    val missingLetters = remember(config.seed) {
        val allLetters = (RibbonData.topVowels + RibbonData.bottomVowels + RibbonData.topConsonants + RibbonData.bottomConsonants)
        val countToHide = when(config.missingCount) {
            MissingCount.EIGHT -> 8
            MissingCount.SIXTEEN -> 16
            MissingCount.ALL -> allLetters.size
        }
        val random = Random(config.seed)
        allLetters.shuffled(random).take(countToHide).toSet()
    }

    // Проверка на победу
    LaunchedEffect(userAnswers.size) {
        if (userAnswers.isNotEmpty() && userAnswers.values.all { it } && userAnswers.size == missingLetters.size) {
            showSuccess = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Решаем на экране") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Нажми на знак вопроса, чтобы выбрать букву:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Сетка
            InteractiveGrid(
                missingLetters = missingLetters,
                userAnswers = userAnswers,
                onCellClick = { letterObj ->
                    activeLetter = letterObj
                    errorOption = null
                    val allLetters = (RibbonData.topVowels + RibbonData.bottomVowels + RibbonData.topConsonants + RibbonData.bottomConsonants).map { it.char }
                    val wrongOptions = allLetters.filter { it != letterObj.char }.shuffled().take(3)
                    options = (wrongOptions + letterObj.char).shuffled()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (showSuccess) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎉 Молодец! Все буквы на месте!",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Кнопка "Новый вариант" внизу
            Button(
                onClick = { 
                    config = config.copy(seed = System.currentTimeMillis())
                    userAnswers.clear()
                    showSuccess = false
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сгенерировать новый вариант")
            }
        }

        // Диалог выбора буквы
        if (activeLetter != null) {
            AlertDialog(
                onDismissRequest = { activeLetter = null },
                title = { Text("Какая здесь буква?") },
                text = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            options.forEach { option ->
                                val isError = errorOption == option
                                Button(
                                    onClick = {
                                        if (option == activeLetter?.char) {
                                            userAnswers[activeLetter!!.char] = true
                                            activeLetter = null
                                        } else {
                                            errorOption = option
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isError) Color.Red else MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text(text = option, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        if (errorOption != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Попробуй другую букву!",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeLetter = null }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun InteractiveGrid(
    missingLetters: Set<RibbonLetter>,
    userAnswers: Map<String, Boolean>,
    onCellClick: (RibbonLetter) -> Unit
) {
    @Composable
    fun Cell(letterObj: RibbonLetter) {
        val letterChar = letterObj.char
        val isMissing = missingLetters.any { it.char == letterChar }
        val isAnswered = userAnswers[letterChar] == true
        
        // ИСПРАВЛЕНО: Используем Compose Color(0xFF...) вместо parseColor
        val baseColor = when (letterObj.color) {
            RibbonColor.RED -> Color(0xFFEF5350)
            RibbonColor.BLUE -> Color(0xFF42A5F5)
            RibbonColor.GREEN -> Color(0xFF66BB6A)
            RibbonColor.WHITE -> Color.White
            RibbonColor.BOTH -> Color(0xFF42A5F5) // Упрощаем, делаем синим
        }

        val bgColor = when {
            isAnswered -> Color(0xFF81C784) // Зеленый при успехе
            isMissing -> baseColor
            else -> Color.LightGray
        }
        
        val text = if (isMissing && !isAnswered) "?" else letterChar
        val textColor = if (isMissing && !isAnswered) Color.White else if (isAnswered) Color.White else Color.Black
        
        Box(
            modifier = Modifier
                .size(34.dp)
                .padding(1.dp)
                .background(bgColor, RoundedCornerShape(4.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                .clickable(enabled = isMissing && !isAnswered) { onCellClick(letterObj) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row { RibbonData.topVowels.forEach { Cell(it) } }
        Row { RibbonData.bottomVowels.forEach { Cell(it) } }
        Spacer(modifier = Modifier.height(8.dp))
        
        Row { RibbonData.topConsonants.forEach { Cell(it) } }
        Row { RibbonData.bottomConsonants.forEach { Cell(it) } }
        Spacer(modifier = Modifier.height(8.dp))
        
        Row { RibbonData.signs.forEach { Cell(it) } }
    }
}
