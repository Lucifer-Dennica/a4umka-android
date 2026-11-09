package com.example.a4umka.ui.screens.reading

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.*
import com.example.a4umka.util.BookmarksPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(BookmarksConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    // ДЕБАУНС: ждём 300мс после последнего изменения, потом рендерим
    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    
    LaunchedEffect(config) {
        delay(300) // Дебаунс чтобы не рендерить при каждом клике
        previewPages = BookmarksPdfDrawer.generatePreview(config, context)
    }

    // При выходе чистим кэш
    DisposableEffect(Unit) {
        onDispose { BookmarksPdfDrawer.clearCache() }
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Закладки для книг") },
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
                        val result = PdfExporter.exportBookmarks(context, config)
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
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.printBacksides, onCheckedChange = { config = config.copy(printBacksides = it) })
                            Column {
                                Text("Печатать обороты", fontWeight = FontWeight.Bold)
                                Text("Второй лист в зеркальном порядке", fontSize = 12.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.isBlackAndWhite, onCheckedChange = { config = config.copy(isBlackAndWhite = it) })
                            Column {
                                Text("Чёрно-белый режим", fontWeight = FontWeight.Bold)
                                Text("Только контур, экономит картридж", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Настройки 4 закладок:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(4) { index ->
                val bm = config.bookmarks[index]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Закладка ${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))

                        var typeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                            OutlinedTextField(
                                value = when(bm.type) {
                                    BookmarkType.COLORING -> "Раскраска"
                                    BookmarkType.NAME -> "С именем"
                                    BookmarkType.CHEAT_SHEET -> "Шпаргалка"
                                    BookmarkType.COUNTER -> "Счётчик"
                                }, onValueChange = {}, readOnly = true,
                                label = { Text("Что на ней") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                listOf(
                                    BookmarkType.COLORING to "Раскраска",
                                    BookmarkType.NAME to "С именем",
                                    BookmarkType.CHEAT_SHEET to "Шпаргалка",
                                    BookmarkType.COUNTER to "Счётчик"
                                ).forEach { (t, name) ->
                                    DropdownMenuItem(text = { Text(name) }, onClick = {
                                        val newList = config.bookmarks.toMutableList()
                                        newList[index] = bm.copy(type = t)
                                        config = config.copy(bookmarks = newList)
                                        typeExpanded = false
                                    })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var heroExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = heroExpanded, onExpandedChange = { heroExpanded = it }, modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = when(bm.character) {
                                        BookmarkCharacter.GIRL_BOW -> "👧 Девочка"
                                        BookmarkCharacter.BOY_SCARF -> "👦 Мальчик"
                                        BookmarkCharacter.GIRL_CAT -> "🐱 Кот"
                                        BookmarkCharacter.BOY_PLANE -> "✈️ Самолётик"
                                    }, onValueChange = {}, readOnly = true,
                                    label = { Text("Герой", fontSize = 12.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = heroExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = heroExpanded, onDismissRequest = { heroExpanded = false }) {
                                    listOf(
                                        BookmarkCharacter.GIRL_BOW to "👧 Девочка",
                                        BookmarkCharacter.BOY_SCARF to "👦 Мальчик",
                                        BookmarkCharacter.GIRL_CAT to "🐱 Кот",
                                        BookmarkCharacter.BOY_PLANE to "✈️ Самолётик"
                                    ).forEach { (c, name) ->
                                        DropdownMenuItem(text = { Text(name) }, onClick = {
                                            val newList = config.bookmarks.toMutableList()
                                            newList[index] = bm.copy(character = c)
                                            config = config.copy(bookmarks = newList)
                                            heroExpanded = false
                                        })
                                    }
                                }
                            }
                            
                            var patternExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = patternExpanded, onExpandedChange = { patternExpanded = it }, modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = when(bm.pattern) {
                                        BookmarkPattern.LEAVES -> "Листики"
                                        BookmarkPattern.STARS -> "Звёзды"
                                        BookmarkPattern.WAVES -> "Волны"
                                        BookmarkPattern.HEARTS -> "Сердечки"
                                    }, onValueChange = {}, readOnly = true,
                                    label = { Text("Узор", fontSize = 12.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patternExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = patternExpanded, onDismissRequest = { patternExpanded = false }) {
                                    listOf(
                                        BookmarkPattern.LEAVES to "Листики",
                                        BookmarkPattern.STARS to "Звёзды",
                                        BookmarkPattern.WAVES to "Волны",
                                        BookmarkPattern.HEARTS to "Сердечки"
                                    ).forEach { (p, name) ->
                                        DropdownMenuItem(text = { Text(name) }, onClick = {
                                            val newList = config.bookmarks.toMutableList()
                                            newList[index] = bm.copy(pattern = p)
                                            config = config.copy(bookmarks = newList)
                                            patternExpanded = false
                                        })
                                    }
                                }
                            }
                        }

                        if (bm.type == BookmarkType.NAME) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bm.name,
                                onValueChange = { 
                                    val newList = config.bookmarks.toMutableList()
                                    newList[index] = bm.copy(name = it)
                                    config = config.copy(bookmarks = newList)
                                },
                                label = { Text("Имя ребёнка") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (bm.type == BookmarkType.CHEAT_SHEET) {
                            Spacer(modifier = Modifier.height(8.dp))
                            var cheatExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = cheatExpanded, onExpandedChange = { cheatExpanded = it }) {
                                OutlinedTextField(
                                    value = when(bm.cheatSheetType) {
                                        CheatSheetType.MULTIPLICATION -> "Таблица умножения"
                                        CheatSheetType.ALPHABET -> "Русский алфавит"
                                        CheatSheetType.NUMBER_COMPOSITION -> "Состав числа 10"
                                    }, onValueChange = {}, readOnly = true,
                                    label = { Text("Шпаргалка") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cheatExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = cheatExpanded, onDismissRequest = { cheatExpanded = false }) {
                                    listOf(
                                        CheatSheetType.MULTIPLICATION to "Таблица умножения",
                                        CheatSheetType.ALPHABET to "Русский алфавит",
                                        CheatSheetType.NUMBER_COMPOSITION to "Состав числа 10"
                                    ).forEach { (cs, name) ->
                                        DropdownMenuItem(text = { Text(name) }, onClick = {
                                            val newList = config.bookmarks.toMutableList()
                                            newList[index] = bm.copy(cheatSheetType = cs)
                                            config = config.copy(bookmarks = newList)
                                            cheatExpanded = false
                                        })
                                    }
                                }
                            }
                            
                            if (bm.cheatSheetType == CheatSheetType.MULTIPLICATION) {
                                Spacer(modifier = Modifier.height(8.dp))
                                var multExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(expanded = multExpanded, onExpandedChange = { multExpanded = it }) {
                                    OutlinedTextField(
                                        value = "На ${bm.multiplier}", onValueChange = {}, readOnly = true,
                                        label = { Text("Какая таблица") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = multExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(expanded = multExpanded, onDismissRequest = { multExpanded = false }) {
                                        (2..9).forEach { num ->
                                            DropdownMenuItem(text = { Text("На $num") }, onClick = {
                                                val newList = config.bookmarks.toMutableList()
                                                newList[index] = bm.copy(multiplier = num)
                                                config = config.copy(bookmarks = newList)
                                                multExpanded = false
                                            })
                                        }
                                    }
                                }
                            }
                        }

                        if (bm.type == BookmarkType.COUNTER) {
                            Spacer(modifier = Modifier.height(8.dp))
                            var counterExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = counterExpanded, onExpandedChange = { counterExpanded = it }) {
                                OutlinedTextField(
                                    value = when(bm.counterType) {
                                        CounterType.BOOKS -> "Книги"
                                        CounterType.PAGES -> "Сотни страниц"
                                        CounterType.DAYS -> "Дни чтения"
                                    }, onValueChange = {}, readOnly = true,
                                    label = { Text("Считаем") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = counterExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = counterExpanded, onDismissRequest = { counterExpanded = false }) {
                                    listOf(
                                        CounterType.BOOKS to "Книги",
                                        CounterType.PAGES to "Сотни страниц",
                                        CounterType.DAYS to "Дни чтения"
                                    ).forEach { (ct, name) ->
                                        DropdownMenuItem(text = { Text(name) }, onClick = {
                                            val newList = config.bookmarks.toMutableList()
                                            newList[index] = bm.copy(counterType = ct)
                                            config = config.copy(bookmarks = newList)
                                            counterExpanded = false
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (previewPages.size > 1) {
                        Text("Сторона ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(BookmarksPdfDrawer.PAGE_WIDTH / BookmarksPdfDrawer.PAGE_HEIGHT)
                    ) { page ->
                        if (page < previewPages.size) {
                            Image(
                                bitmap = previewPages[page].asImageBitmap(),
                                contentDescription = "Предпросмотр",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Закладки для книг", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Генератор собирает лист A4, на котором помещаются четыре закладки. Каждую настраиваете отдельно. Вторым листом печатаются обороты в зеркальном порядке.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
