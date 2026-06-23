package com.dasong.commerce.ui.game.components.phase

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.card.MenuCard
import com.dasong.commerce.model.card.MenuGrade

@Composable
fun PreparePhasePanel(
    player: PlayerState,
    canPrepare: Boolean,
    onRemove: (MenuCard) -> Unit,
    onSkip: () -> Unit
) {
    val allCards = player.refinedChamber + player.kitchen
    // 按品级统计
    val gradeCounts = MenuGrade.entries.associateWith { grade ->
        allCards.count { it.grade == grade }
    }
    // 后厨菜品按品级分组
    val kitchenByGrade = player.kitchen.groupBy { it.grade }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "备菜阶段 — 花费3两从后厨舍弃1张菜单牌",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            // 菜品品级分布
            Text(
                "菜品牌总数: ${player.totalMenuCards}张 (雅阁${player.refinedChamber.size} + 后厨${player.kitchen.size})",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                buildString {
                    append("品级分布: ")
                    MenuGrade.entries.forEachIndexed { idx, grade ->
                        val count = gradeCounts[grade] ?: 0
                        if (idx > 0) append("  ")
                        append("${gradeToChinese(grade)}${count}张")
                    }
                },
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            if (canPrepare) {
                Text(
                    "选择一张后厨菜单牌舍弃（每回合最多舍弃1张）:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))

                val canAfford = player.funds >= 3
                if (!canAfford) {
                    Text("资金不足，无法执行备菜", color = MaterialTheme.colorScheme.error)
                }

                if (player.kitchen.isEmpty()) {
                    Text("后厨暂无菜单牌可移除", color = MaterialTheme.colorScheme.error)
                } else {
                    // 按品级展示后厨卡牌
                    MenuGrade.entries.forEach { grade ->
                        val cards = kitchenByGrade[grade] ?: return@forEach
                        if (cards.isNotEmpty()) {
                            Text(
                                "${gradeToChinese(grade)}(${cards.size}张):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            cards.forEach { card ->
                                Button(
                                    onClick = { onRemove(card) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = canAfford && canPrepare,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Text(
                                        "舍弃 ${gradeToChinese(card.grade)} ${card.name} (${card.baseIncome}收益) — 花费3两"
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            } else {
                Text(
                    "菜品牌总数恰好为6张，不可再移除",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (canPrepare) "跳过备菜，进入招待阶段" else "进入招待阶段")
            }
        }
    }
}

private fun gradeToChinese(grade: MenuGrade): String = when (grade) {
    MenuGrade.ONE -> "一品"
    MenuGrade.TWO -> "二品"
    MenuGrade.THREE -> "三品"
    MenuGrade.FOUR -> "四品"
}
