package com.poker.engine;

import com.poker.model.Card;
import com.poker.model.Hand;

import java.util.List;

public class PokerEngine {

    private final HandEvaluator evaluator = new HandEvaluator();

    public int compare(Hand left, Hand right) {
        HandResult resultLeft = evaluator.evaluate(left.getAllCards());
        HandResult resultRight = evaluator.evaluate(right.getAllCards());
        return compareKeys(resultLeft.key(), resultRight.key());
    }

    public String determineWinner(List<Hand> hands, List<String> playerIds) {
        if (hands == null || hands.isEmpty() || playerIds == null || hands.size() != playerIds.size()) {
            return null;
        }
        if (hands.size() == 1) {
            return playerIds.get(0);
        }

        int winnerIndex = 0;
        Hand winnerHand = hands.get(0);

        for (int i = 1; i < hands.size(); i++) {
            int cmp = compare(winnerHand, hands.get(i));
            if (cmp < 0) {
                winnerIndex = i;
                winnerHand = hands.get(i);
            }
        }

        return playerIds.get(winnerIndex);
    }

    private int compareKeys(List<Integer> key1, List<Integer> key2) {
        for (int i = 0; i < Math.min(key1.size(), key2.size()); i++) {
            int cmp = Integer.compare(key1.get(i), key2.get(i));
            if (cmp != 0) return cmp;
        }
        return Integer.compare(key1.size(), key2.size());
    }

    public HandResult evaluate(Hand hand) {
        return evaluator.evaluate(hand.getAllCards());
    }

    public HandResult evaluate(List<Card> cards) {
        return evaluator.evaluate(cards);
    }
}
