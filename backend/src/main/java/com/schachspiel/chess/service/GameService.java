package com.schachspiel.chess.service;

import com.schachspiel.chess.model.*;
import com.schachspiel.chess.repository.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public Game createGame(String whitePlayer, String blackPlayer, boolean isOnlineMode) {
        Game game = new Game();
        game.setWhitePlayer(whitePlayer);
        game.setBlackPlayer(blackPlayer);
        game.setCurrentTurn(PieceColor.WHITE);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setOnlineMode(isOnlineMode);
        
        ChessBoard board = new ChessBoard();
        game.setBoardState(serializeBoard(board));
        game.setMoveHistory("[]");
        
        return gameRepository.save(game);
    }
    
    public Optional<Game> getGame(Long id) {
        return gameRepository.findById(id);
    }
    
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }
    
    public List<Game> getGamesByPlayer(String playerName) {
        return gameRepository.findByWhitePlayerOrBlackPlayer(playerName, playerName);
    }
    
    public Game makeMove(Long gameId, Move move) throws Exception {
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            throw new Exception("Game not found");
        }
        
        Game game = gameOpt.get();
        ChessBoard board = deserializeBoard(game.getBoardState());
        
        if (!board.isValidMove(move)) {
            throw new Exception("Invalid move");
        }
        
        board.makeMove(move);
        
        game.setBoardState(serializeBoard(board));
        game.setCurrentTurn(board.getCurrentTurn());
        game.setMoveHistory(addMoveToHistory(game.getMoveHistory(), move));
        
        // Check for checkmate or stalemate
        if (board.isInCheck(board.getCurrentTurn())) {
            // Simplified: just set status, full checkmate detection would require more logic
            game.setStatus(GameStatus.IN_PROGRESS);
        }
        
        return gameRepository.save(game);
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
