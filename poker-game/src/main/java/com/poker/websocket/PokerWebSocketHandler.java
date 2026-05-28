package com.poker.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poker.game.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.poker.model.Card;

@Component
public class PokerWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PokerWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameController gameController = new GameController();

    private final Map<String, WebSocketSession> playerSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayerId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        GameMessage msg = objectMapper.readValue(message.getPayload(), GameMessage.class);
        String type = msg.type();

        log.info("Received message type: {}", type);

        switch (type) {
            case "CREATE_ROOM" -> handleCreateRoom(session, msg);
            case "JOIN_ROOM" -> handleJoinRoom(session, msg);
            case "PLAYER_ACTION" -> handlePlayerAction(session, msg);
            case "READY" -> handleReady(session, msg);
            case "RECONNECT" -> handleReconnect(session, msg);
        }
    }

    private void handleCreateRoom(WebSocketSession session, GameMessage msg) throws IOException {
        String playerId = session.getId();
        String playerName;
        String roomId;

        Object payload = msg.payload();
        if (payload instanceof String) {
            playerName = (String) payload;
            roomId = gameController.createRoom(playerId, playerName).getRoomId();
        } else {
            @SuppressWarnings("unchecked")
            Map<String, String> params = (Map<String, String>) payload;
            playerName = params.get("playerName");
            String customRoomId = params.get("roomId");
            if (customRoomId != null && !customRoomId.isEmpty()) {
                roomId = customRoomId;
                gameController.createRoomWithId(roomId, playerId, playerName);
            } else {
                roomId = gameController.createRoom(playerId, playerName).getRoomId();
            }
        }

        playerSessions.put(playerId, session);
        sessionToPlayerId.put(session.getId(), playerId);

        GameMessage response = GameMessage.of("ROOM_CREATED", Map.of(
            "roomId", roomId,
            "playerId", playerId
        ));
        sendToPlayer(playerId, response);
    }

    private void handleJoinRoom(WebSocketSession session, GameMessage msg) throws IOException {
        try {
            log.info("handleJoinRoom called, payload class: {}, payload: {}", msg.payload().getClass(), msg.payload());
            @SuppressWarnings("unchecked")
            Map<String, String> payload = (Map<String, String>) msg.payload();
            String roomId = payload.get("roomId");
            String playerName = payload.get("playerName");
            log.info("Attempting to join room: {}, playerName: {}", roomId, playerName);
            String playerId = session.getId();

            Optional<GameRoom> roomOpt = gameController.joinRoom(roomId, playerId, playerName);
            log.info("joinRoom result: {}", roomOpt.isPresent() ? "found room" : "room not found");
            if (roomOpt.isEmpty()) {
                GameMessage error = GameMessage.of("ERROR", Map.of(
                    "code", "ROOM_NOT_FOUND",
                    "message", "房间不存在或已满"
                ));
                sendToPlayer(playerId, error);
                return;
            }

            GameRoom room = roomOpt.get();
            playerSessions.put(playerId, session);
            sessionToPlayerId.put(session.getId(), playerId);
            log.info("Player registered, sessionToPlayerId size: {}", sessionToPlayerId.size());

            // 给加入的玩家单独发送确认（包含roomId和playerId，ready时需要）
            GameMessage joinedAckMsg = GameMessage.of("ROOM_JOINED", Map.of(
                "roomId", roomId,
                "playerId", playerId,
                "playerName", playerName
            ));
            sendToPlayer(playerId, joinedAckMsg);

            // 广播其他玩家有人加入了
            GameMessage joinedMsg = GameMessage.of("PLAYER_JOINED", roomId, Map.of(
                "playerId", playerId,
                "playerName", playerName,
                "seatIndex", 1
            ));
            log.info("Broadcasting PLAYER_JOINED to room {}", roomId);
            broadcastToRoom(roomId, joinedMsg);
            log.info("PLAYER_JOINED broadcast complete");
        } catch (Exception e) {
            log.error("Error in handleJoinRoom", e);
            throw e;
        }
    }

    private void handlePlayerAction(WebSocketSession session, GameMessage msg) throws IOException {
        String playerId = sessionToPlayerId.get(session.getId());
        if (playerId == null) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) msg.payload();
        String roomId = (String) payload.get("roomId");
        String actionStr = (String) payload.get("action");
        int amount = payload.containsKey("amount") ? ((Number) payload.get("amount")).intValue() : 0;

        GameAction action = GameAction.valueOf(actionStr);
        gameController.handleAction(roomId, playerId, action, amount);

        GameMessage actionMsg = GameMessage.of("PLAYER_ACTION", roomId, Map.of(
            "playerId", playerId,
            "action", actionStr,
            "amount", amount
        ));
        broadcastToRoom(roomId, actionMsg);

        Optional<GameRoom> roomOpt = gameController.getRoom(roomId);
        if (roomOpt.isPresent()) {
            GameRoom room = roomOpt.get();
            notifyPhaseChange(room, playerId);
        }
    }

    private void notifyPhaseChange(GameRoom room, String currentPlayerId) throws IOException {
        String roomId = room.getRoomId();
        GamePhase phase = room.getPhase();

        switch (phase) {
            case PREFLOP -> {
                for (Player p : room.getPlayers()) {
                    GameMessage holeCardsMsg = GameMessage.of("HOLE_CARDS", roomId, p.getId(), Map.of(
                        "cards", p.getHoleCards().stream().map(this::cardToMap).toList(),
                        "yourSeat", p.getSeatIndex()
                    ));
                    sendToPlayer(p.getId(), holeCardsMsg);
                }
                String nextPlayerId = gameController.getCurrentTurnPlayerId(roomId);
                if (nextPlayerId != null) {
                    Player nextPlayer = room.findPlayer(nextPlayerId).orElse(null);
                    if (nextPlayer != null) {
                        GameMessage yourTurnMsg = GameMessage.of("YOUR_TURN", roomId, nextPlayerId, Map.of(
                            "availableActions", new String[]{"CHECK", "CALL", "RAISE", "ALL_IN", "FOLD"},
                            "amountToCall", room.getBettingRound().getAmountToCall(nextPlayerId)
                        ));
                        sendToPlayer(nextPlayerId, yourTurnMsg);
                    }
                }
            }
            case FLOP, TURN, RIVER -> {
                GameMessage communityMsg = GameMessage.of("COMMUNITY_CARDS", roomId, Map.of(
                    "phase", phase.name(),
                    "cards", room.getCommunityCards().stream().map(this::cardToMap).toList()
                ));
                broadcastToRoom(roomId, communityMsg);

                String nextPlayerId = gameController.getCurrentTurnPlayerId(roomId);
                if (nextPlayerId != null) {
                    Player nextPlayer = room.findPlayer(nextPlayerId).orElse(null);
                    if (nextPlayer != null) {
                        GameMessage yourTurnMsg = GameMessage.of("YOUR_TURN", roomId, nextPlayerId, Map.of(
                            "availableActions", new String[]{"CHECK", "CALL", "RAISE", "ALL_IN", "FOLD"},
                            "amountToCall", room.getBettingRound().getAmountToCall(nextPlayerId)
                        ));
                        sendToPlayer(nextPlayerId, yourTurnMsg);
                    }
                }
            }
            case SHOWDOWN -> {
                List<Player> activePlayers = room.getActivePlayers();
                Player winner = null;
                if (activePlayers.size() == 1) {
                    winner = activePlayers.get(0);
                } else {
                    List<Card> communityCards = room.getCommunityCards();
                    List<com.poker.model.Hand> hands = new ArrayList<>();
                    List<String> playerIds = new ArrayList<>();
                    for (Player p : activePlayers) {
                        com.poker.model.Hand hand = p.getHand();
                        for (Card c : communityCards) {
                            hand.addCommunityCard(c);
                        }
                        hands.add(hand);
                        playerIds.add(p.getId());
                    }
                    String winnerId = room.getPokerEngine().determineWinner(hands, playerIds);
                    winner = room.findPlayer(winnerId).orElse(null);
                }

                final Player finalWinner = winner;
                List<Map<String, Object>> playerResults = new ArrayList<>();
                for (Player p : room.getPlayers()) {
                    playerResults.add(Map.<String, Object>of(
                        "playerId", p.getId(),
                        "playerName", p.getName(),
                        "handRank", p.getHand().toString(),
                        "score", p.getScore()
                    ));
                }
                Map<String, Object> showdownPayload = new HashMap<>();
                showdownPayload.put("players", playerResults);
                showdownPayload.put("winnerId", winner != null ? winner.getId() : "");
                showdownPayload.put("winnerName", winner != null ? winner.getName() : "");
                GameMessage showdownMsg = GameMessage.of("SHOWDOWN", roomId, showdownPayload);
                broadcastToRoom(roomId, showdownMsg);

                if (finalWinner != null) {
                    GameMessage gameEndedMsg = GameMessage.of("GAME_ENDED", roomId, Map.of(
                        "winnerId", finalWinner.getId(),
                        "winnerName", finalWinner.getName()
                    ));
                    broadcastToRoom(roomId, gameEndedMsg);
                }
            }
            default -> {}
        }
    }

    private Map<String, String> cardToMap(com.poker.model.Card card) {
        return Map.of(
            "suit", card.suit().name(),
            "rank", card.rank().name()
        );
    }

    private void handleReady(WebSocketSession session, GameMessage msg) throws IOException {
        String playerId = sessionToPlayerId.get(session.getId());
        if (playerId == null) return;

        String roomId = msg.roomId();
        Optional<GameRoom> roomOpt = gameController.getRoom(roomId);
        if (roomOpt.isEmpty()) return;

        GameRoom room = roomOpt.get();
        room.incrementReadyCount();

        // 广播 PLAYER_READY 消息给所有玩家
        String playerName = room.findPlayer(playerId).map(Player::getName).orElse("玩家");
        GameMessage readyMsg = GameMessage.of("PLAYER_READY", roomId, Map.of(
            "playerId", playerId,
            "playerName", playerName
        ));
        broadcastToRoom(roomId, readyMsg);

        // 如果两人都准备好了，才开始游戏
        if (room.allPlayersReady()) {
            room.resetReadyCount();
            gameController.startGame(roomId);
            GameMessage startMsg = GameMessage.of("GAME_STARTED", roomId, Map.of(
                "dealer", room.getDealerSeat()
            ));
            broadcastToRoom(roomId, startMsg);
            notifyPhaseChange(room, null);
        }
    }

    private void handleReconnect(WebSocketSession session, GameMessage msg) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> payload = (Map<String, String>) msg.payload();
        String playerId = payload.get("playerId");

        playerSessions.put(playerId, session);
        sessionToPlayerId.put(session.getId(), playerId);

        GameMessage reconnectMsg = GameMessage.of("PLAYER_RECONNECTED", Map.of(
            "playerId", playerId,
            "sessionId", session.getId()
        ));
        sendToPlayer(playerId, reconnectMsg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String playerId = sessionToPlayerId.remove(session.getId());
        if (playerId != null) {
            playerSessions.remove(playerId);
            log.info("Player disconnected: {}", playerId);

            for (GameRoom room : gameController.getRoom(playerId).stream().toList()) {
                Optional<Player> playerOpt = room.findPlayer(playerId);
                if (playerOpt.isPresent()) {
                    playerOpt.get().setStatus(PlayerStatus.FOLDED);
                    GameMessage disconnectMsg = GameMessage.of("PLAYER_DISCONNECTED", room.getRoomId(), Map.of(
                        "playerId", playerId
                    ));
                    broadcastToRoom(room.getRoomId(), disconnectMsg);
                }
            }
        }
    }

    private void broadcastToRoom(String roomId, GameMessage msg) throws IOException {
        Optional<GameRoom> roomOpt = gameController.getRoom(roomId);
        if (roomOpt.isEmpty()) return;

        GameRoom room = roomOpt.get();
        for (Player player : room.getPlayers()) {
            sendToPlayer(player.getId(), msg);
        }
    }

    private void sendToPlayer(String playerId, GameMessage msg) throws IOException {
        WebSocketSession session = playerSessions.get(playerId);
        if (session != null && session.isOpen()) {
            String json = objectMapper.writeValueAsString(msg);
            session.sendMessage(new TextMessage(json));
        }
    }
}
