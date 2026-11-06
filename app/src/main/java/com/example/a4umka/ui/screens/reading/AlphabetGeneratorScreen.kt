package com.example.a4umka.ui.screens.reading

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a4umka.data.*
import com.example.a4umka.util.AlphabetPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphabetGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(AlphabetConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    // БИТМАП ДЛЯ ПРЕДПРОСМОТРА
    val previewBitmap = remember(config) {
        val bmp = Bitmap.createBitmap(AlphabetPdfDrawer.PAGE_WIDTH.toInt(), AlphabetPdfDrawer.PAGE_HEIGHT.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bmp)
        AlphabetPdfDrawer.drawLayout(canvas, config, context)
        bmp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Генератор Алфавита") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Справка")
                    }
                    IconButton(onClick = {
                        val result = PdfExporter.exportAlphabetPoster(context, config)
                        scope.launch {
                            if (result != null) snackbarHostState.showSnackbar("Сохранено: $result")
                            else snackbarHostState.showSnackbar("Ошибка сохранения")
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить в PDF")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text("Вид листа:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.layoutType == AlphabetLayoutType.POSTER, onClick = { config = config.copy(layoutType = AlphabetLayoutType.POSTER) }, label = { Text("Плакат") })
                    FilterChip(selected = config.layoutType == AlphabetLayoutType.FLASHCARDS, onClick = { config = config.copy(layoutType = AlphabetLayoutType.FLASHCARDS) }, label = { Text("Карточки") })
                    FilterChip(selected = config.layoutType == AlphabetLayoutType.COLORING, onClick = { config = config.copy(layoutType = AlphabetLayoutType.COLORING) }, label = { Text("Раскраска") })
                }

                if (config.layoutType == AlphabetLayoutType.FLASHCARDS) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Карточек на лист:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 4, 6).forEach { count ->
                            FilterChip(selected = config.cardsPerPage == count, onClick = { config = config.copy(cardsPerPage = count) }, label = { Text("$count") })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Диапазон букв:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LetterDropdown(
                        label = "От", selectedIndex = config.startLetterIndex,
                        onSelect = { config = config.copy(startLetterIndex = it.coerceAtMost(config.endLetterIndex)) },
                        modifier = Modifier.weight(1f)
                    )
                    LetterDropdown(
                        label = "До", selectedIndex = config.endLetterIndex,
                        onSelect = { config = config.copy(endLetterIndex = it.coerceAtLeast(config.startLetterIndex)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Какие буквы:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.filter == AlphabetFilter.ALL, onClick = { config = config.copy(filter = AlphabetFilter.ALL) }, label = { Text("Все") })
                    FilterChip(selected = config.filter == AlphabetFilter.VOWELS, onClick = { config = config.copy(filter = AlphabetFilter.VOWELS) }, label = { Text("Гласные") })
                    FilterChip(selected = config.filter == AlphabetFilter.CONSONANTS, onClick = { config = config.copy(filter = AlphabetFilter.CONSONANTS) }, label = { Text("Согласные") })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Регистр букв:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.case == AlphabetCase.UPPERCASE, onClick = { config = config.copy(case = AlphabetCase.UPPERCASE) }, label = { Text("АБВ") })
                    FilterChip(selected = config.case == AlphabetCase.LOWERCASE, onClick = { config = config.copy(case = AlphabetCase.LOWERCASE) }, label = { Text("абв") })
                    FilterChip(selected = config.case == AlphabetCase.BOTH, onClick = { config = config.copy(case = AlphabetCase.BOTH) }, label = { Text("АаБб") })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Цвет листа:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.color == PosterColor.WHITE, onClick = { config = config.copy(color = PosterColor.WHITE) }, label = { Text("Белый") })
                    FilterChip(selected = config.color == PosterColor.GREEN, onClick = { config = config.copy(color = PosterColor.GREEN) }, label = { Text("Зеленый") })
                    FilterChip(selected = config.color == PosterColor.BLUE, onClick = { config = config.copy(color = PosterColor.BLUE) }, label = { Text("Синий") })
                    FilterChip(selected = config.color == PosterColor.BLACK_WHITE, onClick = { config = config.copy(color = PosterColor.BLACK_WHITE) }, label = { Text("Ч/Б") })
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Прописные буквы", modifier = Modifier.weight(1f))
                    Switch(checked = config.isCursive, onCheckedChange = { config = config.copy(isCursive = it) })
                }

                if (config.layoutType != AlphabetLayoutType.COLORING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Печатать картинку", modifier = Modifier.weight(1f))
                        Switch(checked = config.showImage, onCheckedChange = { config = config.copy(showImage = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Печатать слово", modifier = Modifier.weight(1f))
                        Switch(checked = config.showWord, onCheckedChange = { config = config.copy(showWord = it) })
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Text(
                    text = "💡 Совет: не учите больше 1–2 букв за раз.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ПРЕДПРОСМОТР И КНОПКА ПОД НИМ
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Предпросмотр",
                        modifier = Modifier.fillMaxWidth().aspectRatio(AlphabetPdfDrawer.PAGE_WIDTH / AlphabetPdfDrawer.PAGE_HEIGHT)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // КНОПКА ИНФО ПОД ПРЕДПРОСМОТРОМ
                ElevatedCard(
                    onClick = { showInfoSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Как заниматься по этим листам",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Советы для родителей и педагогов",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // ВЫЕЗЖАЮЩАЯ ПАНЕЛЬ С ТЕКСТОМ
        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Как заниматься по этим листам", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "• Не больше одной-двух букв за раз. Плакат целиком хорош как украшение и повод поговорить, а учить по нему все 33 сразу не нужно.\n\n" +
                        "• Называйте звук, а не название буквы. «м», а не «эм»: со звуками слоги складываются сами.\n\n" +
                        "• Ищите буквы вокруг. Вывеска, упаковка, номер дома — узнавание в жизни закрепляет лучше любого листа.\n\n" +
                        "• Играйте карточками. Разложить, найти, перевернуть, собрать своё имя — буквы запоминаются в игре быстрее.\n\n" +
                        "• Вешайте плакат на уровне глаз ребёнка.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Буквы Ъ, Ы и Ь", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "С этих букв не начинается ни одно слово, и в азбуках их обычно неловко пропускают. Здесь для них печатается слово, где буква стоит внутри, и сама буква выделена заглавной: подЪезд, сЫр, медведЬ. Ребёнок видит, что буква существует и работает, просто иначе.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Виды листов", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "• Плакат: Все выбранные буквы на одном листе. Сетка подстраивается автоматически.\n\n" +
                        "• Карточки: По 1, 2, 4 или 6 карточек на лист с пунктиром под разрез. Из них получаются лото, мемори, «найди букву».\n\n" +
                        "• Раскраска: Буквы контуром плюс пустое окошко с заданием нарисовать то, что на неё начинается.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDropdown(label: String, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val letters = AlphabetData.russianAlphabet

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = letters[selectedIndex].uppercase, onValueChange = {}, readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            letters.forEachIndexed { index, letter ->
                DropdownMenuItem(
                    text = { Text(letter.uppercase) },
                    onClick = { onSelect(index); expanded = false }
                )
            }
        }
    }
}
