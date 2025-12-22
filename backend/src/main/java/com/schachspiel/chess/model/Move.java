package com.schachspiel.chess.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Move {
    private Position from;
    private Position to;
    private Piece piece;
    private Piece capturedPiece;
    private boolean isCastling;
    private boolean isEnPassant;
    private PieceType promotionPiece;
}
