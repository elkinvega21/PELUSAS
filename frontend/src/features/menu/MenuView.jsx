import React from 'react';
import { Fireworks } from './Fireworks';
import './MenuView.css';

const MENU_PELUSAS = Array.from({ length: 10 }, (_, index) => `/cards/Pelusa${index + 1}.png`);

export function MenuView({ onPlay, onOpenSettings, onOpenLeaderboard, onOpenJoinCode }) {
  return (
    <>
      <Fireworks />
      <div className="menu-pelusas" aria-hidden="true">
        {MENU_PELUSAS.map((src, index) => (
          <div
            className="menu-pelusa-track"
            key={src}
            style={{ animationDelay: `${index * -1.6}s` }}
          >
            <img
              className="menu-pelusa-image"
              src={src}
              alt=""
              draggable="false"
              style={{ animationDelay: `${(index % 4) * -0.35}s` }}
            />
          </div>
        ))}
      </div>

      <div className="menu-container">
        {/* Botón principal JUGAR (requerido por especificación BDD Cucumber) */}
        <button
          id="btn-play"
          type="button"
          className="retro-btn btn-play"
          onClick={() => onPlay('local')}
        >
          JUGAR
        </button>

        <button
          id="btn-play-bots"
          type="button"
          className="retro-btn btn-play btn-multiplayer-local"
          onClick={() => onPlay('bots')}
        >
          🤖 PARTIDA VS BOTS
        </button>

        <button
          id="btn-multiplayer-online"
          type="button"
          className="retro-btn btn-settings btn-multiplayer-online"
          onClick={() => onPlay('online')}
        >
          🌐 SALA ONLINE CON AMIGOS
        </button>

        <button
          id="btn-join-code-menu"
          type="button"
          className="retro-btn btn-settings btn-join-code-menu"
          onClick={onOpenJoinCode}
        >
          🔢 UNIRME CON CODIGO
        </button>

        <button
          id="btn-leaderboard"
          type="button"
          className="retro-btn btn-settings btn-leaderboard"
          onClick={onOpenLeaderboard}
        >
          🏆 RANKING & HISTORIAL
        </button>

        <button
          id="btn-settings"
          type="button"
          className="retro-btn btn-settings"
          onClick={onOpenSettings}
          aria-label="CONFIGURACIÓN"
        >
          CONFIGURACION
        </button>
      </div>
    </>
  );
}
