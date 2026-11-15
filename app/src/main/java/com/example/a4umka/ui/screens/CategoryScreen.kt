package com.example.a4umka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4umka.data.AppContent
import com.example.a4umka.data.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: String,
    onTaskClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val category = remember(categoryId) { AppContent.categoryById(categoryId) }
    val tasks = remember(categoryId) { AppContent.tasksFor(categoryId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category?.title ?: "Раздел") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskTile(
                    task = task,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onTaskClick(task.id) }
                )
            }
        }
    }
}

@Composable
private fun TaskTile(
    task: TaskItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp) // Уменьшили верхний отступ
    ) {
        // Иконка телефона в правом верхнем углу, если доступен интерактив
        if (task.isInteractive) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = "Можно играть на телефоне",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.Top, // Выравниваем контент по верхнему краю
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = if (task.isInteractive) 24.dp else 0.dp)
            ) {
                if (task.emoji.isNotEmpty()) {
                    Text(
                        text = task.emoji,
                        fontSize = 18.sp, // Чуть уменьшили эмодзи, чтобы не перекрывал заголовок
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp)) // Уменьшили отступ между заголовком и текстом
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 4, // Разрешили до 4 строк описания
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp // Чуть сжали межстрочный интервал
            )
        }
    }
}
