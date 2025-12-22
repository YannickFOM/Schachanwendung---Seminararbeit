package com.schachspiel.chess.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String whitePlayer;
    
    @Column(nullable = false)
    private String blackPlayer;
    
    @Column(columnDefinition = "TEXT")
    private String boardState;
    
    @Column(columnDefinition = "TEXT")
    private String moveHistory;
    
    @Enumerated(EnumType.STRING)
    private PieceColor currentTurn;
    
    @Enumerated(EnumType.STRING)
    private GameStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastMoveAt;
    
    @Column
    private boolean isOnlineMode;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastMoveAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastMoveAt = LocalDateTime.now();
    }
}
