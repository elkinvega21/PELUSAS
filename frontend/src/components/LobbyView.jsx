import React, { useState } from 'react';
import { Users, Play, ArrowLeft, Plus, Trash2, Bot, User } from 'lucide-react';
import { soundManager } from '../utils/soundManager';
import './LobbyView.css';

export function LobbyView({ onBack, onStartGame }) {
  const [players, setPlayers] = useState([
    { id: 1, name: 'Jugador 1', isBot: false },
    { id: 2, name: 'Jugador 2', isBot: false },
  ]);

  const [newPlayerName, setNewPlayerName] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const handleAddPlayer = (asBot = false) => {
    soundManager.playButtonClick();
    if (players.length >= 5) {
      setErrorMsg('Máximo 5 jugadores permitidos');
      return;
    }
    const defaultName = asBot ? `Bot ${players.length + 1}` : (newPlayerName.trim() || `Jugador ${players.length + 1}`);
    const newPlayer = {
      id: Date.now(),
      name: defaultName,
      isBot: asBot,
    };
    setPlayers([...players, newPlayer]);
    setNewPlayerName('');
    setErrorMsg('');
  };

  const handleRemovePlayer = (id) => {
    soundManager.playButtonClick();
    if (players.length <= 2) {
      setErrorMsg('Se requieren mínimo 2 jugadores');
      return;
    }
    setPlayers(players.filter(p => p.id !== id));
    setErrorMsg('');
  };

  const handleStart = () => {
    soundManager.playButtonClick();
    if (players.length < 2) {
      setErrorMsg('Se requieren al menos 2 jugadores');
      return;
    }
    onStartGame(players);
  };

  return (
    <div className="lobby-overlay">
      <div className="lobby-card pixel-box">
        {/* Cabecera del Lobby */}
        <div className="lobby-header">
          <button 
            type="button" 
            className="pixel-btn-icon" 
            onClick={() => {
              soundManager.playButtonClick();
              onBack();
            }}
            title="Volver al menú"
          >
            <ArrowLeft size={18} />
          </button>
          <h2 className="lobby-title">NUEVA PARTIDA</h2>
        </div>

        <p className="lobby-subtitle">Configura los jugadores (Mínimo 2, Máximo 5)</p>

        {errorMsg && <div className="lobby-alert">{errorMsg}</div>}

        {/* Lista de Jugadores */}
        <div className="players-list">
          {players.map((player, index) => (
            <div key={player.id} className="player-row pixel-sub-box">
              <div className="player-info">
                <span className="player-badge">#{index + 1}</span>
                {player.isBot ? (
                  <Bot size={18} className="player-icon bot-icon" />
                ) : (
                  <User size={18} className="player-icon user-icon" />
                )}
                <span className="player-name">{player.name}</span>
                {player.isBot && <span className="bot-tag">BOT</span>}
              </div>

              {players.length > 2 && (
                <button
                  type="button"
                  className="pixel-btn-remove"
                  onClick={() => handleRemovePlayer(player.id)}
                  title="Eliminar jugador"
                >
                  <Trash2 size={16} />
                </button>
              )}
            </div>
          ))}
        </div>

        {/* Añadir jugadores */}
        {players.length < 5 && (
          <div className="add-player-section">
            <input
              type="text"
              className="pixel-input"
              placeholder="Nombre del jugador..."
              value={newPlayerName}
              maxLength={15}
              onChange={(e) => setNewPlayerName(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleAddPlayer(false);
              }}
            />
            <div className="add-buttons-row">
              <button
                type="button"
                className="pixel-action-btn btn-add-human"
                onClick={() => handleAddPlayer(false)}
              >
                <Plus size={16} /> JUGADOR
              </button>
              <button
                type="button"
                className="pixel-action-btn btn-add-bot"
                onClick={() => handleAddPlayer(true)}
              >
                <Bot size={16} /> BOT
              </button>
            </div>
          </div>
        )}

        {/* Botón iniciar partida */}
        <div className="lobby-footer">
          <button
            type="button"
            id="start-match-btn"
            className="retro-btn btn-play lobby-start-btn"
            onClick={handleStart}
          >
            COMENZAR PARTIDA
          </button>
        </div>
      </div>
    </div>
  );
}
