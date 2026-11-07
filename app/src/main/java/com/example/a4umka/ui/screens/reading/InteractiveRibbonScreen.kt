package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveRibbonScreen(
    onBack: () -> Unit
) {
    // Для игры всегда ставим 8 пропусков для начала
    var config by remember { mutableStateOf(RibbonConfig(mode = RibbonMode.MISSING, missingCount = MissingCount.EIGHT)) }
    
    // Стейт для ответов: Буква -> Правильно/Неправильно/Пусто
    val userAnswers = remember { mutableStateMapOf<String, Boolean>() }
    
    // Диалог выбора буквы
    var activeLetter by remember { mutableStateOf<String?>(null) }
    var options by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuccess by remember { mutableStateOf(false) }

    // Вычисляем пропущенные буквы
    val missingSet = remember(config.seed) {
        val allLetters = (RibbonData.topVowels + RibbonData.bottomVowels + RibbonData.topConsonants + RibbonData.bottomConsonants).map { it.char }
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
        if (userAnswers.isNotEmpty() && userAnswers.values.all { it } && userAnswers.size == missingSet.size) {
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
                },
                actions = {
                    IconButton(onClick = { 
                        config = config.copy(seed = System.currentTimeMillis())
                        userAnswers.clear()
                        showSuccess = false
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Новый вариант")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Нажми на пустую клетку и выбери правильную букву:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Сетка (упрощенная версия для экрана)
            InteractiveGrid(
                missingSet = missingSet,
                userAnswers = userAnswers,
                onCellClick = { letter ->
                    activeLetter = letter
                    val allLetters = ("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".map { it.toString() }).toMutableList()
                    allLetters.remove(letter) // убираем правильный ответ из списка
                    val wrongOptions = allLetters.shuffled().take(3)
                    options = (wrongOptions + letter).shuffled()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

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
            }
        }

        // Диалог выбора буквы
        if (activeLetter != null) {
            AlertDialog(
                onDismissRequest = { activeLetter = null },
                title = { Text("Какая здесь буква?") },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        options.forEach { option ->
                            Button(
                                onClick = {
                                    if (option == activeLetter) {
                                        userAnswers[activeLetter!!] = true
                                        activeLetter = null // Закрываем при правильном ответе
                                    } else {
                                        // Подсвечиваем красным (пока просто игнорируем или вибрируем)
                                        // В MVP можно просто не закрывать диалог, пока не выберут правильный
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text(text = option, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
fun InteractiveGrid(
    missingSet: Set<String>,
    userAnswers: Map<String, Boolean>,
    onCellClick: (String) -> Unit
) {
    // Вспомогательная функция для рисования одной ячейки
    @Composable
    fun Cell(letter: String) {
        val isMissing = letter in missingSet
        val isAnswered = userAnswers[letter] == true
        
        val bgColor = if (isAnswered) Color(0xFF81C784) else Color.Gray // Зеленый если решено
        val text = if (isMissing && !isAnswered) "?" else letter
        
        Box(
            modifier = Modifier
                .size(36.dp)
                .padding(1.dp)
                .background(if (isMissing) bgColor else Color.LightGray, RoundedCornerShape(4.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                .clickable(enabled = isMissing && !isAnswered) { onCellClick(letter) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isMissing && !isAnswered) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }

    // Рисуем упрощенную ленту (для экономии места на экране телефона)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Гласные (верхний ряд)
        Row {
            listOf("А", "О", "У", "Ы", "Э").forEach { Cell(it) }
        }
        // Гласные (нижний ряд)
        Row {
            listOf("Я", "Ё", "Ю", "И", "Е").forEach { Cell(it) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Согласные (верхний)
        Row {
            listOf("Л", "М", "Н", "Р", "Й", "Б", "В", "Г", "Д", "Ж", "З").forEach { Cell(it) }
        }
        // Согласные (нижний)
        Row {
            listOf("П", "Ф", "К", "Т", "Ш", "С", "Х", "Ц", "Ч", "Щ").forEach { Cell(it) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Знаки
        Row {
            listOf("ь", "ъ").forEach { Cell(it) }
        }
    }
}
