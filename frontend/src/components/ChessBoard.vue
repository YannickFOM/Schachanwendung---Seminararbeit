<template>
  <div class="chess-board">
    <div v-for="row in 8" :key="row" class="board-row">
      <div
        v-for="col in 8"
        :key="col"
        class="board-square"
        :class="{
          'light-square': (row + col) % 2 === 0,
          'dark-square': (row + col) % 2 === 1,
          'selected': isSelected(8 - row, col - 1),
          'valid-move': isValidMoveSquare(8 - row, col - 1)
        }"
        @click="handleSquareClick(8 - row, col - 1)"
      >
        <div v-if="getPiece(8 - row, col - 1)" class="piece">
          {{ getPieceSymbol(getPiece(8 - row, col - 1)) }}
        </div>
        <div class="coordinates" v-if="col === 1">{{ 8 - row + 1 }}</div>
        <div class="coordinates" v-if="row === 8">{{ String.fromCharCode(96 + col) }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChessBoard',
  props: {
    board: {
      type: Array,
      required: true
    },
    selectedSquare: {
      type: Object,
      default: null
    },
    validMoves: {
      type: Array,
      default: () => []
    }
  },
  emits: ['square-click'],
  methods: {
    getPiece(row, col) {
      if (!this.board || !this.board[row]) return null
      return this.board[row][col]
    },
    getPieceSymbol(piece) {
      if (!piece) return ''
      
      const symbols = {
        PAWN: { WHITE: '♙', BLACK: '♟' },
        ROOK: { WHITE: '♖', BLACK: '♜' },
        KNIGHT: { WHITE: '♘', BLACK: '♞' },
        BISHOP: { WHITE: '♗', BLACK: '♝' },
        QUEEN: { WHITE: '♕', BLACK: '♛' },
        KING: { WHITE: '♔', BLACK: '♚' }
      }
      
      return symbols[piece.type]?.[piece.color] || ''
    },
    isSelected(row, col) {
      return this.selectedSquare && 
             this.selectedSquare.row === row && 
             this.selectedSquare.col === col
    },
    isValidMoveSquare(row, col) {
      return this.validMoves.some(move => 
        move.to.row === row && move.to.col === col
      )
    },
    handleSquareClick(row, col) {
      this.$emit('square-click', { row, col })
    }
  }
}
</script>

<style scoped>
.chess-board {
  display: inline-block;
  border: 3px solid #333;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.board-row {
  display: flex;
}

.board-square {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  transition: background-color 0.2s;
}

.light-square {
  background-color: #f0d9b5;
}

.dark-square {
  background-color: #b58863;
}

.board-square:hover {
  opacity: 0.8;
}

.selected {
  background-color: #7fc97f !important;
}

.valid-move {
  background-color: #ffd700 !important;
}

.piece {
  font-size: 50px;
  user-select: none;
  pointer-events: none;
}

.coordinates {
  position: absolute;
  font-size: 12px;
  color: #666;
  font-weight: bold;
}

.coordinates:first-of-type {
  top: 2px;
  left: 4px;
}

.coordinates:last-of-type {
  bottom: 2px;
  right: 4px;
}
</style>
