package com.schachspiel.chess.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChessBoard {
    private Piece[][] board;
    private PieceColor currentTurn;
    private List<Move> moveHistory;
    private Position enPassantTarget;
    
    public ChessBoard() {
        this.board = new Piece[8][8];
        this.currentTurn = PieceColor.WHITE;
        this.moveHistory = new ArrayList<>();
        initializeBoard();
    }
    
    private void initializeBoard() {
        // Initialize pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Piece(PieceType.PAWN, PieceColor.WHITE);
            board[6][i] = new Piece(PieceType.PAWN, PieceColor.BLACK);
        }
        
        // Initialize rooks
        board[0][0] = new Piece(PieceType.ROOK, PieceColor.WHITE);
        board[0][7] = new Piece(PieceType.ROOK, PieceColor.WHITE);
        board[7][0] = new Piece(PieceType.ROOK, PieceColor.BLACK);
        board[7][7] = new Piece(PieceType.ROOK, PieceColor.BLACK);
        
        // Initialize knights
        board[0][1] = new Piece(PieceType.KNIGHT, PieceColor.WHITE);
        board[0][6] = new Piece(PieceType.KNIGHT, PieceColor.WHITE);
        board[7][1] = new Piece(PieceType.KNIGHT, PieceColor.BLACK);
        board[7][6] = new Piece(PieceType.KNIGHT, PieceColor.BLACK);
        
        // Initialize bishops
        board[0][2] = new Piece(PieceType.BISHOP, PieceColor.WHITE);
        board[0][5] = new Piece(PieceType.BISHOP, PieceColor.WHITE);
        board[7][2] = new Piece(PieceType.BISHOP, PieceColor.BLACK);
        board[7][5] = new Piece(PieceType.BISHOP, PieceColor.BLACK);
        
        // Initialize queens
        board[0][3] = new Piece(PieceType.QUEEN, PieceColor.WHITE);
        board[7][3] = new Piece(PieceType.QUEEN, PieceColor.BLACK);
        
        // Initialize kings
        board[0][4] = new Piece(PieceType.KING, PieceColor.WHITE);
        board[7][4] = new Piece(PieceType.KING, PieceColor.BLACK);
    }
    
    public Piece getPieceAt(Position position) {
        if (!position.isValid()) {
            return null;
        }
        return board[position.getRow()][position.getCol()];
    }
    
    public void setPieceAt(Position position, Piece piece) {
        if (position.isValid()) {
            board[position.getRow()][position.getCol()] = piece;
        }
    }
    
    public boolean isValidMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = getPieceAt(from);
        
        if (piece == null || piece.getColor() != currentTurn) {
            return false;
        }
        
        if (!to.isValid()) {
            return false;
        }
        
        Piece targetPiece = getPieceAt(to);
        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
            return false;
        }
        
        return switch (piece.getType()) {
            case PAWN -> isValidPawnMove(from, to, piece);
            case ROOK -> isValidRookMove(from, to);
            case KNIGHT -> isValidKnightMove(from, to);
            case BISHOP -> isValidBishopMove(from, to);
            case QUEEN -> isValidQueenMove(from, to);
            case KING -> isValidKingMove(from, to, piece);
        };
    }
    
    private boolean isValidPawnMove(Position from, Position to, Piece piece) {
        int direction = piece.getColor() == PieceColor.WHITE ? 1 : -1;
        int startRow = piece.getColor() == PieceColor.WHITE ? 1 : 6;
        
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = Math.abs(to.getCol() - from.getCol());
        
        // Forward move
        if (colDiff == 0) {
            if (rowDiff == direction && getPieceAt(to) == null) {
                return true;
            }
            // Initial two-square move
            if (from.getRow() == startRow && rowDiff == 2 * direction) {
                Position intermediate = new Position(from.getRow() + direction, from.getCol());
                return getPieceAt(intermediate) == null && getPieceAt(to) == null;
            }
        }
        
        // Capture move
        if (colDiff == 1 && rowDiff == direction) {
            Piece target = getPieceAt(to);
            if (target != null && target.getColor() != piece.getColor()) {
                return true;
            }
            // En passant
            if (enPassantTarget != null && to.equals(enPassantTarget)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isValidRookMove(Position from, Position to) {
        if (from.getRow() != to.getRow() && from.getCol() != to.getCol()) {
            return false;
        }
        return isPathClear(from, to);
    }
    
    private boolean isValidKnightMove(Position from, Position to) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
    
    private boolean isValidBishopMove(Position from, Position to) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());
        if (rowDiff != colDiff) {
            return false;
        }
        return isPathClear(from, to);
    }
    
    private boolean isValidQueenMove(Position from, Position to) {
        return isValidRookMove(from, to) || isValidBishopMove(from, to);
    }
    
    private boolean isValidKingMove(Position from, Position to, Piece piece) {
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());
        
        // Normal king move
        if (rowDiff <= 1 && colDiff <= 1) {
            return true;
        }
        
        // Castling
        if (!piece.isHasMoved() && rowDiff == 0 && colDiff == 2) {
            return canCastle(from, to);
        }
        
        return false;
    }
    
    private boolean isPathClear(Position from, Position to) {
        int rowStep = Integer.compare(to.getRow(), from.getRow());
        int colStep = Integer.compare(to.getCol(), from.getCol());
        
        int currentRow = from.getRow() + rowStep;
        int currentCol = from.getCol() + colStep;
        
        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return true;
    }
    
    private boolean canCastle(Position from, Position to) {
        int rookCol = to.getCol() > from.getCol() ? 7 : 0;
        Piece rook = board[from.getRow()][rookCol];
        
        if (rook == null || rook.getType() != PieceType.ROOK || rook.isHasMoved()) {
            return false;
        }
        
        return isPathClear(from, new Position(from.getRow(), rookCol));
    }
    
    public void makeMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = getPieceAt(from);
        
        if (piece == null) {
            return;
        }
        
        move.setPiece(piece);
        move.setCapturedPiece(getPieceAt(to));
        
        // Handle en passant
        enPassantTarget = null;
        if (piece.getType() == PieceType.PAWN) {
            int rowDiff = Math.abs(to.getRow() - from.getRow());
            if (rowDiff == 2) {
                enPassantTarget = new Position((from.getRow() + to.getRow()) / 2, from.getCol());
            }
            
            // Capture en passant
            if (move.isEnPassant()) {
                int captureRow = piece.getColor() == PieceColor.WHITE ? to.getRow() - 1 : to.getRow() + 1;
                board[captureRow][to.getCol()] = null;
            }
            
            // Promotion
            if ((to.getRow() == 7 && piece.getColor() == PieceColor.WHITE) ||
                (to.getRow() == 0 && piece.getColor() == PieceColor.BLACK)) {
                piece.setType(move.getPromotionPiece() != null ? move.getPromotionPiece() : PieceType.QUEEN);
            }
        }
        
        // Handle castling
        if (piece.getType() == PieceType.KING && Math.abs(to.getCol() - from.getCol()) == 2) {
            move.setCastling(true);
            int rookFromCol = to.getCol() > from.getCol() ? 7 : 0;
            int rookToCol = to.getCol() > from.getCol() ? to.getCol() - 1 : to.getCol() + 1;
            Piece rook = board[from.getRow()][rookFromCol];
            board[from.getRow()][rookToCol] = rook;
            board[from.getRow()][rookFromCol] = null;
            rook.setHasMoved(true);
        }
        
        // Move the piece
        setPieceAt(to, piece);
        setPieceAt(from, null);
        piece.setHasMoved(true);
        
        // Add to history and switch turn
        moveHistory.add(move);
        currentTurn = currentTurn == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
    }
    
    public boolean isInCheck(PieceColor color) {
        Position kingPosition = findKing(color);
        if (kingPosition == null) {
            return false;
        }
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() != color) {
                    Move testMove = new Move(new Position(row, col), kingPosition, piece, null, false, false, null);
                    if (isValidMove(testMove)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private Position findKing(PieceColor color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }
}
