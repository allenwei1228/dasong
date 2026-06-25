package com.dasong.commerce.ui.game.components.public

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.MenuPool
import com.dasong.commerce.model.card.MenuGrade
import com.dasong.commerce.ui.theme.*

@Composable
fun MenuCardPool(pool: MenuPool) {
    Column {
        Text("菜单牌", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MenuPile(MenuGrade.ONE,"一品", MenuGradeOneColor, pool.gradeOne.size, 9)
            MenuPile(MenuGrade.TWO,"二品", MenuGradeTwoColor, pool.gradeTwo.size, 6)
            MenuPile(MenuGrade.THREE,"三品", MenuGradeThreeColor, pool.gradeThree.size, 3)
            MenuPile(MenuGrade.FOUR, "四品", MenuGradeFourColor, pool.gradeFour.size, 0)
        }
    }
}

@Composable
private fun MenuPile(
    grade: MenuGrade,
    label: String,
    color: Color,
    count: Int,
    price: Int
) {
    var menuDetail by remember { mutableStateOf<MenuGrade?>(null) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(4.dp)
            .clickable {
                menuDetail = grade
            }
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text("${count}张", style = MaterialTheme.typography.labelSmall)
        Text("售价${price}两", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }

    menuDetail?.let { menu ->
        MenuDetailDialog(
            menuGrade = grade,
            onDismiss = { menuDetail = null }
        )
    }
}

@Composable
fun MenuDetailDialog(menuGrade: MenuGrade, onDismiss: () -> Unit) {
    val menuBaseIncome = when (menuGrade) {
        MenuGrade.ONE -> "基础收益：2\n 额外收益: 骰子机制 - 收入由骰子决定"
        MenuGrade.TWO -> "基础收益：3"
        MenuGrade.THREE -> "基础收益：2"
        else -> "基础收益：1"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(menuBaseIncome, style = MaterialTheme.typography.titleMedium)
        },

        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}