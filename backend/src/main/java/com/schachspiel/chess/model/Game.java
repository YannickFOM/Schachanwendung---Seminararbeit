package com.schachspiel.chess.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    private Long id;

    private String whitePlayer;

    private String blackPlayer;

    private String boardState;

    private String moveHistory;

    private PieceColor currentTurn;

    private String winner; // "WHITE" or "BLACK"

    private GameStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime lastMoveAt;

    private boolean isOnlineMode;

    private boolean isCheck;

    // Time in seconds. null means unlimited.
    private Integer timeLimit;

    private Integer whiteTimeRemaining;

    private Integer blackTimeRemaining;

    public void onCreate() {
        createdAt = LocalDateTime.now();
        lastMoveAt = LocalDateTime.now();
    }

    public void onUpdate() {
        lastMoveAt = LocalDateTime.now();
    }
}
