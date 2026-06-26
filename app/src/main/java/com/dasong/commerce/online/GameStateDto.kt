package com.dasong.commerce.online

import com.dasong.commerce.engine.GameState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase game_states 表对应的 DTO。
 * 存储房间的完整游戏状态快照，用于联机同步。
 */
@Serializable
data class GameStateDto(
    @SerialName("room_code")
    val roomCode: String,
    @SerialName("state")
    val state: GameState,
    @SerialName("version")
    val version: Long = 0,
)
