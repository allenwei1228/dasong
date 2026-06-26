package com.dasong.commerce.di

import com.dasong.commerce.engine.*
import com.dasong.commerce.online.SupabaseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseProvider(): SupabaseProvider = SupabaseProvider()

    @Provides
    @Singleton
    fun provideDeckManager(): DeckManager = DeckManager()

    @Provides
    @Singleton
    fun provideSettlementEngine(): SettlementEngine = SettlementEngine()

    @Provides
    @Singleton
    fun provideEventExecutor(): EventExecutor = EventExecutor()

    @Provides
    @Singleton
    fun provideWinConditionChecker(): WinConditionChecker = WinConditionChecker()

    @Provides
    @Singleton
    fun provideTurnManager(): TurnManager = TurnManager()

    @Provides
    @Singleton
    fun provideGameEngine(
        deckManager: DeckManager,
        settlementEngine: SettlementEngine,
        eventExecutor: EventExecutor,
        winChecker: WinConditionChecker,
        turnManager: TurnManager
    ): GameEngine = GameEngine(
        deckManager = deckManager,
        settlementEngine = settlementEngine,
        eventExecutor = eventExecutor,
        winChecker = winChecker,
        turnManager = turnManager
    )
}
