package com.dasong.commerce.ui.game.components.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasong.commerce.ui.theme.GoldHighlight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** 骰子 Unicode 字符: 1=⚀, 2=⚁, 3=⚂, 4=⚃, 5=⚄, 6=⚅ */
private fun diceFace(v: Int) = when (v) {
    1 -> "⚀"; 2 -> "⚁"; 3 -> "⚂"; 4 -> "⚃"; 5 -> "⚄"; 6 -> "⚅"
    else -> "?"
}

/**
 * 骰子投掷动画对话框。
 * 展示 N 个骰子，玩家点击每个骰子后播放滚动动画，
 * 全部投掷完毕后点击「确认」把结果回调给 ViewModel 继续结算。
 */
@Composable
fun DiceRollAnimationDialog(
    diceCount: Int,
    diceSources: List<String> = emptyList(),
    onRollComplete: (List<Int>) -> Unit
) {
    // -1 = 未投掷，1-6 = 已投出
    val results = remember(diceCount) {
        mutableStateListOf<Int>().apply { repeat(diceCount) { add(-1) } }
    }
    // 动画过程中显示的临时值
    val display = remember(diceCount) {
        mutableStateListOf<Int>().apply { repeat(diceCount) { add(1) } }
    }
    // 是否正在播放动画
    val animating = remember(diceCount) {
        mutableStateListOf<Boolean>().apply { repeat(diceCount) { add(false) } }
    }

    val scope = rememberCoroutineScope()
    val allRolled = results.isNotEmpty() && results.none { it < 0 }

    AlertDialog(
        onDismissRequest = { /* 不可点外部关闭 */ },
        title = {
            Text(
                "🎲 投掷骰子",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 骰子来源说明
                if (diceSources.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = GoldHighlight.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "骰子来源：",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            diceSources.forEach { source ->
                                Text(
                                    "· $source",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldHighlight,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Text(
                    if (diceCount == 1) "点击骰子进行投掷"
                    else "请依次点击骰子投掷（共${diceCount}个）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // 骰子排列：每行最多 3 个
                val perRow = minOf(3, diceCount)
                for (row in 0 until ((diceCount + perRow - 1) / perRow)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0 until perRow) {
                            val i = row * perRow + col
                            if (i >= diceCount) break

                            val valShown = if (results[i] > 0) results[i] else display[i]
                            val isAnim = animating[i]
                            val hasResult = results[i] > 0

                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (hasResult) GoldHighlight.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (hasResult || isAnim) 2.dp else 1.dp,
                                        color = when {
                                            hasResult -> GoldHighlight
                                            isAnim -> MaterialTheme.colorScheme.primary
                                            else -> Color.Gray
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(enabled = !isAnim && !hasResult) {
                                        scope.launch {
                                            animating[i] = true
                                            val finalVal = Random.nextInt(1, 7)
                                            // 滚动动画：0.8 秒内快速切换随机面
                                            val start = System.currentTimeMillis()
                                            while (System.currentTimeMillis() - start < 800) {
                                                display[i] = Random.nextInt(1, 7)
                                                delay(60)
                                            }
                                            display[i] = finalVal
                                            results[i] = finalVal
                                            animating[i] = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    diceFace(valShown),
                                    fontSize = 40.sp
                                )
                            }
                        }
                    }
                }

                // 全部投完后显示汇总
                if (allRolled) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = GoldHighlight.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "掷出: ${results.joinToString("  ") { diceFace(it) }}",
                                fontSize = 28.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "总和: ${results.sum()} 两",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldHighlight
                            )
                        }
                    }
                } else {
                    // 未全部投完时，提供一键投掷按钮
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            results.forEachIndexed { i, r ->
                                if (r < 0 && !animating[i]) {
                                    scope.launch {
                                        animating[i] = true
                                        val finalVal = Random.nextInt(1, 7)
                                        val start = System.currentTimeMillis()
                                        while (System.currentTimeMillis() - start < 800) {
                                            display[i] = Random.nextInt(1, 7)
                                            delay(60)
                                        }
                                        display[i] = finalVal
                                        results[i] = finalVal
                                        animating[i] = false
                                    }
                                }
                            }
                        },
                        enabled = results.any { it < 0 }
                    ) {
                        Text("🎲 全部投掷")
                    }
                }
            }
        },
        confirmButton = {
            if (allRolled) {
                Button(onClick = { onRollComplete(results.toList()) }) {
                    Text("确认，继续结算")
                }
            }
        }
    )
}
