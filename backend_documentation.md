# Technische Dokumentation: Schach-Backend

Diese Dokumentation dient als detaillierte Referenz für die Implementierung der Schachlogik und der Server-Architektur. Sie analysiert die Komponenten auf Klassen-, Feld- und Methodenebene.

---

## 1. Domain Model (`com.schachspiel.chess.model`)

### 1.1 Klasse: `ChessBoard.java`
Die Klasse `ChessBoard` ist das zentrale Element der Anwendungslogik. Sie kapselt den Zustand des 8x8 Spielfelds und implementiert das komplette Regelwerk des Schachspiels (Bewegung, Schlagen, Schach, Matt, Patt, Rochade, En Passant).

#### 1.1.1 Felder (State)
*   **`private Piece[][] board`**: Ein zweidimensionales Array (8x8), das die Felder repräsentiert.
    *   Indexierung: `board[0][0]` entspricht A8 (oben links), `board[7][7]` entspricht H1 (unten rechts).
    *   Wert: Enthält ein `Piece`-Objekt oder `null` (leeres Feld).
*   **`private PieceColor currentTurn`**: Speichert, welcher Spieler am Zug ist (`WHITE` oder `BLACK`). Wird nach jedem gültigen Zug gewechselt.
*   **`private List<Move> moveHistory`**: Eine Liste aller getätigten Züge. Dient der Validierung (z.B. für En Passant oder 50-Züge-Regel) und der Historien-Funktion.
*   **`private Position enPassantTarget`**: Speichert das Feld, das *in diesem Zug* per En Passant geschlagen werden kann.
    *   Beispiel: Zieht ein weißer Bauer von E2 auf E4, wird E3 als `enPassantTarget` gesetzt. Ein schwarzer Bauer auf F4 oder D4 kann dann nach E3 ziehen und den Bauern auf E4 schlagen.

#### 1.1.2 Konstruktoren
*   **`public ChessBoard()`**: Initialisiert ein neues Spiel.
    *   Setzt `currentTurn` auf Weiß.
    *   Ruft `initializeBoard()` auf, um die Figuren in die Startaufstellung zu bringen.
*   **`public ChessBoard(ChessBoard other)` (Copy-Konstruktor)**: Erstellt eine tiefe Kopie (Deep Copy) eines vorhandenen Brettes.
    *   Wichtig für die Zug-Validierung: Um zu prüfen, ob ein Zug ins Schach führt, simulieren wir ihn auf einer Kopie, ohne das echte Brett zu verändern.
    *   Iteriert über das gesamte Array und erstellt neue Instanzen aller Figuren, um Referenz-Probleme zu vermeiden.

#### 1.1.3 Kern-Methoden

**`isLegalMove(Move move)`**
*   **Zweck**: Die "öffentliche" Schnittstelle zur Prüfung eines Zugs. Kombiniert geometrische Regeln mit Schach-Regeln (Königsschutz).
*   **Ablauf**:
    1.  **Geometrie-Check**: Ruft `isValidMove()` auf (zieht der Läufer wirklich diagonal?). Wenn `false`, Abbruch.
    2.  **Simulation**: Erstellt eine Kopie des Brettes (`this.copy()`).
    3.  **Ausführung**: Führt den Zug auf der Kopie aus (`simulation.makeMove(move)`).
    4.  **Königs-Check**: Prüft auf der Kopie, ob der eigene König im Schach steht (`simulation.isInCheck(this.currentTurn)`).
*   **Return**: `true` nur, wenn der Zug geometrisch korrekt ist UND der König danach sicher ist.

**`isValidMove(Move move)` (Private Helper)**
*   **Zweck**: Prüft *nur* die Bewegungsregeln der Figuren, ohne Rücksicht auf Schachgebote.
*   **Logik**:
    *   Prüft Grenzen (Ist das Ziel im 8x8 Feld?).
    *   Prüft Kollisionen (Steht auf dem Ziel eine eigene Figur?).
    *   Delegiert je nach Figur-Typ ("Switch Case") an spezialisierte Methoden:
        *   `isValidPawnMove`: Komplex (1 oder 2 Schritte, Schlagen nur diagonal, En Passant).
        *   `isValidRookMove`: Prüft auf horizontale/vertikale Linie und ruft `isPathClear` auf.
        *   `isValidKnightMove`: Prüft auf das "L-Muster" (2+1 Felder). Springt über Figuren.
        *   `isValidBishopMove`: Prüft auf Diagonalen (`rowDiff == colDiff`) und Pfadfreiheit.
        *   `isValidQueenMove`: Kombination aus Turm und Läufer.
        *   `isValidKingMove`: 1 Schritt in jede Richtung oder Rochade (`canCastle`).

**`makeMove(Move move)`**
*   **Zweck**: Führt einen validierten Zug permanent aus ("Command").
*   **Ablauf**:
    1.  **En Passant Logik**: Erkennt, ob ein Bauerngzug ein En Passant Schlag ist, und entfernt ggf. den geschlagenen Bauern (der nicht auf dem Zielfeld steht!).
    2.  **En Passant Target setzen**: Wenn ein Bauer 2 Felder zieht, wird das übersprungene Feld als Ziel markiert. Andernfalls wird der Marker gelöscht.
    3.  **Umwandlung (Promotion)**: Wenn ein Bauer die letzte Reihe erreicht (Reihe 0 oder 7), wird er in die gewählte Figur (Standard: Dame) verwandelt.
    4.  **Rochade**: Bewegt bei einem Königszug um 2 Felder automatisch den entsprechenden Turm.
    5.  **Bewegung**: Setzt die Figur auf das neue Feld, löscht das alte Feld.
    6.  **Flag**: Setzt `hasMoved = true` (wichtig für Rochade-Rechte).
    7.  **Wechsel**: Invertiert `currentTurn` (Weiß -> Schwarz -> Weiß).

**`isInCheck(PieceColor color)`**
*   **Zweck**: Prüft, ob der König der Farbe `color` bedroht ist.
*   **Algorithmus**:
    1.  Sucht die Position des Königs (`findKing()`).
    2.  Iteriert über **alle** gegnerischen Figuren.
    3.  Prüft für jede Figur: "Kannst du den König schlagen?" (`canPieceAttackSquare`).
    4.  Sobald eine "Ja" sagt: Return `true`.

**`isCheckmate(PieceColor color)`**
*   **Zweck**: Erkennt Schachmatt.
*   **Algorithmus**:
    1.  Ist der König im Schach? Wenn nein -> Kein Matt.
    2.  Gibt es **irgendeinen** Zug, der das Schach aufhebt? (`canAnyMoveEscapeCheck`).
    3.  Dazu werden brute-force *alle* Figuren des Spielers auf *alle* möglichen Felder gezogen. Jeder Zug wird simuliert (`isLegalMove`).
    4.  Wenn kein einziger legaler Zug existiert -> Matt.

### 1.2 Klasse: `Game.java`
Repräsentiert eine Spielinstanz (Session). Dient als Daten-Container (POJO - Plain Old Java Object).

*   **`String boardState`**: Speichert das gesamte `ChessBoard`-Objekt als JSON-String.
    *   *Begründung*: Dies ermöglicht eine einfache Speicherung (z.B. in Datenbanken oder Files) und Entkopplung. Der komplexe Objekt-Graph wird flachgedrückt ("serialisiert").
*   **`LocalDateTime lastMoveAt`**: Zeitstempel des letzten Zugs. Wird benötigt, um die verstrichene Zeit für die Schachuhr zu berechnen.

---

## 2. Service Layer (`com.schachspiel.chess.service`)

### 2.1 Klasse: `GameService.java`
Hier wird die Persistenz (Speicherung) und der Spielablauf gesteuert.

#### Persistenz (In-Memory)
*   Statt einer externen Datenbank nutzen wir eine `ConcurrentHashMap<Long, Game>`.
*   Das macht die Anwendung extrem schnell und einfach zu installieren (kein Setup nötig), bedeutet aber, dass alle Spiele bei einem Neustart verloren gehen.

#### Methode: `makeMove(Long gameId, Move move)`
Dies ist die wichtigste Methode, die vom Controller aufgerufen wird.
1.  **Laden**: Holt das `Game`-Objekt aus der Map.
2.  **Deserialisierung**: Wandelt den JSON-String `boardState` zurück in ein echtes `ChessBoard`-Java-Objekt (`objectMapper.readValue`).
3.  **Validierung**: Ruft `board.isLegalMove(move)` auf. Bei Fehler: Exception.
4.  **Zeit-Messung**:
    *   Berechnet `Duration.between(lastMoveAt, now)`.
    *   Zieht diese Zeit vom Konto des Spielers ab (`whiteTimeRemaining`).
    *   Prüft auf Timeout (`<= 0`). Wenn ja: Setzt Status auf `VICTORY_BY_TIME`.
5.  **Ausführung**: Ruft `board.makeMove(move)` auf.
6.  **Serialisierung**: Wandelt das aktualisierte `ChessBoard` wieder in JSON und speichert es im `Game`.

---

## 3. Architektur-Entscheidungen

### Warum keine Datenbank?
Für den Scope dieser Seminararbeit (lokale Demo / Prototyp) reduziert der Verzicht auf PostgreSQL die Komplexität drastisch. Es gibt keine Treiber-Probleme oder Installationshürden. JSON-Serialisierung simuliert dabei eine echte Persistenzschicht.

### Warum Simulation für Zugvalidierung?
Schachregeln sind kontextabhängig. Ob ein Zug erlaubt ist, hängt oft nicht von der Figur selbst ab, sondern vom gesamten Brett (z.B. "Gefesselte Figur", Abzugsschach).
Anstatt komplexe Formeln zu entwickeln ("Ist die Linie zwischen Turm und König frei?"), ist der Ansatz "Ausprobieren und Prüfen" (Simulation) robuster und weniger fehleranfällig.

### En Passant Erkennung
En Passant ist der einzige Zug im Schach, der von der *Historie* abhängt (man darf nur sofort nach dem Doppelzug des Gegners schlagen). Daher speichert das `ChessBoard` ein `enPassantTarget`. Dieses Feld ist transient (wird in keinem physischen Brett-Array gespeichert), sondern ist ein Status-Marker, der jede Runde neu validiert wird.
