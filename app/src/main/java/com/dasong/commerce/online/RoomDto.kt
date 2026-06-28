package com.dasong.commerce.online

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase 数据库行对应的 DTO，与 rooms 表列名一一对应。
 */
@Serializable
data class RoomDto(
    @SerialName("room_code")
    val roomCode: String,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("player_ids")
    val playerIds: List<String> = emptyList(),
    @SerialName("player_names")
    val playerNames: Map<String, String> = emptyMap(),
    @SerialName("max_players")
    val maxPlayers: Int = 2,
    @SerialName("status")
    val status: String = "WAITING",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("disconnected_player_ids")
    val disconnectedPlayerIds: List<String> = emptyList(),
) {
    fun toRoomData(): RoomData = RoomData(
        roomCode = roomCode,
        ownerId = ownerId,
        playerIds = playerIds,
        playerNames = playerNames,
        maxPlayers = maxPlayers,
        status = try { RoomStatus.valueOf(status) } catch (_: Exception) { RoomStatus.WAITING },
        disconnectedPlayerIds = disconnectedPlayerIds,
    )

    companion object {
        fun fromRoomData(room: RoomData): RoomDto = RoomDto(
            roomCode = room.roomCode,
            ownerId = room.ownerId,
            playerIds = room.playerIds,
            playerNames = room.playerNames,
            maxPlayers = room.maxPlayers,
            status = room.status.name,
            disconnectedPlayerIds = room.disconnectedPlayerIds,
        )
    }
}
