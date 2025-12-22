# Schnellstart-Anleitung

## Voraussetzungen

- Java 17+
- Maven 3.6+
- Node.js 18+
- Docker (optional, für einfache Datenbank-Einrichtung)

## Option 1: Automatische Installation (empfohlen)

```bash
# Setup-Skript ausführen
./setup.sh
```

Das Skript:
1. Prüft alle Voraussetzungen
2. Startet PostgreSQL mit Docker Compose
3. Installiert Backend-Dependencies
4. Installiert Frontend-Dependencies

## Option 2: Manuelle Installation

### 1. Datenbank starten

**Mit Docker:**
```bash
docker-compose up -d
```

**Ohne Docker:**
```bash
# PostgreSQL installieren und starten
sudo service postgresql start

# Datenbank erstellen
psql -U postgres
CREATE DATABASE chess_db;
CREATE USER chess_user WITH PASSWORD 'chess_password';
GRANT ALL PRIVILEGES ON DATABASE chess_db TO chess_user;
\q
```

### 2. Backend starten

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend läuft auf: http://localhost:8080

### 3. Frontend starten

In einem neuen Terminal:
```bash
cd frontend
npm install
npm run dev
```

Frontend läuft auf: http://localhost:5173

## Anwendung nutzen

1. Öffnen Sie http://localhost:5173 im Browser
2. Geben Sie die Namen für beide Spieler ein
3. Wählen Sie optional "Online-Modus"
4. Klicken Sie auf "Spiel starten"
5. Spielen Sie Schach!

## Spielregeln

- Klicken Sie auf eine Figur, um sie auszuwählen
- Klicken Sie auf ein gültiges Zielfeld, um zu ziehen
- Gültige Züge werden gelb markiert
- Das ausgewählte Feld wird grün markiert

## Spezielle Züge

- **Rochade**: König 2 Felder zur Seite bewegen (wenn Bedingungen erfüllt)
- **En Passant**: Bauern können diagonal schlagen, wenn Bedingungen erfüllt
- **Bauernumwandlung**: Bauern werden zur Dame beim Erreichen der Grundlinie

## Fehlerbehebung

### Backend startet nicht
- Prüfen Sie, ob PostgreSQL läuft: `docker-compose ps`
- Prüfen Sie die Datenbank-Verbindung in `backend/src/main/resources/application.properties`

### Frontend startet nicht
- Löschen Sie `frontend/node_modules` und führen Sie `npm install` erneut aus
- Prüfen Sie die Node.js-Version: `node --version` (sollte 18+ sein)

### API-Fehler
- Stellen Sie sicher, dass das Backend läuft (http://localhost:8080)
- Prüfen Sie die API-URL in `frontend/src/services/api.js`

## Entwicklung

### Backend neu kompilieren
```bash
cd backend
mvn clean compile
```

### Frontend Hot-Reload
```bash
cd frontend
npm run dev
```
Änderungen werden automatisch neu geladen.

### Tests ausführen
```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm run test
```

## Logs

### Backend-Logs
Logs werden in der Konsole angezeigt, wo `mvn spring-boot:run` läuft.

### Frontend-Logs
Logs werden im Browser-Console (F12) angezeigt.

## Produktions-Build

### Backend
```bash
cd backend
mvn clean package
java -jar target/chess-backend-1.0.0.jar
```

### Frontend
```bash
cd frontend
npm run build
# Dateien sind in frontend/dist/
```
