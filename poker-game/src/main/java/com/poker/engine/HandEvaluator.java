package com.poker.engine;

import com.poker.model.Card;
import com.poker.model.Rank;

import java.util.*;

public class HandEvaluator {

    public HandResult evaluate(List<Card> all7Cards) {
        List<List<Card>> combinations = generateCombinations(all7Cards, 5);
        HandResult best = null;

        for (List<Card> combo : combinations) {
            HandResult result = evaluateHand(combo);
            if (best == null || compareKeys(result.key(), best.key()) > 0) {
                best = result;
            }
        }
        return best;
    }

    private List<List<Card>> generateCombinations(List<Card> cards, int k) {
        List<List<Card>> result = new ArrayList<>();
        backtrack(cards, k, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(List<Card> cards, int k, int start, List<Card> current, List<List<Card>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            backtrack(cards, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    public HandResult evaluateHand(List<Card> cards) {
        // 检查各牌型（从大到小）
        if (isRoyalFlush(cards)) {
            return makeResult(HandRank.ROYAL_FLUSH, cards, generateKey(HandRank.ROYAL_FLUSH, cards));
        }
        if (isStraightFlush(cards)) {
            return makeResult(HandRank.STRAIGHT_FLUSH, cards, generateKey(HandRank.STRAIGHT_FLUSH, cards));
        }
        if (isFourOfAKind(cards)) {
            return makeResult(HandRank.FOUR_OF_A_KIND, cards, generateKey(HandRank.FOUR_OF_A_KIND, cards));
        }
        if (isFullHouse(cards)) {
            return makeResult(HandRank.FULL_HOUSE, cards, generateKey(HandRank.FULL_HOUSE, cards));
        }
        if (isFlush(cards)) {
            return makeResult(HandRank.FLUSH, cards, generateKey(HandRank.FLUSH, cards));
        }
        if (isStraight(cards)) {
            return makeResult(HandRank.STRAIGHT, cards, generateKey(HandRank.STRAIGHT, cards));
        }
        if (isThreeOfAKind(cards)) {
            return makeResult(HandRank.THREE_OF_A_KIND, cards, generateKey(HandRank.THREE_OF_A_KIND, cards));
        }
        if (isTwoPair(cards)) {
            return makeResult(HandRank.TWO_PAIR, cards, generateKey(HandRank.TWO_PAIR, cards));
        }
        if (isOnePair(cards)) {
            return makeResult(HandRank.ONE_PAIR, cards, generateKey(HandRank.ONE_PAIR, cards));
        }
        return makeResult(HandRank.HIGH_CARD, cards, generateKey(HandRank.HIGH_CARD, cards));
    }

    private HandResult makeResult(HandRank rank, List<Card> cards, List<Integer> key) {
        return new HandResult(rank, new ArrayList<>(cards), key);
    }

    private boolean isRoyalFlush(List<Card> cards) {
        if (!isFlush(cards)) return false;
        List<Integer> values = getSortedValues(cards);
        return values.equals(Arrays.asList(10, 11, 12, 13, 14));
    }

    private boolean isStraightFlush(List<Card> cards) {
        return isFlush(cards) && isStraight(cards);
    }

    private boolean isFourOfAKind(List<Card> cards) {
        Map<Integer, Long> countMap = getCountMap(cards);
        return countMap.containsValue(4L);
    }

    private boolean isFullHouse(List<Card> cards) {
        Map<Integer, Long> countMap = getCountMap(cards);
        return countMap.containsValue(3L) && countMap.containsValue(2L);
    }

    private boolean isFlush(List<Card> cards) {
        if (cards.size() < 5) return false;
        var suit = cards.get(0).suit();
        return cards.stream().allMatch(c -> c.suit() == suit);
    }

    private boolean isStraight(List<Card> cards) {
        List<Integer> values = getSortedValues(cards);
        Set<Integer> unique = new HashSet<>(values);

        if (unique.size() != 5) return false;

        // A2345 是最小的顺子
        if (values.equals(Arrays.asList(2, 3, 4, 5, 14))) {
            return true;
        }

        for (int i = 0; i < 4; i++) {
            if (values.get(i + 1) - values.get(i) != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isThreeOfAKind(List<Card> cards) {
        Map<Integer, Long> countMap = getCountMap(cards);
        return countMap.containsValue(3L) && !countMap.containsValue(2L);
    }

    private boolean isTwoPair(List<Card> cards) {
        Map<Integer, Long> countMap = getCountMap(cards);
        long pairCount = countMap.values().stream().filter(v -> v == 2L).count();
        return pairCount == 2;
    }

    private boolean isOnePair(List<Card> cards) {
        Map<Integer, Long> countMap = getCountMap(cards);
        return countMap.containsValue(2L);
    }

    private List<Integer> getSortedValues(List<Card> cards) {
        List<Integer> values = new ArrayList<>();
        for (Card card : cards) {
            values.add(card.rankValue());
        }
        values.sort(Integer::compareTo);
        return values;
    }

    private Map<Integer, Long> getCountMap(List<Card> cards) {
        Map<Integer, Long> map = new HashMap<>();
        for (Card card : cards) {
            int v = card.rankValue();
            map.merge(v, 1L, Long::sum);
        }
        return map;
    }

    private int compareKeys(List<Integer> key1, List<Integer> key2) {
        for (int i = 0; i < Math.min(key1.size(), key2.size()); i++) {
            int cmp = Integer.compare(key1.get(i), key2.get(i));
            if (cmp != 0) return cmp;
        }
        return Integer.compare(key1.size(), key2.size());
    }

    private List<Integer> generateKey(HandRank rank, List<Card> cards) {
        Map<Integer, Long> countMap = getCountMap(cards);
        List<Integer> sortedValues = getSortedValues(cards);
        Collections.reverse(sortedValues);

        return switch (rank) {
            case HIGH_CARD -> {
                List<Integer> key = new ArrayList<>();
                key.add(1);
                key.addAll(sortedValues);
                yield key;
            }
            case ONE_PAIR -> {
                List<Integer> key = new ArrayList<>();
                key.add(2);
                int pairValue = countMap.entrySet().stream()
                    .filter(e -> e.getValue() == 2).findFirst().get().getKey();
                key.add(pairValue);
                sortedValues.stream().filter(v -> v != pairValue)
                    .limit(3).forEach(key::add);
                yield key;
            }
            case TWO_PAIR -> {
                List<Integer> key = new ArrayList<>();
                key.add(3);
                List<Integer> pairs = countMap.entrySet().stream()
                    .filter(e -> e.getValue() == 2)
                    .map(Map.Entry::getKey)
                    .sorted(Collections.reverseOrder())
                    .toList();
                key.add(pairs.get(0)); // 大对
                key.add(pairs.get(1)); // 小对
                sortedValues.stream().filter(v -> v != pairs.get(0) && v != pairs.get(1))
                    .findFirst().ifPresent(key::add);
                yield key;
            }
            case THREE_OF_A_KIND -> {
                List<Integer> key = new ArrayList<>();
                key.add(4);
                int tripleValue = countMap.entrySet().stream()
                    .filter(e -> e.getValue() == 3).findFirst().get().getKey();
                key.add(tripleValue);
                sortedValues.stream().filter(v -> v != tripleValue)
                    .limit(2).forEach(key::add);
                yield key;
            }
            case STRAIGHT -> {
                List<Integer> key = new ArrayList<>();
                key.add(5);
                // A2345是最小顺子
                if (sortedValues.equals(Arrays.asList(14, 5, 4, 3, 2))) {
                    key.add(5); // A当1用，5是最大牌
                } else {
                    key.add(sortedValues.get(0)); // 最大牌
                }
                yield key;
            }
            case FLUSH -> {
                List<Integer> key = new ArrayList<>();
                key.add(6);
                key.addAll(sortedValues);
                yield key;
            }
            case FULL_HOUSE -> {
                List<Integer> key = new ArrayList<>();
                key.add(7);
                int tripleValue = countMap.entrySet().stream()
                    .filter(e -> e.getValue() == 3).findFirst().get().getKey();
                int pairValue = countMap.entrySet().stream()
                    .filter(e -> e.getValue() == 2).findFirst().get().getKey();
                key.add(tripleValue);
                key.add(pairValue);
                yield key;
            }
            case FOUR_OF_A_KIND -> {
                List<Integer> key = new ArrayList<>();
                key.add(8);
                int quadValue = countMap.entrySet().stream()
                    .filter(e -> e.getValue() == 4).findFirst().get().getKey();
                key.add(quadValue);
                sortedValues.stream().filter(v -> v != quadValue)
                    .findFirst().ifPresent(key::add);
                yield key;
            }
            case STRAIGHT_FLUSH -> {
                List<Integer> key = new ArrayList<>();
                key.add(9);
                if (sortedValues.equals(Arrays.asList(14, 5, 4, 3, 2))) {
                    key.add(5);
                } else {
                    key.add(sortedValues.get(0));
                }
                yield key;
            }
            case ROYAL_FLUSH -> List.of(10, 0);
        };
    }
}