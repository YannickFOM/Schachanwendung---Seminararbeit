package com.schachspiel.chess.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Piece {
    private PieceType type;
    private PieceColor color;
    private boolean hasMoved;
    
    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }
}
