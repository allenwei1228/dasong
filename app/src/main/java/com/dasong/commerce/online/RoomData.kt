package com.dasong.commerce.online

data class RoomData(
    val roomCode: String,              // 房间码（6位唯一标识）
    val ownerId: String,               // 房主 ID
    val playerIds: List<String> = emptyList(),   // 玩家 ID 列表
    val playerNames: Map<String, String> = emptyMap(),  // 玩家 ID -> 昵称
    val maxPlayers: Int = 2,            // 最大人数 (2-4)
    val status: RoomStatus = RoomStatus.WAITING,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isFull: Boolean get() = playerIds.size >= maxPlayers
    val playerCount: Int get() = playerIds.size
}
