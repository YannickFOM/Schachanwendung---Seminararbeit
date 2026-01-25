package com.schachspiel.chess.controller;

import com.schachspiel.chess.model.Game;
import com.schachspiel.chess.model.Move;
import com.schachspiel.chess.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")

public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody Map<String, Object> request) {
        System.out.println("Received createGame request: " + request);
        try {
            String whitePlayer = (String) request.get("whitePlayer");
            String blackPlayer = (String) request.get("blackPlayer");
            boolean isOnlineMode = (boolean) request.getOrDefault("isOnlineMode", false);
            Integer timeLimit = request.containsKey("timeLimit") ? (Integer) request.get("timeLimit") : null;

            Game game = gameService.createGame(whitePlayer, blackPlayer, isOnlineMode, timeLimit);
            System.out.println("Created game: " + game);
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable Long id) {
        return gameService.getGame(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/player/{playerName}")
    public ResponseEntity<List<Game>> getPlayerGames(@PathVariable String playerName) {
        return ResponseEntity.ok(gameService.getGamesByPlayer(playerName));
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<?> makeMove(@PathVariable Long id, @RequestBody Move move) {
        System.out.println("Received move request for game " + id + ": " + move);
        try {
            Game game = gameService.makeMove(id, move);
            System.out.println("Move successful, new state: " + game.getBoardState());
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            System.out.println("Move failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/valid-moves")
    public ResponseEntity<List<Move>> getValidMoves(
            @PathVariable Long id,
            @RequestParam int row,
            @RequestParam int col) {
        return ResponseEntity.ok(gameService.getValidMoves(id, row, col));
    }

    @GetMapping("/{id}/board")
    public ResponseEntity<?> getBoardAtMove(
            @PathVariable Long id,
            @RequestParam int move) {
        try {
            return ResponseEntity.ok(gameService.getBoardAtMove(id, move));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
