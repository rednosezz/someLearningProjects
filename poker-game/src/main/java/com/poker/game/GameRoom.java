package com.poker.game;

import com.poker.engine.PokerEngine;
import com.poker.model.Card;
import com.poker.model.Deck;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameRoom {
    private final String roomId;
    private final List<Player> players;
    private GamePhase phase;
    private final List<Card> communityCards;
    private final Deck deck;
    private int dealerSeat;
    private final BettingRound bettingRound;
    private int currentTurnSeat;
    private final PokerEngine pokerEngine;
    private int readyCount;

    public GameRoom(String roomId) {
        this.roomId = roomId;
        this.players = new ArrayList<>();
        this.phase = GamePhase.WAITING;
        this.communityCards = new ArrayList<>();
        this.deck = new Deck();
        this.dealerSeat = 0;
        this.bettingRound = new BettingRound();
        this.currentTurnSeat = -1;
        this.pokerEngine = new PokerEngine();
        this.readyCount = 0;
    }

    public void resetReadyCount() {
        this.readyCount = 0;
    }

    public int incrementReadyCount() {
        return ++readyCount;
    }

    public boolean allPlayersReady() {
        return readyCount >= 2 && players.size() == 2;
    }

    public String getRoomId() {
        return roomId;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public List<Card> getCommunityCards() {
        return new ArrayList<>(communityCards);
    }

    public int getDealerSeat() {
        return dealerSeat;
    }

    public int getCurrentTurnSeat() {
        return currentTurnSeat;
    }

    public BettingRound getBettingRound() {
        return bettingRound;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public Optional<Player> findPlayer(String playerId) {
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst();
    }

    public void startNewGame() {
        deck.reset();
        communityCards.clear();
        bettingRound.reset();

        dealerSeat = (dealerSeat + 1) % 2;
        currentTurnSeat = (dealerSeat + 1) % 2;

        for (Player player : players) {
            player.clearCards();
            player.setStatus(PlayerStatus.PLAYING);
        }

        phase = GamePhase.PREFLOP;
    }

    public void dealHoleCards() {
        for (Player player : players) {
            player.addHoleCard(deck.dealOne());
            player.addHoleCard(deck.dealOne());
        }
    }

    public void dealCommunityCards(int count) {
        for (int i = 0; i < count; i++) {
            communityCards.add(deck.dealOne());
        }
    }

    public void nextPhase() {
        switch (phase) {
            case PREFLOP -> {
                phase = GamePhase.FLOP;
                dealCommunityCards(3);
            }
            case FLOP -> {
                phase = GamePhase.TURN;
                dealCommunityCards(1);
            }
            case TURN -> {
                phase = GamePhase.RIVER;
                dealCommunityCards(1);
            }
            case RIVER -> phase = GamePhase.SHOWDOWN;
            case SHOWDOWN -> phase = GamePhase.ENDED;
            default -> {}
        }
    }

    public List<Player> getActivePlayers() {
        List<Player> active = new ArrayList<>();
        for (Player player : players) {
            if (player.getStatus() != PlayerStatus.FOLDED) {
                active.add(player);
            }
        }
        return active;
    }

    public boolean isBettingRoundOver() {
        return bettingRound.isComplete(players);
    }

    public void switchTurn() {
        currentTurnSeat = (currentTurnSeat + 1) % 2;
        Player currentPlayer = players.get(currentTurnSeat);
        if (currentPlayer.getStatus() == PlayerStatus.FOLDED) {
            switchTurn();
        }
    }

    public Player getCurrentTurnPlayer() {
        if (currentTurnSeat >= 0 && currentTurnSeat < players.size()) {
            return players.get(currentTurnSeat);
        }
        return null;
    }

    public PokerEngine getPokerEngine() {
        return pokerEngine;
    }

    public boolean isFull() {
        return players.size() >= 2;
    }

    public boolean hasTwoPlayers() {
        return players.size() == 2;
    }
}
