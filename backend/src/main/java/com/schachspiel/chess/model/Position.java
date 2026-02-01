package com.schachspiel.chess.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Repräsentiert eine Koordinate auf dem Schachbrett (Reihe, Spalte).
 * <p>
 * Dient als Hilfsklasse für Berechnungen und konvertiert zwischen
 * interner Array-Indexierung (0-7) und Schach-Notation ("e4").
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private int row;
    private int col;

    /**
     * Erstellt eine Position aus Schachnotation (z.B. "e2").
     * 
     * @param notation Der String (z.B. "a1" bis "h8").
     */
    public Position(String notation) {
        // Convert chess notation (e.g., "e4") to row/col
        this.col = notation.charAt(0) - 'a';
        this.row = notation.charAt(1) - '1';
    }

    /**
     * Konvertiert die Position zurück in Schachnotation.
     * 
     * @return String wie "e4".
     */
    public String toNotation() {
        return String.valueOf((char) ('a' + col)) + (row + 1);
    }

    /**
     * Prüft, ob die Position innerhalb des 8x8 Brettes liegt.
     * 
     * @return true, wenn 0 <= row, col < 8.
     */
    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
