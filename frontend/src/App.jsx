import { useEffect, useState, useCallback } from 'react';
import { soundManager } from './utils/soundManager';
import { MenuView } from './features/menu';
import { LobbyView, BotLoadingModal, JoinCodeModal } from './features/lobby';
import { GameBoardView } from './features/game';
import { SettingsModal } from './features/settings';
import { LeaderboardModal } from './features/leaderboard';
import { registrarIngresoJugador } from './services/supabaseService';
import './App.css';

function getUrlRoomCode() {
  if (typeof window !== 'undefined') {
    const params = new URLSearchParams(window.location.search);
    return params.get('room') || params.get('code') || null;
  }
  return null;
}

function App() {
  const initialUrlCode = getUrlRoomCode();

  // Si se entra por link (ej: ?room=PELU-7926) o por código, llevar directo a la sala online
  const [currentView, setCurrentView] = useState(() => (initialUrlCode ? 'lobby' : 'menu'));
  const [gameMode, setGameMode] = useState(() => (initialUrlCode ? 'online' : 'local'));
  const [activeRoomCode, setActiveRoomCode] = useState(initialUrlCode);

  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isLeaderboardOpen, setIsLeaderboardOpen] = useState(false);
  const [isBotLoadingOpen, setIsBotLoadingOpen] = useState(false);
  const [isJoinCodeModalOpen, setIsJoinCodeModalOpen] = useState(false);
  const [gamePlayers, setGamePlayers] = useState([]);

  useEffect(() => {
    soundManager.playBgMusic();
  }, []);

  // Escuchar cambios en la URL (por ejemplo si el usuario pega o cambia el link en la barra de direcciones)
  useEffect(() => {
    const handlePopState = () => {
      const code = getUrlRoomCode();
      if (code) {
        setActiveRoomCode(code);
        setGameMode('online');
        setCurrentView('lobby');
      }
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const handlePlayClick = (mode = 'local') => {
    soundManager.playButtonClick();
    setGameMode(mode);
    if (mode === 'bots') {
      setIsBotLoadingOpen(true);
    } else {
      setCurrentView('lobby');
    }
  };

  const handleStartBotsGame = (roster) => {
    setIsBotLoadingOpen(false);
    setGamePlayers(roster);
    setGameMode('bots');
    registrarIngresoJugador(roster[0]?.name || 'Tú');
    setCurrentView('game');
  };

  const handleOpenJoinCode = () => {
    soundManager.playButtonClick();
    setIsJoinCodeModalOpen(true);
  };

  // Cuando el usuario ingresa un código de sala para unirse
  const handleJoinWithCode = (code) => {
    soundManager.playButtonClick();
    setIsJoinCodeModalOpen(false);
    setActiveRoomCode(code);
    setGameMode('online');
    setCurrentView('lobby');

    if (typeof window !== 'undefined' && window.history) {
      const newUrl = `${window.location.pathname}?room=${code}`;
      window.history.pushState({ path: newUrl }, '', newUrl);
    }
  };

  const handleBackToMenu = useCallback(() => {
    soundManager.playButtonClick();
    setActiveRoomCode(null);
    if (typeof window !== 'undefined' && window.history) {
      window.history.replaceState({}, '', window.location.pathname);
    }
    setCurrentView('menu');
  }, []);

  const handleSettingsClick = () => {
    soundManager.playButtonClick();
    setIsSettingsOpen(true);
  };

  const handleOpenLeaderboard = () => {
    soundManager.playButtonClick();
    setIsLeaderboardOpen(true);
  };

  const handleStartGame = (players, mode = gameMode) => {
    setGamePlayers(players);
    setGameMode(mode);
    // Registro de las personas que entran a jugar en Supabase
    players.forEach(p => {
      if (!p.isBot) {
        registrarIngresoJugador(p.name);
      }
    });
    setCurrentView('game');
  };

  return (
    <div className="game-screen">
      {/* 1. Menú Principal */}
      {currentView === 'menu' && (
        <MenuView 
          onPlay={handlePlayClick} 
          onOpenSettings={handleSettingsClick}
          onOpenLeaderboard={handleOpenLeaderboard}
          onOpenJoinCode={handleOpenJoinCode}
        />
      )}

      {/* 2. Modal de Configuración */}
      {isSettingsOpen && (
        <SettingsModal onClose={() => setIsSettingsOpen(false)} />
      )}

      {/* 3. Modal de Ranking & Historial (Supabase) */}
      {isLeaderboardOpen && (
        <LeaderboardModal onClose={() => setIsLeaderboardOpen(false)} />
      )}

      {/* 3.1 Modal de Carga para Partida vs Bots */}
      {isBotLoadingOpen && (
        <BotLoadingModal 
          playerName="Tú"
          onCancel={() => setIsBotLoadingOpen(false)}
          onReady={handleStartBotsGame}
        />
      )}

      {/* 3.2 Modal para Unirse con Código desde el Menú */}
      {isJoinCodeModalOpen && (
        <JoinCodeModal
          onClose={() => setIsJoinCodeModalOpen(false)}
          onJoinRoom={handleJoinWithCode}
        />
      )}

      {/* 4. Vista de Configuración de Jugadores (Lobby / Sala) */}
      {currentView === 'lobby' && (
        <LobbyView 
          initialMode={gameMode}
          initialRoomCode={activeRoomCode}
          onBack={handleBackToMenu}
          onStartGame={handleStartGame}
        />
      )}

      {/* 5. Tablero del Juego (Mesa Cuadrada Tabletop) */}
      {currentView === 'game' && (
        <GameBoardView 
          players={gamePlayers}
          gameMode={gameMode}
          onExitGame={handleBackToMenu}
          onOpenSettings={() => setIsSettingsOpen(true)}
          onOpenLeaderboard={handleOpenLeaderboard}
        />
      )}
    </div>
  );
}

export default App;
