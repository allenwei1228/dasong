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
            MenuPile("一品", MenuGradeOneColor, pool.gradeOne.size, 9)
            MenuPile("二品", MenuGradeTwoColor, pool.gradeTwo.size, 6)
            MenuPile("三品", MenuGradeThreeColor, pool.gradeThree.size, 3)
            MenuPile("四品", MenuGradeFourColor, pool.gradeFour.size, 0)
        }
    }
}

@Composable
private fun MenuPile(
    label: String,
    color: Color,
    count: Int,
    price: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text("${count}张", style = MaterialTheme.typography.labelSmall)
        Text("价${price}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
