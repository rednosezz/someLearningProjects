package com.poker.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BettingRound {
    private int currentBet;
    private final Map<String, Integer> playerBets;

    public BettingRound() {
        this.currentBet = 0;
        this.playerBets = new HashMap<>();
    }

    public void placeBet(String playerId, int amount) {
        int previousBet = playerBets.getOrDefault(playerId, 0);
        int newTotalBet = previousBet + amount;
        playerBets.put(playerId, newTotalBet);
        if (newTotalBet > currentBet) {
            currentBet = newTotalBet;
        }
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public int getPlayerBet(String playerId) {
        return playerBets.getOrDefault(playerId, 0);
    }

    public int getAmountToCall(String playerId) {
        return currentBet - playerBets.getOrDefault(playerId, 0);
    }

    public boolean isComplete(List<Player> allPlayers) {
        for (Player player : allPlayers) {
            if (player.getStatus() == PlayerStatus.FOLDED) {
                continue;
            }
            int amountToCall = getAmountToCall(player.getId());
            if (amountToCall > 0) {
                return false;
            }
        }
        return true;
    }

    public void reset() {
        currentBet = 0;
        playerBets.clear();
    }

    public int getTotalPot() {
        return playerBets.values().stream().mapToInt(Integer::intValue).sum();
    }
}