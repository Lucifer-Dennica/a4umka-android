package com.example.a4umka.ui.screens.reading

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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

    // БИТМАП ДЛЯ ПРЕДПРОСМОТРА
    val previewBitmap = remember(config) {
        val bmp = Bitmap.createBitmap(
            AlphabetPdfDrawer.PAGE_WIDTH.toInt(),
            AlphabetPdfDrawer.PAGE_HEIGHT.toInt(),
            Bitmap.Config.ARGB_8888
        )
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // ВИД ЛИСТА
                Text("Вид листа:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.layoutType == AlphabetLayoutType.POSTER, onClick = { config = config.copy(layoutType = AlphabetLayoutType.POSTER) }, label = { Text("Плакат") })
                    FilterChip(selected = config.layoutType == AlphabetLayoutType.FLASHCARDS, onClick = { config = config.copy(layoutType = AlphabetLayoutType.FLASHCARDS) }, label = { Text("Карточки") })
                    FilterChip(selected = config.layoutType == AlphabetLayoutType.COLORING, onClick = { config = config.copy(layoutType = AlphabetLayoutType.COLORING) }, label = { Text("Раскраска") })
                }

                // КОЛИЧЕСТВО КАРТОЧЕК (если выбран этот режим)
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

                // ДИАПАЗОН БУКВ
                Text("Диапазон букв:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LetterDropdown(
                        label = "От буквы",
                        selectedIndex = config.startLetterIndex,
                        onSelect = { config = config.copy(startLetterIndex = it.coerceAtMost(config.endLetterIndex)) },
                        modifier = Modifier.weight(1f)
                    )
                    LetterDropdown(
                        label = "До буквы",
                        selectedIndex = config.endLetterIndex,
                        onSelect = { config = config.copy(endLetterIndex = it.coerceAtLeast(config.startLetterIndex)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // ФИЛЬТР ГЛАСНЫЕ/СОГЛАСНЫЕ
                Text("Какие буквы:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.filter == AlphabetFilter.ALL, onClick = { config = config.copy(filter = AlphabetFilter.ALL) }, label = { Text("Все") })
                    FilterChip(selected = config.filter == AlphabetFilter.VOWELS, onClick = { config = config.copy(filter = AlphabetFilter.VOWELS) }, label = { Text("Гласные") })
                    FilterChip(selected = config.filter == AlphabetFilter.CONSONANTS, onClick = { config = config.copy(filter = AlphabetFilter.CONSONANTS) }, label = { Text("Согласные") })
                }

                Spacer(modifier = Modifier.height(16.dp))
                // РЕГИСТР
                Text("Регистр букв:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.case == AlphabetCase.UPPERCASE, onClick = { config = config.copy(case = AlphabetCase.UPPERCASE) }, label = { Text("АБВ") })
                    FilterChip(selected = config.case == AlphabetCase.LOWERCASE, onClick = { config = config.copy(case = AlphabetCase.LOWERCASE) }, label = { Text("абв") })
                    FilterChip(selected = config.case == AlphabetCase.BOTH, onClick = { config = config.copy(case = AlphabetCase.BOTH) }, label = { Text("АаБб") })
                }

                Spacer(modifier = Modifier.height(16.dp))
                // ЦВЕТ
                Text("Цвет листа:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = config.color == PosterColor.WHITE, onClick = { config = config.copy(color = PosterColor.WHITE) }, label = { Text("Белый") })
                    FilterChip(selected = config.color == PosterColor.GREEN, onClick = { config = config.copy(color = PosterColor.GREEN) }, label = { Text("Зеленый") })
                    FilterChip(selected = config.color == PosterColor.BLUE, onClick = { config = config.copy(color = PosterColor.BLUE) }, label = { Text("Синий") })
                    FilterChip(selected = config.color == PosterColor.PINK, onClick = { config = config.copy(color = PosterColor.PINK) }, label = { Text("Розовый") })
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
                        Text("Печатать слово и картинку", modifier = Modifier.weight(1f))
                        Switch(checked = config.showImages, onCheckedChange = { config = config.copy(showImages = it) })
                    }
                }
            }

            HorizontalDivider()
            Text("Предпросмотр листа A4:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Предпросмотр",
                    modifier = Modifier.fillMaxWidth().aspectRatio(AlphabetPdfDrawer.PAGE_WIDTH / AlphabetPdfDrawer.PAGE_HEIGHT)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDropdown(
    label: String,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val letters = AlphabetData.russianAlphabet

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = letters[selectedIndex].uppercase,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            letters.forEachIndexed { index, letter ->
                DropdownMenuItem(
                    text = { Text(letter.uppercase) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
