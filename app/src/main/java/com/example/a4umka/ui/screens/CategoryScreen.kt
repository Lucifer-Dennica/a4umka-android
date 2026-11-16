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
            .heightIn(min = 130.dp) // <-- Теперь карточка может расти вниз, если текст длинный
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
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
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(end = if (task.isInteractive) 24.dp else 0.dp)
            ) {
                if (task.emoji.isNotEmpty()) {
                    Text(
                        text = task.emoji,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 6.dp, top = 2.dp)
                    )
                }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3, // Увеличили до 3 строк на всякий случай
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}
