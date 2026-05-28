package com.poker.game;

import com.poker.model.Card;
import com.poker.model.Hand;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String id;
    private final String name;
    private final int seatIndex;
    private final List<Card> holeCards;
    private int score;
    private PlayerStatus status;

    public Player(String id, String name, int seatIndex) {
        this.id = id;
        this.name = name;
        this.seatIndex = seatIndex;
        this.holeCards = new ArrayList<>();
        this.score = 1000;
        this.status = PlayerStatus.PLAYING;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSeatIndex() {
        return seatIndex;
    }

    public List<Card> getHoleCards() {
        return new ArrayList<>(holeCards);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public void addHoleCard(Card card) {
        this.holeCards.add(card);
    }

    public void clearCards() {
        this.holeCards.clear();
    }

    public Hand getHand() {
        Hand hand = new Hand();
        for (Card c : holeCards) {
            hand.addHoleCard(c);
        }
        return hand;
    }
}
