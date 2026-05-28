package com.poker.engine;

import com.poker.model.Card;

import java.util.List;

public record HandResult(
    HandRank rank,
    List<Card> best5,
    List<Integer> key
) {}