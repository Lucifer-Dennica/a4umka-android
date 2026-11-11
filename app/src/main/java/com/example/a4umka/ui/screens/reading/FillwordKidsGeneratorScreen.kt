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
import com.example.a4umka.data.FillwordKidsConfig
import com.example.a4umka.data.FillwordKidsDifficulty
import com.example.a4umka.util.FillwordKidsPdfDrawer
import com.example.a4umka.util.PdfExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillwordKidsGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var config by remember { mutableStateOf(FillwordKidsConfig()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    var previewPages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }
    LaunchedEffect(config) {
        delay(300)
        previewPages = FillwordKidsPdfDrawer.generatePreview(config, context)
    }

    val pagerState = rememberPagerState(pageCount = { previewPages.size.coerceAtLeast(1) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Филворд для малышей") },
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
                        val result = PdfExporter.exportFillwordKids(context, config)
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
                Text("Сложность:", style = MaterialTheme.typography.labelLarge)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == FillwordKidsDifficulty.EASY, onClick = { config = config.copy(difficulty = FillwordKidsDifficulty.EASY) })
                        Text("Легко (7×7, 5 слов, только горизонталь)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == FillwordKidsDifficulty.MEDIUM, onClick = { config = config.copy(difficulty = FillwordKidsDifficulty.MEDIUM) })
                        Text("Средне (9×9, 6 слов, горизонт. и вертик.)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == FillwordKidsDifficulty.HARD, onClick = { config = config.copy(difficulty = FillwordKidsDifficulty.HARD) })
                        Text("Сложно (11×11, 8 слов)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = config.difficulty == FillwordKidsDifficulty.SUPER_HARD, onClick = { config = config.copy(difficulty = FillwordKidsDifficulty.SUPER_HARD) })
                        Text("Суперсложно (13×13, 10 слов)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.onlyPictures, onCheckedChange = { config = config.copy(onlyPictures = it) })
                    Text("Только картинки в подсказках (сложнее)", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.addTraps, onCheckedChange = { config = config.copy(addTraps = it) })
                    Text("Ловушки (почти-слова, как ЛИСО)", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = config.addAnswers, onCheckedChange = { config = config.copy(addAnswers = it) })
                    Text("Добавить лист с ответами", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Предпросмотр:", style = MaterialTheme.typography.labelLarge)
                if (previewPages.size > 1) {
                    Text("Страница ${pagerState.currentPage + 1} из ${previewPages.size}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if (previewPages.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().aspectRatio(FillwordKidsPdfDrawer.PAGE_WIDTH / FillwordKidsPdfDrawer.PAGE_HEIGHT)
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
                    Text("Филворд для малышей", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Наш онлайн-генератор детских филвордов создаёт новый лист со спрятанными словами каждый раз, когда вы нажимаете кнопку, — поэтому одинаковый филворд ребёнку не достанется дважды. Достаточно открыть страницу, выбрать уровень сложности и распечатать филворд на обычном принтере в формате A4.\n\n" +
                        "Филворд для детей 5, 6 и 7 лет — это не только увлекательная игра, но и эффективный тренажёр чтения, внимания и усидчивости.\n\n" +
                        "Уровни сложности:\n" +
                        "• Лёгкий — сетка 7×7, пять коротких слов, спрятанных только по горизонтали.\n" +
                        "• Средний — сетка 9×9, шесть слов по горизонтали и вертикали.\n" +
                        "• Сложный — сетка 11×11, восемь слов разной длины.\n" +
                        "• Суперсложный — сетка 13×13, десять слов, включая длинные.\n\n" +
                        "Для самых маленьких есть режим с картинками-подсказками: рядом с сеткой показаны изображения предметов, названия которых нужно найти. Так ребёнок понимает, какие именно слова искать, и чтение даётся легче.\n\n" +
                        "Советы родителям:\n" +
                        "• Начинайте с лёгкого уровня и постепенно усложняйте.\n" +
                        "• Проговаривайте найденные слова вслух.\n" +
                        "• Не торопите ребёнка.\n" +
                        "• Хвалите за каждое найденное слово.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
