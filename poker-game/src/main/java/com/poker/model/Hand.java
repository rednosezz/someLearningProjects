package com.poker.model;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private final List<Card> holeCards;
    private final List<Card> communityCards;

    public Hand() {
        this.holeCards = new ArrayList<>();
        this.communityCards = new ArrayList<>();
    }

    public Hand(List<Card> holeCards, List<Card> communityCards) {
        this.holeCards = new ArrayList<>(holeCards);
        this.communityCards = new ArrayList<>(communityCards);
    }

    public List<Card> getHoleCards() {
        return new ArrayList<>(holeCards);
    }

    public List<Card> getCommunityCards() {
        return new ArrayList<>(communityCards);
    }

    public List<Card> getAllCards() {
        List<Card> all = new ArrayList<>(holeCards);
        all.addAll(communityCards);
        return all;
    }

    public void addHoleCard(Card card) {
        this.holeCards.add(card);
    }

    public void addCommunityCard(Card card) {
        this.communityCards.add(card);
    }

    public void clear() {
        this.holeCards.clear();
        this.communityCards.clear();
    }
}
