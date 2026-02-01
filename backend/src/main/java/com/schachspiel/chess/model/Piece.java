package com.schachspiel.chess.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Repräsentiert eine Schachfigur auf dem Brett.
 * <p>
 * Jede Figur hat einen Typ (z.B. Turm), eine Farbe (Schwarz/Weiß) und
 * einen Status, ob sie bereits bewegt wurde (wichtig für Rochade und
 * Bauernsprung).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Piece {
    private PieceType type;
    private PieceColor color;
    private boolean hasMoved;

    /**
     * Erstellt eine neue Figur.
     * 
     * @param type  Der Typ der Figur (z.B. PAWN).
     * @param color Die Farbe (WHITE/BLACK).
     */
    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }

    public PieceType getType() {
        return type;
    }

    public void setType(PieceType type) {
        this.type = type;
    }

    public PieceColor getColor() {
        return color;
    }

    public void setColor(PieceColor color) {
        this.color = color;
    }

    public boolean isHasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
}
