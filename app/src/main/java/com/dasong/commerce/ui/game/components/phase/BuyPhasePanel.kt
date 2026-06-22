package com.dasong.commerce.ui.game.components.phase

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.MenuPool
import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.ShopPool
import com.dasong.commerce.model.card.IncomeType
import com.dasong.commerce.model.card.MenuCard
import com.dasong.commerce.model.card.MenuGrade
import com.dasong.commerce.model.card.ShopCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuyPhasePanel(
    player: PlayerState,
    menuPool: MenuPool,
    shopPool: ShopPool,
    onBuyMenu: (MenuCard) -> Unit,
    onBuyShop: (ShopCard, Int) -> Unit,
    onEndPhase: () -> Unit
) {
    var selectedMode by remember { mutableStateOf<String?>(null) } // "menu" or "shop"
    var selectedFoundation by remember { mutableIntStateOf(-1) }
    var showShopDetail by remember { mutableStateOf<ShopCard?>(null) }

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
                "购买阶段 - 请选择购买类型",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            // Option A: Buy Menu
            Button(
                onClick = { selectedMode = "menu"; selectedFoundation = -1 },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == "menu")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text("购买菜单牌", color = Color.Black)
            }

            if (selectedMode == "menu") {
                Spacer(Modifier.height(8.dp))
                MenuGrade.entries.forEach { grade ->
                    val pile = menuPool.getPile(grade)
                    val cost = when (grade) {
                        MenuGrade.ONE -> 9
                        MenuGrade.TWO -> 6
                        MenuGrade.THREE -> 3
                        MenuGrade.FOUR -> 0
                    }
                    if (pile.isNotEmpty()) {
                        val card = pile.first()
                        val canAfford = player.funds >= cost
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${grade.name}·${card.name} (${pile.size}张)")
                            Button(
                                onClick = { onBuyMenu(card) },
                                enabled = canAfford
                            ) {
                                Text("买 $cost 两")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Option B: Buy Shop
            Button(
                onClick = { selectedMode = "shop"; selectedFoundation = -1 },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == "shop")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text("购买店铺牌", color = Color.Black)
            }

            if (selectedMode == "shop") {
                Spacer(Modifier.height(8.dp))

                // Select foundation first - only show foundations without shops
                val availableFoundations = player.foundations.filter { it.shopCard == null }
                if (availableFoundations.isEmpty()) {
                    Text("所有地基已建店铺，无法再购买", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("选择地基:", fontWeight = FontWeight.Bold)
                    // Fix Bug 1: 使用 FlowRow 支持换行，展示所有地基
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableFoundations.forEach { foundation ->
                            val isSelected = selectedFoundation == foundation.index
                            val cost = foundation.clearCost
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFoundation = foundation.index },
                                label = {
                                    Text(
                                        "#${foundation.index + 1} 清理${cost}两",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }

                if (selectedFoundation >= 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("选择店铺:", fontWeight = FontWeight.Bold)
                    shopPool.available.forEach { shop ->
                        val totalCost = player.foundations[selectedFoundation].clearCost + shop.buildCost
                        val canAfford = player.funds >= totalCost
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Fix Bug 3 & 4: 店铺名蓝色可点击弹出效果弹窗，支持换行，不展示括号类型
                            Text(
                                text = shop.name,
                                color = Color.Blue,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showShopDetail = shop },
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "总费用${totalCost}两",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Button(
                                onClick = {
                                    // Fix Bug 2: 建造后重置 selectedFoundation，允许继续建造其他地基
                                    onBuyShop(shop, selectedFoundation)
                                    selectedFoundation = -1
                                },
                                enabled = canAfford
                            ) {
                                Text("建造")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Skip to next phase (Prepare Phase)
            Button(
                onClick = onEndPhase,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("跳过购买，进入备菜阶段")
            }
        }
    }

    // Fix Bug 3: 店铺详情弹窗
    showShopDetail?.let { shop ->
        ShopDetailDialog(
            shop = shop,
            onDismiss = { showShopDetail = null }
        )
    }
}

// Fix Bug 3: 店铺详情弹窗
@Composable
private fun ShopDetailDialog(shop: ShopCard, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(shop.name, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column {
                Text("类型: ${shop.type.displayName}")
                Spacer(Modifier.height(4.dp))
                Text("建造费用: ${shop.buildCost}两")
                Spacer(Modifier.height(4.dp))
                Text("基础收入: ${shop.baseIncome}两")
                Spacer(Modifier.height(4.dp))
                Text("收入类型: ${getIncomeTypeDesc(shop.incomeType)}")
                if (shop.hasDiceMechanic) {
                    Spacer(Modifier.height(4.dp))
                    Text("特殊效果: 骰子机制 - 收入由骰子决定", color = MaterialTheme.colorScheme.primary)
                }
                if (shop.hasMenuBonus) {
                    Spacer(Modifier.height(4.dp))
                    Text("特殊效果: 菜单加成 +${shop.menuBonus}两/每个符合的菜单", color = MaterialTheme.colorScheme.primary)
                }
                if (shop.hasHousingBonus) {
                    Spacer(Modifier.height(4.dp))
                    Text("特殊效果: 住房加成 - 根据住房数量获得额外收入", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

private fun getIncomeTypeDesc(incomeType: IncomeType): String = when (incomeType) {
    IncomeType.FIXED -> "固定收入"
    IncomeType.DICE -> "骰子收入"
    IncomeType.MENU_BONUS -> "菜单加成"
    IncomeType.HOUSING_COUNT -> "住房数量加成"
}
