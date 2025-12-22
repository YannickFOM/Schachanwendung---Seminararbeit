# Architektur und Implementierung

## Gesamtarchitektur

Das Schachspiel folgt einer klassischen Drei-Schichten-Architektur:

```
┌─────────────────────────────────────┐
│     Frontend (Vue.js)               │
│  - Benutzeroberfläche               │
│  - State Management                 │
│  - API Integration                  │
└──────────────┬──────────────────────┘
               │ HTTP/REST + WebSocket
               │
┌──────────────▼──────────────────────┐
│     Backend (Spring Boot)           │
│  - REST API Endpoints               │
│  - Schachspiel-Logik                │
│  - WebSocket Handler                │
└──────────────┬──────────────────────┘
               │ JPA/Hibernate
               │
┌──────────────▼──────────────────────┐
│     Datenbank (PostgreSQL)          │
│  - Spielstände                      │
│  - Spielhistorie                    │
└─────────────────────────────────────┘
```

## Backend-Architektur

### Modell-Schicht (Model Layer)

**Piece-Klassen:**
- `PieceType`: Enum für Figurentypen (PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING)
- `PieceColor`: Enum für Spielerfarben (WHITE, BLACK)
- `Piece`: Repräsentiert eine Schachfigur mit Typ, Farbe und Bewegungsstatus

**Spielzustand:**
- `Position`: Repräsentiert eine Position auf dem Schachbrett (Row/Col)
- `Move`: Repräsentiert einen Spielzug mit From/To Position und Spezialzügen
- `ChessBoard`: Kernklasse mit 8x8 Brett und gesamter Spiellogik
- `Game`: JPA Entity für Datenbank-Persistierung

**Spiellogik in ChessBoard:**
1. **Initialisierung**: Standard-Schachbrett-Aufstellung
2. **Zugvalidierung**: 
   - Bauern: Vorwärtsbewegung, Doppelschritt, Diagonale Schläge, En Passant
   - Türme: Horizontal/Vertikal mit freiem Weg
   - Springer: L-förmige Bewegung
   - Läufer: Diagonal mit freiem Weg
   - Dame: Turm + Läufer kombiniert
   - König: Ein Feld in jede Richtung + Rochade
3. **Spezialzüge**:
   - Rochade (Castling)
   - En Passant
   - Bauernumwandlung (Pawn Promotion)
4. **Spielstandsprüfung**: Schach-Erkennung

### Service-Schicht

**GameService:**
- `createGame()`: Erstellt neues Spiel mit initialisiertem Brett
- `makeMove()`: Validiert und führt Züge aus
- `getGame()`: Lädt Spielstand aus Datenbank
- Board-Serialisierung/Deserialisierung zu JSON

### Controller-Schicht

**GameController:**
- `POST /api/games`: Neues Spiel erstellen
- `GET /api/games/{id}`: Spiel abrufen
- `POST /api/games/{id}/move`: Zug ausführen
- `GET /api/games`: Alle Spiele auflisten
- `GET /api/games/player/{name}`: Spieler-Spiele abrufen

### WebSocket-Konfiguration

**WebSocketConfig:**
- STOMP-Protokoll für Echtzeit-Kommunikation
- Endpunkt: `/ws`
- Message Broker für Topic-basierte Nachrichten
- Ermöglicht Online-Mehrspielermodus

## Frontend-Architektur

### Komponenten-Struktur

**ChessBoard.vue:**
- Visuelle Darstellung des 8x8 Schachbretts
- Farbige Felder (hell/dunkel)
- Unicode-Schachfiguren (♔ ♕ ♖ ♗ ♘ ♙)
- Koordinatenanzeige (a-h, 1-8)
- Markierung für ausgewählte Felder und gültige Züge
- Click-Handler für Figurenauswahl und Bewegung

### Views

**HomeView.vue:**
- Startseite mit Spielerstellung
- Formular für Spielernamen
- Online/Offline-Modus-Auswahl
- Liste gespeicherter Spiele
- Navigation zu laufenden Spielen

**GameView.vue:**
- Hauptspielansicht
- ChessBoard-Komponente Integration
- Spielinformationen (Spieler, aktueller Zug, Status)
- Zuglogik und API-Integration
- Spielverwaltung (Zurück, Neues Spiel)

### Services

**api.js:**
- Axios-basierte HTTP-Client-Funktionen
- REST API Integration
- Fehlerbehandlung
- Zentrale API-Konfiguration

### Routing

**Vue Router:**
- `/` - Home (Spielauswahl/Erstellung)
- `/game/:id` - Spielansicht

## Datenbank-Schema

**games Tabelle:**
- `id`: Primary Key (Auto-increment)
- `white_player`: Varchar (Weißer Spieler Name)
- `black_player`: Varchar (Schwarzer Spieler Name)
- `board_state`: Text (JSON-serialisiertes Brett)
- `move_history`: Text (JSON-serialisierte Zughistorie)
- `current_turn`: Enum (WHITE/BLACK)
- `status`: Enum (WAITING, IN_PROGRESS, CHECKMATE, etc.)
- `created_at`: Timestamp
- `last_move_at`: Timestamp
- `is_online_mode`: Boolean

## Spielablauf

### Offline-Modus:
1. Benutzer erstellt Spiel mit zwei Spielernamen
2. Backend initialisiert neues Spiel mit Standard-Brett
3. Frontend zeigt Brett an
4. Spieler wählen abwechselnd Figuren und ziehen
5. Frontend sendet Züge an Backend zur Validierung
6. Backend validiert, führt Zug aus, speichert Zustand
7. Frontend aktualisiert Anzeige

### Online-Modus:
1. Wie Offline-Modus, aber mit WebSocket-Verbindung
2. Züge werden über WebSocket an andere Clients gesendet
3. Echtzeit-Synchronisation zwischen Spielern
4. Ermöglicht Spiel über das Netzwerk

## Technische Highlights

### Backend:
- **Vollständige Schachregeln**: Alle Standard-Züge implementiert
- **Move Validation**: Serverseitige Zugvalidierung
- **Path Checking**: Prüft ob Weg zwischen Feldern frei ist
- **Check Detection**: Erkennt Schach-Situationen
- **State Persistence**: Spielstände in PostgreSQL
- **RESTful API**: Saubere REST-Schnittstelle
- **WebSocket Support**: Echtzeit-Kommunikation

### Frontend:
- **Responsive Design**: Moderne, ansprechende UI
- **Chess Notation**: Standard-Schachnotation (a-h, 1-8)
- **Visual Feedback**: Markierungen für Auswahl und gültige Züge
- **State Management**: Lokales State Management mit Vue 3
- **Component-Based**: Wiederverwendbare Komponenten
- **Type Safety**: TypeScript-Unterstützung

## Erweiterungsmöglichkeiten

1. **Schachmatt-Erkennung**: Vollständige Implementierung
2. **Zugvorschläge**: KI-basierte Hilfe
3. **Zeitkontrolle**: Schachuhr-Funktion
4. **Replay**: Spiele wiedergeben
5. **Statistiken**: Spieler-Statistiken und ELO
6. **Turniere**: Mehrere Spieler, Turniermodus
7. **3D-Ansicht**: Alternative Visualisierung
8. **Mobile App**: Native Mobile-Version
