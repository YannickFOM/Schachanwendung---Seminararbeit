# Dokumentation: Deployment der Schach-Anwendung
**Datum:** 01.02.2026

## 1. Einleitung
Ziel dieser Sitzung war das Deployment (Veröffentlichung) einer Full-Stack Schach-Anwendung auf der Plattform **Render.com**. Die Anwendung besteht aus einem **Java Spring Boot Backend** und einem **Vue.js Frontend**.

## 2. Architektur & Deployment-Setup
Das Deployment wurde in zwei separate Services aufgeteilt, um die Skalierbarkeit und Wartbarkeit zu gewährleisten.

| Komponente | Technologie | Hosting-Typ | Konfiguration |
| :--- | :--- | :--- | :--- |
| **Backend** | Java / Spring Boot | Web Service (Docker) | `Dockerfile` |
| **Frontend** | Vue.js | Static Site | Node.js Build |

## 3. Problemlösungsprozess (Troubleshooting)
Während des Deployments traten mehrere technische Herausforderungen auf. Diese wurden analysiert und wie folgt gelöst:

### A. Backend: Docker Build Fehler
*   **Problem:** Der Build-Prozess schlug fehl ("Copy failed"), da die Pfade im `Dockerfile` nicht zur tatsächlichen Ordnerstruktur des Projekts passten.
*   **Lösung:** Die Pfade im `Dockerfile` wurden korrigiert (Anpassung von `COPY src src` zu `COPY backend/src src`), damit Docker die Dateien im Unterordner findet.

### B. Frontend: Blueprint & Verbindung
*   **Problem:** Die automatische Erkennung des Frontends über die `render.yaml` (Infrastructure as Code) funktionierte nicht zuverlässig; der Service wurde nicht korrekt angelegt.
*   **Lösung:** Wechsel der Strategie zur **manuellen Erstellung** der "Static Site" im Render-Dashboard. Dies erwies sich als deutlich robuster und fehlerfrei.

### C. Kommunikation: CORS Fehler (Error 403)
*   **Problem:** Das Frontend durfte nicht auf das Backend zugreifen. Der Browser blockierte die Anfragen mit einem "Cross-Origin Request Blocked" Fehler (CORS). Das Backend lehnte "Preflight"-Anfragen ab.
*   **Lösung:** Implementierung einer globalen `CorsFilter`-Klasse (`CorsConfig.java`) im Backend. Diese Konfiguration zwingt das Backend dazu, Anfragen von **allen Ursprüngen (*)** explizit zu erlauben.

### D. Datenstruktur: Leeres Schachbrett
*   **Problem:** Das Spiel startete zwar, aber das Schachbrett blieb leer. Die Analyse zeigte, dass das Backend zwar Daten sendete, diese aber "leer" waren (leere JSON-Objekte).
*   **Lösung:** Hinzufügen von expliziten **Getter-Methoden** in der Java-Klasse `Piece.java`. Die Lombok-Annotation `@Data` funktionierte in der Build-Umgebung von Render nicht wie erwartet, sodass der JSON-Serializer die Datenfelder (Farbe, Typ) nicht lesen konnte.

### E. Konfiguration: API URL (404 Fehler)
*   **Problem:** Das Frontend konnte keine Verbindung zum Backend herstellen (Fehler 404 Not Found), obwohl beide Services liefen.
*   **Lösung:** Die Environment-Variable `VITE_API_URL` im Frontend war falsch gesetzt. Sie enthielt nur die Basis-URL. Die Lösung war das Anhängen des Suffix `/api` (z.B. `...onrender.com/api`).

## 4. Fazit & Status
Die Anwendung ist nun erfolgreich online.

*   ✅ **Backend** ist live und erreichbar.
*   ✅ **Frontend** lädt korrekt und kommuniziert mit dem Backend.
*   ✅ **Spielmechanik** (Erstellen, Ziehen, Logik) funktioniert einwandfrei.

**Hinweis zum Hosting:** Da der kostenlose "Free Tier" von Render genutzt wird, geht der Server nach 15 Minuten Inaktivität in einen Schlafmodus. Der **erste Aufruf** nach einer Pause kann daher bis zu **60 Sekunden** dauern ("Cold Start"). Danach reagiert die Anwendung sofort.
