from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, ListFlowable, ListItem
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from datetime import datetime

def create_pdf(filename):
    doc = SimpleDocTemplate(filename, pagesize=letter)
    styles = getSampleStyleSheet()
    story = []

    # Custom Styles
    title_style = styles['Title']
    heading_style = styles['Heading2']
    normal_style = styles['Normal']
    code_style = ParagraphStyle(
        'Code',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=9,
        leading=12,
        backColor=colors.lightgrey,
    )

    # Title
    story.append(Paragraph("Dokumentation: Deployment der Schach-Anwendung", title_style))
    story.append(Spacer(1, 12))
    story.append(Paragraph(f"Datum: {datetime.now().strftime('%d.%m.%Y')}", normal_style))
    story.append(Spacer(1, 24))

    # 1. Einleitung
    story.append(Paragraph("1. Einleitung", heading_style))
    story.append(Paragraph(
        "Ziel dieser Sitzung war das Deployment (Veröffentlichung) einer Full-Stack Schach-Anwendung "
        "auf der Plattform Render.com. Die Anwendung besteht aus einem Java Spring Boot Backend und "
        "einem Vue.js Frontend.", normal_style))
    story.append(Spacer(1, 12))

    # 2. Architektur & Deployment-Setup
    story.append(Paragraph("2. Architektur & Deployment-Setup", heading_style))
    story.append(Paragraph(
        "Das Deployment wurde in zwei Services aufgeteilt:", normal_style))
    
    setup_data = [
        ["Komponente", "Technologie", "Hosting-Typ", "Konfiguration"],
        ["Backend", "Java / Spring Boot", "Web Service (Docker)", "Dockerfile"],
        ["Frontend", "Vue.js", "Static Site", "Node.js Build"]
    ]
    t = Table(setup_data, colWidths=[80, 100, 120, 150])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
        ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
        ('GRID', (0, 0), (-1, -1), 1, colors.black),
    ]))
    story.append(t)
    story.append(Spacer(1, 12))

    # 3. Durchgeführte Schritte & Probleme
    story.append(Paragraph("3. Problemlösungsprozess (Troubleshooting)", heading_style))
    story.append(Paragraph(
        "Während des Deployments traten mehrere technische Herausforderungen auf, die schrittweise gelöst wurden:", normal_style))
    story.append(Spacer(1, 12))

    # Problem 1
    story.append(Paragraph("A. Backend: Docker Build Fehler", styles['Heading3']))
    story.append(Paragraph(
        "<b>Problem:</b> Der Build-Prozess schlug fehl ('Copy failed'), da die Pfade im Dockerfile nicht zur Ordnerstruktur passten.", normal_style))
    story.append(Paragraph(
        "<b>Lösung:</b> Pfade im Dockerfile korrigiert (z.B. <code>COPY backend/src src</code> statt <code>COPY src src</code>).", normal_style))
    story.append(Spacer(1, 6))

    # Problem 2
    story.append(Paragraph("B. Frontend: Blueprint & Verbindung", styles['Heading3']))
    story.append(Paragraph(
        "<b>Problem:</b> Die automatische Erkennung des Frontends via <code>render.yaml</code> (Infrastructure as Code) funktionierte nicht zuverlässig.", normal_style))
    story.append(Paragraph(
        "<b>Lösung:</b> Strategiewechsel zur manuellen Erstellung der 'Static Site' im Dashboard. Dies erwies sich als robuster.", normal_style))
    story.append(Spacer(1, 6))

    # Problem 3
    story.append(Paragraph("C. Kommunikation: CORS Fehler (Error 403)", styles['Heading3']))
    story.append(Paragraph(
        "<b>Problem:</b> Das Frontend durfte nicht auf das Backend zugreifen ('Cross-Origin Request Blocked').", normal_style))
    story.append(Paragraph(
        "<b>Lösung:</b> Implementierung einer globalen <code>CorsFilter</code>-Klasse im Backend, die explizit alle Ursprünge freigibt.", normal_style))
    story.append(Spacer(1, 6))

    # Problem 4
    story.append(Paragraph("D. Datenstruktur: Leeres Schachbrett", styles['Heading3']))
    story.append(Paragraph(
        "<b>Problem:</b> Das Spiel startete, aber das Brett blieb leer. Die Analyse zeigte, dass das Backend leere Objekte sendete.", normal_style))
    story.append(Paragraph(
        "<b>Lösung:</b> Hinzufügen von expliziten Getter-Methoden in der Java-Klasse <code>Piece.java</code>, da die Lombok-Annotation <code>@Data</code> in der Build-Umgebung nicht wie erwartet griff.", normal_style))
    story.append(Spacer(1, 6))
    
    # Problem 5
    story.append(Paragraph("E. Konfiguration: API URL", styles['Heading3']))
    story.append(Paragraph(
        "<b>Problem:</b> 404 Fehler beim Laden des Spiels.", normal_style))
    story.append(Paragraph(
        "<b>Lösung:</b> Die Environment-Variable <code>VITE_API_URL</code> war falsch gesetzt. Korrektur von <code>...onrender.com</code> zu <code>...onrender.com/api</code>.", normal_style))
    story.append(Spacer(1, 12))

    # 4. Fazit
    story.append(Paragraph("4. Fazit & Status", heading_style))
    story.append(Paragraph(
        "Die Anwendung ist nun erfolgreich deployed.", normal_style))
    
    list_data = [
        "Backend ist live und erreichbar.",
        "Frontend lädt korrekt und kommuniziert mit dem Backend.",
        "Spielmechanik (Erstellen, Ziehen) funktioniert.",
        "Besonderheit: Aufgrund des Free-Tiers hat der Server eine 'Cold Start'-Zeit von ca. 60 Sekunden."
    ]
    
    # Simple bullet list manually
    for item in list_data:
        story.append(Paragraph(f"• {item}", normal_style))

    doc.build(story)

if __name__ == "__main__":
    output_filename = "Deployment_Dokumentation.pdf"
    try:
        create_pdf(output_filename)
        print(f"PDF erfolgreich erstellt: {output_filename}")
    except Exception as e:
        print(f"Fehler beim Erstellen der PDF: {e}")
