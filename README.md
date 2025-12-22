# Schachspiel

Eine lauffähige und nutzerfreundliche Schach-Webanwendung mit Online- und Offline-Spielmodus.

## Projektübersicht

Dieses Projekt ist eine vollständige Schachanwendung bestehend aus:
- **Backend**: Java Spring Boot mit vollständiger Schachlogik und REST API
- **Frontend**: Vue.js Webanwendung mit intuitiver Benutzeroberfläche
- **Datenbank**: PostgreSQL für Spielstandsspeicherung

## Funktionen

- ✅ Vollständige Schachspiellogik (alle Figuren, Rochade, En Passant, Bauernumwandlung)
- ✅ Online-Spielmodus für Mehrspieler über das Internet
- ✅ Offline-Spielmodus für lokales Spiel
- ✅ Spielstandsspeicherung in PostgreSQL-Datenbank
- ✅ Übersichtliche Spielanzeige mit Drag-and-Drop-Funktionalität
- ✅ Spielverlauf und gespeicherte Spiele anzeigen

## Voraussetzungen

- Java 17 oder höher
- Maven 3.6+
- Node.js 18+ und npm
- PostgreSQL 14+

## Installation und Start

### 1. Datenbank einrichten

Erstellen Sie eine PostgreSQL-Datenbank:

```bash
psql -U postgres
CREATE DATABASE chess_db;
CREATE USER chess_user WITH PASSWORD 'chess_password';
GRANT ALL PRIVILEGES ON DATABASE chess_db TO chess_user;
```

### 2. Backend starten

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Das Backend läuft auf: http://localhost:8080

### 3. Frontend starten

```bash
cd frontend
npm install
npm run dev
```

Das Frontend läuft auf: http://localhost:5173

## Verwendung

1. Öffnen Sie http://localhost:5173 im Browser
2. Geben Sie Namen für beide Spieler ein
3. Wählen Sie optional "Online-Modus" für Netzwerkspiel
4. Klicken Sie auf "Spiel starten"
5. Spielen Sie Schach durch Klicken auf Figuren und Zielfelder

## API-Endpunkte

- `POST /api/games` - Neues Spiel erstellen
- `GET /api/games` - Alle Spiele abrufen
- `GET /api/games/{id}` - Bestimmtes Spiel abrufen
- `POST /api/games/{id}/move` - Zug ausführen
- `GET /api/games/player/{name}` - Spiele eines Spielers abrufen

## Technologie-Stack

**Backend:**
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- WebSocket (SockJS/STOMP)
- Lombok

**Frontend:**
- Vue.js 3
- Vue Router
- Pinia (State Management)
- Axios (HTTP Client)
- Vite (Build Tool)

## Projektstruktur

```
Schachspiel/
├── backend/
│   ├── src/main/java/com/schachspiel/chess/
│   │   ├── model/          # Datenmodelle (Piece, Board, Game)
│   │   ├── service/        # Geschäftslogik
│   │   ├── controller/     # REST Controllers
│   │   ├── repository/     # Datenbank-Repositories
│   │   └── config/         # Konfiguration (WebSocket, etc.)
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/     # Vue-Komponenten (ChessBoard)
│   │   ├── views/          # Seiten (Home, Game)
│   │   ├── services/       # API-Integration
│   │   └── router/         # Routing-Konfiguration
│   └── package.json
└── README.md
```

## Entwicklung

Das Projekt folgt dem MVC-Pattern:
- **Model**: Schach-Entitäten und Spiellogik
- **View**: Vue.js Frontend-Komponenten
- **Controller**: Spring REST Controllers

## Lizenz

Dieses Projekt wurde als Facharbeit entwickelt.

