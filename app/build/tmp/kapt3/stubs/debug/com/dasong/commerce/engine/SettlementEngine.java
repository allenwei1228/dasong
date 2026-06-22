package com.dasong.commerce.engine;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J0\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\n2\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fJ \u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\n\u00a8\u0006\u0010"}, d2 = {"Lcom/dasong/commerce/engine/SettlementEngine;", "", "()V", "calculateMenuIncome", "Lcom/dasong/commerce/engine/MenuSettlementResult;", "player", "Lcom/dasong/commerce/model/PlayerState;", "guest", "Lcom/dasong/commerce/model/card/GuestCard;", "event", "Lcom/dasong/commerce/model/card/EventCard;", "diceRoller", "Lkotlin/Function0;", "", "calculateShopIncome", "Lcom/dasong/commerce/engine/ShopSettlementResult;", "app_debug"})
public final class SettlementEngine {
    
    public SettlementEngine() {
        super();
    }
    
    /**
     * Calculate menu income for a player serving a guest
     */
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.MenuSettlementResult calculateMenuIncome(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.PlayerState player, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.GuestCard guest, @org.jetbrains.annotations.Nullable()
    com.dasong.commerce.model.card.EventCard event, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<java.lang.Integer> diceRoller) {
        return null;
    }
    
    /**
     * Calculate shop income for a player serving a guest
     */
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.ShopSettlementResult calculateShopIncome(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.PlayerState player, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.GuestCard guest, @org.jetbrains.annotations.Nullable()
    com.dasong.commerce.model.card.EventCard event) {
        return null;
    }
}