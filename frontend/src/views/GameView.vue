<template>
  <div class="game-view">
    <div class="game-header">
      <h1>Schachspiel</h1>
      <div class="game-info">
        <p><strong>Weiß:</strong> {{ whitePlayer }}</p>
        <p><strong>Schwarz:</strong> {{ blackPlayer }}</p>
        <p><strong>Am Zug:</strong> {{ currentTurn === 'WHITE' ? 'Weiß' : 'Schwarz' }}</p>
        <p v-if="gameStatus !== 'IN_PROGRESS'"><strong>Status:</strong> {{ gameStatus }}</p>
      </div>
    </div>

    <div class="game-container">
      <ChessBoard
        :board="board"
        :selected-square="selectedSquare"
        :valid-moves="validMoves"
        @square-click="handleSquareClick"
      />
    </div>

    <div class="game-controls">
      <button @click="goBack" class="btn btn-secondary">Zurück</button>
      <button @click="newGame" class="btn btn-primary">Neues Spiel</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
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
    const selectedSquare = ref(null)
    const validMoves = ref([])

    const loadGame = async () => {
      try {
        const game = await api.getGame(gameId.value)
        whitePlayer.value = game.whitePlayer
        blackPlayer.value = game.blackPlayer
        currentTurn.value = game.currentTurn
        gameStatus.value = game.status
        
        if (game.boardState) {
          const boardData = JSON.parse(game.boardState)
          board.value = boardData.board
        }
      } catch (error) {
        console.error('Error loading game:', error)
        alert('Fehler beim Laden des Spiels')
      }
    }

    const handleSquareClick = async (position) => {
      const { row, col } = position

      if (!selectedSquare.value) {
        // Select a piece
        const piece = board.value[row]?.[col]
        if (piece && piece.color === currentTurn.value) {
          selectedSquare.value = { row, col }
          calculateValidMoves(row, col)
        }
      } else {
        // Try to move
        const isValidMove = validMoves.value.some(
          move => move.to.row === row && move.to.col === col
        )

        if (isValidMove) {
          try {
            const move = {
              from: selectedSquare.value,
              to: { row, col },
              isCastling: false,
              isEnPassant: false,
              promotionPiece: null
            }

            await api.makeMove(gameId.value, move)
            await loadGame()
          } catch (error) {
            console.error('Error making move:', error)
            alert('Ungültiger Zug')
          }
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

    const newGame = () => {
      router.push('/new-game')
    }

    onMounted(() => {
      loadGame()
    })

    return {
      board,
      whitePlayer,
      blackPlayer,
      currentTurn,
      gameStatus,
      selectedSquare,
      validMoves,
      handleSquareClick,
      goBack,
      newGame
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.game-header {
  text-align: center;
  color: white;
  margin-bottom: 20px;
}

.game-header h1 {
  font-size: 48px;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.game-info {
  background: rgba(255, 255, 255, 0.2);
  padding: 15px;
  border-radius: 10px;
  backdrop-filter: blur(10px);
}

.game-info p {
  margin: 5px 0;
  font-size: 18px;
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
</style>
