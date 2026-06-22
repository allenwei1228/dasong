package com.dasong.commerce.ui.game.components.phase;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000F\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\\\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00010\t2\u0018\u0010\u000b\u001a\u0014\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\f2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0010H\u0007\u001a\u001e\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\r2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u0010H\u0003\u001a\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\u0002\u00a8\u0006\u0018"}, d2 = {"BuyPhasePanel", "", "player", "Lcom/dasong/commerce/model/PlayerState;", "menuPool", "Lcom/dasong/commerce/model/MenuPool;", "shopPool", "Lcom/dasong/commerce/model/ShopPool;", "onBuyMenu", "Lkotlin/Function1;", "Lcom/dasong/commerce/model/card/MenuCard;", "onBuyShop", "Lkotlin/Function2;", "Lcom/dasong/commerce/model/card/ShopCard;", "", "onEndPhase", "Lkotlin/Function0;", "ShopDetailDialog", "shop", "onDismiss", "getIncomeTypeDesc", "", "incomeType", "Lcom/dasong/commerce/model/card/IncomeType;", "app_debug"})
public final class BuyPhasePanelKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.layout.ExperimentalLayoutApi.class})
    @androidx.compose.runtime.Composable()
    public static final void BuyPhasePanel(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.PlayerState player, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.MenuPool menuPool, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.ShopPool shopPool, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.dasong.commerce.model.card.MenuCard, kotlin.Unit> onBuyMenu, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.dasong.commerce.model.card.ShopCard, ? super java.lang.Integer, kotlin.Unit> onBuyShop, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onEndPhase) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ShopDetailDialog(com.dasong.commerce.model.card.ShopCard shop, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    private static final java.lang.String getIncomeTypeDesc(com.dasong.commerce.model.card.IncomeType incomeType) {
        return null;
    }
}