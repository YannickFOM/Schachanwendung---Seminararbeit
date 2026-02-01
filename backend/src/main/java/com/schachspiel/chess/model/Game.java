package com.schachspiel.chess.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Repräsentiert eine Spielinstanz (Session).
 * <p>
 * Diese Klasse speichert den gesamten Zustand eines Spiels, inklusive
 * serialisiertem Schachbrett, Spielernamen, Zeitlimits und Status.
 * Sie dient als Datentransferobjekt (DTO) für die API und als
 * Entität für die Speicherung.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    /** Eindeutige ID des Spiels. */
    private Long id;

    /** Name des Spielers (Weiß). */
    private String whitePlayer;

    /** Name des Spielers (Schwarz). */
    private String blackPlayer;

    /** Das Schachbrett als JSON-String serialisiert. */
    private String boardState;

    /** Die Historie der Züge als JSON-String (oder referenziert). */
    private String moveHistory;

    /** Wer ist gerade am Zug? */
    private PieceColor currentTurn;

    /** Der Gewinner des Spiels (null, wenn noch nicht entschieden). */
    private String winner; // "WHITE" or "BLACK"

    /** Der aktuelle Status (LÄUFT, MATT, etc.). */
    private GameStatus status;

    /** Zeitstempel der Erstellung. */
    private LocalDateTime createdAt;

    /** Zeitstempel des letzten Zuges (für Timer-Berechnung). */
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
