package com.dasong.commerce.ui.game.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.card.MenuCard
import com.dasong.commerce.ui.theme.SongTeal

@Composable
fun RefinedChamber(cards: List<MenuCard>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                SongTeal.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, SongTeal.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
            .width(100.dp)
    ) {
        Text("雅阁", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text("手牌库", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        // Show as card backs (stacked)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    SongTeal.copy(alpha = 0.3f),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("${cards.size}张", fontWeight = FontWeight.Bold)
        }
    }
}
