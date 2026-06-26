package com.dasong.commerce.ui.game.components.phase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dasong.commerce.ui.game.components.public.MenuDetailDialog
import com.dasong.commerce.ui.theme.FoundationEmpty
import com.dasong.commerce.ui.theme.GoldHighlight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuyPhasePanel(
    player: PlayerState,
    menuPool: MenuPool,
    shopPool: ShopPool,
    menuBoughtThisTurn: Boolean = false,
    shopPlacedThisTurn: Boolean = false,
    onBuyMenu: (MenuCard) -> Unit,
    onPlaceShop: (ShopCard, Int) -> Unit,
    onBuildHouse: (Int) -> Unit,
    onEndPhase: () -> Unit
) {
    var selectedMode by remember { mutableStateOf<String?>(null) } // "menu" or "shop"
    var selectedFoundation by remember { mutableIntStateOf(-1) }
    var showShopDetail by remember { mutableStateOf<ShopCard?>(null) }
    var menuDetail by remember { mutableStateOf<MenuCard?>(null) }

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

            // Option A: Buy Menu (互斥：买菜单后不能再买店铺，反之亦然)
            val canBuyMenu = !menuBoughtThisTurn && !shopPlacedThisTurn
            Button(
                onClick = { selectedMode = "menu"; selectedFoundation = -1 },
                modifier = Modifier.fillMaxWidth(),
                enabled = canBuyMenu,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == "menu" && canBuyMenu)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    when {
                        menuBoughtThisTurn -> "购买菜单牌 (本回合已购买)"
                        shopPlacedThisTurn -> "购买菜单牌 (已放置店铺，不可购买)"
                        else -> "购买菜单牌"
                    },
                    color = Color.Black
                )
            }

            if (selectedMode == "menu" && canBuyMenu) {
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
                                .padding(vertical = 2.dp)
                                .clickable { menuDetail = card },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${card.cardGrade(card.grade)}·${card.name} (${pile.size}张)",
                                modifier = Modifier.weight(1f, fill = false),
                                color = Color.Blue)
                            Button(
                                onClick = { onBuyMenu(card) },
                                enabled = canAfford
                            ) {
                                Text("买${cost}两")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Option B: Place Shop (互斥：放置店铺后不能再买菜单，反之亦然)
            val canPlaceShop = !menuBoughtThisTurn && !shopPlacedThisTurn
            Button(
                onClick = { selectedMode = "shop"; selectedFoundation = -1 },
                modifier = Modifier.fillMaxWidth(),
                enabled = canPlaceShop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == "shop" && canPlaceShop)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    when {
                        shopPlacedThisTurn -> "购买店铺牌 (本回合已放置)"
                        menuBoughtThisTurn -> "购买店铺牌 (已购买菜单，不可放置)"
                        else -> "购买店铺牌"
                    },
                    color = Color.Black
                )
            }

            if (selectedMode == "shop" && canPlaceShop) {
                Spacer(Modifier.height(8.dp))
                Text("第一步：放置店铺牌（仅支付清理地基费用，一回合一次）", fontWeight = FontWeight.Bold)

                // Select foundation first - show all foundations, disable those with shops
                val availableFoundations = player.foundations.filter { it.shopCard == null }
                if (availableFoundations.isEmpty()) {
                    Text("所有地基已放置店铺", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("选择地基:", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    // 与 PlayerPanel 中 FoundationSlots 风格一致的地基选择
                    Column {
                        for (row in 0..1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0..3) {
                                    val gridIndex = row * 4 + col
                                    val foundation = player.foundations.getOrNull(gridIndex)
                                    if (foundation == null) continue
                                    val hasShop = foundation.shopCard != null
                                    val isSelected = selectedFoundation == foundation.index
                                    val isAvailable = !hasShop
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(68.dp)
                                            .background(
                                                if (isSelected) GoldHighlight.copy(alpha = 0.4f)
                                                else FoundationEmpty,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) GoldHighlight else Color.Gray,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(enabled = isAvailable) { selectedFoundation = foundation.index },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "#${foundation.index + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (hasShop) {
                                                Text(
                                                    "已占用",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            } else {
                                                Text(
                                                    "清理${foundation.clearCost}两",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedFoundation >= 0) {
                    Spacer(Modifier.height(8.dp))
                    Text("选择店铺:", fontWeight = FontWeight.Bold)
                    shopPool.available.forEach { shop ->
                        val clearCost = player.foundations[selectedFoundation].clearCost
                        val canAfford = player.funds >= clearCost
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                text = "清理费${clearCost}两 + 建房${shop.buildCost}两",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Button(
                                onClick = {
                                    onPlaceShop(shop, selectedFoundation)
                                    selectedFoundation = -1
                                },
                                enabled = canAfford
                            ) {
                                Text("放置")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Option C: Build House (Step 2 - 支付店铺价格购买房屋，不限次数)
            val placedUnbuiltShops = player.foundations.filter { it.shopCard != null && !it.isBuilt }
            if (placedUnbuiltShops.isEmpty()) {
                // 无可建造房屋时，若已购买过菜单或店铺，直接提示进入下一阶段
                if (menuBoughtThisTurn || shopPlacedThisTurn) {
                    Text("无可建造的房屋，请进入下一阶段", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                Text("购买店铺房屋（支付店铺价格，不限次数，购买后效果生效）", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                placedUnbuiltShops.forEach { foundation ->
                    val shop = foundation.shopCard!!
                    val canAfford = player.funds >= shop.buildCost
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${shop.name} (#${foundation.index + 1}号地)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "建房${shop.buildCost}两",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Button(
                            onClick = { onBuildHouse(foundation.index) },
                            enabled = canAfford
                        ) {
                            Text("建房")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

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
    // 菜品详情弹窗
    menuDetail?.let { menu ->
        MenuDetailDialog(
            menuGrade = menu.grade,
            onDismiss = { menuDetail = null }
        )
    }
    // 店铺详情弹窗
    showShopDetail?.let { shop ->
        ShopDetailDialog(
            shop = shop,
            onDismiss = { showShopDetail = null }
        )
    }
}

// Fix Bug 3: 店铺详情弹窗
@Composable
fun ShopDetailDialog(shop: ShopCard, onDismiss: () -> Unit) {
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
