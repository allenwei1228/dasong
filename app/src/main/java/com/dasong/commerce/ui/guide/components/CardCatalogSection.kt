package com.dasong.commerce.ui.guide.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.CardDataProvider
import com.dasong.commerce.model.card.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardCatalogSection() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("菜品", "客人", "商铺", "事件")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> MenuCardCatalog()
            1 -> GuestCardCatalog()
            2 -> ShopCardCatalog()
            3 -> EventCardCatalog()
        }
    }
}

@Composable
private fun MenuCardCatalog() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text("一品 · 御宴大餐 (9铜钱, 2+🎲)", fontWeight = FontWeight.Bold)
            Text("🦑火爆鱿鱼、🦐金丝虾球、🦀蟹酿橙、🦈鱼翅捞饭", style = MaterialTheme.typography.bodySmall)
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text("二品 · 酒楼硬菜 (6铜钱, 3固定)", fontWeight = FontWeight.Bold)
            Text("🍖葱爆羊肉、🥓椰子鸡、🦆北京烤鸭、🐠松鼠鳜鱼", style = MaterialTheme.typography.bodySmall)
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text("三品 · 家常菜 (3铜钱, 2固定)", fontWeight = FontWeight.Bold)
            Text("🥬炒青菜、🍗葱油鸡、🐟红烧鱼、🥣豆腐羹、🥩酱牛肉", style = MaterialTheme.typography.bodySmall)
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Text("四品 · 低价小吃 (免费, 2固定)", fontWeight = FontWeight.Bold)
            Text("🫓炊饼、🥮月饼、🥟饺子、🍜汤面", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun GuestCardCatalog() {
    val guests = CardDataProvider.guestCards
    val distinctGuests = guests.distinctBy { it.name }.sortedBy { it.menuConsumption }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(distinctGuests) { guest ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(guest.name, fontWeight = FontWeight.Bold)
                        Text("菜单:${guest.menuConsumption}张", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "光顾: ${guest.shopTypes.joinToString(" · ") { it.displayName }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopCardCatalog() {
    val shops = CardDataProvider.shopCards.distinctBy { it.name }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(shops.sortedBy { it.buildCost }) { shop ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(shop.name, fontWeight = FontWeight.Bold)
                        Text("建造:${shop.buildCost}两", style = MaterialTheme.typography.bodySmall)
                    }
                    val incomeDesc = when (shop.incomeType) {
                        IncomeType.FIXED -> "固定收入:${shop.baseIncome}两"
                        IncomeType.DICE -> "骰子收入"
                        IncomeType.MENU_BONUS -> "菜单+${shop.menuBonus}"
                        IncomeType.HOUSING_COUNT -> "房屋数收益"
                    }
                    Text(incomeDesc, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun EventCardCatalog() {
    val events = CardDataProvider.eventCards

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(events) { event ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (event.type) {
                        EventType.POSITIVE -> MaterialTheme.colorScheme.primaryContainer
                        EventType.NEGATIVE -> MaterialTheme.colorScheme.secondaryContainer
                        EventType.RESHUFFLE -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(event.name, fontWeight = FontWeight.Bold)
                        Text(
                            when (event.type) {
                                EventType.POSITIVE -> "🟢 正面"
                                EventType.NEGATIVE -> "🔴 负面"
                                EventType.RESHUFFLE -> "🔵 刷新"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(event.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
