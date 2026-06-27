package com.dasong.commerce.ui.game.components.public

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.ShopPool
import com.dasong.commerce.model.card.ShopCard
import com.dasong.commerce.ui.game.components.phase.ShopDetailDialog

@Composable
fun ShopCardPool(shopPool: ShopPool) {
    var showShopDetail by remember { mutableStateOf<ShopCard?>(null) }
    // 店铺详情弹窗
    showShopDetail?.let { shop ->
        ShopDetailDialog(
            shop = shop,
            onDismiss = { showShopDetail = null }
        )
    }
    Column {
        Text("店铺选购区", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            shopPool.available.forEach { shop ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clickable { showShopDetail = shop }
                        .width(64.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.tertiary,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp)
                ) {
                    Row {
                        Text(
                            shop.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            shop.type.emoji,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    Text(
                        "售价${shop.buildCost}两",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
