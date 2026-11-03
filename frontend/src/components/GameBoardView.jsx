import React, { useState } from 'react';
import { ArrowLeft, Settings, Shield } from 'lucide-react';
import { soundManager } from '../utils/soundManager';
import { CardReveal } from './CardReveal';
import './GameBoardView.css';

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

export function GameBoardView({ players = [], onExitGame, onOpenSettings }) {
  const activePlayers = players.length >= 2 ? players : [
    { id: 1, name: 'Jugador 1', isBot: false },
    { id: 2, name: 'Jugador 2 (Bot)', isBot: true },
    { id: 3, name: 'Jugador 3 (Bot)', isBot: true },
    { id: 4, name: 'Jugador 4 (Bot)', isBot: true },
  ];

  const [activeTurnIdx, setActiveTurnIdx] = useState(0);
  const [deckRemaining, setDeckRemaining] = useState(105);
  const [pendingDraw, setPendingDraw] = useState(null);

  const [playersState, setPlayersState] = useState(() => {
    const initialState = {};
    activePlayers.forEach((p, idx) => {
      initialState[p.id] = {
        activeCards: idx === 0 
          ? [{ id: 'c1', value: 4 }, { id: 'c2', value: 7 }] 
          : [{ id: `c-${idx}-1`, value: ((idx * 2) % 10) + 1 }, { id: `c-${idx}-2`, value: ((idx * 3) % 10) + 1 }],
        safePoints: 0,
        canSaveAtStart: idx === 0,
      };
    });
    return initialState;
  });

  const activePlayer = activePlayers[activeTurnIdx];
  const activeState = playersState[activePlayer.id] || { activeCards: [], safePoints: 0, canSaveAtStart: false };

  // Organizar asientos en mesa cuadrada:
  // Asiento Sur: Jugador Activo (abajo)
  // Asiento Norte: Oponente de enfrente (arriba)
  // Asiento Oeste: Oponente izquierda
  // Asiento Este: Oponente derecha
  const rivalPlayers = activePlayers.filter((_, idx) => idx !== activeTurnIdx);
  const northPlayer = rivalPlayers[0] || null;
  const westPlayer = rivalPlayers[1] || null;
  const eastPlayer = rivalPlayers[2] || null;

  // Paso 1: Guardar Pelusas
  const handleSavePelusas = () => {
    soundManager.playButtonClick();
    if (!activeState.canSaveAtStart || activeState.activeCards.length === 0) return;

    const pointsGained = activeState.activeCards.reduce((acc, c) => acc + c.value, 0);

    setPlayersState(prev => ({
      ...prev,
      [activePlayer.id]: {
        ...prev[activePlayer.id],
        safePoints: prev[activePlayer.id].safePoints + pointsGained,
        activeCards: [],
        canSaveAtStart: false,
      }
    }));
  };

  // Paso 2: Sacar carta del mazo
  const handleDrawCard = () => {
    soundManager.playButtonClick();
    if (deckRemaining <= 0 || pendingDraw) return;

    const cardVal = Math.floor(Math.random() * 10) + 1;
    const drawnCard = { id: `card-${Date.now()}`, value: cardVal };
    setPendingDraw(drawnCard);
    setDeckRemaining(prev => prev - 1);
  };

  const handleDrawRevealComplete = () => {
    if (!pendingDraw) return;

    const currentActiveCards = activeState.activeCards;
    const cardVal = pendingDraw.value;
    const hasDuplicate = currentActiveCards.some(c => c.value === cardVal);
    const totalAfterDraw = currentActiveCards.length + 1;

    // Pérdida por repetición con >= 3 cartas
    if (hasDuplicate && totalAfterDraw >= 3) {
      soundManager.playStolenCards();
      setPlayersState(prev => ({
        ...prev,
        [activePlayer.id]: {
          ...prev[activePlayer.id],
          activeCards: [],
          canSaveAtStart: false,
        }
      }));
      setPendingDraw(null);
      setTimeout(() => advanceTurn(), 1600);
      return;
    }

    setPlayersState(prev => ({
      ...prev,
      [activePlayer.id]: {
        ...prev[activePlayer.id],
        activeCards: [...prev[activePlayer.id].activeCards, pendingDraw],
        canSaveAtStart: false,
      }
    }));
    setPendingDraw(null);
  };

  // BOTÓN ROBAR MANUAL:
  // Color estático, sin avisar al jugador. Si presiona ROBAR, el sistema revisa si tiene valores coincidentes con rivales.
  const handleManualSteal = () => {
    soundManager.playButtonStealClick(); 
    const myValues = new Set(activeState.activeCards.map(c => c.value));
    let stolen = [];
    const updated = { ...playersState };

    rivalPlayers.forEach(rival => {
      if (!rival) return;
      const rivalCards = updated[rival.id].activeCards;
      const matching = rivalCards.filter(c => myValues.has(c.value));
      if (matching.length > 0) {
        stolen.push(...matching);
        updated[rival.id] = {
          ...updated[rival.id],
          activeCards: rivalCards.filter(c => !myValues.has(c.value)),
        };
      }
    });

    if (stolen.length > 0) {
      updated[activePlayer.id] = {
        ...updated[activePlayer.id],
        activeCards: [...updated[activePlayer.id].activeCards, ...stolen],
      };
      setPlayersState(updated);
    }
  };

  // Paso 3: Plantarse
  const handleStay = () => {
    soundManager.playButtonClick();
    advanceTurn();
  };

  const advanceTurn = () => {
    const nextIdx = (activeTurnIdx + 1) % activePlayers.length;
    const nextPlayer = activePlayers[nextIdx];

    setPlayersState(prev => ({
      ...prev,
      [nextPlayer.id]: {
        ...prev[nextPlayer.id],
        canSaveAtStart: (prev[nextPlayer.id].activeCards.length > 0),
      }
    }));

    setActiveTurnIdx(nextIdx);
  };

  const renderSeat = (player, positionClass) => {
    if (!player) return null;
    const pState = playersState[player.id] || { activeCards: [], safePoints: 0 };

    return (
      <div className={`table-seat ${positionClass} pixel-box`}>
        <div className="seat-header">
          <span className="seat-player-name">{player.name}</span>
          <span className="seat-safe-points">
            <Shield size={11} /> {pState.safePoints}
          </span>
        </div>

        {/* Cartas boca arriba delante del jugador (visibles para que el rival las examine) */}
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
              />
            ))
          )}
        </div>
      </div>
    );
  };

  return (
    <div className="square-table-screen">
      {/* Barra superior con navegación */}
      <div className="square-table-nav">
        <button
          type="button"
          className="pixel-btn-icon"
          disabled={Boolean(pendingDraw)}
          onClick={() => {
            soundManager.playButtonClick();
            onExitGame();
          }}
          title="Salir al menú"
        >
          <ArrowLeft size={18} />
        </button>

        <div className="deck-tag-counter pixel-sub-box">
          <span>MONTON DE POLVO: {deckRemaining}/110</span>
        </div>

        <button
          type="button"
          className="pixel-btn-icon"
          disabled={Boolean(pendingDraw)}
          onClick={onOpenSettings}
          title="Configuración de sonido"
        >
          <Settings size={18} />
        </button>
      </div>

      {/* LA MESA CUADRADA */}
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

          {/* Centro: Mazo */}
          <div
            className="table-center-deck"
            onClick={handleDrawCard}
            title="Montón de polvo"
            aria-disabled={Boolean(pendingDraw) || deckRemaining <= 0}
          >
            <img src="/cards/QuestionCard.png" alt="Mazo" className="square-deck-img" />
            <span className="square-deck-label">Monton de polvo</span>
          </div>

          <div className="table-zone-east">
            {renderSeat(eastPlayer, 'seat-east')}
          </div>
        </div>

        {/* LADO SUR (Abajo): Jugador Activo */}
        <div className="table-zone-south">
          <div className="south-player-box pixel-box">
            <div className="south-header">
              <span className="south-title">★ {activePlayer.name} (TU TURNO)</span>
              <span className="south-safe-badge">
                <Shield size={12} /> PUNTOS: {activeState.safePoints}
              </span>
            </div>

            {/* Cartas descubiertas delante del jugador activo */}
            <div className="south-cards-rack">
              {activeState.activeCards.length === 0 ? (
                <span className="south-empty-hint">Sin cartas en mesa. Roba del mazo para comenzar.</span>
              ) : (
                activeState.activeCards.map(c => (
                  <img
                    key={c.id}
                    src={CARD_IMAGES[c.value] || '/cards/QuestionCard.png'}
                    alt={`Carta ${c.value}`}
                    className="south-card-img"
                  />
                ))
              )}
            </div>

            {/* Botones de acción: Paso 1, Sacar Carta, Robar (Estático manual) y Plantarse */}
            <div className="south-actions-row">
              {activeState.canSaveAtStart && activeState.activeCards.length > 0 && (
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
                onClick={handleDrawCard}
              >
                SACAR CARTA
              </button>

              {/* Botón estático de ROBAR manual */}
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
            </div>
          </div>
        </div>
      </div>
      {pendingDraw && (
        <CardReveal
          card={pendingDraw}
          imageSrc={CARD_IMAGES[pendingDraw.value] || '/cards/QuestionCard.png'}
          playerName={activePlayer.name}
          onComplete={handleDrawRevealComplete}
        />
      )}
    </div>
  );
}
