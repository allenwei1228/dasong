package com.dasong.commerce.ui.guide.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RuleDetailSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("胜利条件", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("1. 自建8间带房屋模型的店铺\n2. 手头流动资金 ≥ 50两")
        Spacer(Modifier.height(16.dp))

        Text("回合阶段", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("阶段1·购买：买1张菜单牌 或 买1张店铺牌\n" +
                "阶段2·备菜：花3两移除1张后厨菜单（总牌数>6时可用）\n" +
                "阶段3·招待：选客人 → 结算菜单收入 → 结算店铺收入 → 刷新队列")
        Spacer(Modifier.height(16.dp))

        Text("初始资金", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("顺位1: 5两 | 顺位2: 6两 | 顺位3: 7两 | 顺位4: 8两")
        Spacer(Modifier.height(16.dp))

        Text("地基清理费用", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("#1免费 | #2: 2两 | #3: 3两 | #4: 4两 | #5: 5两 | #6: 6两 | #7: 7两 | #8: 9两")
    }
}
