import { useEffect, useState } from 'react';
import { soundManager } from './utils/soundManager';
import { LobbyView } from './components/LobbyView';
import { GameBoardView } from './components/GameBoardView';
import { SettingsModal } from './components/SettingsModal';
import './App.css';

const MENU_PELUSAS = Array.from({ length: 10 }, (_, index) => `/cards/Pelusa${index + 1}.png`);

function App() {
  const [currentView, setCurrentView] = useState('menu'); // 'menu' | 'lobby' | 'game'
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [gamePlayers, setGamePlayers] = useState([]);

  useEffect(() => {
    soundManager.playBgMusic();
  }, []);

  const handlePlayClick = () => {
    soundManager.playButtonClick();
    setCurrentView('lobby');
  };

  const handleSettingsClick = () => {
    soundManager.playButtonClick();
    setIsSettingsOpen(true);
  };

  const handleStartGame = (players) => {
    setGamePlayers(players);
    setCurrentView('game');
  };

  return (
    <div className="game-screen">
      {/* 1. Menú Principal */}
      {currentView === 'menu' && (
        <>
          <div className="menu-pelusas" aria-hidden="true">
            {MENU_PELUSAS.map((src, index) => (
              <div
                className="menu-pelusa-track"
                key={src}
                style={{ animationDelay: `${index * -2}s` }}
              >
                <img
                  className="menu-pelusa-image"
                  src={src}
                  alt=""
                  draggable="false"
                  style={{ animationDelay: `${index * -0.15}s` }}
                />
              </div>
            ))}
          </div>

          <div className="menu-container">
            <button 
              id="btn-play"
              type="button" 
              className="retro-btn btn-play"
              onClick={handlePlayClick}
            >
              JUGAR
            </button>

            <button 
              id="btn-settings"
              type="button" 
              className="retro-btn btn-settings"
              onClick={handleSettingsClick}
            >
              CONFIGURACIÓN
            </button>
          </div>
        </>
      )}

      {/* 2. Modal de Configuración */}
      {isSettingsOpen && (
        <SettingsModal onClose={() => setIsSettingsOpen(false)} />
      )}

      {/* 3. Vista de Configuración de Jugadores (Lobby) */}
      {currentView === 'lobby' && (
        <LobbyView 
          onBack={() => setCurrentView('menu')}
          onStartGame={handleStartGame}
        />
      )}

      {/* 4. Tablero del Juego (Mesa Cuadrada) */}
      {currentView === 'game' && (
        <GameBoardView 
          players={gamePlayers}
          onExitGame={() => setCurrentView('menu')}
          onOpenSettings={() => setIsSettingsOpen(true)}
        />
      )}
    </div>
  );
}

export default App;


