package com.dasong.commerce.model.card

enum class MenuGrade { ONE, TWO, THREE, FOUR }

enum class ShopType(val displayName: String) {
    GUA_SI("卦肆"),
    SHU_FANG("书坊"),
    JIU_SI("酒肆"),
    CHOU_DUAN("绸缎庄"),
    GOU_LAN("勾栏瓦肆"),
    CI_QI("瓷器铺"),
    CU_JU("蹴鞠场"),
    YIN_ZI("饮子铺"),
    GUAN_PU("关扑铺"),
    CHA_GUAN("茶馆"),
    SHUO_SHU("说书场"),
    SHOU_SHI("首饰铺")
}

enum class EventType { POSITIVE, NEGATIVE, RESHUFFLE }

enum class EventDuration { CONTINUOUS, IMMEDIATE }

enum class EventEffect {
    JIAN_YI_YANG_DE,
    MEN_KE_LUO_QUE,
    WU_JIAN_BU_SHANG,
    SHI_HE_NIAN_FENG,
    ZHANG_DENG_JIE_CAI,
    YIN_ZHUANG_SU_GUO,
    CI_JIU_YING_XIN,
    KE_JUAN_ZA_SHUI,
    SHUO_GUO_LEI_LEI
}

enum class GamePhase { BUY, PREPARE, SERVE }

enum class TurnStep {
    PHASE_1_BUY_MENU_OR_SHOP,
    PHASE_2_PREPARE_OPTIONAL,
    PHASE_3_SELECT_GUEST,
    PHASE_3_SETTLE_MENU,
    PHASE_3_SETTLE_SHOP,
    PHASE_3_REFRESH_GUEST,
    PHASE_3_EVENT_ANNOUNCE,
    TURN_END_CHECK
}

enum class IncomeType {
    FIXED,
    DICE,
    MENU_BONUS,
    HOUSING_COUNT
}
