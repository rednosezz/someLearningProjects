package com.poker.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController();
    }

    @Test
    void testCreateAndJoinRoom() {
        GameRoom room = controller.createRoom("player1", "Alice");
        assertNotNull(room);
        assertEquals("player1", room.getPlayers().get(0).getId());
        assertEquals(1, room.getPlayers().size());

        Optional<GameRoom> joinedRoom = controller.joinRoom(room.getRoomId(), "player2", "Bob");
        assertTrue(joinedRoom.isPresent());
        assertEquals(2, joinedRoom.get().getPlayers().size());
    }

    @Test
    void testFoldWinsWithoutShowdown() {
        GameRoom room = controller.createRoom("player1", "Alice");
        controller.joinRoom(room.getRoomId(), "player2", "Bob");
        controller.startGame(room.getRoomId());

        Player player1 = room.getPlayers().get(0);
        Player player2 = room.getPlayers().get(1);

        assertEquals(2, room.getActivePlayers().size());

        controller.handleAction(room.getRoomId(), player1.getId(), GameAction.FOLD, 0);

        assertEquals(1, room.getActivePlayers().size());
    }

    @Test
    void testCheckAndCallCompletesRound() {
        GameRoom room = controller.createRoom("player1", "Alice");
        controller.joinRoom(room.getRoomId(), "player2", "Bob");
        controller.startGame(room.getRoomId());

        Player player1 = room.getPlayers().get(0);
        Player player2 = room.getPlayers().get(1);

        controller.handleAction(room.getRoomId(), player1.getId(), GameAction.CHECK, 0);
        controller.handleAction(room.getRoomId(), player2.getId(), GameAction.CHECK, 0);

        assertTrue(room.isBettingRoundOver());
    }

    @Test
    void testRaiseTriggersRebetting() {
        GameRoom room = controller.createRoom("player1", "Alice");
        controller.joinRoom(room.getRoomId(), "player2", "Bob");
        controller.startGame(room.getRoomId());

        Player player1 = room.getPlayers().get(0);
        Player player2 = room.getPlayers().get(1);

        int player1InitialScore = player1.getScore();
        controller.handleAction(room.getRoomId(), player1.getId(), GameAction.RAISE, 100);

        int raiseAmount = 100;
        assertEquals(player1InitialScore - raiseAmount, player1.getScore());

        int amountToCallForP2 = room.getBettingRound().getAmountToCall(player2.getId());
        assertEquals(100, amountToCallForP2);
    }

    @Test
    void testShowdownAfterRiver() {
        GameRoom room = controller.createRoom("player1", "Alice");
        controller.joinRoom(room.getRoomId(), "player2", "Bob");
        controller.startGame(room.getRoomId());

        Player player1 = room.getPlayers().get(0);
        Player player2 = room.getPlayers().get(1);

        for (int i = 0; i < 4; i++) {
            controller.handleAction(room.getRoomId(), player1.getId(), GameAction.CHECK, 0);
            controller.handleAction(room.getRoomId(), player2.getId(), GameAction.CHECK, 0);
        }

        assertEquals(GamePhase.SHOWDOWN, room.getPhase());
    }

    @Test
    void testWrongPlayerCannotAct() {
        GameRoom room = controller.createRoom("player1", "Alice");
        controller.joinRoom(room.getRoomId(), "player2", "Bob");
        controller.startGame(room.getRoomId());

        Player player2 = room.getPlayers().get(1);

        String currentTurnId = controller.getCurrentTurnPlayerId(room.getRoomId());
        assertNotEquals(player2.getId(), currentTurnId);
    }
}
