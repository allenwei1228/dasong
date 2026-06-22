package com.dasong.commerce.ui.game.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.Foundation
import com.dasong.commerce.ui.theme.*

@Composable
fun FoundationSlots(
    foundations: List<Foundation>,
    onSlotClick: (Int) -> Unit
) {
    Column {
        for (row in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..3) {
                    val index = row * 4 + col
                    val foundation = foundations[index]
                    FoundationSlotView(
                        foundation = foundation,
                        index = index + 1,
                        onClick = { onSlotClick(index) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun FoundationSlotView(
    foundation: Foundation,
    index: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .background(
                when {
                    foundation.isBuilt -> FoundationBuilt  // 房屋已购买，效果生效
                    foundation.shopCard != null -> FoundationOccupied  // 店铺牌已放置但房屋未购买
                    else -> FoundationEmpty
                },
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (foundation.shopCard != null) {
                Text(
                    foundation.shopCard!!.name.take(2),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (foundation.isBuilt) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "已建",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            } else {
                Text(
                    "#$index",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "清理${foundation.clearCost}两",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
