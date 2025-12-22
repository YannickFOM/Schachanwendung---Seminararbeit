package com.schachspiel.chess.repository;

import com.schachspiel.chess.model.Game;
import com.schachspiel.chess.model.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStatus(GameStatus status);
    List<Game> findByWhitePlayerOrBlackPlayer(String whitePlayer, String blackPlayer);
}
