package com.dasong.commerce.ui.game.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.card.MenuCard
import com.dasong.commerce.model.card.MenuGrade
import com.dasong.commerce.ui.theme.SongBrown

@Composable
fun KitchenPile(cards: List<MenuCard>) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                SongBrown.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, SongBrown.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(8.dp)
            .width(100.dp)
    ) {
        Text("后厨", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text("弃牌堆", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))

        // Show top 3 cards face up
        val displayCards = cards.takeLast(3)
        if (displayCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        SongBrown.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("空", style = MaterialTheme.typography.labelSmall)
            }
        } else {
            displayCards.forEach { card ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            when (card.grade) {
                                MenuGrade.ONE -> Color(0xFFDAA520).copy(alpha = 0.3f)
                                MenuGrade.TWO -> Color(0xFFC0C0C0).copy(alpha = 0.3f)
                                MenuGrade.THREE -> Color(0xFFCD853F).copy(alpha = 0.3f)
                                MenuGrade.FOUR -> Color(0xFF8B7355).copy(alpha = 0.3f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(2.dp)
                ) {
                    Text(
                        "${card.cardGrade(card.grade)}·${card.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(2.dp))
            }
            if (cards.size > 3) {
                Text(
                    "还有${cards.size - 3}张...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }

    // Dialog showing all discarded cards
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("后厨弃牌堆 (共${cards.size}张)")
            },
            text = {
                if (cards.isEmpty()) {
                    Text("弃牌堆为空", color = Color.Gray)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        itemsIndexed(cards) { index, card ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        when (card.grade) {
                                            MenuGrade.ONE -> Color(0xFFDAA520).copy(alpha = 0.2f)
                                            MenuGrade.TWO -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
                                            MenuGrade.THREE -> Color(0xFFCD853F).copy(alpha = 0.2f)
                                            MenuGrade.FOUR -> Color(0xFF8B7355).copy(alpha = 0.2f)
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "#${index + 1} ${card.cardGrade(card.grade)}·${card.displayName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "收入${card.baseIncome}两",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
}
