package com.dasong.commerce.engine;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b5\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u009b\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0019\u00a2\u0006\u0002\u0010\u001aJ\t\u0010@\u001a\u00020\u0003H\u00c6\u0003J\t\u0010A\u001a\u00020\u0010H\u00c6\u0003J\t\u0010B\u001a\u00020\u0010H\u00c6\u0003J\t\u0010C\u001a\u00020\u0010H\u00c6\u0003J\t\u0010D\u001a\u00020\u0019H\u00c6\u0003J\t\u0010E\u001a\u00020\u0005H\u00c6\u0003J\u000f\u0010F\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00c6\u0003J\u000f\u0010G\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00c6\u0003J\u000b\u0010H\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\u000f\u0010I\u001a\b\u0012\u0004\u0012\u00020\u000e0\rH\u00c6\u0003J\t\u0010J\u001a\u00020\u0010H\u00c6\u0003J\t\u0010K\u001a\u00020\u0012H\u00c6\u0003J\t\u0010L\u001a\u00020\u0014H\u00c6\u0003J\u009f\u0001\u0010M\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00102\b\b\u0002\u0010\u0016\u001a\u00020\u00102\b\b\u0002\u0010\u0017\u001a\u00020\u00102\b\b\u0002\u0010\u0018\u001a\u00020\u0019H\u00c6\u0001J\u0013\u0010N\u001a\u00020O2\b\u0010P\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010Q\u001a\u00020\u0010H\u00d6\u0001J\t\u0010R\u001a\u00020SH\u00d6\u0001R\u001c\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u001a\u0010\u0011\u001a\u00020\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u0011\u0010#\u001a\u00020\u000e8F\u00a2\u0006\u0006\u001a\u0004\b$\u0010%R\u001a\u0010\u000f\u001a\u00020\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b&\u0010\'\"\u0004\b(\u0010)R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010+R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010+R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010+R\u001a\u0010\u0016\u001a\u00020\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b0\u0010\'\"\u0004\b1\u0010)R\u001a\u0010\u0017\u001a\u00020\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b2\u0010\'\"\u0004\b3\u0010)R\u001a\u0010\u0015\u001a\u00020\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b4\u0010\'\"\u0004\b5\u0010)R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u00107R\u001a\u0010\u0018\u001a\u00020\u0019X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b8\u00109\"\u0004\b:\u0010;R\u001a\u0010\u0013\u001a\u00020\u0014X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b<\u0010=\"\u0004\b>\u0010?\u00a8\u0006T"}, d2 = {"Lcom/dasong/commerce/engine/GameState;", "", "menuPool", "Lcom/dasong/commerce/model/MenuPool;", "shopPool", "Lcom/dasong/commerce/model/ShopPool;", "guestDeck", "", "Lcom/dasong/commerce/model/card/GuestCard;", "guestQueue", "activeEvent", "Lcom/dasong/commerce/model/card/EventCard;", "players", "", "Lcom/dasong/commerce/model/PlayerState;", "currentPlayerIndex", "", "currentPhase", "Lcom/dasong/commerce/model/card/GamePhase;", "turnStep", "Lcom/dasong/commerce/model/card/TurnStep;", "settlementTip", "settlementMenuIncome", "settlementShopIncome", "stateVersion", "", "(Lcom/dasong/commerce/model/MenuPool;Lcom/dasong/commerce/model/ShopPool;Ljava/util/List;Ljava/util/List;Lcom/dasong/commerce/model/card/EventCard;Ljava/util/List;ILcom/dasong/commerce/model/card/GamePhase;Lcom/dasong/commerce/model/card/TurnStep;IIIJ)V", "getActiveEvent", "()Lcom/dasong/commerce/model/card/EventCard;", "setActiveEvent", "(Lcom/dasong/commerce/model/card/EventCard;)V", "getCurrentPhase", "()Lcom/dasong/commerce/model/card/GamePhase;", "setCurrentPhase", "(Lcom/dasong/commerce/model/card/GamePhase;)V", "currentPlayer", "getCurrentPlayer", "()Lcom/dasong/commerce/model/PlayerState;", "getCurrentPlayerIndex", "()I", "setCurrentPlayerIndex", "(I)V", "getGuestDeck", "()Ljava/util/List;", "getGuestQueue", "getMenuPool", "()Lcom/dasong/commerce/model/MenuPool;", "getPlayers", "getSettlementMenuIncome", "setSettlementMenuIncome", "getSettlementShopIncome", "setSettlementShopIncome", "getSettlementTip", "setSettlementTip", "getShopPool", "()Lcom/dasong/commerce/model/ShopPool;", "getStateVersion", "()J", "setStateVersion", "(J)V", "getTurnStep", "()Lcom/dasong/commerce/model/card/TurnStep;", "setTurnStep", "(Lcom/dasong/commerce/model/card/TurnStep;)V", "component1", "component10", "component11", "component12", "component13", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"})
public final class GameState {
    @org.jetbrains.annotations.NotNull()
    private final com.dasong.commerce.model.MenuPool menuPool = null;
    @org.jetbrains.annotations.NotNull()
    private final com.dasong.commerce.model.ShopPool shopPool = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.dasong.commerce.model.card.GuestCard> guestDeck = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.dasong.commerce.model.card.GuestCard> guestQueue = null;
    @org.jetbrains.annotations.Nullable()
    private com.dasong.commerce.model.card.EventCard activeEvent;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.dasong.commerce.model.PlayerState> players = null;
    private int currentPlayerIndex;
    @org.jetbrains.annotations.NotNull()
    private com.dasong.commerce.model.card.GamePhase currentPhase;
    @org.jetbrains.annotations.NotNull()
    private com.dasong.commerce.model.card.TurnStep turnStep;
    private int settlementTip;
    private int settlementMenuIncome;
    private int settlementShopIncome;
    private long stateVersion;
    
    public GameState(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.MenuPool menuPool, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.ShopPool shopPool, @org.jetbrains.annotations.NotNull()
    java.util.List<com.dasong.commerce.model.card.GuestCard> guestDeck, @org.jetbrains.annotations.NotNull()
    java.util.List<com.dasong.commerce.model.card.GuestCard> guestQueue, @org.jetbrains.annotations.Nullable()
    com.dasong.commerce.model.card.EventCard activeEvent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.dasong.commerce.model.PlayerState> players, int currentPlayerIndex, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.GamePhase currentPhase, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.TurnStep turnStep, int settlementTip, int settlementMenuIncome, int settlementShopIncome, long stateVersion) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.MenuPool getMenuPool() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.ShopPool getShopPool() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.dasong.commerce.model.card.GuestCard> getGuestDeck() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.dasong.commerce.model.card.GuestCard> getGuestQueue() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.dasong.commerce.model.card.EventCard getActiveEvent() {
        return null;
    }
    
    public final void setActiveEvent(@org.jetbrains.annotations.Nullable()
    com.dasong.commerce.model.card.EventCard p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.dasong.commerce.model.PlayerState> getPlayers() {
        return null;
    }
    
    public final int getCurrentPlayerIndex() {
        return 0;
    }
    
    public final void setCurrentPlayerIndex(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.card.GamePhase getCurrentPhase() {
        return null;
    }
    
    public final void setCurrentPhase(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.GamePhase p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.card.TurnStep getTurnStep() {
        return null;
    }
    
    public final void setTurnStep(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.TurnStep p0) {
    }
    
    public final int getSettlementTip() {
        return 0;
    }
    
    public final void setSettlementTip(int p0) {
    }
    
    public final int getSettlementMenuIncome() {
        return 0;
    }
    
    public final void setSettlementMenuIncome(int p0) {
    }
    
    public final int getSettlementShopIncome() {
        return 0;
    }
    
    public final void setSettlementShopIncome(int p0) {
    }
    
    public final long getStateVersion() {
        return 0L;
    }
    
    public final void setStateVersion(long p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.PlayerState getCurrentPlayer() {
        return null;
    }
    
    public GameState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.MenuPool component1() {
        return null;
    }
    
    public final int component10() {
        return 0;
    }
    
    public final int component11() {
        return 0;
    }
    
    public final int component12() {
        return 0;
    }
    
    public final long component13() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.ShopPool component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.dasong.commerce.model.card.GuestCard> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.dasong.commerce.model.card.GuestCard> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.dasong.commerce.model.card.EventCard component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.dasong.commerce.model.PlayerState> component6() {
        return null;
    }
    
    public final int component7() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.card.GamePhase component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.model.card.TurnStep component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.GameState copy(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.MenuPool menuPool, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.ShopPool shopPool, @org.jetbrains.annotations.NotNull()
    java.util.List<com.dasong.commerce.model.card.GuestCard> guestDeck, @org.jetbrains.annotations.NotNull()
    java.util.List<com.dasong.commerce.model.card.GuestCard> guestQueue, @org.jetbrains.annotations.Nullable()
    com.dasong.commerce.model.card.EventCard activeEvent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.dasong.commerce.model.PlayerState> players, int currentPlayerIndex, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.GamePhase currentPhase, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.TurnStep turnStep, int settlementTip, int settlementMenuIncome, int settlementShopIncome, long stateVersion) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}