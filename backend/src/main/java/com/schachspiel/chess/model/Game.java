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

    private GameStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime lastMoveAt;

    private boolean isOnlineMode;

    private boolean isCheck;

    public void onCreate() {
        createdAt = LocalDateTime.now();
        lastMoveAt = LocalDateTime.now();
    }

    public void onUpdate() {
        lastMoveAt = LocalDateTime.now();
    }
}
