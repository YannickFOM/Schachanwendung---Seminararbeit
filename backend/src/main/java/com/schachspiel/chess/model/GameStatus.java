package com.schachspiel.chess.model;

/**
 * Enum für den Status eines Spiels.
 */
public enum GameStatus {
    WAITING,
    IN_PROGRESS,
    CHECKMATE,
    STALEMATE,
    DRAW,
    RESIGNED,
    VICTORY_BY_TIME
}
