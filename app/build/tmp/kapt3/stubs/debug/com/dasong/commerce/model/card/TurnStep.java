package com.dasong.commerce.model.card;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\t\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\t\u00a8\u0006\n"}, d2 = {"Lcom/dasong/commerce/model/card/TurnStep;", "", "(Ljava/lang/String;I)V", "PHASE_1_BUY_MENU_OR_SHOP", "PHASE_2_PREPARE_OPTIONAL", "PHASE_3_SELECT_GUEST", "PHASE_3_SETTLE_MENU", "PHASE_3_SETTLE_SHOP", "PHASE_3_REFRESH_GUEST", "TURN_END_CHECK", "app_debug"})
public enum TurnStep {
    /*public static final*/ PHASE_1_BUY_MENU_OR_SHOP /* = new PHASE_1_BUY_MENU_OR_SHOP() */,
    /*public static final*/ PHASE_2_PREPARE_OPTIONAL /* = new PHASE_2_PREPARE_OPTIONAL() */,
    /*public static final*/ PHASE_3_SELECT_GUEST /* = new PHASE_3_SELECT_GUEST() */,
    /*public static final*/ PHASE_3_SETTLE_MENU /* = new PHASE_3_SETTLE_MENU() */,
    /*public static final*/ PHASE_3_SETTLE_SHOP /* = new PHASE_3_SETTLE_SHOP() */,
    /*public static final*/ PHASE_3_REFRESH_GUEST /* = new PHASE_3_REFRESH_GUEST() */,
    /*public static final*/ TURN_END_CHECK /* = new TURN_END_CHECK() */;
    
    TurnStep() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.dasong.commerce.model.card.TurnStep> getEntries() {
        return null;
    }
}