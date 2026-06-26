package com.dasong.commerce.ui.room

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dasong.commerce.online.OnlineViewModel
import com.dasong.commerce.online.RoomData
import com.dasong.commerce.online.RoomStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    onBack: () -> Unit,
    onStartGame: (playerCount: Int, playerNames: List<String>) -> Unit,
    viewModel: OnlineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldNavigateToGame by viewModel.shouldNavigateToGame.collectAsState()
    val playerNames by viewModel.playerNames.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState()
    // 使用 OnlineManager.roomFlow 直接订阅轮询更新，而非 uiState 中的快照
    val room by viewModel.currentRoom.collectAsState()

    var maxPlayers by remember { mutableIntStateOf(2) }
    val clipboardManager = LocalClipboardManager.current

    // 监听游戏开始
    LaunchedEffect(shouldNavigateToGame) {
        if (shouldNavigateToGame) {
            viewModel.resetNavigationFlag()
            onStartGame(playerCount, playerNames)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("联机游戏") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.leaveRoom()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ============ 昵称输入 ============
            OutlinedTextField(
                value = uiState.playerName,
                onValueChange = viewModel::updatePlayerName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("你的昵称") },
                placeholder = { Text("输入昵称（默认: 玩家）") },
                singleLine = true,
                enabled = room == null,
            )

            if (room == null) {
                // ============ 创建房间区域 ============
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "创建房间",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        Text(
                            "选择本局人数上限",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2, 3, 4).forEach { count ->
                                FilterChip(
                                    selected = maxPlayers == count,
                                    onClick = { maxPlayers = count },
                                    label = { Text("${count}人") },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.createRoom(maxPlayers) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading && uiState.playerName.isNotBlank(),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("创建联机房间")
                        }
                    }
                }

                // ============ 加入房间区域 ============
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "加入房间",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.roomCodeInput,
                            onValueChange = viewModel::updateRoomCodeInput,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("输入 6 位房间码") },
                            singleLine = true,
                            enabled = !uiState.isLoading,
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = viewModel::joinRoom,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                                    && uiState.roomCodeInput.length == 6
                                    && uiState.playerName.isNotBlank(),
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("加入现有房间")
                        }
                    }
                }
            } else {
                val currentRoom = room!!
                // ============ 房间信息区域 ============
                if (currentRoom != null) {
                    RoomInfoCard(
                        room = currentRoom,
                        isRoomOwner = viewModel.isRoomOwner,
                        onStartGame = viewModel::startGame,
                        onCopyRoomCode = {
                            clipboardManager.setText(AnnotatedString(currentRoom.roomCode))
                        },
                        onAddLocalPlayer = viewModel::addLocalPlayer,
                    )
                }

                // ============ 等待提示（非房主） ============
                if (!viewModel.isRoomOwner && currentRoom.status == RoomStatus.WAITING) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            "⏳ 等待房主开始游戏……",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                // 非房主自动检测游戏开始
                LaunchedEffect(currentRoom.status) {
                    if (!viewModel.isRoomOwner && currentRoom.status == RoomStatus.PLAYING) {
                        // 收集玩家名称并触发导航
                        val names = currentRoom.playerIds.map { id ->
                            currentRoom.playerNames[id] ?: "玩家${currentRoom.playerIds.indexOf(id) + 1}"
                        }
                        onStartGame(currentRoom.playerIds.size, names)
                    }
                }
            }

            // ============ 错误提示 ============
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            // ============ 加载提示 ============
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun RoomInfoCard(
    room: RoomData,
    isRoomOwner: Boolean,
    onStartGame: () -> Unit,
    onCopyRoomCode: () -> Unit,
    onAddLocalPlayer: (String) -> Unit = {},
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "房间信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // 房间码
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "房间码：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.roomCode,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onCopyRoomCode) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "复制房间码",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 人数
            Text(
                "当前人数：${room.playerIds.size}/${room.maxPlayers}",
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(4.dp))

            // 状态
            val statusText = when (room.status) {
                RoomStatus.WAITING -> "等待中"
                RoomStatus.PLAYING -> "游戏中"
                RoomStatus.FINISHED -> "已结束"
            }
            val statusColor = when (room.status) {
                RoomStatus.WAITING -> MaterialTheme.colorScheme.primary
                RoomStatus.PLAYING -> MaterialTheme.colorScheme.error
                RoomStatus.FINISHED -> MaterialTheme.colorScheme.outline
            }
            Text(
                "房间状态：$statusText",
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // 玩家列表
            Text(
                "玩家列表：",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            room.playerIds.forEachIndexed { index, playerId ->
                val playerName = room.playerNames[playerId] ?: playerId.takeLast(4)
                val seatOrder = index + 1
                val isOwner = playerId == room.ownerId
                Text(
                    "顺位$seatOrder: $playerName${if (isOwner) "（房主）" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }

            // 房主：添加本地玩家
            if (isRoomOwner && room.status == RoomStatus.WAITING && !room.isFull) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                var newPlayerName by remember { mutableStateOf("") }

                Text(
                    "添加玩家（同一设备）",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("玩家${room.playerIds.size + 1}名称") },
                        placeholder = { Text("玩家${room.playerIds.size + 1}") },
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            onAddLocalPlayer(newPlayerName)
                            newPlayerName = ""
                        },
                        enabled = room.playerIds.size < room.maxPlayers,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("添加")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // 操作按钮
            if (isRoomOwner && room.status == RoomStatus.WAITING) {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = room.playerIds.size >= 2,
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("开始游戏")
                }

                if (room.playerIds.size < 2) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "至少需要 2 名玩家才能开始",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
