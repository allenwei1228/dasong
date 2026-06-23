package com.dasong.commerce.ui.game.components.settlement

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.card.ShopType
import com.dasong.commerce.ui.game.SettlementDisplayData
import com.dasong.commerce.ui.theme.GoldHighlight
import com.dasong.commerce.util.CurrencyFormatter

@Composable
fun SettlementDialog(
    data: SettlementDisplayData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("💰 结算结果", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                // Tip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("小费收入")
                    Text(CurrencyFormatter.format(data.tip))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Menu income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("菜单收入")
                    Text(CurrencyFormatter.format(data.menuIncome))
                }

                // Show menu cards with income values
                if (data.menuCards.isNotEmpty()) {
                    Text(
                        "菜单牌: ${data.menuCards.joinToString(", ") { "${it.name}(${it.baseIncome}两)" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Dice results
                if (data.diceResults.isNotEmpty()) {
                    data.diceResults.forEach { dice ->
                        Text(
                            "🎲 ${dice.cardName}: 掷出${dice.diceValue}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Shop income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("店铺收入")
                    Text(CurrencyFormatter.format(data.shopIncome))
                }

                if (data.shopActivations.isNotEmpty()) {
                    data.shopActivations.forEach { activation ->
                        val baseDesc = if (activation.shop.type == ShopType.GUA_SI) {
                            "基础收入:🎲 掷出${activation.baseIncome}，"
                        } else {
                            "基础收入:${activation.baseIncome}"
                        }
                        Text(
                            "  ${activation.shop.name}: $baseDesc${if(activation.linkageBonus > 0) " +${activation.linkageBonus}(联动)" else ""} = ${activation.totalIncome}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "总计收入",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        CurrencyFormatter.format(data.totalIncome),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldHighlight
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("确认")
            }
        }
    )
}
