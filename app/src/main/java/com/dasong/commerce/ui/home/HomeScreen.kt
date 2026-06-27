package com.dasong.commerce.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dasong.commerce.ui.theme.SongGold
import com.dasong.commerce.ui.theme.SongInk
import com.dasong.commerce.ui.theme.SongRed

@Composable
fun HomeScreen(
    onStartGame: () -> Unit,
    onGuide: () -> Unit,
    onCreateRoom: () -> Unit = {},
    onJoinRoom: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(48.dp))

        // Title
        Text(
            text = "大宋百商图",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
            color = SongInk,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "卡牌构筑 · 店铺经营 · 客人招揽",
            style = MaterialTheme.typography.bodyLarge,
            color = SongGold
        )

        Spacer(Modifier.height(48.dp))

        // Menu buttons
        MenuButton(
            text = "游戏说明",
            icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
            onClick = onGuide,
            color = SongInk
        )

        Spacer(Modifier.height(16.dp))

        MenuButton(
            text = "单机游戏",
            icon = { Icon(Icons.Default.Gamepad, contentDescription = null) },
            onClick = onStartGame,
            color = SongGold
        )

        Spacer(Modifier.height(16.dp))

        MenuButton(
            text = "联机游戏",
            icon = { Icon(Icons.Default.Link, contentDescription = null) },
            onClick = onJoinRoom,
            color = SongRed
        )

        Spacer(Modifier.height(48.dp))
        Text(
            text = "allenwei 开发",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.height(4.dp))
        Text(
            text = "鳐鳐鱼 Yaofish 出品",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun MenuButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    color: Color = SongRed,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        icon()
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
        if (!enabled) {
            Spacer(Modifier.width(8.dp))
            Text("(开发中)", style = MaterialTheme.typography.labelSmall)
        }
    }
}
