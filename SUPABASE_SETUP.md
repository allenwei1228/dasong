# Supabase 联机服务设置指南

本文档教你如何在 10 分钟内完成 Supabase 配置，让大宋百商图支持真正的多人联机。

---

## 第一步：创建 Supabase 项目

1. 打开 [supabase.com](https://supabase.com/) 并注册/登录
2. 点击 **「New project」** 创建新项目
3. 填写信息：
   - **Name**: `dasong-commerce`（任意名称）
   - **Database Password**: 设一个强密码（**请记下来**，后面要用）
   - **Region**: 选择离你最近的区域（建议 `Northeast Asia (Tokyo)` 或 `Southeast Asia (Singapore)`）
4. 点击 **「Create new project」**，等待 1-2 分钟初始化完成

---

## 第二步：获取 API 密钥

1. 进入项目后，左侧菜单 → **Settings** → **API**
2. 你会看到两个关键信息：
   - **Project URL**: 类似于 `https://xxxxx.supabase.co`
   - **anon public key**: 类似于 `eyJhbGciOi...`（很长的字符串）
3. **复制这两个值**，稍后填入项目配置

---

## 第三步：创建数据库表

进入左侧菜单 → **SQL Editor**，点击 **「New query」**，粘贴以下 SQL 并执行：

```sql
-- 1. 创建 rooms 表
CREATE TABLE IF NOT EXISTS rooms (
    room_code   VARCHAR(6) PRIMARY KEY,        -- 6位房间码
    owner_id    VARCHAR(128) NOT NULL,          -- 房主ID
    player_ids  JSONB NOT NULL DEFAULT '[]',    -- 玩家ID列表
    player_names JSONB NOT NULL DEFAULT '{}',   -- 玩家ID -> 昵称
    max_players INTEGER NOT NULL DEFAULT 2
        CHECK (max_players BETWEEN 2 AND 4),    -- 人数上限 2-4
    status      VARCHAR(20) NOT NULL DEFAULT 'WAITING'
        CHECK (status IN ('WAITING', 'PLAYING', 'FINISHED')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. 索引（加速按状态和房主查询）
CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);
CREATE INDEX IF NOT EXISTS idx_rooms_owner  ON rooms(owner_id);
```

点击 **「Run」** 执行。你应该在左侧 **Table Editor** 中看到新创建的 `rooms` 表。

---

## 第四步：配置 RLS（行级安全策略）

默认情况下 Supabase 会阻止所有公开访问。我们需要开启公开读写权限。

在同一个 SQL Editor 中，执行以下 SQL：

```sql
-- 开启 RLS
ALTER TABLE rooms ENABLE ROW LEVEL SECURITY;

-- 允许任何人读取所有房间（用于查找和加入房间）
CREATE POLICY "allow_public_read"
    ON rooms FOR SELECT
    USING (true);

-- 允许任何人插入新房间（创建房间）
CREATE POLICY "allow_public_insert"
    ON rooms FOR INSERT
    WITH CHECK (true);

-- 允许任何人更新房间（加入、状态变更）
CREATE POLICY "allow_public_update"
    ON rooms FOR UPDATE
    USING (true)
    WITH CHECK (true);
```

---

## 第五步：配置项目认证信息

打开项目中的 `gradle.properties` 文件（位于项目根目录），将你刚才复制的 **Project URL** 和 **anon key** 填入：

```properties
# Supabase 配置（请替换为你自己的 Supabase 项目信息）
SUPABASE_URL=https://你的项目ID.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOi你的anon-key
```

**示例**（请勿直接复制，填入你自己的值）：
```properties
SUPABASE_URL=https://drqbgjkablbnhoxyoohd.supabase.co
SUPABASE_ANON_KEY=sb_publishable_lEOdkBYxevKSOmIFBDhJcA_LI95s1p1
```

---

## 第六步：编译运行

```bash
# 在项目根目录执行
./gradlew clean assembleDebug
```

如果编译成功，就大功告成了！现在你的联机模式使用的是真正的 Supabase 远端服务器。

---

## 验证是否配置成功

1. 启动应用，点击 **「创建房间」**
2. 输入昵称 → 选择人数 → 点击创建
3. 如果成功显示房间信息（含6位房间码），说明 Supabase 连接正常
4. 打开 Supabase 后台 → **Table Editor** → `rooms` 表，你应该能看到刚才创建的房间记录

---

## 常见问题

### Q1: 编译时报错 `BuildConfig not found`
确保 `app/build.gradle.kts` 中 `buildFeatures { buildConfig = true }` 已添加。

### Q2: 运行时连接失败（超时/网络错误）
- 检查 `gradle.properties` 中的 URL 是否以 `https://` 开头
- 检查 anon key 是否完整复制（没有多余空格/换行）
- 确保手机有网络连接
- 某些地区可能需要 VPN 访问 Supabase

### Q3: 创建房间时显示 "权限不足"
- 回到 SQL Editor，确认第四步的 RLS Policy 已全部执行
- 在左侧 **Authentication** → **Policies** 中确认 `rooms` 表有 3 条 policy

### Q4: 房间码被别人猜到怎么办？
6位房间码有约 7.2 亿种组合（排除易混淆字符后），随机猜测几乎不可能命中。如果不放心，可以在 Supabase 后台给 `rooms` 表添加 `DELETE` policy 做房间过期清理。

### Q5: 免费额度够用吗？
Supabase 免费版提供：
- 500MB 数据库
- 每月 5GB 流量
- 50,000 月活用户

对于小型联机游戏绰绰有余。

---

## 表结构速查

| 列名 | 类型 | 说明 |
|------|------|------|
| `room_code` | VARCHAR(6) | 主键，6位房间码 |
| `owner_id` | VARCHAR(128) | 房主ID |
| `player_ids` | JSONB | 玩家ID列表，如 `["id1","id2"]` |
| `player_names` | JSONB | ID→昵称映射，如 `{"id1":"张三","id2":"李四"}` |
| `max_players` | INTEGER | 人数上限 (2-4) |
| `status` | VARCHAR(20) | WAITING / PLAYING / FINISHED |
| `created_at` | TIMESTAMPTZ | 创建时间 |
