package com.dasong.commerce.engine;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\rJ\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u000f\u001a\u00020\u0010J\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\t0\u0004J\u000e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0013J\u001e\u0010\u0015\u001a\u001a\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\r\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\r0\u0016J\u0006\u0010\u0017\u001a\u00020\u0018J\u0006\u0010\u0019\u001a\u00020\u0010R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00070\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/dasong/commerce/engine/DeckManager;", "", "()V", "allEventCards", "", "Lcom/dasong/commerce/model/card/EventCard;", "allGuestCards", "Lcom/dasong/commerce/model/card/GuestCard;", "allMenuCards", "Lcom/dasong/commerce/model/card/MenuCard;", "allShopCards", "Lcom/dasong/commerce/model/card/ShopCard;", "createCombinedDeck", "", "drawShopFromPool", "shopPool", "Lcom/dasong/commerce/model/ShopPool;", "getInitialMenuForPlayer", "getStartingFunds", "", "seatOrder", "initGuestDeck", "Lkotlin/Pair;", "initMenuPool", "Lcom/dasong/commerce/model/MenuPool;", "initShopPool", "app_debug"})
public final class DeckManager {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.dasong.commerce.model.card.MenuCard> allMenuCards = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.dasong.commerce.model.card.ShopCard> allShopCards = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.dasong.commerce.model.card.GuestCard> allGuestCards = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.dasong.commerce.model.card.EventCard> allEventCards = null;
    
    public DeckManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.MenuPool initMenuPool() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.ShopPool initShopPool() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.util.List<com.dasong.commerce.model.card.GuestCard>, java.util.List<com.dasong.commerce.model.card.GuestCard>> initGuestDeck() {
        return null;
    }
    
    /**
     * Create combined guest+event deck properly
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Object> createCombinedDeck() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.dasong.commerce.model.card.ShopCard drawShopFromPool(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.ShopPool shopPool) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.dasong.commerce.model.card.MenuCard> getInitialMenuForPlayer() {
        return null;
    }
    
    public final int getStartingFunds(int seatOrder) {
        return 0;
    }
}