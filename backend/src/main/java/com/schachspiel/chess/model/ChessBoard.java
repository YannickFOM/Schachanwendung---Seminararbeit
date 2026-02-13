package com.schachspiel.chess.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repräsentiert das Schachbrett und beinhaltet die gesamte Spielregellogik.
 * <p>
 * Diese Klasse verwaltet das 8x8 Spielfeld, die Positionen der Figuren und
 * überprüft
 * die Gültigkeit von Zügen gemäß den offiziellen FIDE-Schachregeln.
 * Sie implementiert Logik für Spezialzüge wie Rochade und En Passant sowie
 * die Erkennung von Schach, Matt und Patt.
 * </p>
 */
@Data
public class ChessBoard {
    /** Das 8x8 Spielfeld. Indexierung: [0][0] = A8, [7][7] = H1. */
    private Piece[][] board;

    /** Die Farbe des Spielers, der aktuell am Zug ist. */
    private PieceColor currentTurn;

    /** Historie aller getätigten Züge (für Validierung und Analyse). */
    private List<Move> moveHistory;

    /**
     * Speichert das Ziel-Feld für einen En Passant Schlag, falls verfügbar.
     * Wenn ein Bauer einen Doppelschritt macht, wird das übersprungene Feld hier
     * markiert.
     */
    private Position enPassantTarget;

    /**
     * Erstellt ein neues Schachbrett in der Standard-Startaufstellung.
     */
    public ChessBoard() {
        this.board = new Piece[8][8];
        this.currentTurn = PieceColor.WHITE;
        this.moveHistory = new ArrayList<>();
        initializeBoard();
    }

    /**
     * Copy-Konstruktor zur Erstellung einer tiefen Kopie (Deep Copy) des Brettes.
     * <p>
     * Dieser Konstruktor wird hauptsächlich für die Zugsimulation verwendet, um zu
     * prüfen,
     * ob ein Zug den eigenen König im Schach zurücklässt, ohne das echte Brett zu
     * verändern.
     * </p>
     *
     * @param other Das zu kopierende Schachbrett.
     */
    public ChessBoard(ChessBoard other) {
        this.board = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (other.board[i][j] != null) {
                    Piece p = other.board[i][j];
                    this.board[i][j] = new Piece(p.getType(), p.getColor());
                    this.board[i][j].setHasMoved(p.isHasMoved());
                }
            }
        }
        this.currentTurn = other.currentTurn;
        this.moveHistory = new ArrayList<>(other.moveHistory);
        this.enPassantTarget = other.enPassantTarget;
    }

    /**
     * Erstellt eine exakte Kopie dieses Brettes.
     * 
     * @return Eine neue Instanz von ChessBoard mit identischem Zustand.
     */
    public ChessBoard copy() {
        return new ChessBoard(this);
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

    /**
     * Prüft, ob ein Zug nach den Schachregeln legal ist.
     * <p>
     * Ein Zug ist legal, wenn:
     * 1. Er den geometrischen Bewegungsregeln der Figur entspricht (`isValidMove`).
     * 2. Er den eigenen König nicht im Schach zurücklässt (Selbstschach-Verbot).
     * </p>
     *
     * @param move Der zu prüfende Zug.
     * @return true, wenn der Zug erlaubt ist, sonst false.
     */
    public boolean isLegalMove(Move move) {
        // 1. Check basic geometric rules
        if (!isValidMove(move)) {
            return false;
        }

        // 2. Simulate move to check if it leaves king in check
        ChessBoard simulation = this.copy();
        simulation.makeMove(move); // This now executes the move on the copy

        // 3. Make sure the current player's king is NOT in check after the move
        // Note: makeMove switches the turn, so we check "isInCheck" for the *previous*
        // turn color (who just moved)
        return !simulation.isInCheck(this.currentTurn);
    }

    public List<Move> getValidMoves(Position from) {
        List<Move> validMoves = new ArrayList<>();
        Piece piece = getPieceAt(from);

        if (piece == null || piece.getColor() != currentTurn) {
            return validMoves;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position to = new Position(row, col);
                Move move = new Move();
                move.setFrom(from);
                move.setTo(to);
                move.setPiece(piece);

                // We need to set up the move object completely for isValidMove to work
                // correctly for complex moves
                // but isValidMove handles most logic. isLegalMove handles the check simulation.
                if (isLegalMove(move)) {
                    validMoves.add(move);
                }
            }
        }
        return validMoves;
    }

    /**
     * Prüft die rein geometrische Gültigkeit eines Zuges für eine Figur.
     * <p>
     * Diese Methode prüft NICHT auf Schachgebote, sondern nur:
     * - Sind Start- und Zielkoordinaten auf dem Brett?
     * - Ist das Bewegunsmuster für den Figurentyp korrekt (z.B. Läufer diagonal)?
     * - Ist der Pfad frei (für Figuren, die nicht springen können)?
     * </p>
     *
     * @param move Der Zug.
     * @return true, wenn das Bewegungsmuster korrekt ist.
     */
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

        if (from.equals(to)) {
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

        // Loop until we reach the target (exclusive of target for captures, inclusive
        // if checking path)
        // Here we stop BEFORE the target
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

        // Check if path is clear
        if (!isPathClear(from, new Position(from.getRow(), rookCol))) {
            return false;
        }

        // Cannot castle out of check, through check, or into check
        // "Out of check" is handled by canCastle callsite usually, or isLegalMove check
        // "Into check" is handled by isLegalMove
        // "Through check" needs specific handling here or in isLegalMove

        // Check "through check" - the square the king crosses
        Position crossedSquare = new Position(from.getRow(), (from.getCol() + to.getCol()) / 2);

        // We can check this by seeing if the king would be attacked on the crossed
        // square
        // Simplified: we rely on isLegalMove logic to check destination, but for
        // "through check" we need extra logic.
        // For this task, we will verify the crossed square is safe.
        // However, isLegalMove only checks the END state.

        // Let's implement a quick check for the middle square safety for castling
        if (isInCheck(currentTurn)) {
            return false; // Cannot castle out of check
        }

        // Check if crossed square is under attack
        // This requires simulation or "isAttacked" logic.
        // Re-using isInCheck logic slightly modified or creating isSquareAttacked
        return !isSquareAttacked(crossedSquare, currentTurn == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE);
    }

    /**
     * Führt einen Zug auf dem Brett aus und aktualisiert den Spielzustand.
     * <p>
     * Diese Methode:
     * - Bewegt die Figur.
     * - Entfernt geschlagene Figuren (inkl. En Passant).
     * - Führt Bauernumwandlung (Promotion) durch.
     * - Bewegt den Turm bei einer Rochade.
     * - Aktualisiert die Zughistorie und wechselt den aktiven Spieler.
     * </p>
     *
     * @param move Der auszuführende Zug.
     */
    public void makeMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = getPieceAt(from);

        if (piece == null) {
            return;
        }

        move.setPiece(piece);
        move.setCapturedPiece(getPieceAt(to));

        // Auto-detect En Passant
        boolean isEnPassantMove = false;
        if (piece.getType() == PieceType.PAWN &&
                Math.abs(to.getCol() - from.getCol()) == 1 && // Diagonal
                getPieceAt(to) == null && // Empty target
                enPassantTarget != null && to.equals(enPassantTarget)) { // Matches EP target

            isEnPassantMove = true;
            move.setEnPassant(true);
        }

        // Handle En Passant Execution
        if (isEnPassantMove) {
            int captureRow = piece.getColor() == PieceColor.WHITE ? to.getRow() - 1 : to.getRow() + 1;
            board[captureRow][to.getCol()] = null; // Remove captured pawn
        }

        // Reset EP target for next turn (unless this move creates one)
        Position nextEnPassantTarget = null;

        if (piece.getType() == PieceType.PAWN) {
            int rowDiff = Math.abs(to.getRow() - from.getRow());
            if (rowDiff == 2) {
                // Set metadata for next turn
                nextEnPassantTarget = new Position((from.getRow() + to.getRow()) / 2, from.getCol());
            }

            // Promotion
            if ((to.getRow() == 7 && piece.getColor() == PieceColor.WHITE) ||
                    (to.getRow() == 0 && piece.getColor() == PieceColor.BLACK)) {
                PieceType promo = move.getPromotionPiece() != null ? move.getPromotionPiece() : PieceType.QUEEN;
                if (promo == PieceType.KING || promo == PieceType.PAWN) {
                    promo = PieceType.QUEEN;
                }
                piece.setType(promo);
            }
        }
        enPassantTarget = nextEnPassantTarget;

        // Handle castling
        if (piece.getType() == PieceType.KING && Math.abs(to.getCol() - from.getCol()) == 2) {
            move.setCastling(true);
            int rookFromCol = to.getCol() > from.getCol() ? 7 : 0;
            int rookToCol = to.getCol() > from.getCol() ? to.getCol() - 1 : to.getCol() + 1;
            Piece rook = board[from.getRow()][rookFromCol];
            board[from.getRow()][rookToCol] = rook;
            board[from.getRow()][rookFromCol] = null;
            if (rook != null)
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

        PieceColor attackerColor = color == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        return isSquareAttacked(kingPosition, attackerColor);
    }

    public boolean isCheckmate(PieceColor color) {
        if (!isInCheck(color)) {
            return false;
        }
        // If in check, check if any move can relieve it
        return !canAnyMoveEscapeCheck(color);
    }

    public boolean isStalemate(PieceColor color) {
        if (isInCheck(color)) {
            return false;
        }
        // If not in check, but no valid moves
        return !canAnyMoveEscapeCheck(color);
    }

    private boolean canAnyMoveEscapeCheck(PieceColor color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece != null && piece.getColor() == color) {
                    Position from = new Position(r, c);
                    List<Move> validMoves = getValidMoves(from);
                    if (!validMoves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSquareAttacked(Position target, PieceColor attackerColor) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() == attackerColor) {
                    if (canPieceAttackSquare(piece, new Position(row, col), target)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canPieceAttackSquare(Piece piece, Position from, Position to) {
        if (!to.isValid()) {
            return false;
        }

        return switch (piece.getType()) {
            case PAWN -> canPawnAttack(from, to, piece);
            case ROOK -> isValidRookMove(from, to);
            case KNIGHT -> isValidKnightMove(from, to);
            case BISHOP -> isValidBishopMove(from, to);
            case QUEEN -> isValidQueenMove(from, to);
            case KING -> isValidKingMove(from, to, piece); // Note: King attack range is just normal move
        };
    }

    private boolean canPawnAttack(Position from, Position to, Piece piece) {
        int direction = piece.getColor() == PieceColor.WHITE ? 1 : -1;
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = Math.abs(to.getCol() - from.getCol());

        // Pawns only attack diagonally
        return colDiff == 1 && rowDiff == direction;
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

    public boolean isInsufficientMaterial() {
        List<Piece> allPieces = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null) {
                    allPieces.add(board[r][c]);
                }
            }
        }

        // King vs King
        if (allPieces.size() == 2) {
            return true;
        }

        // King + Minor Piece vs King
        if (allPieces.size() == 3) {
            Piece minorPiece = allPieces.stream()
                    .filter(p -> p.getType() != PieceType.KING)
                    .findFirst()
                    .orElse(null);
            return minorPiece != null
                    && (minorPiece.getType() == PieceType.BISHOP || minorPiece.getType() == PieceType.KNIGHT);
        }

        // King + Bishop vs King + Bishop (Same color squares)
        if (allPieces.stream().allMatch(p -> p.getType() == PieceType.KING || p.getType() == PieceType.BISHOP)) {
            List<Position> bishopPositions = new ArrayList<>();
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Piece p = board[r][c];
                    if (p != null && p.getType() == PieceType.BISHOP) {
                        bishopPositions.add(new Position(r, c));
                    }
                }
            }

            if (bishopPositions.size() == 2 && allPieces.size() == 4) {
                Position b1 = bishopPositions.get(0);
                Position b2 = bishopPositions.get(1);
                boolean b1Light = (b1.getRow() + b1.getCol()) % 2 == 0;
                boolean b2Light = (b2.getRow() + b2.getCol()) % 2 == 0;
                return b1Light == b2Light;
            }
        }

        return false;
    }
}
