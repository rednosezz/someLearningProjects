package com.poker.engine;

import com.poker.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PokerEngineTest {

    private PokerEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PokerEngine();
    }

    // === 高牌测试 ===
    @Test
    void highCardVsHighCard_differentValues() {
        // A高牌 > K高牌
        Hand handA = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.TWO)),
            List.of(new Card(Suit.SPADES, Rank.THREE), new Card(Suit.DIAMONDS, Rank.FOUR), new Card(Suit.CLUBS, Rank.FIVE))
        );
        Hand handK = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.KING), new Card(Suit.HEARTS, Rank.TWO)),
            List.of(new Card(Suit.SPADES, Rank.THREE), new Card(Suit.DIAMONDS, Rank.FOUR), new Card(Suit.CLUBS, Rank.FIVE))
        );
        assertEquals(1, engine.compare(handA, handK));
    }

    @Test
    void highCardVsHighCard_sameValues_returnsZero() {
        Hand hand1 = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.KING)),
            List.of(new Card(Suit.SPADES, Rank.QUEEN), new Card(Suit.DIAMONDS, Rank.JACK), new Card(Suit.CLUBS, Rank.TEN))
        );
        Hand hand2 = makeHand(
            List.of(new Card(Suit.HEARTS, Rank.ACE), new Card(Suit.SPADES, Rank.KING)),
            List.of(new Card(Suit.DIAMONDS, Rank.QUEEN), new Card(Suit.CLUBS, Rank.JACK), new Card(Suit.SPADES, Rank.TEN))
        );
        assertEquals(0, engine.compare(hand1, hand2));
    }

    // === 对子类测试 ===
    @Test
    void onePairBeatsHighCard() {
        Hand onePair = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.ACE)),
            List.of(new Card(Suit.SPADES, Rank.TWO), new Card(Suit.DIAMONDS, Rank.THREE), new Card(Suit.CLUBS, Rank.FOUR))
        );
        Hand highCard = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.KING), new Card(Suit.HEARTS, Rank.QUEEN)),
            List.of(new Card(Suit.SPADES, Rank.TWO), new Card(Suit.DIAMONDS, Rank.THREE), new Card(Suit.CLUBS, Rank.FOUR))
        );
        assertTrue(engine.compare(onePair, highCard) > 0);
    }

    @Test
    void higherPairWins() {
        Hand pairOfA = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.ACE)),
            List.of(new Card(Suit.SPADES, Rank.TWO), new Card(Suit.DIAMONDS, Rank.THREE), new Card(Suit.CLUBS, Rank.FOUR))
        );
        Hand pairOfK = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.KING), new Card(Suit.HEARTS, Rank.KING)),
            List.of(new Card(Suit.SPADES, Rank.TWO), new Card(Suit.DIAMONDS, Rank.THREE), new Card(Suit.CLUBS, Rank.FOUR))
        );
        assertEquals(1, engine.compare(pairOfA, pairOfK));
    }

    @Test
    void twoPairBeatsOnePair() {
        Hand twoPair = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.ACE)),
            List.of(new Card(Suit.SPADES, Rank.KING), new Card(Suit.DIAMONDS, Rank.KING), new Card(Suit.CLUBS, Rank.TWO))
        );
        Hand onePair = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.QUEEN), new Card(Suit.HEARTS, Rank.QUEEN)),
            List.of(new Card(Suit.SPADES, Rank.ACE), new Card(Suit.DIAMONDS, Rank.KING), new Card(Suit.CLUBS, Rank.TWO))
        );
        assertEquals(1, engine.compare(twoPair, onePair));
    }

    @Test
    void higherTwoPairWins() {
        Hand twoPairAK = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.ACE)),
            List.of(new Card(Suit.SPADES, Rank.KING), new Card(Suit.DIAMONDS, Rank.KING), new Card(Suit.CLUBS, Rank.TWO))
        );
        Hand twoPairKQ = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.KING), new Card(Suit.HEARTS, Rank.KING)),
            List.of(new Card(Suit.SPADES, Rank.QUEEN), new Card(Suit.DIAMONDS, Rank.QUEEN), new Card(Suit.CLUBS, Rank.TWO))
        );
        assertEquals(1, engine.compare(twoPairAK, twoPairKQ));
    }

    @Test
    void threeOfAKindBeatsTwoPair() {
        Hand threeOfAKind = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.ACE)),
            List.of(new Card(Suit.SPADES, Rank.ACE), new Card(Suit.DIAMONDS, Rank.KING), new Card(Suit.CLUBS, Rank.TWO))
        );
        Hand twoPair = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.KING), new Card(Suit.HEARTS, Rank.KING)),
            List.of(new Card(Suit.SPADES, Rank.QUEEN), new Card(Suit.DIAMONDS, Rank.QUEEN), new Card(Suit.CLUBS, Rank.TWO))
        );
        assertEquals(1, engine.compare(threeOfAKind, twoPair));
    }

    @Test
    void straightBeatsThreeOfAKind() {
        Hand straight = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.SIX), new Card(Suit.HEARTS, Rank.SEVEN)),
            List.of(new Card(Suit.SPADES, Rank.EIGHT), new Card(Suit.DIAMONDS, Rank.NINE), new Card(Suit.CLUBS, Rank.TEN))
        );
        Hand threeOfAKind = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.ACE)),
            List.of(new Card(Suit.SPADES, Rank.ACE), new Card(Suit.DIAMONDS, Rank.KING), new Card(Suit.CLUBS, Rank.TWO))
        );
        assertEquals(1, engine.compare(straight, threeOfAKind));
    }

    @Test
    void flushBeatsStraight() {
        Hand flush = makeHand(
            List.of(new Card(Suit.HEARTS, Rank.TWO), new Card(Suit.HEARTS, Rank.FIVE)),
            List.of(new Card(Suit.HEARTS, Rank.SEVEN), new Card(Suit.HEARTS, Rank.NINE), new Card(Suit.HEARTS, Rank.KING))
        );
        Hand straight = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.SIX), new Card(Suit.HEARTS, Rank.SEVEN)),
            List.of(new Card(Suit.SPADES, Rank.EIGHT), new Card(Suit.DIAMONDS, Rank.NINE), new Card(Suit.CLUBS, Rank.TEN))
        );
        assertEquals(1, engine.compare(flush, straight));
    }

    @Test
    void fullHouseBeatsFlush() {
        Hand fullHouse = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.ACE)),
            List.of(new Card(Suit.SPADES, Rank.ACE), new Card(Suit.DIAMONDS, Rank.KING), new Card(Suit.CLUBS, Rank.KING))
        );
        Hand flush = makeHand(
            List.of(new Card(Suit.HEARTS, Rank.TWO), new Card(Suit.HEARTS, Rank.FIVE)),
            List.of(new Card(Suit.HEARTS, Rank.SEVEN), new Card(Suit.HEARTS, Rank.NINE), new Card(Suit.HEARTS, Rank.KING))
        );
        assertEquals(1, engine.compare(fullHouse, flush));
    }

    @Test
    void fourOfAKindBeatsFullHouse() {
        Hand fourOfAKind = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.ACE)),
            List.of(new Card(Suit.SPADES, Rank.ACE), new Card(Suit.DIAMONDS, Rank.ACE), new Card(Suit.CLUBS, Rank.KING))
        );
        Hand fullHouse = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.KING), new Card(Suit.HEARTS, Rank.KING)),
            List.of(new Card(Suit.SPADES, Rank.KING), new Card(Suit.DIAMONDS, Rank.QUEEN), new Card(Suit.CLUBS, Rank.QUEEN))
        );
        assertEquals(1, engine.compare(fourOfAKind, fullHouse));
    }

    @Test
    void straightFlushBeatsFourOfAKind() {
        Hand straightFlush = makeHand(
            List.of(new Card(Suit.HEARTS, Rank.SIX), new Card(Suit.HEARTS, Rank.SEVEN)),
            List.of(new Card(Suit.HEARTS, Rank.EIGHT), new Card(Suit.HEARTS, Rank.NINE), new Card(Suit.HEARTS, Rank.TEN))
        );
        Hand fourOfAKind = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.ACE)),
            List.of(new Card(Suit.SPADES, Rank.ACE), new Card(Suit.DIAMONDS, Rank.ACE), new Card(Suit.CLUBS, Rank.KING))
        );
        assertEquals(1, engine.compare(straightFlush, fourOfAKind));
    }

    @Test
    void royalFlushAlwaysWins() {
        Hand royalFlush = makeHand(
            List.of(new Card(Suit.HEARTS, Rank.TEN), new Card(Suit.HEARTS, Rank.JACK)),
            List.of(new Card(Suit.HEARTS, Rank.QUEEN), new Card(Suit.HEARTS, Rank.KING), new Card(Suit.HEARTS, Rank.ACE))
        );
        Hand straightFlush = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.SIX), new Card(Suit.CLUBS, Rank.SEVEN)),
            List.of(new Card(Suit.CLUBS, Rank.EIGHT), new Card(Suit.CLUBS, Rank.NINE), new Card(Suit.CLUBS, Rank.TEN))
        );
        assertEquals(1, engine.compare(royalFlush, straightFlush));
    }

    // === 特殊顺子测试 ===
    @Test
    void a2345IsSmallestStraight() {
        // A2345 是最小的顺子
        Hand a2345 = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.ACE), new Card(Suit.HEARTS, Rank.TWO)),
            List.of(new Card(Suit.SPADES, Rank.THREE), new Card(Suit.DIAMONDS, Rank.FOUR), new Card(Suit.CLUBS, Rank.FIVE))
        );
        Hand six789Ten = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.SIX), new Card(Suit.HEARTS, Rank.SEVEN)),
            List.of(new Card(Suit.SPADES, Rank.EIGHT), new Card(Suit.DIAMONDS, Rank.NINE), new Card(Suit.CLUBS, Rank.TEN))
        );
        assertTrue(engine.compare(a2345, six789Ten) < 0);
    }

    @Test
    void tenToAceIsBiggestStraight() {
        // 10-J-Q-K-A 是最大的顺子
        Hand tenToAce = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.TEN), new Card(Suit.HEARTS, Rank.JACK)),
            List.of(new Card(Suit.SPADES, Rank.QUEEN), new Card(Suit.DIAMONDS, Rank.KING), new Card(Suit.CLUBS, Rank.ACE))
        );
        Hand nineToKing = makeHand(
            List.of(new Card(Suit.CLUBS, Rank.NINE), new Card(Suit.HEARTS, Rank.TEN)),
            List.of(new Card(Suit.SPADES, Rank.JACK), new Card(Suit.DIAMONDS, Rank.QUEEN), new Card(Suit.CLUBS, Rank.KING))
        );
        assertTrue(engine.compare(tenToAce, nineToKing) > 0);
    }

    // === determineWinner 测试 ===
    @Test
    void determineWinner_returnsCorrectPlayer() {
        Hand hand1 = new Hand();
        hand1.addHoleCard(new Card(Suit.CLUBS, Rank.ACE));
        hand1.addHoleCard(new Card(Suit.HEARTS, Rank.ACE));
        hand1.addCommunityCard(new Card(Suit.SPADES, Rank.TWO));
        hand1.addCommunityCard(new Card(Suit.DIAMONDS, Rank.THREE));
        hand1.addCommunityCard(new Card(Suit.CLUBS, Rank.FOUR));

        Hand hand2 = new Hand();
        hand2.addHoleCard(new Card(Suit.CLUBS, Rank.KING));
        hand2.addHoleCard(new Card(Suit.HEARTS, Rank.QUEEN));
        hand2.addCommunityCard(new Card(Suit.SPADES, Rank.TWO));
        hand2.addCommunityCard(new Card(Suit.DIAMONDS, Rank.THREE));
        hand2.addCommunityCard(new Card(Suit.CLUBS, Rank.FOUR));

        List<Hand> hands = List.of(hand1, hand2);
        List<String> playerIds = List.of("p1", "p2");
        String winner = engine.determineWinner(hands, playerIds);
        assertEquals("p1", winner);
    }

    private Hand makeHand(List<Card> holeCards, List<Card> communityCards) {
        Hand hand = new Hand();
        for (Card c : holeCards) hand.addHoleCard(c);
        for (Card c : communityCards) hand.addCommunityCard(c);
        return hand;
    }
}