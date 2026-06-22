package com.dasong.commerce.ui.result

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dasong.commerce.ui.theme.GoldHighlight

@Composable
fun ResultScreen(
    winnerName: String,
    onBackHome: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GoldHighlight
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "🎉 恭喜获胜 🎉",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Text(
            winnerName,
            style = MaterialTheme.typography.headlineMedium,
            color = GoldHighlight,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "达成了胜利条件：\n8间带房屋模型的店铺 + 50两流动资金",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onBackHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("返回主菜单", style = MaterialTheme.typography.titleMedium)
        }
    }
}
