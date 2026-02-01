<template>
  <div class="board-layout">
    <!-- Ranks (Numbers) -->
    <div class="coordinate-bar ranks left">
      <div v-for="rank in ranks" :key="rank" class="coord-num">{{ rank }}</div>
    </div>

    <!-- The Board -->
    <div class="chess-board" :class="{ 'rotated': isRotated }">
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
            'last-move-highlight': isLastMoveSquare(8 - row, col - 1),
            'capture-move': isValidMoveSquare(8 - row, col - 1) && getPiece(8 - row, col - 1),
            'king-check': isKingInCheck(8 - row, col - 1),
            'king-mate': isKingCheckmated(8 - row, col - 1)
          }"
          @click="handleSquareClick(8 - row, col - 1)"
        >
          <div 
            v-if="getPiece(8 - row, col - 1)" 
            class="piece"
            :class="{ 
              'white-piece': getPiece(8 - row, col - 1).color === 'WHITE', 
              'black-piece': getPiece(8 - row, col - 1).color === 'BLACK',
              'rotated-piece': isRotated 
            }"
          >
            {{ getPieceSymbol(getPiece(8 - row, col - 1)) }}
          </div>
        </div>
      </div>
    </div>

    <!-- Ranks (Numbers) Right - Optional, acts as spacer if empty or duplicated -->
    <!-- For simplicity, just left ranks. Or visually balanced? Standard is Left and Bottom. -->

    <!-- Files (Letters) -->
    <div class="coordinate-bar files bottom">
      <div v-for="file in files" :key="file" class="coord-letter">{{ file }}</div>
    </div>
  </div>
</template>

<script>
import { computed, watch } from 'vue'

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
    },
    isRotated: {
      type: Boolean,
      default: false
    },
    lastMove: {
      type: Object,
      default: null
    }
  },
  emits: ['square-click'],
  setup(props) {
    // Computed coords based on rotation
    const ranks = computed(() => {
        return props.isRotated ? [1, 2, 3, 4, 5, 6, 7, 8] : [8, 7, 6, 5, 4, 3, 2, 1]
    })

    const files = computed(() => {
        return props.isRotated ? ['h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'] : ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h']
    })

    // Debug Watcher
    watch(() => props.lastMove, (newVal) => {
        console.log('ChessBoard received lastMove:', newVal)
    })

    return {
        ranks,
        files
    }
  },
  methods: {
    isLastMoveSquare(row, col) {
        if (!this.lastMove) return false
        const from = this.lastMove.from
        const to = this.lastMove.to
        return (from.row === row && from.col === col) || (to.row === row && to.col === col)
    },
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
.board-layout {
  display: grid;
  grid-template-columns: 30px auto;
  grid-template-rows: auto 30px;
  grid-template-areas:
    "ranks board"
    ". files";
  gap: 5px;
}

.chess-board {
  grid-area: board;
  display: inline-block;
  border: 5px solid #2c3e50;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  /* Removed transition for hard switch */
}

.chess-board.rotated {
  transform: rotate(180deg);
}

.coordinate-bar {
  display: flex;
  justify-content: space-around;
  color: white; /* Clean white output */
  font-weight: bold;
  font-size: 16px;
  text-shadow: 1px 1px 2px black;
}

.coordinate-bar.ranks {
    grid-area: ranks;
    flex-direction: column;
    height: 100%;
    /* Align numbers to center of their respective squares */
    /* Assumes square height ~80px. */
}

.coordinate-bar.files {
    grid-area: files;
    flex-direction: row;
    width: 100%;
    /* Align letters to center of columns */
}

.coord-num, .coord-letter {
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 1; /* Distribute space evenly */
}

.piece {
  font-size: 60px;
  user-select: none;
  pointer-events: none;
  z-index: 20;
  line-height: 1;
  /* Sync with board for hard rotation */
  /* If board rotates instantly, piece needs to counter-rotate instantly */
}

.piece.rotated-piece {
  transform: rotate(180deg);
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

.last-move-highlight {
  background-color: rgba(255, 235, 59, 0.5) !important; /* Stronger Yellow */
  box-shadow: inset 0 0 0 1000px rgba(255, 235, 59, 0.2);
}

.king-check {
  background-color: rgba(255, 60, 60, 0.6) !important;
  box-shadow: inset 0 0 10px 5px rgba(255, 0, 0, 0.5);
}

.king-mate {
  background-color: rgba(139, 0, 0, 0.9) !important;
  box-shadow: inset 0 0 20px 10px rgba(0, 0, 0, 0.7);
}

/* Base style for valid moves */
.valid-move::after {
  content: '';
  position: absolute;
  width: 24px;
  height: 24px;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 50%;
  pointer-events: none;
  z-index: 30;
}

.dark-square.valid-move::after {
    background-color: rgba(0, 0, 0, 0.3);
}

.board-square:hover.valid-move::after {
    background-color: rgba(0, 0, 0, 0.4);
    transform: scale(1.2);
    transition: all 0.2s;
}

.board-square.capture-move::after {
    background: transparent;
    border: 6px solid rgba(200, 50, 50, 0.5);
    border-radius: 50%;
    width: 70px;
    height: 70px;
    z-index: 30;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}

.board-square:hover.capture-move::after {
    background: transparent;
    border-color: rgba(200, 50, 50, 0.8);
    transform: translate(-50%, -50%) scale(1.05);
}

.white-piece {
  color: #ffffff;
  text-shadow: 
    -1px -1px 0 #000, 1px -1px 0 #000,
    -1px 1px 0 #000, 1px 1px 0 #000,
    0px 2px 4px rgba(0,0,0,0.5);
}

.black-piece {
  color: #1a1a1a;
  text-shadow: 0px 2px 4px rgba(0,0,0,0.4);
}

/* --- Mobile Responsiveness --- */
@media (max-width: 800px) {
  .board-layout {
    grid-template-columns: 20px auto;
    grid-template-rows: auto 20px;
    gap: 2px;
  }

  .board-square {
    width: 11vw;
    height: 11vw;
    max-width: 48px; /* Prevent it getting too big on tablets if simpler */
    max-height: 48px;
  }

  .piece {
    font-size: 8vw; /* Scale piece with viewport */
    text-shadow: none; /* Remove shadow on mobile for cleaner look */
  }

  /* Improve touch response */
  .board-square {
      touch-action: manipulation; 
  }

  /* Adjust capture/valid markers */
  .valid-move::after {
    width: 16px;
    height: 16px;
  }

  .board-square.capture-move::after {
    width: 90%;
    height: 90%;
    border-width: 4px;
  }
  
  .coordinate-bar {
      font-size: 10px;
  }
}
