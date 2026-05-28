package com.poker.model;

public record Card(Suit suit, Rank rank) {

    public String display() {
        String suitSymbol = switch (suit) {
            case CLUBS -> "♣";
            case DIAMONDS -> "♦";
            case HEARTS -> "♥";
            case SPADES -> "♠";
        };
        String rankStr = switch (rank) {
            case TWO -> "2";
            case THREE -> "3";
            case FOUR -> "4";
            case FIVE -> "5";
            case SIX -> "6";
            case SEVEN -> "7";
            case EIGHT -> "8";
            case NINE -> "9";
            case TEN -> "10";
            case JACK -> "J";
            case QUEEN -> "Q";
            case KING -> "K";
            case ACE -> "A";
        };
        return suitSymbol + rankStr;
    }

    public int rankValue() {
        return rank.value();
    }

    public boolean isHigherThan(Card other) {
        return this.rankValue() > other.rankValue();
    }
}
