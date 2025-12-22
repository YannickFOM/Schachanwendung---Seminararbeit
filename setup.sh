#!/bin/bash

echo "=== Schachspiel Setup Script ==="
echo ""

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi
echo "✓ Java found: $(java -version 2>&1 | head -n 1)"

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi
echo "✓ Maven found: $(mvn -version 2>&1 | head -n 1)"

if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18 or higher."
    exit 1
fi
echo "✓ Node.js found: $(node --version)"

if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm."
    exit 1
fi
echo "✓ npm found: $(npm --version)"

echo ""
echo "Starting PostgreSQL with Docker Compose..."
docker-compose up -d

echo ""
echo "Installing backend dependencies..."
cd backend
mvn clean install -DskipTests
cd ..

echo ""
echo "Installing frontend dependencies..."
cd frontend
npm install
cd ..

echo ""
echo "=== Setup Complete! ==="
echo ""
echo "To start the application:"
echo "  1. Backend:  cd backend && mvn spring-boot:run"
echo "  2. Frontend: cd frontend && npm run dev"
echo ""
echo "Then open http://localhost:5173 in your browser"
