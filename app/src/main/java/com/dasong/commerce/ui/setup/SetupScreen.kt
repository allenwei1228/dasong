package com.dasong.commerce.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onGameStart: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    var selectedPlayerCount by remember { mutableIntStateOf(2) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开局设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                "选择玩家人数",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(24.dp))

            // Player count selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(2, 3, 4).forEach { count ->
                    val isSelected = selectedPlayerCount == count
                    Card(
                        modifier = Modifier
                            .size(80.dp)
                            .selectable(
                                selected = isSelected,
                                onClick = { selectedPlayerCount = count },
                                role = Role.RadioButton
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$count",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "人",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Info cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎮 热座模式", fontWeight = FontWeight.Bold)
                    Text(
                        "同一设备上 2-4 人轮流操作，当前玩家回合结束后切换设备给下一位玩家",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Starting funds info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💰 初始资金与行动顺序", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "玩家序号随机分配，行动顺序固定如下：",
                        style = MaterialTheme.typography.bodySmall
                    )
                    (1..selectedPlayerCount).forEach { i ->
                        val funds = when (i) { 1 -> 5; 2 -> 6; 3 -> 7; 4 -> 8; else -> 5 }
                        Text(
                            "玩家$i: $funds 两 (${if (i == 1) "先手" else "第${i}位行动"})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Start button
            Button(
                onClick = { onGameStart(selectedPlayerCount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "开始游戏",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
