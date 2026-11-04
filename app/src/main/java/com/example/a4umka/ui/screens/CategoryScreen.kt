package com.example.a4umka.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.a4umka.data.AppContent
import com.example.a4umka.data.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: String,
    onTaskClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val category = AppContent.categoryById(categoryId)
    val tasks = AppContent.tasksFor(categoryId)

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
            items(tasks) { task ->
                TaskTile(task = task, onClick = { onTaskClick(task.id) })
            }
        }
    }
}

@Composable
private fun TaskTile(task: TaskItem, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start
            )
        }
    }
}
