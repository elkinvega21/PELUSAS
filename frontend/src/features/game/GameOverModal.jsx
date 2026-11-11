import React, { useEffect } from 'react';
import confetti from 'canvas-confetti';
import { ArrowLeft, RotateCcw, Shield, Bot, User } from 'lucide-react';
import './GameOverModal.css';

export function GameOverModal({ results = [], isAbandoned = false, onPlayAgain, onExitToMenu, onOpenLeaderboard }) {
  useEffect(() => {
    // Si la partida no fue abandonada, disparo festivo de confeti retro
    if (isAbandoned) return;

    try {
      confetti({
        particleCount: 80,
        spread: 70,
        origin: { y: 0.6 },
        colors: ['#fbbf24', '#a855f7', '#38bdf8', '#4ade80', '#ef4444'],
      });
      const timeout = setTimeout(() => {
        confetti({
          particleCount: 50,
          angle: 60,
          spread: 55,
          origin: { x: 0 },
        });
        confetti({
          particleCount: 50,
          angle: 120,
          spread: 55,
          origin: { x: 1 },
        });
      }, 400);
      return () => clearTimeout(timeout);
    } catch {
      // Si falla confetti en entornos sin canvas, ignorar limpiamente
    }
  }, [isAbandoned]);

  const winner = results[0] || null;

  return (
    <div className="game-over-overlay" role="dialog" aria-modal="true" aria-label="Fin de la partida">
      <div className="game-over-card pixel-box">
        {/* Alerta de Abandono si el humano se salió */}
        {isAbandoned && (
          <div className="abandoned-banner pixel-sub-box">
            <span className="abandoned-banner-title">💀 SALIDA ANTICIPADA / ABANDONO</span>
            <span className="abandoned-banner-desc">
              Has abandonado la partida contra los bots. Se simuló el desenlace aleatorio entre la IA.
            </span>
          </div>
        )}

        {/* Cabecera */}
        <div className="game-over-header">
          <h2 className="game-over-title">
            {isAbandoned ? 'RESULTADO FINAL SIMULADO' : '¡PARTIDA FINALIZADA!'}
          </h2>
          <p className="game-over-subtitle">
            {isAbandoned ? 'Victoria concedida a la inteligencia artificial.' : 'Se han agotado las cartas del montón de polvo.'}
          </p>
        </div>

        {/* Tarjeta del Ganador */}
        {winner && (
          <div className="winner-banner pixel-sub-box">
            <div className="winner-badge-icon" aria-hidden="true">🏆</div>
            <div className="winner-info">
              <span className="winner-label">★ ¡GANADOR DE LA PARTIDA! ★</span>
              <span className="winner-name">{winner.name}</span>
              <span className="winner-score">{winner.score} PUNTOS</span>
            </div>
          </div>
        )}

        {/* Tabla de Clasificación */}
        <div className="ranking-table">
          {results.map((player, idx) => (
            <div
              key={player.id}
              className={`ranking-row pixel-sub-box ${idx === 0 ? 'winner-row' : ''}`}
            >
              <div className="ranking-col-left">
                <span className="ranking-position">#{idx + 1}</span>
                {player.isBot ? (
                  <Bot size={16} className="player-icon bot-icon" />
                ) : (
                  <User size={16} className="player-icon user-icon" />
                )}
                <span className="ranking-player-name">{player.name}</span>
                {player.isBot && <span className="bot-tag">BOT</span>}
                {player.abandoned && <span className="abandoned-tag">ABANDONÓ</span>}
              </div>

              <div className="ranking-col-right">
                <span className="ranking-cards">{player.cardsCount} cartas</span>
                <span className="ranking-points">
                  <Shield size={12} /> {player.score} pts
                </span>
              </div>
            </div>
          ))}
        </div>

        {/* Botones de acción */}
        <div className="game-over-actions">
          <button
            type="button"
            className="retro-btn btn-settings"
            onClick={onExitToMenu}
          >
            <ArrowLeft size={16} style={{ display: 'inline', marginRight: 6 }} />
            MENÚ
          </button>

          {onOpenLeaderboard && (
            <button
              type="button"
              className="retro-btn btn-settings"
              style={{ backgroundColor: '#2563eb' }}
              onClick={onOpenLeaderboard}
            >
              🏆 RANKING
            </button>
          )}

          <button
            type="button"
            className="retro-btn btn-play"
            onClick={onPlayAgain}
          >
            <RotateCcw size={16} style={{ display: 'inline', marginRight: 6 }} />
            OTRA VEZ
          </button>
        </div>
      </div>
    </div>
  );
}
