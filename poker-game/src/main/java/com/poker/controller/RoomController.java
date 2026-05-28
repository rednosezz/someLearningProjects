package com.poker.controller;

import com.poker.game.GameController;
import com.poker.game.GameRoom;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    private final GameController gameController;

    public RoomController() {
        this.gameController = new GameController();
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable String roomId) {
        Optional<GameRoom> roomOpt = gameController.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        GameRoom room = roomOpt.get();
        return ResponseEntity.ok(Map.of(
            "roomId", room.getRoomId(),
            "phase", room.getPhase().name(),
            "playerCount", room.getPlayers().size()
        ));
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> body) {
        String playerName = body.get("playerName");
        GameRoom room = gameController.createRoom("temp_" + System.currentTimeMillis(), playerName);
        return ResponseEntity.ok(Map.of("roomId", room.getRoomId()));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId) {
        gameController.removeRoom(roomId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
