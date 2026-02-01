# Anleitung zum Deployment des Schach-Projekts auf Render.com

Diese Anleitung führt Sie Schritt für Schritt durch den Prozess, sowohl das Backend (Spring Boot) als auch das Frontend (Vue.js) auf Render.com zu veröffentlichen.

## Voraussetzungen

1.  Ein GitHub-Account.
2.  Der Programmcode muss auf GitHub hochgeladen sein.
3.  Ein Account bei [Render.com](https://render.com/).

## Schritt 1: Code auf GitHub vorbereiten

Stellen Sie sicher, dass die neuesten Änderungen in Ihr GitHub-Repository gepusht sind. Das beinhaltet:
- `render.yaml` (Definiert Backend & Frontend)
- `Dockerfile` (Für das Backend)
- Änderungen in `frontend/src/services/api.js` (Für die variable URL)

## Schritt 2: Services auf Render erstellen

Wir verwenden die Datei `render.yaml` ("Blueprints"), um alles automatisch anzulegen.

1.  Loggen Sie sich bei [Render.com](https://render.com/) ein.
2.  Gehen Sie auf **"Blueprints"**.
3.  Klicken Sie auf Ihr bestehendes Blueprint (z.B. "Yannicks Schachapp Deployment").
4.  Klicken Sie auf **"Manual Sync"** (oben rechts) oder bestätigen Sie die erkannten Änderungen.
5.  Render zeigt Ihnen nun an, dass ein **neuer Service** (`chess-frontend`) erstellt wird.
6.  Bestätigen Sie mit **"Apply"** oder **"Sync"**.
    Anschließend werden die Services erstellt:
    - `chess-backend` (Web Service)
    - `chess-frontend` (Static Site)
7.  Klicken Sie auf **"Apply"**.

## Schritt 3: Backend-URL herausfinden & Frontend konfigurieren

Das Frontend muss wissen, wo das Backend läuft.

1.  Warten Sie, bis der **Backend-Service** (`chess-backend`) erfolgreich deployed wurde (grüner Haken).
2.  Klicken Sie auf den Backend-Service und kopieren Sie die **URL** (oben links, z.B. `https://chess-backend-xyz.onrender.com`).
3.  Gehen Sie zurück zum Dashboard und klicken Sie auf den **Frontend-Service** (`chess-frontend`).
4.  Gehen Sie auf den Reiter **"Environment"**.
5.  Sie sehen dort eine Variable `VITE_API_URL` mit dem Wert `REPLACE_WITH_BACKEND_URL`.
6.  Klicken Sie auf **"Edit"** und fügen Sie **Ihre Backend-URL** ein.
    - **WICHTIG:** Hängen Sie `/api` hinten an!
    - Beispiel: `https://chess-backend-xyz.onrender.com/api`
7.  Speichern Sie ("Save Changes").

## Schritt 4: Frontend neu deployen

Nach der Änderung der Variable muss das Frontend neu gebaut werden.

1.  Klicken Sie im Frontend-Service oben rechts auf **"Manual Deploy"** -> **"Deploy latest commit"**.
2.  Warten Sie, bis der Build fertig ist.

## Schritt 5: Fertig!

Klicken Sie auf die URL des Frontends (z.B. `https://chess-frontend-abc.onrender.com`).
Das Spiel sollte nun laden und sich mit dem Backend verbinden.

## Fehlerbehebung

- **Frontend zeigt keine Spiele?** Prüfen Sie in der Browser-Konsole (F12), ob Netzwerkfehler auftreten.
    - Falls ja: Stimmt die `VITE_API_URL`? Haben Sie `/api` am Ende?
- **Backend startet nicht?** Prüfen Sie die Logs. Läuft der Docker-Build durch?
- **Spin-down**: Denken Sie daran, dass beide Services im Free Tier nach 15 Minuten Inaktivität schlafen gehen. Der erste Aufruf dauert dann etwas länger.
