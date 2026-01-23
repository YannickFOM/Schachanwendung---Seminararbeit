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
          'valid-move': isValidMoveSquare(8 - row, col - 1),
          'capture-move': isValidMoveSquare(8 - row, col - 1) && getPiece(8 - row, col - 1),
          'king-check': isKingInCheck(8 - row, col - 1),
          'king-mate': isKingCheckmated(8 - row, col - 1)
        }"
        @click="handleSquareClick(8 - row, col - 1)"
      >
        <div 
          v-if="getPiece(8 - row, col - 1)" 
          class="piece"
          :class="{ 'white-piece': getPiece(8 - row, col - 1).color === 'WHITE', 'black-piece': getPiece(8 - row, col - 1).color === 'BLACK' }"
        >
          {{ getPieceSymbol(getPiece(8 - row, col - 1)) }}
        </div>
        <div class="coordinates" v-if="col === 1">{{ 9 - row }}</div>
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
    },
    checkState: {
      type: Object,
      default: () => ({ isCheck: false, isMate: false, turn: 'WHITE' })
    }
  },
  emits: ['square-click'],
  methods: {
    isKingInCheck(row, col) {
      if (!this.checkState.isCheck || this.checkState.isMate) return false
      const piece = this.getPiece(row, col)
      return piece && piece.type === 'KING' && piece.color === this.checkState.turn
    },
    isKingCheckmated(row, col) {
      if (!this.checkState.isMate) return false
      const piece = this.getPiece(row, col)
      return piece && piece.type === 'KING' && piece.color === this.checkState.turn
    },
    getPiece(row, col) {
      if (!this.board || !this.board[row]) return null
      return this.board[row][col]
    },
    getPieceSymbol(piece) {
      if (!piece) return ''
      
      const symbols = {
        PAWN: '♟',
        ROOK: '♜',
        KNIGHT: '♞',
        BISHOP: '♝',
        QUEEN: '♛',
        KING: '♚'
      }
      
      return symbols[piece.type] || ''
    },
    isSelected(row, col) {
      return this.selectedSquare && 
             this.selectedSquare.row === row && 
             this.selectedSquare.col === col
    },
    isValidMoveSquare(row, col) {
      if (!this.validMoves) return false
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
  border: 5px solid #2c3e50;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);
  border-radius: 4px;
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
}

.light-square {
  background-color: #eeeed2;
}

.dark-square {
  background-color: #769656;
}

.board-square:hover {
  filter: brightness(1.1);
}

.selected {
  background-color: #baca44 !important;
}

.king-check {
  background-color: rgba(255, 60, 60, 0.6) !important; /* Light Red */
  box-shadow: inset 0 0 10px 5px rgba(255, 0, 0, 0.5);
}

.king-mate {
  background-color: rgba(139, 0, 0, 0.9) !important; /* Dark Red */
  box-shadow: inset 0 0 20px 10px rgba(0, 0, 0, 0.7);
}

/* Base style for valid moves (dots) */
.valid-move::after {
  content: '';
  position: absolute;
  width: 24px;
  height: 24px;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 50%;
  pointer-events: none;
  z-index: 30; /* Above pieces */
}

.dark-square.valid-move::after {
    background-color: rgba(0, 0, 0, 0.3);
}

.board-square:hover.valid-move::after {
    background-color: rgba(0, 0, 0, 0.4);
    transform: scale(1.2);
    transition: all 0.2s;
}

/* Capture Moves (Rings) - Now using explicit class .capture-move */
.board-square.capture-move::after {
    background: transparent;
    border: 6px solid rgba(200, 50, 50, 0.5); /* Red tint for captures */
    border-radius: 50%;
    width: 70px;
    height: 70px;
    z-index: 30; /* Above pieces */
    /* Ensure it is centered */
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}

.board-square:hover.capture-move::after {
    background: transparent;
    border-color: rgba(200, 50, 50, 0.8);
    transform: translate(-50%, -50%) scale(1.05);
}

.piece {
  font-size: 60px;
  user-select: none;
  pointer-events: none;
  z-index: 20;
  line-height: 1;
}

.white-piece {
  color: #ffffff;
  /* Text shadow creates a nice outline */
  text-shadow: 
    -1px -1px 0 #000,  
     1px -1px 0 #000,
    -1px  1px 0 #000,
     1px  1px 0 #000,
     0px 2px 4px rgba(0,0,0,0.5);
}

.black-piece {
  color: #1a1a1a;
  text-shadow: 0px 2px 4px rgba(0,0,0,0.4);
}

.coordinates {
  position: absolute;
  font-size: 14px;
  font-weight: bold;
  pointer-events: none;
}

.light-square .coordinates {
   color: #769656;
}

.dark-square .coordinates {
   color: #eeeed2;
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
