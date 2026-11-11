import React, { useEffect, useState, useCallback } from 'react';
import { Bot } from 'lucide-react';
import { soundManager } from '../../utils/soundManager';
import './BotLoadingModal.css';

const DEFAULT_BOTS = [
  { id: 2, name: 'Bot Chispa', isBot: true, pelusaId: 3, trait: '⚡ Arriesgado', color: '#eab308' },
  { id: 3, name: 'Bot Pelusín', isBot: true, pelusaId: 5, trait: '🧶 Calculador', color: '#3b82f6' },
  { id: 4, name: 'Bot Rayo', isBot: true, pelusaId: 8, trait: '🔥 Ladrón veloz', color: '#ef4444' },
  { id: 5, name: 'Bot Sombrío', isBot: true, pelusaId: 10, trait: '👾 Sigiloso', color: '#a855f7' },
];

export function BotLoadingModal({ playerName = 'Tú', onCancel, onReady }) {
  const [progress, setProgress] = useState(0);

  const handleEnterGame = useCallback(() => {
    const finalRoster = [
      { id: 1, name: playerName || 'Tú', isBot: false },
      ...DEFAULT_BOTS.map(b => ({ id: b.id, name: b.name, isBot: true })),
    ];
    onReady(finalRoster);
  }, [playerName, onReady]);

  useEffect(() => {
    soundManager.playButtonClick();

    const interval = setInterval(() => {
      setProgress(prev => {
        const next = prev + 4;
        if (next >= 100) {
          clearInterval(interval);
          return 100;
        }
        return next;
      });
    }, 70);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (progress >= 100) {
      const timer = setTimeout(() => {
        handleEnterGame();
      }, 700);
      return () => clearTimeout(timer);
    }
  }, [progress, handleEnterGame]);

  const statusText = 
    progress < 25 ? '🔍 Localizando rivales virtuales...' :
    progress < 50 ? '🤖 Cargando Bot Chispa y Bot Pelusín...' :
    progress < 75 ? '⚡ Conectando a Bot Rayo y Bot Sombrío...' :
    progress < 100 ? '🎴 Barajando el montón de polvo (110 cartas)...' :
    '✨ ¡Los 4 bots están listos! Entrando a la mesa...';

  const botsReady = [
    progress >= 20,
    progress >= 45,
    progress >= 70,
    progress >= 95,
  ];

  return (
    <div className="bot-loader-overlay">
      <div className="bot-loader-card pixel-box">
        <div className="bot-loader-header">
          <div className="bot-loader-badge">
            <Bot size={18} /> MODO VS BOTS
          </div>
          <h2 className="bot-loader-title">ENTRANDO AL JUEGO...</h2>
          <p className="bot-loader-subtitle">
            Cargando la mesa y conectando a tus 4 contrincantes IA
          </p>
        </div>

        {/* Barra de progreso retro */}
        <div className="bot-progress-container">
          <div className="bot-progress-bar" style={{ width: `${progress}%` }} />
          <span className="bot-progress-text">{progress}%</span>
        </div>
        <p className="bot-status-msg">{statusText}</p>

        {/* Tarjetas de los 4 Bots */}
        <div className="bots-grid">
          {DEFAULT_BOTS.map((bot, index) => {
            const isReady = botsReady[index];
            return (
              <div 
                key={bot.id} 
                className={`bot-card pixel-sub-box ${isReady ? 'ready' : 'connecting'}`}
                style={{ borderColor: isReady ? bot.color : 'rgba(255,255,255,0.1)' }}
              >
                <div className="bot-avatar-wrap">
                  <img 
                    src={`/cards/Pelusa${bot.pelusaId}.png`} 
                    alt={bot.name}
                    className="bot-pelusa-avatar"
                  />
                  {isReady && <span className="bot-ready-dot" />}
                </div>

                <div className="bot-details">
                  <div className="bot-name-row">
                    <span className="bot-name">{bot.name}</span>
                  </div>
                  <span className="bot-trait">{bot.trait}</span>
                  <span className={`bot-status-pill ${isReady ? 'online' : 'loading'}`}>
                    {isReady ? '🟢 LISTO' : '⏳ CONECTANDO...'}
                  </span>
                </div>
              </div>
            );
          })}
        </div>

        {/* Footer con acciones */}
        <div className="bot-loader-footer">
          <button 
            type="button" 
            className="retro-btn btn-settings bot-cancel-btn"
            onClick={onCancel}
          >
            CANCELAR
          </button>

          <button 
            type="button" 
            className="retro-btn btn-play bot-start-now-btn"
            onClick={handleEnterGame}
          >
            ¡ENTRAR AHORA! ({playerName})
          </button>
        </div>
      </div>
    </div>
  );
}
