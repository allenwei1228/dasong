package com.dasong.commerce.model

import com.dasong.commerce.model.card.*

object CardDataProvider {

    // ===== 菜单牌 76张 =====
    val menuCards: List<MenuCard> = buildList {
        // 四品 (免费小吃) - 28张
        addAll((1..7).map { MenuCard(400 + it, "炊饼", MenuGrade.FOUR, 0, emoji = "🫓") })
        addAll((1..7).map { MenuCard(410 + it, "月饼", MenuGrade.FOUR, 0, emoji = "🥮") })
        addAll((1..7).map { MenuCard(420 + it, "饺子", MenuGrade.FOUR, 0, emoji = "🥟") })
        addAll((1..7).map { MenuCard(430 + it, "汤面", MenuGrade.FOUR, 0, emoji = "🍜") })

        // 三品 (家常菜) - 25张
        addAll((1..5).map { MenuCard(500 + it, "炒青菜", MenuGrade.THREE, 3, emoji = "🥬") })
        addAll((1..5).map { MenuCard(510 + it, "葱油鸡", MenuGrade.THREE, 3, emoji = "🍗") })
        addAll((1..5).map { MenuCard(520 + it, "红烧鱼", MenuGrade.THREE, 3, emoji = "🐟") })
        addAll((1..5).map { MenuCard(530 + it, "豆腐羹", MenuGrade.THREE, 3, emoji = "🥣") })
        addAll((1..5).map { MenuCard(540 + it, "酱牛肉", MenuGrade.THREE, 3, emoji = "🥩") })

        // 二品 (硬菜) - 19张
        addAll((1..5).map { MenuCard(600 + it, "葱爆羊肉", MenuGrade.TWO, 6, emoji = "🍖") })
        addAll((1..5).map { MenuCard(610 + it, "椰子鸡", MenuGrade.TWO, 6, emoji = "🐔") })
        addAll((1..5).map { MenuCard(620 + it, "北京烤鸭", MenuGrade.TWO, 6, emoji = "🦆") })
        addAll((1..4).map { MenuCard(630 + it, "松鼠鳜鱼", MenuGrade.TWO, 6, emoji = "🐠") })

        // 一品 (御宴) - 4张
        addAll((1..1).map { MenuCard(700 + it, "火爆鱿鱼", MenuGrade.ONE, 9, emoji = "🦑") })
        addAll((1..1).map { MenuCard(701, "金丝虾球", MenuGrade.ONE, 9, emoji = "🦐") })
        addAll((1..1).map { MenuCard(702, "蟹酿橙", MenuGrade.ONE, 9, emoji = "🦀") })
        addAll((1..1).map { MenuCard(703, "鱼翅捞饭", MenuGrade.ONE, 9, emoji = "🦈") })
    }

    // ===== 店铺牌 36张 (12种 × 3张) =====
    val shopCards: List<ShopCard> = listOf(
        ShopCard(1, "卦肆", ShopType.GUA_SI, 6, 0, hasDiceMechanic = true, incomeType = IncomeType.DICE),
        ShopCard(2, "书坊", ShopType.SHU_FANG, 6, 3, incomeType = IncomeType.FIXED),
        ShopCard(3, "酒肆", ShopType.JIU_SI, 6, 0, hasMenuBonus = true, menuBonus = 1, incomeType = IncomeType.MENU_BONUS),
        ShopCard(4, "绸缎庄", ShopType.CHOU_DUAN, 7, 4, incomeType = IncomeType.FIXED),
        ShopCard(5, "勾栏瓦肆", ShopType.GOU_LAN, 8, 0, hasHousingBonus = true, incomeType = IncomeType.HOUSING_COUNT),
        ShopCard(6, "瓷器铺", ShopType.CI_QI, 9, 6, incomeType = IncomeType.FIXED),
        ShopCard(7, "蹴鞠场", ShopType.CU_JU, 6, 0, hasMenuBonus = true, menuBonus = 2, incomeType = IncomeType.MENU_BONUS),
        ShopCard(8, "饮子铺", ShopType.YIN_ZI, 5, 2, incomeType = IncomeType.FIXED),
        ShopCard(9, "关扑铺", ShopType.GUAN_PU, 5, 2, incomeType = IncomeType.FIXED),
        ShopCard(10, "茶馆", ShopType.CHA_GUAN, 5, 2, incomeType = IncomeType.FIXED),
        ShopCard(11, "说书场", ShopType.SHUO_SHU, 5, 2, incomeType = IncomeType.FIXED),
        ShopCard(12, "首饰铺", ShopType.SHOU_SHI, 5, 2, incomeType = IncomeType.FIXED)
    ).flatMap { listOf(it, it.copy(id = it.id + 100), it.copy(id = it.id + 200)) }

    // ===== 客人牌 81张 (27种 × 3张) =====
    private val guestTemplates: List<GuestCard> = listOf(
        // 消耗2张菜单 - 6位
        GuestCard(1, "货郎", 2, listOf(ShopType.YIN_ZI, ShopType.GUAN_PU)),
        GuestCard(2, "秀才", 2, listOf(ShopType.CHA_GUAN, ShopType.SHU_FANG)),
        GuestCard(3, "绸缎商", 2, listOf(ShopType.CHOU_DUAN, ShopType.SHOU_SHI)),
        GuestCard(4, "瓷商", 2, listOf(ShopType.CI_QI, ShopType.SHOU_SHI)),
        GuestCard(5, "酒馆客人", 2, listOf(ShopType.JIU_SI, ShopType.SHUO_SHU)),
        GuestCard(6, "游人", 2, listOf(ShopType.CU_JU, ShopType.JIU_SI)),

        // 消耗3张菜单 - 11位
        GuestCard(7, "走贩", 3, listOf(ShopType.YIN_ZI, ShopType.SHUO_SHU)),
        GuestCard(8, "茶客", 3, listOf(ShopType.CHA_GUAN, ShopType.SHUO_SHU)),
        GuestCard(9, "算卦客人", 3, listOf(ShopType.GUA_SI, ShopType.GUAN_PU)),
        GuestCard(10, "乡绅", 3, listOf(ShopType.YIN_ZI, ShopType.GUAN_PU, ShopType.SHUO_SHU)),
        GuestCard(11, "文人雅士", 3, listOf(ShopType.CHA_GUAN, ShopType.SHU_FANG, ShopType.SHUO_SHU)),
        GuestCard(12, "酒楼宾客", 3, listOf(ShopType.JIU_SI, ShopType.YIN_ZI, ShopType.SHUO_SHU)),
        GuestCard(13, "贵妇人", 3, listOf(ShopType.CHOU_DUAN, ShopType.CI_QI, ShopType.SHOU_SHI)),
        GuestCard(14, "纨绔子弟", 3, listOf(ShopType.JIU_SI, ShopType.CU_JU, ShopType.SHUO_SHU)),
        GuestCard(15, "街巷百姓", 3, listOf(ShopType.YIN_ZI, ShopType.CHA_GUAN, ShopType.JIU_SI)),
        GuestCard(16, "庙会游人", 3, listOf(ShopType.GUA_SI, ShopType.GUAN_PU, ShopType.GOU_LAN)),
        GuestCard(17, "富商", 3, listOf(ShopType.CHOU_DUAN, ShopType.SHOU_SHI, ShopType.GOU_LAN)),

        // 消耗4张菜单 - 10位
        GuestCard(18, "庙会大户", 4, listOf(ShopType.JIU_SI, ShopType.CU_JU, ShopType.GOU_LAN)),
        GuestCard(19, "文坛大家", 4, listOf(ShopType.CHA_GUAN, ShopType.SHU_FANG, ShopType.CHOU_DUAN)),
        GuestCard(20, "珠宝商人", 4, listOf(ShopType.CI_QI, ShopType.SHOU_SHI, ShopType.CHOU_DUAN)),
        GuestCard(21, "街头看客", 4, listOf(ShopType.YIN_ZI, ShopType.GUAN_PU, ShopType.GUA_SI)),
        GuestCard(22, "汴京游人", 4, listOf(ShopType.YIN_ZI, ShopType.CHA_GUAN, ShopType.JIU_SI, ShopType.SHUO_SHU)),
        GuestCard(23, "豪门贵妇", 4, listOf(ShopType.CHOU_DUAN, ShopType.CI_QI, ShopType.SHOU_SHI, ShopType.GOU_LAN)),
        GuestCard(24, "节庆宾客", 4, listOf(ShopType.JIU_SI, ShopType.CU_JU, ShopType.YIN_ZI, ShopType.SHUO_SHU)),
        GuestCard(25, "赶集百姓", 4, listOf(ShopType.YIN_ZI, ShopType.GUAN_PU, ShopType.CHA_GUAN, ShopType.GUA_SI)),
        GuestCard(26, "达官贵人", 4, listOf(ShopType.CHOU_DUAN, ShopType.SHOU_SHI, ShopType.CI_QI, ShopType.JIU_SI)),
        GuestCard(27, "市井闲客", 4, listOf(ShopType.SHUO_SHU, ShopType.GOU_LAN, ShopType.CU_JU, ShopType.GUA_SI))
    )

    val guestCards: List<GuestCard> = guestTemplates.flatMapIndexed { idx, template ->
        (1..3).map { template.copy(id = idx * 3 + it) }
    }

    // ===== 事件牌 9张 =====
    // 持续事件牌: 只要未被覆盖为其他事件，就一直生效
    // 即时事件牌: 效果立即触发，触发后丢弃
    val eventCards: List<EventCard> = listOf(
        EventCard(1, "俭以养德", EventType.NEGATIVE, "菜单消耗 -1", EventEffect.JIAN_YI_YANG_DE, EventDuration.CONTINUOUS),
        EventCard(2, "门可罗雀", EventType.NEGATIVE, "仅选1店铺结算", EventEffect.MEN_KE_LUO_QUE, EventDuration.CONTINUOUS),
        EventCard(3, "无尖不商", EventType.NEGATIVE, "店铺收入-1", EventEffect.WU_JIAN_BU_SHANG, EventDuration.CONTINUOUS),
        EventCard(4, "时和年丰", EventType.POSITIVE, "菜单消耗+1", EventEffect.SHI_HE_NIAN_FENG, EventDuration.CONTINUOUS),
        EventCard(5, "张灯结彩", EventType.POSITIVE, "队列扩为6张", EventEffect.ZHANG_DENG_JIE_CAI, EventDuration.CONTINUOUS),
        EventCard(6, "银装素裹", EventType.NEGATIVE, "弃置最右2张", EventEffect.YIN_ZHUANG_SU_GUO, EventDuration.CONTINUOUS),
        EventCard(7, "辞旧迎新", EventType.RESHUFFLE, "重洗队列", EventEffect.CI_JIU_YING_XIN, EventDuration.IMMEDIATE),
        EventCard(8, "苛捐杂税", EventType.NEGATIVE, "每店铺模型付2铜钱", EventEffect.KE_JUAN_ZA_SHUI, EventDuration.IMMEDIATE),
        EventCard(9, "硕果累累", EventType.POSITIVE, "每人拿2张四品菜单", EventEffect.SHUO_GUO_LEI_LEI, EventDuration.IMMEDIATE)
    )
}
