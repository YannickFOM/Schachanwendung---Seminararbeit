package com.schachspiel.chess.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("isCastling")
    private boolean isCastling;

    @JsonProperty("isEnPassant")
    private boolean isEnPassant;

    private PieceType promotionPiece;
}
