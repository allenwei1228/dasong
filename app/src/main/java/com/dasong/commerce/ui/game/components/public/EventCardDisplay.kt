package com.dasong.commerce.ui.game.components.public

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.card.EventCard
import com.dasong.commerce.model.card.EventType

@Composable
fun EventCardDisplay(event: EventCard?) {
    if (event != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    when (event.type) {
                        EventType.POSITIVE -> MaterialTheme.colorScheme.primaryContainer
                        EventType.NEGATIVE -> MaterialTheme.colorScheme.secondaryContainer
                        EventType.RESHUFFLE -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                "📜 当前事件: ${event.name} — ${event.description}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
