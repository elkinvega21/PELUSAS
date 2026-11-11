import React, { useState, useEffect, useCallback } from 'react';
import { ArrowLeft, Settings, Shield, Bot, User, Clock, Share2 } from 'lucide-react';
import { soundManager } from '../../utils/soundManager';
import { CardReveal } from './CardReveal';
import { TurnTimer } from './TurnTimer';
import { GameOverModal } from './GameOverModal';
import { guardarPartidaFinalizada } from '../../services/supabaseService';
import './GameBoardView.css';

const DEFAULT_PLAYERS = [
  { id: 1, name: 'Jugador 1', isBot: false },
  { id: 2, name: 'Jugador 2 (Bot)', isBot: true },
  { id: 3, name: 'Jugador 3 (Bot)', isBot: true },
  { id: 4, name: 'Jugador 4 (Bot)', isBot: true },
];

const MINI_CARD_IMAGES = {
  1: '/cards/MiniOneCard.png',
  2: '/cards/MiniTwoCard.png',
  3: '/cards/MiniThreeCard.png',
  4: '/cards/MiniFourCard.png',
  5: '/cards/MiniFiveCard.png',
  6: '/cards/MiniSixCard.png',
  7: '/cards/MiniSevenCard.png',
  8: '/cards/MiniEightCard.png',
  9: '/cards/MiniNineCard.png',
  10: '/cards/MiniTenCard.png',
};

const CARD_IMAGES = {
  1: '/cards/OneCard.png',
  2: '/cards/TwoCard.png',
  3: '/cards/ThreeCard.png',
  4: '/cards/FourCard.png',
  5: '/cards/FiveCard.png',
  6: '/cards/SixCard.png',
  7: '/cards/SevenCard.png',
  8: '/cards/EightCard.png',
  9: '/cards/NineCard.png',
  10: '/cards/TenCard.png',
};

export function GameBoardView({ players = [], gameMode = 'local', onExitGame, onOpenSettings, onOpenLeaderboard }) {
  const [currentPlayers, setCurrentPlayers] = useState(() => (
    players && players.length >= 2 ? players : DEFAULT_PLAYERS
  ));

  const [activeTurnIdx, setActiveTurnIdx] = useState(0);
  const [deckRemaining, setDeckRemaining] = useState(110);
  const [pendingDraw, setPendingDraw] = useState(null);
  const [flyingCard, setFlyingCard] = useState(null); // { target: 'north' | 'south' | 'west' | 'east' }
  const [isGameOver, setIsGameOver] = useState(false);
  const [gameOverResults, setGameOverResults] = useState([]);
  const [isAbandonedGame, setIsAbandonedGame] = useState(false);
  const [showExitConfirm, setShowExitConfirm] = useState(false);
  const [turnNotification, setTurnNotification] = useState('');

  // Estados visuales de acción por jugador
  const [playerStatus, setPlayerStatus] = useState({});

  // Cronómetro de 10 segundos
  const [secondsLeft, setSecondsLeft] = useState(10);

  const [playersState, setPlayersState] = useState(() => {
    const initialState = {};
    const list = players && players.length >= 2 ? players : DEFAULT_PLAYERS;
    list.forEach((p) => {
      initialState[p.id] = {
        activeCards: [],
        safePoints: 0,
        savedCardsCount: 0,
        canSaveAtStart: false,
      };
    });
    return initialState;
  });

  // El jugador activo SIEMPRE ocupa el asiento principal inferior (Perspectiva Dinámica)
  const activePlayer = currentPlayers[activeTurnIdx] || currentPlayers[0];
  const isCurrentTurnBot = Boolean(activePlayer.isBot);
  const isCurrentTurnHuman = !isCurrentTurnBot;

  // Los demás jugadores se sitúan alrededor de la mesa como oponentes
  const opponents = currentPlayers.filter(p => p.id !== activePlayer.id);
  const northPlayer = opponents[0] || null;
  const westPlayer = opponents[1] || null;
  const eastPlayer = opponents[2] || null;

  // Determinar la posición visual de un jugador en la mesa
  const getPlayerPosition = useCallback((playerId) => {
    if (playerId === activePlayer.id) return 'south';
    if (northPlayer && playerId === northPlayer.id) return 'north';
    if (westPlayer && playerId === westPlayer.id) return 'west';
    if (eastPlayer && playerId === eastPlayer.id) return 'east';
    return 'north';
  }, [activePlayer.id, northPlayer, westPlayer, eastPlayer]);

  // Actualizar el estado visual de un jugador
  const updatePlayerStatus = useCallback((playerId, status) => {
    setPlayerStatus(prev => ({ ...prev, [playerId]: status }));
  }, []);

  // Alternar entre Humano y Bot para cualquier jugador
  const handleToggleBot = useCallback((playerId) => {
    soundManager.playButtonClick();
    setCurrentPlayers(prev => prev.map(p => {
      if (p.id === playerId) {
        const nextIsBot = !p.isBot;
        return { ...p, isBot: nextIsBot };
      }
      return p;
    }));
  }, []);

  // Finalizar partida (Requerimiento 5.4)
  const handleFinalizeGame = useCallback(() => {
    setIsGameOver(true);

    const finalResults = currentPlayers.map(p => {
      const pState = playersState[p.id] || { activeCards: [], safePoints: 0, savedCardsCount: 0 };
      const remainingPoints = pState.activeCards.reduce((acc, c) => acc + c.value, 0);
      const totalScore = pState.safePoints + remainingPoints;
      const totalCards = pState.savedCardsCount + pState.activeCards.length;

      return {
        id: p.id,
        name: p.name,
        isBot: p.isBot,
        score: totalScore,
        cardsCount: totalCards,
      };
    });

    finalResults.sort((a, b) => {
      if (b.score !== a.score) return b.score - a.score;
      return b.cardsCount - a.cardsCount;
    });

    setGameOverResults(finalResults);

    // Registro automático de la partida e historial en Supabase
    guardarPartidaFinalizada({
      modo: gameMode,
      ganador: finalResults[0],
      resultados: finalResults,
    });
  }, [currentPlayers, playersState, gameMode]);

  // Manejador de solicitud de salida con confirmación para partidas vs bots
  const handleRequestExit = useCallback(() => {
    soundManager.playButtonClick();
    if (gameMode === 'bots') {
      setShowExitConfirm(true);
    } else {
      onExitGame();
    }
  }, [gameMode, onExitGame]);

  // Confirmación de abandono de partida (en modo bots simula resultado donde el bot siempre gana)
  const handleConfirmExit = useCallback(() => {
    soundManager.playButtonClick();
    setShowExitConfirm(false);

    if (gameMode === 'bots') {
      const bots = currentPlayers.filter(p => p.isBot);
      const human = currentPlayers.find(p => !p.isBot);

      if (bots.length === 0) {
        onExitGame();
        return;
      }

      // Elegir un bot ganador aleatorio
      const winningBotIdx = Math.floor(Math.random() * bots.length);
      const topScore = Math.floor(38 + Math.random() * 22); // 38 a 59 pts

      const botResults = bots.map((bot, index) => {
        const score = index === winningBotIdx 
          ? topScore 
          : Math.floor(12 + Math.random() * (topScore - 14));
        return {
          id: bot.id,
          name: bot.name,
          isBot: true,
          score,
          cardsCount: Math.floor(score / 5) + 1,
        };
      });

      // El humano nunca gana: se marca con 0 puntos y abandono
      const humanResult = human ? {
        id: human.id,
        name: human.name,
        isBot: false,
        score: 0,
        cardsCount: 0,
        abandoned: true,
      } : null;

      botResults.sort((a, b) => b.score - a.score);
      const finalResults = humanResult ? [...botResults, humanResult] : botResults;

      setGameOverResults(finalResults);
      setIsAbandonedGame(true);
      setIsGameOver(true);

      // Registrar partida simulada en Supabase / Historial
      guardarPartidaFinalizada({
        modo: 'bots',
        ganador: finalResults[0],
        resultados: finalResults,
      });
    } else {
      onExitGame();
    }
  }, [gameMode, currentPlayers, onExitGame]);

  // Avanzar el turno
  const advanceTurn = useCallback(() => {
    if (deckRemaining <= 0) {
      handleFinalizeGame();
      return;
    }

    const nextIdx = (activeTurnIdx + 1) % currentPlayers.length;
    const nextPlayer = currentPlayers[nextIdx];

    setPlayersState(prev => ({
      ...prev,
      [nextPlayer.id]: {
        ...prev[nextPlayer.id],
        canSaveAtStart: (prev[nextPlayer.id]?.activeCards?.length > 0),
      }
    }));

    updatePlayerStatus(activePlayer.id, 'Plantado');
    updatePlayerStatus(nextPlayer.id, 'Su turno');
    setActiveTurnIdx(nextIdx);
    setSecondsLeft(10);

    // Aviso de cambio de turno dinámico
    setTurnNotification(`¡Turno de ${nextPlayer.name}! 🎮`);
    setTimeout(() => {
      setTurnNotification('');
    }, 2000);
  }, [deckRemaining, handleFinalizeGame, activeTurnIdx, currentPlayers, activePlayer.id, updatePlayerStatus]);

  // Paso 1: Guardar Pelusas
  const handleSavePelusas = useCallback(() => {
    soundManager.playButtonClick();
    const activeState = playersState[activePlayer.id];
    if (!activeState || !activeState.canSaveAtStart || activeState.activeCards.length === 0) return;

    const pointsGained = activeState.activeCards.reduce((acc, c) => acc + c.value, 0);
    const countGained = activeState.activeCards.length;

    updatePlayerStatus(activePlayer.id, 'Guardando pelusas...');

    setPlayersState(prev => ({
      ...prev,
      [activePlayer.id]: {
        ...prev[activePlayer.id],
        safePoints: prev[activePlayer.id].safePoints + pointsGained,
        savedCardsCount: prev[activePlayer.id].savedCardsCount + countGained,
        activeCards: [],
        canSaveAtStart: false,
      }
    }));
  }, [activePlayer.id, playersState, updatePlayerStatus]);

  // Paso 2: Sacar carta del mazo (con animación de agarre)
  const handleDrawCard = useCallback((drawingPlayer = activePlayer) => {
    if (deckRemaining <= 0 || pendingDraw) return;

    soundManager.playButtonClick();

    const targetPos = getPlayerPosition(drawingPlayer.id);
    setFlyingCard({ target: targetPos });
    updatePlayerStatus(drawingPlayer.id, 'Tomando carta...');

    const cardVal = Math.floor(Math.random() * 10) + 1;
    const drawnCard = { id: `card-${Date.now()}`, value: cardVal, playerId: drawingPlayer.id };

    if (!drawingPlayer.isBot) {
      setSecondsLeft(10);
    }

    setTimeout(() => {
      setFlyingCard(null);
      setPendingDraw(drawnCard);
      setDeckRemaining(prev => {
        const nextRemaining = prev - 1;
        if (nextRemaining <= 0) {
          setTimeout(() => handleFinalizeGame(), 1800);
        }
        return nextRemaining;
      });
    }, 450);
  }, [deckRemaining, pendingDraw, activePlayer, getPlayerPosition, updatePlayerStatus, handleFinalizeGame]);

  // Completar la revelación de la carta
  const handleDrawRevealComplete = useCallback(() => {
    if (!pendingDraw) return;

    const drawingPlayerId = pendingDraw.playerId || activePlayer.id;
    const pState = playersState[drawingPlayerId] || { activeCards: [], safePoints: 0, savedCardsCount: 0 };
    const currentActiveCards = pState.activeCards;
    const cardVal = pendingDraw.value;
    const hasDuplicate = currentActiveCards.some(c => c.value === cardVal);
    const totalAfterDraw = currentActiveCards.length + 1;

    // Regla de Pérdida por repetición con >= 3 cartas (Requerimiento 5.3)
    if (hasDuplicate && totalAfterDraw >= 3) {
      soundManager.playStolenCards();
      updatePlayerStatus(drawingPlayerId, '¡PERDIÓ EL TURNO!');

      setPlayersState(prev => ({
        ...prev,
        [drawingPlayerId]: {
          ...prev[drawingPlayerId],
          activeCards: [],
          canSaveAtStart: false,
        }
      }));
      setPendingDraw(null);

      setTimeout(() => advanceTurn(), 1500);
      return;
    }

    updatePlayerStatus(drawingPlayerId, `Sacó un ${cardVal}`);

    // Detección automática de robo a rivales del mismo valor (Requerimiento 5.2 - Paso 3, máximo 3 cartas)
    let stolen = [];
    const updated = { ...playersState };

    currentPlayers.forEach(rival => {
      if (rival.id === drawingPlayerId) return;
      const rivalCards = (updated[rival.id] && updated[rival.id].activeCards) || [];
      const matching = rivalCards.filter(c => c.value === cardVal);
      if (matching.length > 0) {
        const toSteal = matching.slice(0, 3);
        stolen.push(...toSteal);
        let removed = 0;
        const remaining = rivalCards.filter(c => {
          if (c.value === cardVal && removed < 3) {
            removed++;
            return false;
          }
          return true;
        });
        updated[rival.id] = {
          ...updated[rival.id],
          activeCards: remaining,
        };
      }
    });

    if (stolen.length > 0) {
      soundManager.playButtonStealClick();
      updatePlayerStatus(drawingPlayerId, `¡Robó ${stolen.length} carta(s) de valor ${cardVal}!`);
    }

    const currentCardsForPlayer = (updated[drawingPlayerId] && updated[drawingPlayerId].activeCards) || [];
    updated[drawingPlayerId] = {
      ...updated[drawingPlayerId],
      activeCards: [...currentCardsForPlayer, pendingDraw, ...stolen],
      canSaveAtStart: false,
    };

    setPlayersState(updated);
    setPendingDraw(null);

    const isDrawingHuman = !currentPlayers.find(p => p.id === drawingPlayerId)?.isBot;
    if (isDrawingHuman) {
      setSecondsLeft(10);
    }
  }, [pendingDraw, activePlayer.id, playersState, currentPlayers, advanceTurn, updatePlayerStatus]);

  // Botón manual de robar para el jugador humano (máximo 3 cartas por valor)
  const handleManualSteal = () => {
    soundManager.playButtonStealClick();
    const activeState = playersState[activePlayer.id] || { activeCards: [] };
    const myValues = new Set(activeState.activeCards.map(c => c.value));
    let stolen = [];
    const updated = { ...playersState };

    currentPlayers.forEach(rival => {
      if (rival.id === activePlayer.id) return;
      const rivalCards = updated[rival.id]?.activeCards || [];
      const matching = rivalCards.filter(c => myValues.has(c.value));
      if (matching.length > 0) {
        const toSteal = [];
        const valueCounts = {};
        const remaining = [];
        for (const card of rivalCards) {
          if (myValues.has(card.value)) {
            const count = valueCounts[card.value] || 0;
            if (count < 3) {
              valueCounts[card.value] = count + 1;
              toSteal.push(card);
            } else {
              remaining.push(card);
            }
          } else {
            remaining.push(card);
          }
        }
        stolen.push(...toSteal);
        updated[rival.id] = {
          ...updated[rival.id],
          activeCards: remaining,
        };
      }
    });

    if (stolen.length > 0) {
      updated[activePlayer.id] = {
        ...updated[activePlayer.id],
        activeCards: [...(updated[activePlayer.id]?.activeCards || []), ...stolen],
      };
      setPlayersState(updated);
      updatePlayerStatus(activePlayer.id, `¡Robó ${stolen.length} carta(s)!`);
    }
  };

  // Paso 3: Plantarse
  const handleStay = useCallback(() => {
    soundManager.playButtonClick();
    updatePlayerStatus(activePlayer.id, 'Se ha plantado');
    advanceTurn();
  }, [activePlayer.id, advanceTurn, updatePlayerStatus]);

  // CRONÓMETRO DE 10 SEGUNDOS (Corre durante el turno de cualquier jugador humano)
  useEffect(() => {
    if (!isCurrentTurnHuman || pendingDraw || isGameOver) return;

    const timer = setInterval(() => {
      setSecondsLeft(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          handleStay();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [isCurrentTurnHuman, pendingDraw, isGameOver, handleStay]);

  // IA DE LOS BOTS
  useEffect(() => {
    if (!isCurrentTurnBot || pendingDraw || isGameOver) return;

    let timeoutId;
    const botId = activePlayer.id;
    const bState = playersState[botId] || { activeCards: [], canSaveAtStart: false };

    // Si puede guardar al inicio de su turno, lo hace primero
    if (bState.canSaveAtStart && bState.activeCards.length > 0) {
      timeoutId = setTimeout(() => {
        handleSavePelusas();
      }, 700);
      return () => clearTimeout(timeoutId);
    }

    timeoutId = setTimeout(() => {
      const cardsInMesa = bState.activeCards.length;

      // Si no tiene cartas, saca siempre una
      if (cardsInMesa === 0) {
        handleDrawCard(activePlayer);
        return;
      }

      // Si tiene 1 o 2 cartas, saca con alta probabilidad (85%)
      if (cardsInMesa < 3) {
        if (Math.random() < 0.85) {
          handleDrawCard(activePlayer);
        } else {
          handleStay();
        }
        return;
      }

      // Si tiene 3 o más cartas, evalúa riesgo de perder turno (repetición)
      const currentValues = bState.activeCards.map(c => c.value);
      const uniqueCount = new Set(currentValues).size;
      const riskFactor = uniqueCount / 10;

      if (Math.random() > riskFactor) {
        handleDrawCard(activePlayer);
      } else {
        handleStay();
      }
    }, 850);

    return () => clearTimeout(timeoutId);
  }, [isCurrentTurnBot, activePlayer, pendingDraw, isGameOver, playersState, handleSavePelusas, handleDrawCard, handleStay]);

  // Reiniciar partida completa
  const handlePlayAgain = () => {
    setDeckRemaining(110);
    setIsGameOver(false);
    setGameOverResults([]);
    setActiveTurnIdx(0);
    setSecondsLeft(10);
    setPlayerStatus({});

    const initialState = {};
    currentPlayers.forEach(p => {
      initialState[p.id] = {
        activeCards: [],
        safePoints: 0,
        savedCardsCount: 0,
        canSaveAtStart: false,
      };
    });
    setPlayersState(initialState);
  };

  // Renderizar asiento de un rival
  const renderSeat = (player, positionClass) => {
    if (!player) return null;
    const pState = playersState[player.id] || { activeCards: [], safePoints: 0 };
    const isActive = activePlayer.id === player.id;
    const status = playerStatus[player.id] || (isActive ? 'Su turno' : 'Esperando');

    return (
      <div className={`table-seat ${positionClass} pixel-box ${isActive ? 'is-active-turn' : ''}`}>
        <div className="seat-header">
          <div className="seat-player-box">
            {player.isBot ? <Bot size={15} className="bot-icon" /> : <User size={15} className="user-icon" />}
            <span className="seat-player-name">{player.name}</span>
            {isActive && <span className="seat-turn-badge">TURNO</span>}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <button
              type="button"
              className={`seat-role-pill ${player.isBot ? 'is-bot' : 'is-human'}`}
              onClick={() => handleToggleBot(player.id)}
              title={`Haz clic para cambiar a ${player.isBot ? 'Humano' : 'Bot'}`}
            >
              {player.isBot ? 'BOT' : 'HUM'}
            </button>
            <span className="seat-safe-points">
              <Shield size={11} /> {pState.safePoints}
            </span>
          </div>
        </div>

        {/* Estado visible de la acción del rival */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span className={`seat-status-pill ${isActive ? 'active' : ''} ${status.includes('PERDIÓ') ? 'loss' : ''}`}>
            {status}
          </span>
        </div>

        {/* Cartas boca arriba delante del rival */}
        <div className="seat-cards-shelf">
          {pState.activeCards.length === 0 ? (
            <span className="seat-empty-text">Sin cartas</span>
          ) : (
            pState.activeCards.map(c => (
              <img
                key={c.id}
                src={MINI_CARD_IMAGES[c.value] || '/cards/MiniQuestionCard.png'}
                alt={`Carta ${c.value}`}
                className="seat-mini-card"
                title={`Valor: ${c.value}`}
              />
            ))
          )}
        </div>
      </div>
    );
  };

  // La bandeja inferior refleja SIEMPRE el estado del jugador en turno (South)
  const activePlayerState = playersState[activePlayer.id] || {
    activeCards: [],
    safePoints: 0,
    savedCardsCount: 0,
    canSaveAtStart: false,
  };

  return (
    <div className="square-table-screen">
      {/* Banner flotante de transición de turno */}
      {turnNotification && (
        <div className="turn-change-banner" role="status" aria-live="polite">
          <span>{turnNotification}</span>
        </div>
      )}

      {/* Barra superior con navegación */}
      <div className="square-table-nav">
        <button
          type="button"
          className="pixel-btn-icon"
          disabled={Boolean(pendingDraw) || isGameOver}
          onClick={handleRequestExit}
          title="Salir al menú"
        >
          <ArrowLeft size={18} />
        </button>

        <div className={`deck-tag-counter pixel-sub-box ${deckRemaining <= 15 ? 'low-cards' : ''}`}>
          <span>MONTON DE POLVO: {deckRemaining}/110</span>
          {gameMode === 'online' && (
            <span className="online-badge-tag">
              <Share2 size={12} /> ONLINE
            </span>
          )}
        </div>

        <button
          type="button"
          className="pixel-btn-icon"
          disabled={Boolean(pendingDraw) || isGameOver}
          onClick={onOpenSettings}
          title="Configuración de sonido"
        >
          <Settings size={18} />
        </button>
      </div>

      {/* LA MESA CUADRADA TABLETOP */}
      <div className="square-table-board">
        {/* LADO NORTE (Arriba) */}
        <div className="table-zone-north">
          {renderSeat(northPlayer, 'seat-north')}
        </div>

        {/* LADO MEDIO: OESTE - CENTRO (MAZO) - ESTE */}
        <div className="table-zone-middle">
          <div className="table-zone-west">
            {renderSeat(westPlayer, 'seat-west')}
          </div>

          {/* Centro: Mazo y Carta voladora */}
          <div
            className={`table-center-deck ${flyingCard ? 'is-drawing' : ''}`}
            onClick={() => {
              if (isCurrentTurnHuman && !pendingDraw && deckRemaining > 0) {
                handleDrawCard(activePlayer);
              }
            }}
            title={isCurrentTurnHuman ? `Haz clic para sacar carta para ${activePlayer.name}` : "Mazo de cartas"}
            aria-disabled={Boolean(pendingDraw) || deckRemaining <= 0 || !isCurrentTurnHuman}
          >
            <img src="/cards/QuestionCard.png" alt="Mazo" className="square-deck-img" />
            <span className="square-deck-label">Monton de polvo</span>
          </div>

          {/* Animación visual de carta volando del mazo hacia el jugador */}
          {flyingCard && (
            <img
              src="/cards/MiniQuestionCard.png"
              alt="Carta en movimiento"
              className={`flying-card-anim to-${flyingCard.target}`}
            />
          )}

          <div className="table-zone-east">
            {renderSeat(eastPlayer, 'seat-east')}
          </div>
        </div>

        {/* LADO SUR (Abajo): Perspectiva del Jugador Activo */}
        <div className="table-zone-south">
          <div className="south-player-box pixel-box is-my-turn">
            <div className="south-header">
              <span className="south-title">
                {activePlayer.isBot ? <Bot size={16} /> : <User size={16} />}
                ★ {activePlayer.name} {isCurrentTurnHuman ? '(¡TU TURNO!)' : '(Turno del Bot)'}
              </span>
              <span className="south-safe-badge">
                <Shield size={12} /> PUNTOS: {activePlayerState.safePoints}
              </span>
            </div>

            {/* Cronómetro de 10 segundos para el turno del jugador humano */}
            {isCurrentTurnHuman && !isGameOver && (
              <TurnTimer secondsLeft={secondsLeft} maxSeconds={10} />
            )}

            {/* Cartas descubiertas grandes del jugador en turno */}
            <div className="south-cards-rack">
              {activePlayerState.activeCards.length === 0 ? (
                <span className="south-empty-hint">
                  {isCurrentTurnHuman
                    ? 'Sin cartas en mesa. Saca una carta del mazo para comenzar tu turno.'
                    : 'Esperando que el bot elija su jugada...'}
                </span>
              ) : (
                activePlayerState.activeCards.map(c => (
                  <img
                    key={c.id}
                    src={CARD_IMAGES[c.value] || '/cards/QuestionCard.png'}
                    alt={`Carta ${c.value}`}
                    className="south-card-img"
                    title={`Valor: ${c.value}`}
                  />
                ))
              )}
            </div>

            {/* Botones de acción del jugador en turno */}
            <div className="south-actions-row">
              {isCurrentTurnHuman ? (
                <>
                  {activePlayerState.canSaveAtStart && activePlayerState.activeCards.length > 0 && (
                    <button
                      type="button"
                      className="retro-btn btn-settings board-btn-sm"
                      disabled={Boolean(pendingDraw)}
                      onClick={handleSavePelusas}
                    >
                      GUARDAR
                    </button>
                  )}

                  <button
                    type="button"
                    className="retro-btn btn-play board-btn-sm"
                    disabled={Boolean(pendingDraw) || deckRemaining <= 0}
                    onClick={() => handleDrawCard(activePlayer)}
                  >
                    SACAR CARTA
                  </button>

                  <button
                    type="button"
                    id="btn-manual-steal"
                    className="retro-btn btn-settings board-btn-sm btn-steal-static"
                    disabled={Boolean(pendingDraw)}
                    onClick={handleManualSteal}
                    title="Presiona si ves que tienes cartas del mismo valor que algún oponente"
                  >
                    ROBAR
                  </button>

                  <button
                    type="button"
                    className="retro-btn btn-settings board-btn-sm"
                    disabled={Boolean(pendingDraw)}
                    onClick={handleStay}
                  >
                    PLANTARSE
                  </button>

                  {/* Botón para convertir a Bot si no se desea jugar manualmente */}
                  <button
                    type="button"
                    className="retro-btn btn-settings board-btn-sm btn-auto-bot"
                    onClick={() => handleToggleBot(activePlayer.id)}
                    title="Convertir a este jugador en Bot para que juegue automáticamente"
                  >
                    🤖 AUTO-BOT
                  </button>
                </>
              ) : (
                <div className="south-waiting-notice">
                  <Clock size={14} />
                  <span>Turno de {activePlayer.name}: {playerStatus[activePlayer.id] || 'Pensando...'}</span>
                  <button
                    type="button"
                    className="seat-role-pill is-human"
                    style={{ marginLeft: '8px' }}
                    onClick={() => handleToggleBot(activePlayer.id)}
                    title="Tomar el control de este turno manualmente como Humano"
                  >
                    TOMAR CONTROL
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Modal de carta revelada con animación */}
      {pendingDraw && (
        <CardReveal
          card={pendingDraw}
          imageSrc={CARD_IMAGES[pendingDraw.value] || '/cards/QuestionCard.png'}
          playerName={currentPlayers.find(p => p.id === pendingDraw.playerId)?.name || activePlayer.name}
          onComplete={handleDrawRevealComplete}
        />
      )}

      {/* Modal de confirmación al salir / abandonar */}
      {showExitConfirm && (
        <div className="exit-confirm-overlay" role="dialog" aria-modal="true">
          <div className="exit-confirm-card pixel-box">
            <div className="exit-confirm-header">
              <span className="exit-confirm-icon">⚠️</span>
              <h3 className="exit-confirm-title">¿ABANDONAR PARTIDA?</h3>
            </div>
            <p className="exit-confirm-desc">
              {gameMode === 'bots'
                ? 'Estás a punto de salirte de la partida contra los bots. Si confirmas tu salida, se generará una simulación automática al azar donde uno de los bots ganará la partida y se registrará tu abandono con 0 puntos.'
                : '¿Estás seguro de que deseas salir al menú? El progreso de la partida actual se perderá.'}
            </p>
            <div className="exit-confirm-actions">
              <button
                type="button"
                className="retro-btn btn-settings exit-cancel-btn"
                onClick={() => {
                  soundManager.playButtonClick();
                  setShowExitConfirm(false);
                }}
              >
                CONTINUAR JUGANDO
              </button>
              <button
                type="button"
                className="retro-btn btn-play exit-confirm-btn"
                onClick={handleConfirmExit}
              >
                {gameMode === 'bots' ? 'SALIR Y SIMULAR' : 'SALIR AL MENÚ'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Fin de Partida y Proclamación del Ganador */}
      {isGameOver && (
        <GameOverModal
          results={gameOverResults}
          isAbandoned={isAbandonedGame}
          onPlayAgain={handlePlayAgain}
          onExitToMenu={onExitGame}
          onOpenLeaderboard={onOpenLeaderboard}
        />
      )}
    </div>
  );
}
