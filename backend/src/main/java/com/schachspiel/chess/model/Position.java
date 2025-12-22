package com.schachspiel.chess.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private int row;
    private int col;
    
    public Position(String notation) {
        // Convert chess notation (e.g., "e4") to row/col
        this.col = notation.charAt(0) - 'a';
        this.row = notation.charAt(1) - '1';
    }
    
    public String toNotation() {
        return String.valueOf((char)('a' + col)) + (row + 1);
    }
    
    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
