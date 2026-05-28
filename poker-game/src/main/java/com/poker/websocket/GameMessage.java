package com.poker.websocket;

public record GameMessage(
    String type,
    String roomId,
    String playerId,
    Object payload
) {
    public static GameMessage of(String type, Object payload) {
        return new GameMessage(type, null, null, payload);
    }

    public static GameMessage of(String type, String roomId, Object payload) {
        return new GameMessage(type, roomId, null, payload);
    }

    public static GameMessage of(String type, String roomId, String playerId, Object payload) {
        return new GameMessage(type, roomId, playerId, payload);
    }
}
