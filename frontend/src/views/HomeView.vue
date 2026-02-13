<template>
  <div class="home-view">
    <div class="header">
      <h1>Yannicks Schachapp</h1>
      <p class="subtitle">Schach spielen mit Freunden</p>
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
            <label for="timeLimit">Zeitlimit</label>
            <select id="timeLimit" v-model="timeLimit" required>
              <option :value="5">5 Sekunden (Test)</option>
              <option :value="300">5 Minuten</option>
              <option :value="600">10 Minuten</option>
              <option :value="null">Unbegrenzt</option>
            </select>
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
              <span class="time-badge">{{ formatTimeLimit(game.timeLimit) }}</span>
            </div>
            <div class="game-status">
              Status: {{ getStatusText(game.status) }}
            </div>
          </div>
        </div>

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
    const timeLimit = ref(300)
    const games = ref([])

    const startGame = async () => {
      try {
        const game = await api.createGame(
          whitePlayer.value,
          blackPlayer.value,
          isOnlineMode.value,
          timeLimit.value
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

    const formatTimeLimit = (seconds) => {
      if (seconds === null) return 'Unbegrenzt'
      if (seconds === 5) return '5 Sek (Test)'
      return Math.floor(seconds / 60) + ' Min'
    }

    onMounted(() => {
      loadGames()
    })

    return {
      whitePlayer,
      blackPlayer,
      isOnlineMode,
      timeLimit,
      games,
      startGame,
      loadGames,
      continueGame,
      getStatusText,
      formatTimeLimit
    }
  }
}
</script>

<style scoped>
.home-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #060538 0%, #021324 10%);
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

.form-group select {
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  background-color: white;
  cursor: pointer;
  transition: border-color 0.3s;
}

.form-group select:focus {
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
  overflow-x: hidden;
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
  color: #2c3e50; /* Darker text */
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-badge {
  background: #eee;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  color: #555;
  font-weight: bold;
}

.game-status {
  font-size: 14px;
  color: #444; /* Slightly darker grey */
  font-weight: 500;
}

/* Promotion Modal */
.promotion-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.promotion-modal {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.promotion-modal h3 {
  color: #2c3e50;
  margin-top: 0;
}

.promotion-options {
  display: flex;
  gap: 15px;
  margin-top: 15px;
}

.promo-btn {
  font-size: 32px;
  width: 60px;
  height: 60px;
  border: 2px solid #ddd;
  border-radius: 8px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.promo-btn:hover {
  background: #e9ecef;
  transform: translateY(-2px);
  border-color: #667eea;
}
</style>
