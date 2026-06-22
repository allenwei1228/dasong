package com.dasong.commerce.ui.game.components.public

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.card.GuestCard

@Composable
fun GuestQueue(guestQueue: List<GuestCard>) {
    Column {
        Text("客人队列", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            guestQueue.reversed().forEachIndexed { index, guest ->
                val posLabel = index + 1 // reversed: index 0 = position 1
                GuestCardView(
                    guest = guest,
                    position = posLabel,
                    cost = posLabel - 1
                )
            }
        }
    }
}

@Composable
private fun GuestCardView(
    guest: GuestCard,
    position: Int,
    cost: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.secondary,
                RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
    ) {
        Text(
            "#$position ${guest.name}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            "菜×${guest.menuConsumption}",
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            if (cost > 0) "小费${cost}两" else "免费",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            guest.shopTypes.joinToString("") { it.displayName.take(2) },
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
