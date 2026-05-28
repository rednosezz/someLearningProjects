# poker-game

德州扑克（Texas Hold'em）在线对战游戏，支持 WebSocket 实时通信。

## 技术栈

- Java 17+
- Maven
- Spring Boot 3.x
- Spring WebSocket

## 运行

```bash
./mvnw spring-boot:run
```

访问 `http://localhost:8080` 进入游戏页面。

## 核心模块

- `engine/` — 牌型判定引擎（手牌评估、花色/点数）
- `game/` — 游戏控制器（回合管理、下注逻辑、玩家状态）
- `model/` — 数据模型（扑克牌、发牌、玩家）
- `websocket/` — WebSocket 通信处理
