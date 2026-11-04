package com.example.a4umka.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.a4umka.data.SettingsRepository
import com.example.a4umka.ui.components.ParentalGate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { SettingsRepository(context.applicationContext) }

    var gatePassed by remember { mutableStateOf(false) }
    val showLabels by repository.showLabels.collectAsState(initial = true)

    if (!gatePassed) {
        ParentalGate(
            onPassed = { gatePassed = true },
            onCancel = onBack
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Показывать подписи разделов", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Если выключено, на главном экране останутся только иконки",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = showLabels,
                    onCheckedChange = { value ->
                        scope.launch { repository.setShowLabels(value) }
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("О приложении", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("А4Умка — генератор развивающих заданий для школьников.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Версия 1.0", style = MaterialTheme.typography.bodySmall)
        }
    }
}
