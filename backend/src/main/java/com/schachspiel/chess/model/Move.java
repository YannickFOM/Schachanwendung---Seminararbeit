package com.schachspiel.chess.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Repräsentiert einen Schachzug.
 * <p>
 * Enthält Start- und Zielkoordinaten sowie Metadaten über Spezialzüge
 * wie Rochade, En Passant oder Bauernumwandlung.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Move {
    /** Startfeld. */
    private Position from;

    /** Zielfeld. */
    private Position to;

    /** Die gezogene Figur (wird vom Backend gesetzt). */
    private Piece piece;

    /** Die geschlagene Figur (falls vorhanden). */
    private Piece capturedPiece;

    /** Markiert, ob dieser Zug eine Rochade ist. */
    @JsonProperty("isCastling")
    private boolean isCastling;

    /** Markiert, ob dieser Zug ein En Passant Schlag ist. */
    @JsonProperty("isEnPassant")
    private boolean isEnPassant;

    /**
     * Bei Bauernumwandlung: In welche Figur soll umgewandelt werden?
     * Standard: QUEEN.
     */
    private PieceType promotionPiece;
}
