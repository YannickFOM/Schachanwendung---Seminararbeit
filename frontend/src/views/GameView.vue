<template>
  <div class="game-view">
    <div class="game-header">
      <div class="player-panel left" :class="{ 'active-turn': currentTurn === 'WHITE' }">
        <div class="player-name">{{ whitePlayer }}</div>
        <div class="timer" :class="{ 'low-time': whiteTime < 60 }">{{ formatTime(whiteTime) }}</div>
      </div>
      
      <div class="game-center">
        <h1>Yannicks Schachapp</h1>
        <div class="status-badge" :class="gameStatus.toLowerCase().replace('_', '-')">
          {{ getStatusText(gameStatus) }}
        </div>
      </div>

      <div class="player-panel right" :class="{ 'active-turn': currentTurn === 'BLACK' }">
        <div class="player-name">{{ blackPlayer }}</div>
        <div class="timer" :class="{ 'low-time': blackTime < 60 }">{{ formatTime(blackTime) }}</div>
      </div>
    </div>

    <!-- Promotion Modal -->
    <div v-if="showPromotionModal" class="promotion-modal-overlay">
      <div class="promotion-modal">
        <h3>Figur wählen</h3>
        <div class="promotion-options">
          <button @click="confirmPromotion('QUEEN')" class="promo-btn" title="Dame">♛</button>
          <button @click="confirmPromotion('ROOK')" class="promo-btn" title="Turm">♜</button>
          <button @click="confirmPromotion('BISHOP')" class="promo-btn" title="Läufer">♝</button>
          <button @click="confirmPromotion('KNIGHT')" class="promo-btn" title="Springer">♞</button>
        </div>
      </div>
    </div>

    <div class="game-container">
      <ChessBoard
        :board="board"
        :selected-square="selectedSquare"
        :valid-moves="validMoves"
        :check-state="checkState"

        :is-rotated="isBoardRotated"
        :last-move="lastMoveHighlight"
        @square-click="handleSquareClick"
      />
    </div>

    <!-- Analysis Controls (Visible when Game Over) -->
    <div v-if="isGameOver" class="analysis-controls">
        <button @click="navigateHistory('start')" :disabled="currentMoveIndex === 0" class="nav-btn" title="Start"> |&lt; </button>
        <button @click="navigateHistory('prev')" :disabled="currentMoveIndex === 0" class="nav-btn" title="Zurück"> &lt; </button>
        <div class="move-counter">{{ currentMoveIndex }} / {{ totalMoves }}</div>
        <button @click="navigateHistory('next')" :disabled="currentMoveIndex === totalMoves" class="nav-btn" title="Vor"> &gt; </button>
        <button @click="navigateHistory('end')" :disabled="currentMoveIndex === totalMoves" class="nav-btn" title="Ende"> &gt;| </button>
        <button @click="toggleRotation" class="nav-btn rotate-btn" title="Brett drehen"> 🔄 </button>
    </div>

    <div class="game-controls">
      <button @click="goBack" class="btn btn-secondary">Zurück</button>
      <!-- DEBUG ONLY: Remove later -->
      <!-- <pre style="position:fixed; bottom:0; right:0; background:white; z-index:9999; font-size:10px;">{{ lastMoveHighlight }}</pre> -->
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChessBoard from '../components/ChessBoard.vue'
import api from '../services/api'

export default {
  name: 'GameView',
  components: {
    ChessBoard
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const gameId = ref(route.params.id)
    const board = ref(Array(8).fill(null).map(() => Array(8).fill(null)))
    const whitePlayer = ref('')
    const blackPlayer = ref('')
    const currentTurn = ref('WHITE')
    const gameStatus = ref('IN_PROGRESS')
    const isCheck = ref(false)
    const winnerColor = ref(null) // Added winner tracking
    const selectedSquare = ref(null)
    const validMoves = ref([])
    
    // Promotion State
    const showPromotionModal = ref(false)
    const pendingMove = ref(null)
    
    // Timer Refs
    const whiteTime = ref(0)
    const blackTime = ref(0)
    const timeLimit = ref(null)
    let timerInterval = null

    const confirmPromotion = async (pieceType) => {
      if (!pendingMove.value) return
      
      const move = {
        ...pendingMove.value,
        promotionPiece: pieceType
      }
      
      try {
        await api.makeMove(gameId.value, move)
        
        // Highlight the promotion move (same as normal moves)
        lastMoveHighlight.value = move
        
        await loadGame()
      } catch (error) {
        console.error('Error making promotion move:', error)
        alert('Fehler beim Umwandeln: ' + (error.response?.data?.error || error.message))
      }
      
      showPromotionModal.value = false
      pendingMove.value = null
      selectedSquare.value = null
      validMoves.value = []
    }

    const formatTime = (seconds) => {
      if (timeLimit.value === null) return "∞"
      if (seconds < 0) return "0:00"
      const m = Math.floor(seconds / 60)
      const s = seconds % 60
      return `${m}:${s.toString().padStart(2, '0')}`
    }

    const getStatusText = (status) => {
      // Game Over States
      if (status === 'VICTORY_BY_TIME' || status === 'CHECKMATE' || status === 'RESIGNED') {
        const winnerName = winnerColor.value === 'WHITE' ? whitePlayer.value : blackPlayer.value
        return `${winnerName} hat gewonnen. Glückwunsch!`
      }
      
      if (status === 'STALEMATE' || status === 'DRAW') {
        return 'Unentschieden'
      }

      // Active State
      if (status === 'IN_PROGRESS') {
        return currentTurn.value === 'WHITE' ? 'Weiß ist am Zug' : 'Schwarz ist am Zug'
      }
      
      return status
    }

    const checkState = computed(() => {
      // During replay, use historical state; otherwise use current game state
      const isInReplay = currentMoveIndex.value < totalMoves.value
      
      return {
        isCheck: isInReplay ? historicalIsCheck.value : isCheck.value,
        isMate: isInReplay ? historicalIsMate.value : (gameStatus.value === 'CHECKMATE'),
        turn: currentTurn.value
      }
    })

    // Analysis State
    const currentMoveIndex = ref(0)
    const totalMoves = ref(0)
    const historicalIsCheck = ref(false) // Check state at current replay position
    const historicalIsMate = ref(false)  // Mate state at current replay position
    const isGameOver = computed(() => ['CHECKMATE', 'STALEMATE', 'DRAW', 'RESIGNED', 'VICTORY_BY_TIME'].includes(gameStatus.value))
    const isManualRotation = ref(false) // Toggle for manual override
    const isFlipped = ref(false) // The manual flip state
    const lastMoveHighlight = ref(null)

    const isBoardRotated = computed(() => {
        if (isGameOver.value && isManualRotation.value) {
            return isFlipped.value
        }
        return currentTurn.value === 'BLACK'
    })

    const navigateHistory = async (action) => {
        // Clear highlights in analysis mode
        selectedSquare.value = null
        validMoves.value = []

        let targetIndex = currentMoveIndex.value

        if (action === 'start') targetIndex = 0
        if (action === 'prev') targetIndex = Math.max(0, currentMoveIndex.value - 1)
        if (action === 'next') targetIndex = Math.min(totalMoves.value, currentMoveIndex.value + 1)
        if (action === 'end') targetIndex = totalMoves.value

        if (targetIndex !== currentMoveIndex.value) {
            try {
                const data = await api.getBoardAtMove(gameId.value, targetIndex)
                const boardData = JSON.parse(data.boardState)
                board.value = boardData.board
                currentMoveIndex.value = targetIndex
                
                // Update historical check/mate state for replay
                historicalIsCheck.value = data.isCheck || false
                historicalIsMate.value = data.isCheckmate || false
                
                // Set highlight
                if (data.lastMove) {
                    lastMoveHighlight.value = data.lastMove
                } else {
                    lastMoveHighlight.value = null
                }
            } catch (e) {
                console.error("History navigation failed", e)
            }
        }
    }

    const toggleRotation = () => {
        isManualRotation.value = true
        isFlipped.value = !isFlipped.value
    }

    const loadGame = async () => {
      try {
        const game = await api.getGame(gameId.value)
        whitePlayer.value = game.whitePlayer
        blackPlayer.value = game.blackPlayer
        currentTurn.value = game.currentTurn
        gameStatus.value = game.status
        isCheck.value = game.check
        winnerColor.value = game.winner // Sync winner
        
        // Timer Sync
        timeLimit.value = game.timeLimit
        whiteTime.value = game.whiteTimeRemaining
        blackTime.value = game.blackTimeRemaining
        
        if (game.boardState) {
          const boardData = JSON.parse(game.boardState)
          console.log('Parsed Board Data:', boardData) // DEBUG
          console.log('First square (0,0):', boardData.board[0][0]) // DEBUG
          board.value = boardData.board
        }

        // Initialize History Count
        if (game.moveHistory) {
            const moves = JSON.parse(game.moveHistory)
            totalMoves.value = moves.length
            // Always set to end position on initial load
            currentMoveIndex.value = moves.length
        }

        // Initialize historical check/mate state to current game state
        // This ensures checkmate shows correctly when game ends
        historicalIsCheck.value = game.check || false
        historicalIsMate.value = game.status === 'CHECKMATE'

        // Cleanup board state if game is over (remove potential leftover highlights)
        if (isGameOver.value) {
            selectedSquare.value = null
            validMoves.value = []
        }

      } catch (error) {
        console.error('Error loading game:', error)
        alert('Fehler beim Laden des Spiels')
      }
    }

    const handleSquareClick = async (position) => {
      // Freeze game if not in progress
      if (gameStatus.value !== 'IN_PROGRESS') return

      const { row, col } = position

      if (!selectedSquare.value) {
        // Select a piece
        const piece = board.value[row]?.[col]
        if (piece && piece.color === currentTurn.value) {
          selectedSquare.value = { row, col }
          // Fetch valid moves from backend
          try {
            validMoves.value = await api.getValidMoves(gameId.value, row, col)
            console.log('Fetched valid moves:', validMoves.value)
          } catch (e) {
            console.error("Failed to load valid moves", e)
          }
        }
      } else {
        // Deselect if clicking same square
        if (selectedSquare.value.row === row && selectedSquare.value.col === col) {
            selectedSquare.value = null
            return
        }

        // Try to move directly (let backend validate)
        try {
            const move = {
              from: selectedSquare.value,
              to: { row, col },
              isCastling: false,
              isEnPassant: false,
              promotionPiece: null
            }

            // Check for promotion
            const piece = board.value[selectedSquare.value.row][selectedSquare.value.col]
            const isWhite = piece.color === 'WHITE'
            const isPawn = piece.type === 'PAWN'
            const targetRank = isWhite ? 0 : 7 // Wait, backend board logic says White starts at 1, moves to? 
            // In backend array: 
            // White pawns at row 1. White "forward" is +1 or -1? 
            // ChessBoard.java: "direction = piece.getColor() == PieceColor.WHITE ? 1 : -1"
            // Start Row White = 1. So White moves 1 -> 2 -> ... -> 7.
            // Start Row Black = 6. Black moves 6 -> 5 -> ... -> 0.
            // So White promotes at 7. Black promotes at 0.
            
            // Correction: Backend check says:
            // (to.getRow() == 7 && piece.getColor() == PieceColor.WHITE) || (to.getRow() == 0 && piece.getColor() == PieceColor.BLACK)
            
            if (isPawn && ((isWhite && row === 7) || (!isWhite && row === 0))) {
                pendingMove.value = move
                showPromotionModal.value = true
                return // Stop here, wait for modal
            }

            await api.makeMove(gameId.value, move)
            
            // Highlight the move that was just made (same as in replay mode)
            lastMoveHighlight.value = move
            
            await loadGame()
        } catch (error) {
            console.error('Error making move:', error)
            alert('Ungültiger Zug!: ' + (error.response?.data?.error || error.message))
        }

        selectedSquare.value = null
        validMoves.value = []
      }
    }

    const calculateValidMoves = (row, col) => {
      // Note: Client-side validation removed to rely on backend validation
      // In a future version, this could call a backend endpoint to get valid moves
      validMoves.value = []
      
      const piece = board.value[row]?.[col]
      if (!piece) return

      // For now, we don't highlight valid moves on the client side
      // The backend will validate moves when they are attempted
    }

    const goBack = () => {
      router.push('/')
    }

    onMounted(() => {
      loadGame()
      
      // Timer Interval
      timerInterval = setInterval(() => {
        if (gameStatus.value !== 'IN_PROGRESS' || timeLimit.value === null) return
        
        if (currentTurn.value === 'WHITE') {
          if (whiteTime.value > 0) {
            whiteTime.value--
            if (whiteTime.value === 0) {
               gameStatus.value = 'VICTORY_BY_TIME'
               winnerColor.value = 'BLACK'
               clearInterval(timerInterval)
            }
          }
        } else {
          if (blackTime.value > 0) {
            blackTime.value--
            if (blackTime.value === 0) {
               gameStatus.value = 'VICTORY_BY_TIME'
               winnerColor.value = 'WHITE'
               clearInterval(timerInterval)
            }
          }
        }
      }, 1000)
    })
    
    // Cleanup if needed using onUnmounted (needs import) but simplified here
    // Ideally import onUnmounted


    return {
      board,
      whitePlayer,
      blackPlayer,
      currentTurn,
      gameStatus,
      selectedSquare,
      validMoves,
      checkState,
      handleSquareClick,
      goBack,
      whiteTime,
      blackTime,
      formatTime,
      getStatusText,
      showPromotionModal,
      confirmPromotion,
      isGameOver,
      currentMoveIndex,
      totalMoves,
      navigateHistory,
      toggleRotation,
      isBoardRotated,
      lastMoveHighlight
    }
  }
}
</script>

<style scoped>
.game-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #060538 0%, #021324 10%);
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 800px;
  margin-bottom: 20px;
  padding: 0 20px;
}

.game-center {
  text-align: center;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.game-center h1 {
  font-size: 32px;
  margin: 0 0 10px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.player-panel {
  background: rgba(0, 0, 0, 0.4);
  padding: 15px;
  border-radius: 12px;
  text-align: center;
  min-width: 140px;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  border: 2px solid transparent;
  opacity: 0.6; /* Dimmed when not active */
}

.player-panel.active-turn {
  background: rgba(2,19, 36, 0.15);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.2);
  opacity: 1; /* Full brightness */
}

.player-name {
  color: white;
  font-weight: bold;
  font-size: 1.1rem;
  margin-bottom: 5px;
}

.timer {
  font-family: 'Courier New', monospace;
  font-size: 2rem;
  color: #fff;
  font-weight: bold;
  text-shadow: 1px 1px 2px black;
}

.timer.low-time {
  color: #ff4444;
  animation: pulse 1s infinite;
}

.status-badge {
  background: white;
  color: #333;
  padding: 5px 15px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 0.9rem;
  margin-bottom: 5px;
}

.turn-indicator {
  color: #eee;
  font-size: 0.9rem;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.game-container {
  margin: 20px 0;
}

.game-controls {
  margin-top: 20px;
  display: flex;
  gap: 15px;
}

.btn {
  padding: 12px 24px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #4CAF50;
  color: white;
}

.btn-primary:hover {
  background-color: #45a049;
}

.btn-secondary {
  background-color: #f44336;
  color: white;
}

.btn-secondary:hover {
  background-color: #da190b;
}

/* Promotion Modal */
.promotion-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.promotion-modal {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.promotion-modal h3 {
  color: #2c3e50;
  margin-top: 0;
}

.promotion-options {
  display: flex;
  gap: 15px;
  margin-top: 15px;
}

.promo-btn {
  font-size: 32px;
  width: 60px;
  height: 60px;
  border: 2px solid #ddd;
  border-radius: 8px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.promo-btn:hover {
  background: #e9ecef;
  transform: translateY(-2px);
  border-color: #667eea;
}

/* Analysis Controls */
.analysis-controls {
    display: flex;
    align-items: center;
    gap: 10px;
    background: rgba(255, 255, 255, 0.9);
    padding: 10px 20px;
    border-radius: 12px;
    box-shadow: 0 4px 6px rgba(0,0,0,0.2);
    margin-top: 15px;
}

.nav-btn {
    background: #f8f9fa;
    border: 1px solid #ced4da;
    border-radius: 6px;
    padding: 5px 12px;
    font-weight: bold;
    cursor: pointer;
    color: #495057;
    transition: all 0.2s;
    font-size: 1.1rem;
}

.nav-btn:hover:not(:disabled) {
    background: #e2e6ea;
    border-color: #adb5bd;
}

.nav-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.move-counter {
    font-family: monospace;
    font-size: 1.1rem;
    font-weight: bold;
    color: #333;
    padding: 0 10px;
}

.rotate-btn {
    margin-left: 10px;
    background: #e3f2fd;
    border-color: #90caf9;
    color: #1976d2;
}

.rotate-btn:hover {
    background: #bbdefb;
}

/* --- Mobile Responsiveness --- */
@media (max-width: 768px) {
  .game-view {
    padding: 10px 5px; /* Reduce padding */
  }

  .game-header {
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }

  /* Reorder: Player 2 (Top), Status, Player 1 (Bottom) logic is hard with Flex column without order
     Let's keep simple stack: Player (Left Panel in desktop), Center, Player (Right Panel in desktop).
     In column: Left Panel becomes Top. Right Panel becomes Bottom.
     
     Actually, often mobile wants: Opponent at top, YOU at bottom.
     If we assume we are one of them, we might want to swap.
     For hotseat (this app), Top/Center/Bottom is fine.
  */

  .player-panel {
    width: 100%; /* Full width bars */
    display: flex;
    justify-content: space-between; /* Name left, Timer right */
    align-items: center;
    padding: 10px 20px;
  }

  .player-name {
    margin: 0;
    font-size: 1rem;
  }

  .timer {
    font-size: 1.5rem;
  }

  .game-center h1 {
    font-size: 24px;
  }
  
  .logo-area {
      /* If there was a logo, scale it */
  }
  
  .game-controls {
      flex-wrap: wrap;
      justify-content: center;
  }
  
  .analysis-controls {
      flex-wrap: wrap;
      justify-content: center;
      width: 100%;
  }
}
</style>
