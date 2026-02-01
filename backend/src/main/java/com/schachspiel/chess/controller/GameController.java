package com.schachspiel.chess.controller;

import com.schachspiel.chess.model.Game;
import com.schachspiel.chess.model.Move;
import com.schachspiel.chess.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST-Controller für die Schach-API.
 * <p>
 * Stellt HTTP-Endpunkte bereit, über die Frontend und Backend kommunizieren.
 * </p>
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;

    /**
     * Erstellt ein neues Spiel.
     *
     * @param request Map mit 'whitePlayer', 'blackPlayer', 'timeLimit' etc.
     * @return Das neu erstellte Spiel als JSON.
     */
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

    /**
     * Ruft eine Liste aller aktiven Spiele ab.
     * 
     * @return Liste von Game-Objekten.
     */
    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    /**
     * Lädt den aktuellen Spielstand.
     *
     * @param id Die Spiel-ID.
     * @return Das Spiel-Objekt (inkl. Brettzustand und Zeiten).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable Long id) {
        return gameService.getGame(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Sucht Spiele eines bestimmten Spielers.
     * 
     * @param playerName Name des Spielers.
     * @return Liste der gefundenen Spiele.
     */
    @GetMapping("/player/{playerName}")
    public ResponseEntity<List<Game>> getPlayerGames(@PathVariable String playerName) {
        return ResponseEntity.ok(gameService.getGamesByPlayer(playerName));
    }

    /**
     * Führt einen Zug aus.
     *
     * @param id   Die ID des Spiels aus der URL.
     * @param move Der Zug (Start/Ziel) als JSON-Body.
     * @return Das aktualisierte SpielOder eine Fehlermeldung (HTTP 400), wenn der
     *         Zug ungültig ist.
     */
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

    /**
     * Berechnet alle gültigen Züge für eine Figur auf dem Feld.
     * Hilfreich für das Highlighting im Frontend.
     *
     * @param id  Spiel-ID.
     * @param row Zeile der Figur.
     * @param col Spalte der Figur.
     * @return Liste möglicher Züge (Moves).
     */
    @GetMapping("/{id}/valid-moves")
    public ResponseEntity<List<Move>> getValidMoves(
            @PathVariable Long id,
            @RequestParam int row,
            @RequestParam int col) {
        return ResponseEntity.ok(gameService.getValidMoves(id, row, col));
    }

    /**
     * Replay-Funktion: Liest den Brettzustand zu einem historischen Zeitpunkt.
     *
     * @param id   Spiel-ID.
     * @param move Die Nummer des Zugs (Index).
     * @return JSON mit 'boardState' und 'lastMove'.
     */
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
