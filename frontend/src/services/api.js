import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export default {
  async createGame(whitePlayer, blackPlayer, isOnlineMode = false, timeLimit = null) {
    const response = await axios.post(`${API_BASE_URL}/games`, {
      whitePlayer,
      blackPlayer,
      isOnlineMode,
      timeLimit
    })
    return response.data
  },

  async getGame(gameId) {
    const response = await axios.get(`${API_BASE_URL}/games/${gameId}`)
    return response.data
  },

  async makeMove(gameId, move) {
    const response = await axios.post(`${API_BASE_URL}/games/${gameId}/move`, move)
    return response.data
  },

  async getAllGames() {
    const response = await axios.get(`${API_BASE_URL}/games`)
    return response.data
  },

  async getPlayerGames(playerName) {
    const response = await axios.get(`${API_BASE_URL}/games/player/${playerName}`)
    return response.data
  },

  async getValidMoves(gameId, row, col) {
    const response = await axios.get(`${API_BASE_URL}/games/${gameId}/valid-moves`, {
      params: { row, col }
    })
    return response.data
  },

  async getBoardAtMove(gameId, moveIndex) {
    const response = await axios.get(`${API_BASE_URL}/games/${gameId}/board`, {
      params: { move: moveIndex }
    })
    return response.data
  }
}
