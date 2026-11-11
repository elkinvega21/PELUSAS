import React, { useState } from 'react';
import { 
  ArrowLeft, Plus, Trash2, Bot, User, Copy, Check, Users, Globe, 
  QrCode, Link2, Edit2, KeyRound
} from 'lucide-react';
import { soundManager } from '../../utils/soundManager';
import './LobbyView.css';

export function LobbyView({ initialMode = 'local', initialRoomCode = null, onBack, onStartGame }) {
  const [activeTab, setActiveTab] = useState(initialMode === 'online' ? 'online' : 'local');

  // Jugadores del modo Local (Pasa y Juega)
  const [localPlayers, setLocalPlayers] = useState([
    { id: 1, name: 'Jugador 1', isBot: false },
    { id: 2, name: 'Jugador 2', isBot: false },
  ]);

  // Estado para el modo Online (Salas con código)
  const [roomCode, setRoomCode] = useState(() => initialRoomCode || `PELU-${Math.floor(1000 + Math.random() * 9000)}`);
  const [isGuest, setIsGuest] = useState(Boolean(initialRoomCode));
  const [copiedCode, setCopiedCode] = useState(false);
  const [copiedLink, setCopiedLink] = useState(false);

  // Modo de acceso a la sala: 'all' (QR + link + código) o 'code_only' (solo por código)
  const [accessMode, setAccessMode] = useState('all'); 
  const [joinCodeInput, setJoinCodeInput] = useState('');
  const [joinStatusMsg, setJoinStatusMsg] = useState(initialRoomCode ? `¡Conectado exitosamente a la sala ${initialRoomCode}!` : '');

  // ID del jugador cuyo nombre se está editando (null si ninguno)
  const [editingPlayerId, setEditingPlayerId] = useState(null);

  // Amigos conectados a la sala online
  const [onlinePlayers, setOnlinePlayers] = useState(() => {
    if (initialRoomCode) {
      return [
        { id: 1, name: 'Anfitrión (Host)', isHost: true, isBot: false, isReady: true },
        { id: 2, name: 'Tú', isHost: false, isBot: false, isReady: true },
      ];
    }
    return [
      { id: 1, name: 'Tú (Host)', isHost: true, isBot: false, isReady: true },
      { id: 2, name: 'Amigo 1', isHost: false, isBot: false, isReady: true },
      { id: 3, name: 'Amigo 2', isHost: false, isBot: false, isReady: false },
    ];
  });

  const [newPlayerName, setNewPlayerName] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const roomUrl = `${typeof window !== 'undefined' ? window.location.origin : 'https://pelusas.online'}/?room=${roomCode}`;
  const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=140x140&data=${encodeURIComponent(roomUrl)}&color=000000&bgcolor=ffffff`;

  const handleToggleRole = (id) => {
    soundManager.playButtonClick();
    setLocalPlayers(prev => prev.map(p => {
      if (p.id === id) {
        return { ...p, isBot: !p.isBot };
      }
      return p;
    }));
  };

  const handleAddPlayer = (asBot = false) => {
    soundManager.playButtonClick();
    if (localPlayers.length >= 5) {
      setErrorMsg('Máximo 5 jugadores permitidos');
      return;
    }
    const defaultName = asBot ? `Bot ${localPlayers.length + 1}` : (newPlayerName.trim() || `Jugador ${localPlayers.length + 1}`);
    const newPlayer = {
      id: Date.now(),
      name: defaultName,
      isBot: asBot,
    };
    setLocalPlayers([...localPlayers, newPlayer]);
    setNewPlayerName('');
    setErrorMsg('');
  };

  const handleRemovePlayer = (id) => {
    soundManager.playButtonClick();
    if (localPlayers.length <= 2) {
      setErrorMsg('Se requieren mínimo 2 jugadores');
      return;
    }
    setLocalPlayers(localPlayers.filter(p => p.id !== id));
    setErrorMsg('');
  };

  const handleCopyRoomCode = () => {
    soundManager.playButtonClick();
    navigator.clipboard?.writeText(roomCode);
    setCopiedCode(true);
    setTimeout(() => setCopiedCode(false), 2000);
  };

  const handleCopyRoomLink = () => {
    soundManager.playButtonClick();
    navigator.clipboard?.writeText(roomUrl);
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 2000);
  };

  const handleAddOnlineFriend = () => {
    soundManager.playButtonClick();
    if (onlinePlayers.length >= 5) {
      setErrorMsg('Máximo 5 jugadores en la sala');
      return;
    }
    const friendName = `Amigo ${onlinePlayers.length}`;
    setOnlinePlayers([...onlinePlayers, { id: Date.now(), name: friendName, isHost: false, isBot: false, isReady: true }]);
  };

  const handleRemoveOnlinePlayer = (id) => {
    soundManager.playButtonClick();
    if (onlinePlayers.length <= 2) {
      setErrorMsg('Se requieren al menos 2 jugadores en la sala');
      return;
    }
    setOnlinePlayers(onlinePlayers.filter(p => p.id !== id));
    setErrorMsg('');
  };

  const handleUpdateOnlineName = (id, newName) => {
    setOnlinePlayers(prev => prev.map(p => p.id === id ? { ...p, name: newName } : p));
  };

  const handleJoinExistingRoom = (codeToJoin = joinCodeInput) => {
    soundManager.playButtonClick();
    const cleanCode = (codeToJoin || '').trim().toUpperCase();
    if (!cleanCode) {
      setErrorMsg('Ingresa un código de sala válido');
      return;
    }
    setRoomCode(cleanCode);
    setIsGuest(true);
    setJoinStatusMsg(`¡Conectado exitosamente a la sala ${cleanCode}!`);
    setOnlinePlayers([
      { id: 1, name: 'Anfitrión (Host)', isHost: true, isBot: false, isReady: true },
      { id: 2, name: 'Tú', isHost: false, isBot: false, isReady: true },
    ]);
    setErrorMsg('');

    if (typeof window !== 'undefined' && window.history) {
      const newUrl = `${window.location.pathname}?room=${cleanCode}`;
      window.history.replaceState({ path: newUrl }, '', newUrl);
    }
  };

  const handleStart = async () => {
    soundManager.playButtonClick();
    if (activeTab === 'local') {
      if (localPlayers.length < 2) {
        setErrorMsg('Se requieren al menos 2 jugadores');
        return;
      }
      onStartGame(localPlayers, 'local');
    } else {
      // Modo Online
      if (onlinePlayers.length < 2) {
        setErrorMsg('Se requieren al menos 2 amigos en la sala');
        return;
      }

      // Conexión y persistencia opcional con backend REST
      try {
        await fetch('http://localhost:8080/api/partidas', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ jugadores: onlinePlayers.map(p => p.name) }),
        });
      } catch (e) {
        console.warn('Backend REST sync no disponible, continuando en frontend:', e);
      }

      onStartGame(onlinePlayers, 'online');
    }
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

        {/* Pestañas de Selección de Modo */}
        <div className="lobby-tabs-row">
          <button
            type="button"
            className={`lobby-tab-btn ${activeTab === 'local' ? 'active' : ''}`}
            onClick={() => {
              soundManager.playButtonClick();
              setActiveTab('local');
              setErrorMsg('');
            }}
          >
            <Users size={14} /> PASA Y JUEGA (LOCAL)
          </button>

          <button
            type="button"
            className={`lobby-tab-btn ${activeTab === 'online' ? 'active' : ''}`}
            onClick={() => {
              soundManager.playButtonClick();
              setActiveTab('online');
              setErrorMsg('');
            }}
          >
            <Globe size={14} /> ONLINE CON AMIGOS
          </button>
        </div>

        {errorMsg && <div className="lobby-alert">{errorMsg}</div>}

        {/* CONTENIDO PESTAÑA 1: LOCAL (PASA Y JUEGA) */}
        {activeTab === 'local' && (
          <>
            <p className="lobby-subtitle">
              🎮 Juega con amigos en este dispositivo. La pantalla rotará dinámicamente al jugador en turno con sus cartas grandes y 10s de cronómetro.
            </p>

            {/* Lista de Jugadores Locales */}
            <div className="players-list">
              {localPlayers.map((player, index) => (
                <div key={player.id} className="player-row pixel-sub-box">
                  <div className="player-info">
                    <span className="player-badge">#{index + 1}</span>
                    {player.isBot ? (
                      <Bot size={18} className="player-icon bot-icon" />
                    ) : (
                      <User size={18} className="player-icon user-icon" />
                    )}
                    <span className="player-name">{player.name}</span>
                  </div>

                  <div className="player-actions">
                    <button
                      type="button"
                      className={`pixel-role-toggle ${player.isBot ? 'is-bot' : 'is-human'}`}
                      onClick={() => handleToggleRole(player.id)}
                      title="Haz clic para alternar entre Humano y Bot"
                    >
                      {player.isBot ? '🤖 BOT' : '👤 HUMANO'}
                    </button>

                    {localPlayers.length > 2 && (
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
                </div>
              ))}
            </div>

            {/* Añadir jugadores */}
            {localPlayers.length < 5 && (
              <div className="add-player-section">
                <input
                  type="text"
                  className="pixel-input"
                  placeholder="Nombre de amigo o jugador..."
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
                    <Bot size={16} /> BOT (IA)
                  </button>
                </div>
              </div>
            )}
          </>
        )}

        {/* CONTENIDO PESTAÑA 2: ONLINE (SALA CON QR, LINK, CÓDIGO Y GESTIÓN HOST) */}
        {activeTab === 'online' && (
          <div className="online-room-section">
            {/* Selector de modo de acceso */}
            <div className="online-access-toggle-box">
              <span className="online-access-label">CONFIGURACIÓN DE ACCESO:</span>
              <div className="online-toggle-btns">
                <button
                  type="button"
                  className={`access-toggle-btn ${accessMode === 'all' ? 'active' : ''}`}
                  onClick={() => {
                    soundManager.playButtonClick();
                    setAccessMode('all');
                  }}
                >
                  <QrCode size={13} /> QR + LINK + CÓDIGO
                </button>
                <button
                  type="button"
                  className={`access-toggle-btn ${accessMode === 'code_only' ? 'active' : ''}`}
                  onClick={() => {
                    soundManager.playButtonClick();
                    setAccessMode('code_only');
                  }}
                >
                  <KeyRound size={13} /> SÓLO POR CÓDIGO
                </button>
              </div>
            </div>

            {/* 1. MODO QR + LINK + CÓDIGO */}
            {accessMode === 'all' && (
              <div className="online-share-container pixel-sub-box">
                {/* QR Code */}
                <div className="online-qr-box">
                  <img 
                    src={qrUrl} 
                    alt="Código QR de la sala" 
                    className="online-qr-img"
                  />
                  <span className="online-qr-hint">ESCANEA PARA UNIRTE</span>
                </div>

                {/* Código y Enlace directo */}
                <div className="online-share-details">
                  {/* Código */}
                  <div className="online-code-block">
                    <span className="online-sublabel">CÓDIGO DE SALA:</span>
                    <div className="room-code-display">
                      <span className="code-text">{roomCode}</span>
                      <button
                        type="button"
                        className="pixel-copy-btn"
                        onClick={handleCopyRoomCode}
                        title="Copiar código de sala"
                      >
                        {copiedCode ? <Check size={14} color="#4ade80" /> : <Copy size={14} />}
                        <span>{copiedCode ? '¡LISTO!' : 'CÓDIGO'}</span>
                      </button>
                    </div>
                  </div>

                  {/* Enlace Directo */}
                  <div className="online-link-block">
                    <span className="online-sublabel">ENLACE DIRECTO:</span>
                    <div className="room-link-display">
                      <input 
                        type="text" 
                        readOnly 
                        value={roomUrl} 
                        className="room-link-input"
                      />
                      <button
                        type="button"
                        className="pixel-copy-btn link-copy-btn"
                        onClick={handleCopyRoomLink}
                        title="Copiar link directo"
                      >
                        {copiedLink ? <Check size={14} color="#4ade80" /> : <Link2 size={14} />}
                        <span>{copiedLink ? '¡COPIADO!' : 'ENLACE'}</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* 2. MODO SÓLO POR CÓDIGO */}
            {accessMode === 'code_only' && (
              <div className="code-only-panel pixel-sub-box">
                <div className="code-only-header">
                  <KeyRound size={16} className="code-only-icon" />
                  <div>
                    <span className="code-only-title">SALA PRIVADA POR CÓDIGO</span>
                    <p className="code-only-desc">Tus amigos sólo pueden ingresar introduciendo el código.</p>
                  </div>
                </div>

                <div className="room-code-display code-only-display">
                  <span className="code-text code-highlight">{roomCode}</span>
                  <button
                    type="button"
                    className="pixel-copy-btn"
                    onClick={handleCopyRoomCode}
                  >
                    {copiedCode ? <Check size={14} color="#4ade80" /> : <Copy size={14} />}
                    <span>{copiedCode ? '¡COPIADO!' : 'COPIAR CÓDIGO'}</span>
                  </button>
                </div>

                {/* Lugar para unirse a otra sala por código */}
                <div className="join-by-code-box">
                  <span className="online-sublabel">¿UNIRTE A OTRA SALA CON CÓDIGO?</span>
                  <div className="join-code-row">
                    <input 
                      type="text"
                      className="pixel-input join-code-input"
                      placeholder="Ej: PELU-5491"
                      maxLength={12}
                      value={joinCodeInput}
                      onChange={(e) => setJoinCodeInput(e.target.value)}
                    />
                    <button 
                      type="button"
                      className="pixel-action-btn btn-join-code"
                      onClick={handleJoinExistingRoom}
                    >
                      UNIRME
                    </button>
                  </div>
                  {joinStatusMsg && <div className="join-status-alert">{joinStatusMsg}</div>}
                </div>
              </div>
            )}

            {/* Banner de invitado si entró por enlace o código */}
            {isGuest && (
              <div className="guest-joined-banner pixel-sub-box">
                <span className="guest-joined-text">
                  🎉 ¡Te has unido a la sala online: <strong>{roomCode}</strong>!
                </span>
              </div>
            )}

            {/* 3. LISTA DE JUGADORES Y EDICIÓN POR PARTE DEL HOST */}
            <div className="online-players-header">
              <span>
                JUGADORES EN SALA ({onlinePlayers.length}/5) - {isGuest ? 'CONECTADO COMO INVITADO 👤' : 'ERES HOST 👑'}
              </span>
              {onlinePlayers.length < 5 && (
                <button
                  type="button"
                  className="pixel-btn-add-friend"
                  onClick={handleAddOnlineFriend}
                  title="Añadir slot de amigo"
                >
                  <Plus size={12} /> + AMIGO
                </button>
              )}
            </div>

            <div className="players-list online-list">
              {onlinePlayers.map((p, idx) => {
                const isEditing = editingPlayerId === p.id;
                return (
                  <div key={p.id} className="player-row pixel-sub-box online-player-item">
                    <div className="player-info" style={{ flex: 1 }}>
                      <span className="player-badge">#{idx + 1}</span>
                      <User size={18} className="player-icon user-icon" />

                      {isEditing ? (
                        <input 
                          type="text"
                          className="pixel-input edit-player-input"
                          value={p.name}
                          maxLength={15}
                          autoFocus
                          onChange={(e) => handleUpdateOnlineName(p.id, e.target.value)}
                          onBlur={() => setEditingPlayerId(null)}
                          onKeyDown={(e) => {
                            if (e.key === 'Enter') setEditingPlayerId(null);
                          }}
                        />
                      ) : (
                        <div className="player-name-wrapper">
                          <span className="player-name">{p.name}</span>
                          {p.isHost && <span className="host-tag">HOST</span>}
                        </div>
                      )}
                    </div>

                    <div className="online-actions-wrapper">
                      {/* Botón para editar nombre */}
                      <button
                        type="button"
                        className="pixel-btn-edit-name"
                        onClick={() => {
                          soundManager.playButtonClick();
                          setEditingPlayerId(isEditing ? null : p.id);
                        }}
                        title={isEditing ? "Guardar nombre" : "Editar nombre de este jugador"}
                      >
                        {isEditing ? <Check size={14} color="#4ade80" /> : <Edit2 size={13} />}
                      </button>

                      {/* Estado */}
                      <span className={`online-status-badge ${p.isReady ? 'ready' : 'waiting'}`}>
                        {p.isReady ? '🟢 LISTO' : '🟡 CONECTANDO...'}
                      </span>

                      {/* Eliminar si no es el host */}
                      {!p.isHost && onlinePlayers.length > 2 && (
                        <button
                          type="button"
                          className="pixel-btn-remove"
                          onClick={() => handleRemoveOnlinePlayer(p.id)}
                          title="Expulsar jugador"
                        >
                          <Trash2 size={14} />
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
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
            {activeTab === 'online' ? 'INICIAR PARTIDA ONLINE' : 'COMENZAR PARTIDA'}
          </button>
        </div>
      </div>
    </div>
  );
}
