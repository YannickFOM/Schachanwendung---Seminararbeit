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
@CrossOrigin(origins = "*")
public class GameController {
    
    @Autowired
    private GameService gameService;
    
    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody Map<String, Object> request) {
        String whitePlayer = (String) request.get("whitePlayer");
        String blackPlayer = (String) request.get("blackPlayer");
        boolean isOnlineMode = (boolean) request.getOrDefault("isOnlineMode", false);
        
        Game game = gameService.createGame(whitePlayer, blackPlayer, isOnlineMode);
        return ResponseEntity.ok(game);
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
        try {
            Game game = gameService.makeMove(id, move);
            return ResponseEntity.ok(game);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
