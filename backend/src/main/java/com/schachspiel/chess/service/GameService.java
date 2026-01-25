package com.schachspiel.chess.service;

import com.schachspiel.chess.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    // In-memory storage
    private final java.util.Map<Long, Game> games = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicLong idGenerator = new java.util.concurrent.atomic.AtomicLong(1);

    @Autowired
    private ObjectMapper objectMapper;

    public Game createGame(String whitePlayer, String blackPlayer, boolean isOnlineMode, Integer timeLimit) {
        Game game = new Game();
        game.setId(idGenerator.getAndIncrement());
        game.setWhitePlayer(whitePlayer);
        game.setBlackPlayer(blackPlayer);
        game.setCurrentTurn(PieceColor.WHITE);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setOnlineMode(isOnlineMode);

        // Parse time limit (default to null/unlimited if not provided or 0)
        // Only if > 0 we consider it a limit
        Integer timeLimitVal = null; // Default unlimited
        // Logic to be passed from controller, for now we assume it might be passed
        // later or defaulting
        // We will update controller to pass this. For now let's modify signature or
        // wait?
        // Let's rely on standard logic:
        // We need to update the method signature or handle it in the map request in
        // controller.

        game.onCreate();

        game.setTimeLimit(timeLimit);
        if (timeLimit != null && timeLimit > 0) {
            game.setWhiteTimeRemaining(timeLimit);
            game.setBlackTimeRemaining(timeLimit);
        }

        ChessBoard board = new ChessBoard();
        game.setBoardState(serializeBoard(board));
        game.setMoveHistory("[]");

        games.put(game.getId(), game);
        return game;
    }

    public Optional<Game> getGame(Long id) {
        return Optional.ofNullable(games.get(id));
    }

    public List<Game> getAllGames() {
        return new ArrayList<>(games.values());
    }

    public List<Game> getGamesByPlayer(String playerName) {
        return games.values().stream()
                .filter(g -> playerName.equals(g.getWhitePlayer()) || playerName.equals(g.getBlackPlayer()))
                .collect(java.util.stream.Collectors.toList());
    }

    public Game makeMove(Long gameId, Move move) throws Exception {
        Game game = games.get(gameId);
        if (game == null) {
            throw new Exception("Game not found");
        }

        // Ensure it's the correct turn
        // Note: Client might send moves out of turn, verify locally
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new Exception("Game is over");
        }

        ChessBoard board = deserializeBoard(game.getBoardState());

        // Use IS LEGAL MOVE (checks king safety) instead of just valid move
        if (!board.isLegalMove(move)) {
            throw new Exception("Invalid move or King is in check");
        }

        board.makeMove(move);

        game.setBoardState(serializeBoard(board));

        // Time Calculation
        if (game.getTimeLimit() != null && game.getTimeLimit() > 0) {
            long elapsedSeconds = java.time.Duration.between(game.getLastMoveAt(), java.time.LocalDateTime.now())
                    .getSeconds();

            if (game.getCurrentTurn() == PieceColor.WHITE) {
                int remaining = (int) Math.max(0, game.getWhiteTimeRemaining() - elapsedSeconds);
                game.setWhiteTimeRemaining(remaining);
                if (remaining == 0) {
                    game.setStatus(GameStatus.VICTORY_BY_TIME);
                    game.setWinner("BLACK");
                }
            } else {
                int remaining = (int) Math.max(0, game.getBlackTimeRemaining() - elapsedSeconds);
                game.setBlackTimeRemaining(remaining);
                if (remaining == 0) {
                    game.setStatus(GameStatus.VICTORY_BY_TIME);
                    game.setWinner("WHITE");
                }
            }
        }

        game.setCurrentTurn(board.getCurrentTurn());
        game.setMoveHistory(addMoveToHistory(game.getMoveHistory(), move));
        game.onUpdate();

        // Check for checkmate or stalemate
        boolean inCheck = board.isInCheck(board.getCurrentTurn());
        game.setCheck(inCheck);

        if (board.isCheckmate(board.getCurrentTurn())) {
            game.setStatus(GameStatus.CHECKMATE);
            // If current turn (who just moved? No, checkmate checks if CURRENT turn player
            // has no moves)
            // If white moved, it passes turn to black. Checkmate checks if black has moves.
            // If black has no moves and is in check -> Black is mated. White wins.
            game.setWinner(board.getCurrentTurn() == PieceColor.WHITE ? "BLACK" : "WHITE");
        } else if (board.isStalemate(board.getCurrentTurn())) {
            game.setStatus(GameStatus.STALEMATE);
        }

        return game;
    }

    public List<Move> getValidMoves(Long gameId, int row, int col) {
        Game game = games.get(gameId);
        if (game == null)
            return new ArrayList<>();

        ChessBoard board = deserializeBoard(game.getBoardState());
        return board.getValidMoves(new Position(row, col));
    }

    private String serializeBoard(ChessBoard board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private ChessBoard deserializeBoard(String boardState) {
        try {
            return objectMapper.readValue(boardState, ChessBoard.class);
        } catch (JsonProcessingException e) {
            return new ChessBoard();
        }
    }

    public java.util.Map<String, Object> getBoardAtMove(Long gameId, int moveIndex) throws Exception {
        Game game = games.get(gameId);
        if (game == null) {
            throw new Exception("Game not found");
        }

        List<Move> moves = new ArrayList<>();
        try {
            moves = objectMapper.readValue(game.getMoveHistory(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Move.class));
        } catch (JsonProcessingException e) {
            // Empty history
        }

        if (moveIndex < 0 || moveIndex > moves.size()) {
            throw new Exception("Invalid move index");
        }

        ChessBoard replayBoard = new ChessBoard();
        // Replay moves
        for (int i = 0; i < moveIndex; i++) {
            replayBoard.makeMove(moves.get(i));
        }

        Move lastMove = null;
        if (moveIndex > 0) {
            lastMove = moves.get(moveIndex - 1);
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("boardState", serializeBoard(replayBoard));
        result.put("lastMove", lastMove);

        return result;
    }

    private String addMoveToHistory(String moveHistory, Move move) {
        try {
            List<Move> moves = objectMapper.readValue(moveHistory,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Move.class));
            moves.add(move);
            return objectMapper.writeValueAsString(moves);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
