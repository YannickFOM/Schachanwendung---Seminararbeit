# Anleitung zum Deployment des Schach-Projekts auf Render.com

Diese Anleitung führt Sie Schritt für Schritt durch den Prozess, sowohl das Backend (Spring Boot) als auch das Frontend (Vue.js) auf Render.com zu veröffentlichen.

## Voraussetzungen

1.  Ein GitHub-Account.
2.  Der Programmcode muss auf GitHub hochgeladen sein.
3.  Ein Account bei [Render.com](https://render.com/).

## Schritt 1: Code auf GitHub vorbereiten

Stellen Sie sicher, dass die neuesten Änderungen gepusht sind (Backend & Frontend Code).

## Schritt 2: Backend deployen (via Blueprint)

Da das Backend bereits läuft (oder via `render.yaml` konfiguriert ist), lassen wir das so.
Falls noch nicht geschehen: Syncen Sie Ihr Blueprint für das Backend.

## Schritt 3: Frontend manuell erstellen

Da die automatische Erkennung des Frontends im Blueprint Probleme macht, erstellen wir es einfach manuell. Das geht sehr schnell.

1.  Klicken Sie im Render-Dashboard oben rechts auf **"New +"** -> **"Static Site"**.
2.  Wählen Sie Ihr Repository **"Schachspiel"**.
3.  Füllen Sie die Felder wie folgt aus:
    - **Name**: `chess-frontend`
    - **Region**: Frankfurt (oder Ihre Wahl)
    - **Root Directory**: `frontend`  <-- WICHTIG!
    - **Build Command**: `npm install && npm run build`
    - **Publish Directory**: `dist`
4.  Klicken Sie auf **"Create Static Site"**.

## Schritt 4: Frontend mit Backend verbinden

1.  Holen Sie sich die URL Ihres Backends (z.B. `https://chess-backend-xyz.onrender.com`).
2.  Gehen Sie in Ihrem neuen Frontend-Service auf **"Environment"**.
3.  Klicken Sie auf **"Add Environment Variable"**.
4.  Tragen Sie ein:
    - **Key**: `VITE_API_URL`
    - **Value**: `https://chess-backend-xyz.onrender.com/api` (Vergessen Sie nicht das `/api` am Ende!)
5.  Speichern Sie ("Save Changes").

Render wird das Frontend nun automatisch neu bauen.

## Schritt 5: Fertig!

Klicken Sie auf die URL des Frontends (z.B. `https://chess-frontend-abc.onrender.com`).
Das Spiel läuft! 🎉

## Fehlerbehebung

- **Weiße Seite?** Prüfen Sie die Browser-Konsole (F12).
- **Verbindungsfehler?** Prüfen Sie, ob `VITE_API_URL` korrekt ist (mit `https://` und `/api`).
