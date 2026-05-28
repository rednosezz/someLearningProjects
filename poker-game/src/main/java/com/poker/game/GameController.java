package com.poker.game;

import com.poker.engine.PokerEngine;
import com.poker.model.Card;
import com.poker.model.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private final Map<String, GameRoom> rooms;
    private final PokerEngine pokerEngine;

    public GameController() {
        this.rooms = new HashMap<>();
        this.pokerEngine = new PokerEngine();
    }

    public GameRoom createRoom(String playerId, String playerName) {
        String roomId = generateRoomId();
        return createRoomWithId(roomId, playerId, playerName);
    }

    public GameRoom createRoomWithId(String roomId, String playerId, String playerName) {
        String normalizedRoomId = normalizeRoomId(roomId);
        GameRoom room = new GameRoom(normalizedRoomId);
        Player player = new Player(playerId, playerName, 0);
        room.addPlayer(player);
        rooms.put(normalizedRoomId, room);
        return room;
    }

    public Optional<GameRoom> joinRoom(String roomId, String playerId, String playerName) {
        String normalizedRoomId = normalizeRoomId(roomId);
        log.info("joinRoom: input={}, normalized={}, available rooms={}", roomId, normalizedRoomId, rooms.keySet());
        GameRoom room = rooms.get(normalizedRoomId);
        if (room == null || room.isFull()) {
            log.info("joinRoom: room {} not found or full", normalizedRoomId);
            return Optional.empty();
        }
        Player player = new Player(playerId, playerName, 1);
        room.addPlayer(player);
        log.info("joinRoom: player {} joined room {}", playerName, normalizedRoomId);
        return Optional.of(room);
    }

    public void startGame(String roomId) {
        String normalized = normalizeRoomId(roomId);
        GameRoom room = rooms.get(normalized);
        if (room == null || !room.hasTwoPlayers()) {
            return;
        }
        room.startNewGame();
        room.dealHoleCards();
    }

    public void handleAction(String roomId, String playerId, GameAction action, int amount) {
        String normalized = normalizeRoomId(roomId);
        GameRoom room = rooms.get(normalized);
        if (room == null) return;

        Optional<Player> playerOpt = room.findPlayer(playerId);
        if (playerOpt.isEmpty()) return;
        Player player = playerOpt.get();

        BettingRound bettingRound = room.getBettingRound();

        switch (action) {
            case FOLD -> {
                player.setStatus(PlayerStatus.FOLDED);
                List<Player> active = room.getActivePlayers();
                if (active.size() == 1) {
                    endGameEarly(roomId, active.get(0));
                    return;
                } else {
                    room.switchTurn();
                }
            }
            case CHECK -> {
                int amountToCall = bettingRound.getAmountToCall(playerId);
                if (amountToCall != 0) {
                    return;
                }
                room.switchTurn();
            }
            case CALL -> {
                int amountToCall = bettingRound.getAmountToCall(playerId);
                player.setScore(player.getScore() - amountToCall);
                bettingRound.placeBet(playerId, amountToCall);
                room.switchTurn();
            }
            case RAISE -> {
                int amountToCall = bettingRound.getAmountToCall(playerId);
                int totalBet = amount;
                int additionalBet = totalBet - amountToCall;
                player.setScore(player.getScore() - additionalBet);
                bettingRound.placeBet(playerId, additionalBet);
                room.switchTurn();
            }
            case ALL_IN -> {
                int amountToCall = bettingRound.getAmountToCall(playerId);
                int playerScore = player.getScore();
                int allInAmount = Math.min(playerScore, amountToCall + amount);
                player.setScore(player.getScore() - allInAmount);
                bettingRound.placeBet(playerId, allInAmount);
                player.setStatus(PlayerStatus.ALL_IN);
                room.switchTurn();
            }
        }

        if (room.isBettingRoundOver() && room.getPhase() != GamePhase.SHOWDOWN && room.getPhase() != GamePhase.ENDED) {
            if (room.getPhase() == GamePhase.RIVER) {
                processShowdown(roomId);
            } else {
                room.nextPhase();
            }
        }
    }

    private void endGameEarly(String roomId, Player winner) {
        String normalized = normalizeRoomId(roomId);
        GameRoom room = rooms.get(normalized);
        if (room == null) return;
        int pot = room.getBettingRound().getTotalPot();
        winner.setScore(winner.getScore() + pot);
        room.getBettingRound().reset();
        room.setPhase(GamePhase.SHOWDOWN);
    }

    public void processShowdown(String roomId) {
        String normalized = normalizeRoomId(roomId);
        GameRoom room = rooms.get(normalized);
        if (room == null) return;

        List<Player> activePlayers = room.getActivePlayers();
        if (activePlayers.size() <= 1) {
            if (activePlayers.size() == 1) {
                int pot = room.getBettingRound().getTotalPot();
                activePlayers.get(0).setScore(activePlayers.get(0).getScore() + pot);
                room.getBettingRound().reset();
                room.setPhase(GamePhase.SHOWDOWN);
            }
            return;
        }

        List<Card> communityCards = room.getCommunityCards();
        List<Hand> hands = new ArrayList<>();
        List<String> playerIds = new ArrayList<>();
        for (Player player : activePlayers) {
            Hand hand = player.getHand();
            for (Card c : communityCards) {
                hand.addCommunityCard(c);
            }
            hands.add(hand);
            playerIds.add(player.getId());
        }

        String winnerId = pokerEngine.determineWinner(hands, playerIds);
        int pot = room.getBettingRound().getTotalPot();
        Optional<Player> winnerOpt = room.findPlayer(winnerId);
        if (winnerOpt.isPresent()) {
            winnerOpt.get().setScore(winnerOpt.get().getScore() + pot);
        }
        room.getBettingRound().reset();
        room.setPhase(GamePhase.SHOWDOWN);
    }

    public String getCurrentTurnPlayerId(String roomId) {
        String normalized = normalizeRoomId(roomId);
        GameRoom room = rooms.get(normalized);
        if (room == null) return null;
        Player player = room.getCurrentTurnPlayer();
        return player != null ? player.getId() : null;
    }

    public Optional<GameRoom> getRoom(String roomId) {
        String normalized = normalizeRoomId(roomId);
        return Optional.ofNullable(rooms.get(normalized));
    }

    public void removeRoom(String roomId) {
        String normalized = normalizeRoomId(roomId);
        rooms.remove(normalized);
    }

    private String generateRoomId() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

    private String normalizeRoomId(String roomId) {
        if (roomId == null) return null;
        try {
            int num = Integer.parseInt(roomId.trim());
            return String.format("%04d", num);
        } catch (NumberFormatException e) {
            return roomId;
        }
    }
}