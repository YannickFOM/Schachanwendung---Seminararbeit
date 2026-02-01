# Anleitung zum Deployment des Schach-Backends auf Render.com

Diese Anleitung führt Sie Schritt für Schritt durch den Prozess, Ihr Spring Boot Backend im kostenlosen Tier von Render.com zu veröffentlichen.

## Voraussetzungen

1.  Ein GitHub-Account.
2.  Der Programmcode muss auf GitHub hochgeladen sein.
3.  Ein Account bei [Render.com](https://render.com/).

## Schritt 1: Code auf GitHub vorbereiten

Stellen Sie sicher, dass die neuesten Änderungen (insbesondere `application.properties` und `render.yaml`) in Ihr GitHub-Repository gepusht sind.

## Schritt 2: Service auf Render erstellen

Wir verwenden die Datei `render.yaml`, um die Konfiguration zu automatisieren.

1.  Loggen Sie sich bei [Render.com](https://render.com/) ein.
2.  Klicken Sie oben rechts auf **"New +"** und wählen Sie **"Blueprint"**.
3.  Verknüpfen Sie Ihren GitHub-Account, falls noch nicht geschehen.
4.  Wählen Sie Ihr Repository **"Schachspiel"** (oder wie Sie es genannt haben) aus.
5.  Klicken Sie auf **"Connect"**.
6.  Geben Sie dem Blueprint einen Namen (z.B. "Schach-Deployment") und klicken Sie auf **"Apply"**.

Render wird nun automatisch:
- Den Code herunterladen.
- Das Projekt mit Maven bauen (`mvn clean package`).
- Den Server starten.

## Schritt 3: Status überprüfen

Im Dashboard von Render sehen Sie nun Ihren Service `chess-backend`.
Klicken Sie darauf, um die Logs zu sehen.

- Der Prozess "Build" kann einige Minuten dauern.
- Achten Sie auf die Meldung `Build successful`.
- Danach startet der Service. Warten Sie auf Logs wie `Started ChessApplication in ... seconds`.
- Sobald der Service läuft, sehen Sie oben links die URL (z.B. `https://chess-backend-xyz.onrender.com`).

## Schritt 4: Testen

Öffnen Sie die URL in Ihrem Browser. Da wir keinen Root-Endpoint (`/`) definiert haben, sehen Sie vielleicht eine Fehlerseite (Whitelabel Error Page), aber das bedeutet, dass der Server läuft!

Testen Sie einen API-Endpunkt, um sicherzugehen:
`https://IHR-SERVICE-URL.onrender.com/api/games`

Sie sollten eine leere Liste `[]` oder JSON-Daten sehen.

## Wichtige Hinweise zum Free Tier

- **Spin-down**: Wenn Ihr Service 15 Minuten lang keine Anfragen erhält, geht er in den "Schlafmodus". Die nächste Anfrage kann dann 30-60 Sekunden dauern, bis der Server wieder wach ist.
- Für Ihre Seminararbeit ist das okay, aber erwähnen Sie es bei einer Live-Demo (einmal vorher aufrufen, um ihn zu wecken).

## Fehlerbehebung

- **Build fehlgeschlagen?** Prüfen Sie die Logs. Oft liegt es an Java-Versionen. Wir haben Java 17 in `render.yaml` eingestellt.
- **Service startet nicht?** Prüfen Sie, ob der JAR-Dateiname im `startCommand` in `render.yaml` korrekt ist. Ich habe `target/chess-backend-1.0.0.jar` angenommen. Falls Sie den Namen in `pom.xml` geändert haben, müssen Sie ihn hier anpassen.
