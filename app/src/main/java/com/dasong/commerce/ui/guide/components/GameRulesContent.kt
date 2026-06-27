package com.dasong.commerce.ui.guide.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 游戏规则完整说明，内容对齐 CardDataProvider / CardEnums。
 */
@Composable
fun GameRulesContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==================== 一、基础信息 ====================
        SectionHeader("一、基础信息")
        SimpleTable(
            headers = listOf("项目", "内容"),
            rows = listOf(
                listOf("人数", "2–4 人"),
                listOf("年龄", "8+"),
                listOf("单局时长", "45–60 分钟"),
                listOf("核心机制", "卡牌构筑 + 店铺经营 + 客人招揽 + 事件随机"),
            )
        )

        SubHeader("胜利条件")
        BodyText("同时达成以下两点，立刻获胜：")
        NumberedItem("1", "自建 8 间带房屋模型的店铺")
        NumberedItem("2", "手头流动资金 ≥ 50 两")

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ==================== 二、开局准备 ====================
        SectionHeader("二、开局准备")

        SubHeader("1. 公共版图摆放")
        NumberedItem("1", "菜单牌（76张）：按品级分4堆（一品/二品/三品/四品）正面摆放，每堆顶部翻开若干可购买")
        NumberedItem("2", "店铺牌（36张）：洗堆，翻4张作为公开选购区")
        NumberedItem("3", "客人牌（72张）+ 事件牌（9张）：混洗为总牌堆，翻4张排成客人队列；若翻出事件牌，塞回重翻")
        NumberedItem("4", "铜钱、5两/10两交子、骰子、房屋模型全部作为银行公用资金")

        SubHeader("2. 每位玩家个人配置")
        NumberedItem("1", "拿个人玩家面板：雅阁（手牌库）、后厨（弃牌堆）、8块地基空位")
        NumberedItem("2", "初始手牌：6张四品菜单 + 2张三品菜单，洗背面朝上放入【雅阁】")
        NumberedItem("3", "起始资金：第1位5两、第2位6两、第3位7两、第4位8两")
        NumberedItem("4", "人手1张提示卡")

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ==================== 三、单人完整回合 ====================
        SectionHeader("三、单人完整回合（固定3阶段）")
        CodeBlock("阶段1：购买阶段 → 阶段2：备菜阶段 → 阶段3：招待阶段")

        // 阶段1
        SubHeader("阶段1：购买阶段（二选一）")
        BodyText("二选一，只能选一类买1张；可额外不限量买房屋模型。", color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionHeader("选项A：购买1张菜单牌")
        NumberedItem("1", "支付牌右上角价格：四品免费、三品3铜钱、二品6铜钱、一品9铜钱")
        NumberedItem("2", "新买菜单放入自己【后厨】，雅阁打空后，后厨整堆洗回雅阁循环使用")

        SectionHeader("选项B：购买1张店铺牌")
        NumberedItem("1", "先选空地基，支付地基清理费")

        SimpleTable(
            headers = listOf("地基序号", "1", "2", "3", "4", "5", "6", "7", "8"),
            rows = listOf(listOf("清理费", "免费", "2两", "3两", "6两", "7两", "8两", "9两", "10两"))
        )
        NumberedItem("2", "拿公开区店铺牌放在地基上（仅卡牌无模型 = 未开业，无收益）")
        NumberedItem("3", "支付店铺建造费，放上房屋模型，店铺正式生效")

        // 阶段2
        Spacer(Modifier.height(4.dp))
        SubHeader("阶段2：备菜阶段（可选）")
        BodyText("花费3两银子，从后厨永久移除1张菜单牌（精简低收益牌）。限制：菜品牌总数（雅阁+后厨）至少保持6张，恰好6张时必须跳过。")

        // 阶段3
        Spacer(Modifier.height(4.dp))
        SubHeader("阶段3：招待阶段（必做，整局唯一赚钱环节）")

        SectionHeader("① 选客人规则")
        BodyText("客人队列左->右顺位，最左侧是1号。每回合只能招待1位。")
        SimpleTable(
            headers = listOf("选择目标", "所需费用"),
            rows = listOf(
                listOf("第1位（最左侧）", "免费"),
                listOf("第2位", "给第1位压1两"),
                listOf("第3位", "给第1、2位各压1两"),
                listOf("第4位", "给第1、2、3位各压1两"),
            )
        )

        SectionHeader("② 结算3项收入（总和为本回合赚银）")
        BulletItem("A. 菜单菜品收入：从雅阁翻牌凑够对应道菜数，每张菜单左下角数字相加。用过菜单送后厨。雅阁不够时后厨洗回")
        BulletItem("B. 店铺加成收入：客人下方带店铺图标才触发，带房屋模型同类型店铺全部结算（基础+联动）")
        BulletItem("C. 小费：选客人时压在前面客人身上的铜钱")

        SectionHeader("③ 结算顺序（固定）")
        CodeBlock("① 小费 → ② 菜单牌总收益 → ③ 店铺基础收益 + 联动加成")

        BodyText("招待完的客人弃置，从公共牌堆补1张到队列末尾保持4人。翻到事件牌立刻执行效果再补客人。", color = MaterialTheme.colorScheme.onSurfaceVariant)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ==================== 四、四大卡牌全详解 ====================
        SectionHeader("四、四大卡牌全详解")

        // ---- 1. 菜单牌 ----
        SectionHeader("1. 菜单牌（决定上菜收益）")

        SubHeader("一、菜单牌基础分级")
        BodyText("所有菜单牌两个核心数值：右上角 = 购买成本，左上角 = 单张菜品基础收益（一品带骰子额外收益）。")

        SimpleTable(
            headers = listOf("品级", "购买成本", "单张收益", "特殊效果", "开局发放"),
            rows = listOf(
                listOf("四品", "免费", "2 铜钱", "无额外效果", "每人6张"),
                listOf("三品", "3 铜钱", "2 铜钱", "无额外效果", "每人2张"),
                listOf("二品", "6 铜钱", "3 铜钱", "无额外效果", "公共区购买"),
                listOf("一品", "9 铜钱", "2铜钱 + 骰子", "骰子点数全额叠加", "公共区购买"),
            )
        )

        SubHeader("二、菜单收益结算流程")
        BulletItem("1. 看客人牌右上角数字（需消耗N张菜单），从雅阁牌库顶部连续翻N张")
        BulletItem("2. 累加所有翻出菜单收益：四品2、三品2、二品3、一品2+骰子")
        BulletItem("3. 结算完菜单后再结算店铺基础+联动")
        BulletItem("4. 用过菜单全部丢入后厨弃牌堆")

        SubHeader("三、菜单构筑策略")
        BulletItem("速铺店铺流：全四品免费菜，低成本快速攒钱盖店")
        BulletItem("酒楼爆发流：多买一品/二品，搭配酒肆+蹴鞠场，单轮菜单收益碾压对手")

        SubHeader("四、补充规则")
        NumberedItem("1", "菜单收益独立计算，和店铺收益分开，先算菜钱再算店铺钱")
        NumberedItem("2", "只有客人牌要求光顾酒肆/蹴鞠场，才触发菜单数量加成；客人不逛则不生效")
        NumberedItem("3", "一品骰子收益属于菜单收益，不参与店铺联动加成计算")

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ---- 2. 店铺牌 ----
        SectionHeader("2. 店铺牌（12类商铺，共36张）")
        BodyText("必须建造房屋模型（支付建造费）后店铺效果才生效。客人光顾对应店铺时结算收益，联动效果全场叠加。", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(8.dp))

        // ===== 一、高收益特色店铺 =====
        SubHeader("一、高收益特色店铺（特殊结算机制）")

        ShopCard(
            name = "1. 卦肆 ☯️",
            cost = "6",
            incomeType = "类型：骰子收益",
            baseIncome = "投1枚骰子，获得骰子点数铜钱",
            synergy = "关扑铺联动 → 每间卦肆 +2 铜钱"
        )
        ShopCard(
            name = "2. 书坊 📖",
            cost = "6",
            incomeType = "类型：固定收益",
            baseIncome = "固定 +3 铜钱",
            synergy = "茶馆联动 → 每间书坊 +2 铜钱"
        )
        ShopCard(
            name = "3. 酒肆 🍾",
            cost = "6",
            incomeType = "类型：菜单加成（+1）",
            baseIncome = "本回合客人消耗菜单数量 +1",
            synergy = "说书场联动 → 每间酒肆 +1 铜钱"
        )
        ShopCard(
            name = "4. 绸缎庄 🧵",
            cost = "7",
            incomeType = "类型：固定收益",
            baseIncome = "固定 +4 铜钱",
            synergy = "首饰铺联动 → 每间绸缎庄 +1 铜钱"
        )
        ShopCard(
            name = "5. 勾栏瓦肆 🪕",
            cost = "8",
            incomeType = "类型：房屋数量收益",
            baseIncome = "当前持有的全部房屋模型数量",
            synergy = "无直接联动加成，靠多铺放大收益",
            note = "建店越多收益越高，后期爆发力极强"
        )
        ShopCard(
            name = "6. 瓷器铺 🏺",
            cost = "9",
            incomeType = "类型：固定收益",
            baseIncome = "固定 +6 铜钱（全游戏单店基础收益最高）",
            synergy = "首饰铺联动 → 每间瓷器铺 +1 铜钱"
        )
        ShopCard(
            name = "7. 蹴鞠场 ⚽️",
            cost = "6",
            incomeType = "类型：菜单加成（+2）",
            baseIncome = "客人光顾时，该客人消耗的菜单数量 +2",
            synergy = "无联动加成，配套酒楼高收益打法",
            note = "拉高菜单总收益，搭配酒肆效果更佳"
        )

        Spacer(Modifier.height(8.dp))
        SubHeader("二、基础市井铺（固定2铜钱，强连锁联动）")

        ShopCard(
            name = "8. 饮子铺 🍵",
            cost = "5",
            incomeType = "类型：固定收益（2铜钱）",
            baseIncome = "固定 +2 铜钱",
            synergy = "基础联动：客人光顾关扑铺时，每间饮子铺给每间关扑铺+2铜钱",
            extraSynergy = "额外加成：说书场联动 → 每间饮子铺 +1 铜钱"
        )
        ShopCard(
            name = "9. 关扑铺 💿",
            cost = "5",
            incomeType = "类型：固定收益（2铜钱）",
            baseIncome = "固定 +2 铜钱",
            synergy = "基础联动：客人光顾卦肆时，每间关扑铺给每间卦肆+2铜钱",
            extraSynergy = "额外加成：饮子铺联动放大关扑铺收益"
        )
        ShopCard(
            name = "10. 茶馆 🫖",
            cost = "5",
            incomeType = "类型：固定收益（2铜钱）",
            baseIncome = "固定 +2 铜钱",
            synergy = "基础联动：客人光顾书坊时，每间茶馆给每间书坊+2铜钱",
            extraSynergy = "额外加成：说书场联动 → 每间茶馆 +1 铜钱"
        )
        ShopCard(
            name = "11. 说书场 🪭",
            cost = "5",
            incomeType = "类型：固定收益（2铜钱）",
            baseIncome = "固定 +2 铜钱",
            synergy = "核心联动（覆盖茶馆/酒肆/饮子铺）：客人光顾时上述每间店铺+1铜钱",
            extraSynergy = "多间叠加：多间说书场可叠加加成"
        )
        ShopCard(
            name = "12. 首饰铺 💍",
            cost = "5",
            incomeType = "类型：固定收益（2铜钱）",
            baseIncome = "固定 +2 铜钱",
            synergy = "核心联动（覆盖说书场/绸缎庄/瓷器铺）：客人光顾时上述每间店铺+1铜钱",
            extraSynergy = "多间叠加：多间首饰铺可叠加加成"
        )

        Spacer(Modifier.height(8.dp))

        // ===== 联动结算核心规则 =====
        SubHeader("三、联动结算核心规则")
        BodyText("联动效果不在本店被逛时触发，而是其他关联店铺被客人光顾时自动叠加收益。同类型店铺可重复建造，全部享受加成。")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("示例：", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("2间饮子铺 + 1间说书场，任意饮子铺被客人光顾：")
                Text("• 每间饮子铺 = 基础2 + 说书场+1 = 3 铜钱")
                Text("• 两间合计 = 6 铜钱")
            }
        }

        SectionHeader("联动关系速查")
        CodeBlock(
            "关扑铺 ──(+2/间)──→ 卦肆\n" +
            "饮子铺 ──(+2/间)──→ 关扑铺\n" +
            "茶馆   ──(+2/间)──→ 书坊\n" +
            "说书场 ──(+1/间)──→ 茶馆、酒肆、饮子铺\n" +
            "首饰铺 ──(+1/间)──→ 说书场、绸缎庄、瓷器铺\n\n" +
            "特殊结算店铺（不受联动加成影响）：\n" +
            "卦肆(骰子)、酒肆(菜单+1)、勾栏瓦肆(房屋数)、蹴鞠场(菜单+2)"
        )

        // 店铺速查表（对齐CardDataProvider）
        SectionHeader("店铺费用与收益速查表")
        SimpleTable(
            headers = listOf("#", "店铺", "建费", "收入类型", "基础收益", "联动来源", "联动加成"),
            rows = listOf(
                listOf("1", "卦肆", "6", "骰子", "🎲骰子点数", "关扑铺", "+2/间"),
                listOf("2", "书坊", "6", "固定", "3", "茶馆", "+2/间"),
                listOf("3", "酒肆", "6", "菜单+1", "菜单数+1", "说书场", "+1/间"),
                listOf("4", "绸缎庄", "7", "固定", "4", "首饰铺", "+1/间"),
                listOf("5", "勾栏瓦肆", "8", "房屋数", "房屋模型数", "—", "—"),
                listOf("6", "瓷器铺", "9", "固定", "6", "首饰铺", "+1/间"),
                listOf("7", "蹴鞠场", "6", "菜单+2", "菜单数+2", "—", "—"),
                listOf("8", "饮子铺", "5", "固定", "2", "说书场", "+1/间"),
                listOf("9", "关扑铺", "5", "固定", "2", "饮子铺", "+2/间"),
                listOf("10", "茶馆", "5", "固定", "2", "说书场", "+1/间"),
                listOf("11", "说书场", "5", "固定", "2", "首饰铺", "+1/间"),
                listOf("12", "首饰铺", "5", "固定", "2", "—", "—"),
            )
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ---- 3. 客人牌 ----
        SectionHeader("3. 客人牌（收益触发器，81张）")
        BodyText("右上角数字 = 需消耗菜单张数；下方图标 = 可触发店铺类型组合。每种客人各3张。", color = MaterialTheme.colorScheme.onSurfaceVariant)

        SubHeader("需消耗 2 张菜单（6位 × 3 = 18张）")
        SimpleTable(
            headers = listOf("客人", "店铺组合", "菜单数"),
            rows = listOf(
                listOf("货郎", "饮子铺 + 关扑铺", "2"),
                listOf("秀才", "茶馆 + 书坊", "2"),
                listOf("绸缎商", "绸缎庄 + 首饰铺", "2"),
                listOf("瓷商", "瓷器铺 + 首饰铺", "2"),
                listOf("酒馆客人", "酒肆 + 说书场", "2"),
                listOf("游人", "蹴鞠场 + 酒肆", "2"),
            )
        )

        SubHeader("需消耗 3 张菜单（11位 × 3 = 33张）")
        SimpleTable(
            headers = listOf("客人", "店铺组合", "菜单数"),
            rows = listOf(
                listOf("走贩", "饮子铺 + 说书场", "3"),
                listOf("茶客", "茶馆 + 说书场", "3"),
                listOf("算卦客人", "卦肆 + 关扑铺", "3"),
                listOf("乡绅", "饮子铺+关扑铺+说书场", "3"),
                listOf("文人雅士", "茶馆+书坊+说书场", "3"),
                listOf("酒楼宾客", "酒肆+饮子铺+说书场", "3"),
                listOf("贵妇人", "绸缎庄+瓷器铺+首饰铺", "3"),
                listOf("纨绔子弟", "酒肆+蹴鞠场+说书场", "3"),
                listOf("街巷百姓", "饮子铺+茶馆+酒肆", "3"),
                listOf("庙会游人", "卦肆+关扑铺+勾栏瓦肆", "3"),
                listOf("富商", "绸缎庄+首饰铺+勾栏瓦肆", "3"),
            )
        )

        SubHeader("需消耗 4 张菜单（10位 × 3 = 30张）")
        SimpleTable(
            headers = listOf("客人", "店铺组合", "菜单数"),
            rows = listOf(
                listOf("庙会大户", "酒肆+蹴鞠场+勾栏瓦肆", "4"),
                listOf("文坛大家", "茶馆+书坊+绸缎庄", "4"),
                listOf("珠宝商人", "瓷器铺+首饰铺+绸缎庄", "4"),
                listOf("街头看客", "饮子铺+关扑铺+卦肆", "4"),
                listOf("汴京游人", "饮子铺+茶馆+酒肆+说书场", "4"),
                listOf("豪门贵妇", "绸缎庄+瓷器铺+首饰铺+勾栏瓦肆", "4"),
                listOf("节庆宾客", "酒肆+蹴鞠场+饮子铺+说书场", "4"),
                listOf("赶集百姓", "饮子铺+关扑铺+茶馆+卦肆", "4"),
                listOf("达官贵人", "绸缎庄+首饰铺+瓷器铺+酒肆", "4"),
                listOf("市井闲客", "说书场+勾栏瓦肆+蹴鞠场+卦肆", "4"),
            )
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ---- 4. 事件牌 ----
        SectionHeader("4. 事件牌（全局随机，共9张）")
        BodyText("混在客人堆里，翻到立刻执行。若执行中翻到新事件牌，中断当前效果更新为新事件。", color = MaterialTheme.colorScheme.onSurfaceVariant)

        SubHeader("持续事件（生效后一直持续，直到被新事件覆盖）")
        EventCard("1. 俭以养德", "负面", "持续", "菜单消耗数 -1：所有客人结算时消费菜单数量减少1张")
        EventCard("2. 门可罗雀", "负面", "持续", "仅选1店铺结算：玩家只可选择1张店铺牌结算收益")
        EventCard("3. 无尖不商", "负面", "持续", "店铺收入-1：所有店铺获得收入时减少1铜钱")
        EventCard("4. 时和年丰", "正面", "持续", "菜单消耗数 +1：所有客人结算时消费菜单数量增加1张")
        EventCard("5. 张灯结彩", "正面", "持续", "客人队列扩为6张（更多选择，更高效率）")
        EventCard("6. 银装素裹", "负面", "持续", "弃置最右2张客人，保持2张客人队列（选择变少）")

        SubHeader("即时事件（效果立即触发，触发后丢弃）")
        EventCard("7. 辞旧迎新", "刷新", "即时", "洗牌，重新翻开4张客人牌")
        EventCard("8. 苛捐杂税", "负面", "即时", "每有1个店铺模型支付2铜钱（建店越多罚款越重）")
        EventCard("9. 硕果累累", "正面", "即时", "所有玩家从菜单供应堆拿取2张四品菜单牌")

        // 事件牌速查表
        SectionHeader("事件牌速查表")
        SimpleTable(
            headers = listOf("#", "事件名", "类型", "持续/即时", "效果摘要"),
            rows = listOf(
                listOf("1", "俭以养德", "🔴负面", "持续", "菜单消耗数-1"),
                listOf("2", "门可罗雀", "🔴负面", "持续", "仅选1店铺结算"),
                listOf("3", "无尖不商", "🔴负面", "持续", "店铺收入-1"),
                listOf("4", "时和年丰", "🟢正面", "持续", "菜单消耗数+1"),
                listOf("5", "张灯结彩", "🟢正面", "持续", "队列扩为6张"),
                listOf("6", "银装素裹", "🔴负面", "持续", "弃置最右2张"),
                listOf("7", "辞旧迎新", "🔵刷新", "即时", "重洗队列翻开4张"),
                listOf("8", "苛捐杂税", "🔴负面", "即时", "每店铺付2铜钱"),
                listOf("9", "硕果累累", "🟢正面", "即时", "每人拿2张四品"),
            )
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

// ==================== 可复用组件 ====================

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SubHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun BodyText(text: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = color)
}

@Composable
private fun BulletItem(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)) {
        Text("• ", style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NumberedItem(number: String, text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)) {
        Text("$number. ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CodeBlock(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ShopCard(
    name: String,
    cost: String,
    incomeType: String,
    baseIncome: String,
    synergy: String,
    extraSynergy: String? = null,
    note: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text("建造费用：$cost 铜钱", style = MaterialTheme.typography.bodySmall)
            Text(incomeType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            Text("效果：$baseIncome", style = MaterialTheme.typography.bodySmall)
            Text("联动：$synergy", style = MaterialTheme.typography.bodySmall)
            extraSynergy?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
            note?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun EventCard(
    name: String,
    type: String,
    duration: String,
    effect: String
) {
    val typeColor = when (type) {
        "负面" -> MaterialTheme.colorScheme.error
        "正面" -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.tertiary
    }
    val durationColor = when (duration) {
        "持续" -> androidx.compose.ui.graphics.Color(0xFFE65100)
        "即时" -> androidx.compose.ui.graphics.Color(0xFF1565C0)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text("[$type] ", fontWeight = FontWeight.Bold, color = typeColor, style = MaterialTheme.typography.bodyMedium)
                    Text("[$duration] ", fontWeight = FontWeight.Bold, color = durationColor, style = MaterialTheme.typography.bodyMedium)
                }
                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(effect, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SimpleTable(
    headers: List<String>,
    rows: List<List<String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                headers.forEachIndexed { index, header ->
                    Text(
                        header,
                        modifier = Modifier.weight(getWeight(index, headers.size, rows)),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
            HorizontalDivider()
            rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    row.forEachIndexed { colIndex, cell ->
                        Text(
                            cell,
                            modifier = Modifier.weight(getWeight(colIndex, headers.size, rows)),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 3
                        )
                    }
                }
                if (rowIndex < rows.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                }
            }
        }
    }
}

private fun getWeight(index: Int, totalCols: Int, rows: List<List<String>>): Float {
    return when {
        totalCols <= 3 -> 1f
        index == 0 -> 0.4f
        index == totalCols - 1 -> 1.5f
        else -> 1f
    }
}
