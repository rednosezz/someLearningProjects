package com.poker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card dealOne() {
        return cards.remove(cards.size() - 1);
    }

    public void reset() {
        initialize();
        shuffle();
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }
}
