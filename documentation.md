# Projekt-Dokumentation: Schachspiel Refactoring & Features

Diese Dokumentation fasst die durchgeführten Arbeiten am Schachspiel-Projekt zusammen, inklusive relevanter Code-Ausschnitte.

## 1. Initiale Analyse & Datenbank-Entfernung
**Ziel**: Vereinfachung der Architektur für eine lokale Seminar-Demo.
*   **Maßnahme**: Entfernung der PostgreSQL-Datenbank.
*   **Code**: Umstellung auf In-Memory Storage im `GameService`.

```java
// GameService.java
// Statt @Repository nutzen wir eine ConcurrentHashMap für schnelle lokale Speicherung
private final java.util.Map<Long, Game> games = new java.util.concurrent.ConcurrentHashMap<>();
private final java.util.concurrent.atomic.AtomicLong idGenerator = new java.util.concurrent.atomic.AtomicLong(1);
```

## 2. Debugging & Fehlerbehebung

### JSON Mapping Fehler
*   **Problem**: Das Frontend sendete `isCastling`, das Backend erwartete `castling`.
*   **Lösung**: Explizites Mapping via Jackson Annotationen.

```java
// Move.java
@JsonProperty("isCastling")
private boolean isCastling;

@JsonProperty("isEnPassant")
private boolean isEnPassant;
```

## 3. Implementierung der Schach-Logik

### Simulation gültiger Züge (Check Protection)
*   **Logik**: Ein Zug ist nur legal, wenn der eigene König danach nicht im Schach steht. Wir simulieren den Zug auf einer Kopie des Brettes.

```java
// ChessBoard.java
public boolean isLegalMove(Move move) {
    // 1. Geometrische Prüfung
    if (!isValidMove(move)) return false;
    
    // 2. Simulation auf Kopie
    ChessBoard simulation = this.copy();
    simulation.makeMove(move); 
    
    // 3. Prüfen ob König im Schach steht
    return !simulation.isInCheck(this.currentTurn); 
}
```

### En Passant Erkennung
*   **Logik**: Das Backend erkennt En Passant automatisch, wenn ein Bauer diagonal auf ein leeres Feld zieht, das als `enPassantTarget` markiert ist.

```java
// ChessBoard.java - makeMove()
if (piece.getType() == PieceType.PAWN && 
    Math.abs(to.getCol() - from.getCol()) == 1 && // Diagonal
    getPieceAt(to) == null && // Ziel ist leer
    enPassantTarget != null && to.equals(enPassantTarget)) { // Passt zum Target
    
    isEnPassantMove = true;
    move.setEnPassant(true);
}
```

### Schachmatt & Patt
*   **Logik**: Nach jedem Zug wird geprüft, ob der Gegner noch ziehen kann.

```java
// GameService.java
boolean inCheck = board.isInCheck(board.getCurrentTurn());
game.setCheck(inCheck); // Status für Frontend speichern

if (board.isCheckmate(board.getCurrentTurn())) {
    game.setStatus(GameStatus.CHECKMATE);
} else if (board.isStalemate(board.getCurrentTurn())) {
    game.setStatus(GameStatus.STALEMATE);
}
```

## 4. UI & Design Verbesserungen

### Schachbrett & Figuren Styling
*   **Design**: Nutzung von CSS `text-shadow` für hohen Kontrast der Unicode-Figuren.
*   **Zughilfen**: Nutzung von Pseudo-Elementen (`::after`) für Punkte und Ringe.

```css
/* ChessBoard.vue */

/* Weiße Figuren mit schwarzem Rand für bessere Sichtbarkeit */
.white-piece {
  color: #ffffff;
  text-shadow: 
    -1px -1px 0 #000, 1px -1px 0 #000,
    -1px 1px 0 #000, 1px 1px 0 #000,
    0px 2px 4px rgba(0,0,0,0.5);
}

/* Mögliche Züge als graue Punkte */
.valid-move::after {
  content: '';
  position: absolute;
  width: 24px; height: 24px;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 50%;
  z-index: 30; /* Über den Figuren */
}

/* Schlag-Züge als roter Ring */
.board-square.capture-move::after {
    background: transparent;
    border: 6px solid rgba(200, 50, 50, 0.5);
    width: 70px; height: 70px;
}
```

### König Highlights bei Schach
*   **Frontend**: Dynamische Klassen basierend auf dem Spielstatus.

```javascript
// ChessBoard.vue
isKingInCheck(row, col) {
  if (!this.checkState.isCheck) return false
  const piece = this.getPiece(row, col)
  // Markiere nur den König der aktuellen Farbe
  return piece && piece.type === 'KING' && piece.color === this.checkState.turn
}
```

```css
/* Rotes Leuchten bei Schach */
.king-check {
  background-color: rgba(255, 60, 60, 0.6) !important;
  box-shadow: inset 0 0 10px 5px rgba(255, 0, 0, 0.5);
}
```
