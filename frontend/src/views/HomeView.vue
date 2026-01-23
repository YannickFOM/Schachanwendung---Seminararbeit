<template>
  <div class="home-view">
    <div class="header">
      <h1>Schachspiel</h1>
      <p class="subtitle">Online und Offline Schach spielen</p>
    </div>

    <div class="menu-container">
      <div class="menu-card">
        <h2>Neues Spiel starten</h2>
        <form @submit.prevent="startGame" class="game-form">
          <div class="form-group">
            <label for="whitePlayer">Weißer Spieler:</label>
            <input
              id="whitePlayer"
              v-model="whitePlayer"
              type="text"
              placeholder="Name eingeben"
              required
            />
          </div>

          <div class="form-group">
            <label for="blackPlayer">Schwarzer Spieler:</label>
            <input
              id="blackPlayer"
              v-model="blackPlayer"
              type="text"
              placeholder="Name eingeben"
              required
            />
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input v-model="isOnlineMode" type="checkbox" />
              Online-Modus
            </label>
          </div>

          <button type="submit" class="btn btn-primary">
            Spiel starten
          </button>
        </form>
      </div>

      <div class="menu-card">
        <h2>Gespeicherte Spiele</h2>
        <div v-if="games.length === 0" class="no-games">
          <p>Keine gespeicherten Spiele vorhanden</p>
        </div>
        <div v-else class="games-list">
          <div
            v-for="game in games"
            :key="game.id"
            class="game-item"
            @click="continueGame(game.id)"
          >
            <div class="game-info">
              <strong>{{ game.whitePlayer }}</strong> vs <strong>{{ game.blackPlayer }}</strong>
            </div>
            <div class="game-status">
              Status: {{ getStatusText(game.status) }}
            </div>
          </div>
        </div>
        <button @click="loadGames" class="btn btn-secondary">
          Spiele aktualisieren
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'

export default {
  name: 'HomeView',
  setup() {
    const router = useRouter()
    
    const whitePlayer = ref('')
    const blackPlayer = ref('')
    const isOnlineMode = ref(false)
    const games = ref([])

    const startGame = async () => {
      try {
        const game = await api.createGame(
          whitePlayer.value,
          blackPlayer.value,
          isOnlineMode.value
        )
        router.push(`/game/${game.id}`)
      } catch (error) {
        console.error('Error creating game:', error)
        alert('Fehler beim Erstellen des Spiels')
      }
    }

    const loadGames = async () => {
      try {
        games.value = await api.getAllGames()
      } catch (error) {
        console.error('Error loading games:', error)
      }
    }

    const continueGame = (gameId) => {
      router.push(`/game/${gameId}`)
    }

    const getStatusText = (status) => {
      const statusMap = {
        'WAITING': 'Wartend',
        'IN_PROGRESS': 'Läuft',
        'CHECKMATE': 'Schachmatt',
        'STALEMATE': 'Patt',
        'DRAW': 'Unentschieden',
        'RESIGNED': 'Aufgegeben'
      }
      return statusMap[status] || status
    }

    onMounted(() => {
      loadGames()
    })

    return {
      whitePlayer,
      blackPlayer,
      isOnlineMode,
      games,
      startGame,
      loadGames,
      continueGame,
      getStatusText
    }
  }
}
</script>

<style scoped>
.home-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.header {
  text-align: center;
  color: white;
  margin-bottom: 50px;
}

.header h1 {
  font-size: 60px;
  margin-bottom: 10px;
  text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.3);
}

.subtitle {
  font-size: 24px;
  opacity: 0.9;
}

.menu-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 30px;
}

.menu-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.menu-card h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 28px;
  font-weight: bold;
}

.game-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: bold;
  color: #2c3e50;
  font-size: 1.1rem;
}

.form-group input[type="text"] {
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input[type="text"]:focus {
  outline: none;
  border-color: #667eea;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  margin-right: 10px;
  width: 20px;
  height: 20px;
  cursor: pointer;
}

.btn {
  padding: 14px 28px;
  font-size: 18px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
  margin-top: 10px;
}

.btn-primary {
  background-color: #4CAF50;
  color: white;
}

.btn-primary:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.btn-secondary {
  background-color: #667eea;
  color: white;
}

.btn-secondary:hover {
  background-color: #5568d3;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.no-games {
  text-align: center;
  padding: 40px;
  color: #999;
}

.games-list {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 20px;
}

.game-item {
  padding: 15px;
  border: 2px solid #eee;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.game-item:hover {
  border-color: #667eea;
  background-color: #f8f8ff;
  transform: translateX(5px);
}

.game-info {
  font-size: 16px;
  margin-bottom: 5px;
}

.game-status {
  font-size: 14px;
  color: #666;
}
</style>
