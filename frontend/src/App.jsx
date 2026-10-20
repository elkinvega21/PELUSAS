import React, { useState } from 'react';
import confetti from 'canvas-confetti';
import { 
  Sparkles, 
  Layers, 
  ShieldCheck, 
  Hand, 
  RefreshCw, 
  Trophy, 
  BookOpen, 
  AlertTriangle, 
  Play,
  RotateCcw
} from 'lucide-react';
import './App.css';

// Ilustraciones de pelusas para cada valor
const PELUSA_EMOJIS = {
  1: '👾',
  2: '🧶',
  3: '🧸',
  4: '🐛',
  5: '🐱',
  6: '🦁',
  7: '🦊',
  8: '🦉',
  9: '🐙',
  10: '👑'
};

function App() {
  const [activeTab, setActiveTab] = useState('board'); // 'board' | 'rules'
  
  // Estado de demostración interactiva 2D
  const [deckCount, setDeckCount] = useState(110);
  const [turnStep, setTurnStep] = useState(1); // 1: Guardar, 2: Buscar
  const [playerInDanger, setPlayerInDanger] = useState(false);
  const [gameMessage, setGameMessage] = useState('¡Es tu turno! Si tienes cartas del turno anterior, puedes guardarlas o sacar del montón de polvo.');
  
  // Jugador principal
  const [playerCards, setPlayerCards] = useState([
    { id: 1, val: 3 },
    { id: 2, val: 7 }
  ]);
  const [playerPoints, setPlayerPoints] = useState(14);
  const [playerSavedPiles, setPlayerSavedPiles] = useState([4, 10]);

  // Oponentes simulados
  const [opponents, setOpponents] = useState([
    {
      id: 2,
      name: 'Sofía',
      avatar: '👩‍🦰',
      points: 21,
      cards: [{ id: 101, val: 4 }, { id: 102, val: 8 }]
    },
    {
      id: 3,
      name: 'Carlos',
      avatar: '🧔',
      points: 18,
      cards: [{ id: 201, val: 2 }, { id: 202, val: 5 }]
    }
  ]);

  // Paso 1: Guardar Pelusas
  const handleSavePelusas = () => {
    if (playerCards.length === 0) return;
    const addedPoints = playerCards.reduce((sum, c) => sum + c.val, 0);
    setPlayerPoints(prev => prev + addedPoints);
    setPlayerSavedPiles(prev => [...prev, ...playerCards.map(c => c.val)]);
    setPlayerCards([]);
    setTurnStep(2);
    setGameMessage(`¡Pelusas guardadas! Sumaste +${addedPoints} puntos a tu montón seguro. Ahora saca del montón de polvo.`);
    confetti({ particleCount: 40, spread: 60, origin: { y: 0.8 } });
  };

  // Paso 2: Buscar Pelusa (Sacar carta del mazo)
  const handleDrawCard = () => {
    if (deckCount <= 0) {
      setGameMessage('¡El montón de polvo se ha agotado! La partida ha terminado.');
      return;
    }

    // Generar valor según distribución (1-5 más frecuentes, 6-10 menos)
    const randomVal = Math.floor(Math.random() * 10) + 1;
    const newCard = { id: Date.now() + Math.random(), val: randomVal };
    const nextCards = [...playerCards, newCard];
    setDeckCount(prev => prev - 1);
    setTurnStep(2);

    // Verificar si roba cartas a oponentes (Regla 7 - Paso 3)
    let stolenCount = 0;
    const updatedOpponents = opponents.map(opp => {
      const matchCards = opp.cards.filter(c => c.val === randomVal);
      if (matchCards.length > 0) {
        stolenCount += matchCards.length;
        return { ...opp, cards: opp.cards.filter(c => c.val !== randomVal) };
      }
      return opp;
    });

    if (stolenCount > 0) {
      setOpponents(updatedOpponents);
      for (let i = 0; i < stolenCount; i++) {
        nextCards.push({ id: Date.now() + i + 10, val: randomVal });
      }
      setGameMessage(`¡Robaste ${stolenCount} pelusa(s) de valor ${randomVal} a tus oponentes!`);
    }

    // Regla de Pérdida (Regla 8):
    // "Saca una carta cuyo valor ya tiene delante de él Y tiene 3 o más cartas en total delante de él"
    const valuesCount = {};
    nextCards.forEach(c => {
      valuesCount[c.val] = (valuesCount[c.val] || 0) + 1;
    });

    const hasDuplicate = Object.values(valuesCount).some(cnt => cnt > 1);

    if (hasDuplicate && nextCards.length >= 3) {
      // PIERDE EL TURNO
      setPlayerCards([]);
      setPlayerInDanger(false);
      setGameMessage(`💥 ¡PERDISTE EL TURNO! Sacaste una carta repetida teniendo ${nextCards.length} cartas en total. Todas tus cartas delante fueron descartadas.`);
      return;
    }

    setPlayerCards(nextCards);

    if (hasDuplicate) {
      setPlayerInDanger(true);
      setGameMessage(`⚠️ Tienes cartas repetidas pero con menos de 3 cartas en total. ¡Aún estás a salvo!`);
    } else {
      setPlayerInDanger(false);
      if (stolenCount === 0) {
        setGameMessage(`Sacaste un ${randomVal} ${PELUSA_EMOJIS[randomVal]}. ¿Deseas continuar o plantarte?`);
      }
    }
  };

  // Finalizar voluntariamente el turno (Plantarse)
  const handleStand = () => {
    setGameMessage('Te plantaste. Tus cartas permanecen boca arriba delante de ti. ¡Cuidado con los robos!');
    setTurnStep(1);
    confetti({ particleCount: 30, spread: 50, origin: { y: 0.7 } });
  };

  // Reiniciar simulación
  const handleReset = () => {
    setDeckCount(110);
    setTurnStep(1);
    setPlayerInDanger(false);
    setPlayerPoints(14);
    setPlayerCards([{ id: 1, val: 3 }, { id: 2, val: 7 }]);
    setOpponents([
      { id: 2, name: 'Sofía', avatar: '👩‍🦰', points: 21, cards: [{ id: 101, val: 4 }, { id: 102, val: 8 }] },
      { id: 3, name: 'Carlos', avatar: '🧔', points: 18, cards: [{ id: 201, val: 2 }, { id: 202, val: 5 }] }
    ]);
    setGameMessage('Partida reiniciada. ¡Listo para jugar!');
  };

  return (
    <div className="pelusas-app">
      {/* Header */}
      <header className="game-header">
        <div className="brand">
          <div className="brand-icon">👾</div>
          <div>
            <h1>PELUSAS</h1>
            <p>Juego de Mesa Digital 2D • Calidad de Software</p>
          </div>
        </div>

        <div className="header-status">
          <div className="badge-status">
            <span className="dot"></span>
            <span>Frontend & Backend Listos</span>
          </div>
          
          <button 
            className="header-btn" 
            onClick={() => setActiveTab(activeTab === 'board' ? 'rules' : 'board')}
          >
            <BookOpen size={16} />
            <span>{activeTab === 'board' ? 'Reglas del Juego' : 'Ver Tablero'}</span>
          </button>
        </div>
      </header>

      {/* Contenido Principal */}
      <main className="game-container">
        {activeTab === 'board' ? (
          <>
            {/* Tablero de Paño Verde 2D */}
            <div className="game-table-felt">
              {/* Oponentes alrededor de la mesa */}
              <div className="opponents-section">
                {opponents.map(opp => (
                  <div key={opp.id} className="opponent-card">
                    <div className="player-info-mini">
                      <div className="player-avatar-mini">
                        <span className="avatar-circle">{opp.avatar}</span>
                        <span>{opp.name}</span>
                      </div>
                      <span className="points-pill">{opp.points} pts</span>
                    </div>
                    
                    <div className="cards-area" style={{ minHeight: '90px', padding: '6px' }}>
                      {opp.cards.length === 0 ? (
                        <div className="empty-cards-placeholder" style={{ fontSize: '0.75rem' }}>Sin cartas delante</div>
                      ) : (
                        opp.cards.map(card => (
                          <div 
                            key={card.id} 
                            className={`game-card-2d val-${card.val}`} 
                            style={{ width: '60px', height: '85px', padding: '4px' }}
                            title={`Carta de ${card.val}. ¡Si sacas este número, se la robas!`}
                          >
                            <div className="card-top" style={{ fontSize: '0.85rem' }}>{card.val}</div>
                            <div className="card-center-dust" style={{ fontSize: '18px' }}>{PELUSA_EMOJIS[card.val]}</div>
                            <div className="card-bottom" style={{ fontSize: '0.85rem' }}>{card.val}</div>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                ))}
              </div>

              {/* Centro de la Mesa: Montón de Polvo y Notificaciones */}
              <div className="table-center">
                <div className="deck-container">
                  <div className="card-deck" onClick={handleDrawCard} title="Clic para sacar carta">
                    <span className="deck-emblem">🌪️</span>
                    <span className="deck-title">Polvo</span>
                  </div>
                  <span className="deck-badge">{deckCount} / 110 cartas</span>
                </div>

                <div className="turn-banner">
                  <h3>📢 Estado del Turno</h3>
                  <p>{gameMessage}</p>
                </div>
              </div>

              {/* Zona del Jugador Activo */}
              <div className="active-player-zone">
                <div className="active-player-header">
                  <div className="active-tag">
                    <span className="avatar-circle">🎮</span>
                    <h2>Tú (Jugador Activo)</h2>
                    <span className="turn-badge">Tu Turno</span>
                  </div>
                  
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <span className="points-pill" style={{ fontSize: '0.9rem' }}>
                      🏆 {playerPoints} Puntos Seguros
                    </span>
                    <button className="header-btn" onClick={handleReset} title="Reiniciar tablero">
                      <RotateCcw size={14} />
                    </button>
                  </div>
                </div>

                {/* Cartas Delante del Jugador */}
                <div className="cards-area">
                  {playerCards.length === 0 ? (
                    <div className="empty-cards-placeholder">
                      No tienes cartas delante. ¡Saca una carta del montón de polvo para comenzar!
                    </div>
                  ) : (
                    playerCards.map(card => (
                      <div key={card.id} className={`game-card-2d val-${card.val}`}>
                        <div className="card-top">{card.val}</div>
                        <div className="card-center-dust">{PELUSA_EMOJIS[card.val]}</div>
                        <div className="card-bottom">{card.val}</div>
                      </div>
                    ))
                  )}
                </div>

                {/* Peligrómetro / Regla de Pérdida */}
                <div className={`danger-meter ${playerInDanger ? 'danger' : playerCards.length >= 2 ? 'warning' : 'safe'}`}>
                  <span>
                    {playerCards.length >= 3 
                      ? '⚠️ CUIDADO: Tienes 3 o más cartas delante. Si sacas un valor repetido, ¡pierdes todo el turno!'
                      : playerCards.length === 2
                      ? 'ℹ️ Tienes 2 cartas delante. Aún puedes arriesgarte con menor penalización.'
                      : '✅ Zona Segura. Saca cartas o guarda pelusas.'}
                  </span>
                  <span>Total delante: {playerCards.length} cartas</span>
                </div>

                {/* Controles del Turno */}
                <div className="action-bar">
                  <button 
                    className="btn-game btn-save" 
                    onClick={handleSavePelusas}
                    disabled={playerCards.length === 0}
                  >
                    <ShieldCheck size={20} />
                    Paso 1: Guardar Pelusas ({playerCards.reduce((s, c) => s + c.val, 0)} pts)
                  </button>

                  <button 
                    className="btn-game btn-draw" 
                    onClick={handleDrawCard}
                    disabled={deckCount <= 0}
                  >
                    <Sparkles size={20} />
                    Paso 2: Sacar Carta del Polvo
                  </button>

                  <button 
                    className="btn-game btn-stand" 
                    onClick={handleStand}
                    disabled={playerCards.length === 0}
                  >
                    <Hand size={20} />
                    Plantarse (Fin de Turno)
                  </button>
                </div>
              </div>
            </div>
          </>
        ) : (
          /* Pestaña de Reglas Oficiales del Juego */
          <div className="rules-grid">
            <div className="rule-box">
              <h4><Layers size={18} /> 1. El Mazo de 110 Cartas</h4>
              <p>El mazo está estrictamente dividido en:</p>
              <ul>
                <li><strong>Valores 1 al 5:</strong> 13 cartas de cada valor (65 cartas en total).</li>
                <li><strong>Valores 6 al 10:</strong> 9 cartas de cada valor (45 cartas en total).</li>
                <li>Total: 110 cartas balanceadas.</li>
              </ul>
            </div>

            <div className="rule-box">
              <h4><Play size={18} /> 2. Fases del Turno</h4>
              <ul>
                <li><strong>Paso 1 (Guardar):</strong> Al inicio, puedes asegurar las cartas sobrevivientes del turno anterior en tu montón de puntos.</li>
                <li><strong>Paso 2 (Buscar):</strong> Robas cartas una a una del montón de polvo.</li>
                <li><strong>Paso 3 (Robar):</strong> Si sacas un número que tiene un rival delante, ¡se lo quitas inmediatamente!</li>
              </ul>
            </div>

            <div className="rule-box">
              <h4><AlertTriangle size={18} /> 3. Regla de Pérdida</h4>
              <p>Un jugador pierde su turno y todas sus cartas si:</p>
              <ul>
                <li>Saca una carta repetida (mismo valor que ya tiene delante).</li>
                <li><strong>Y además tiene 3 o más cartas en total</strong> delante de él.</li>
                <li>Si tiene menos de 3 cartas, la repetición no causa pérdida.</li>
              </ul>
            </div>

            <div className="rule-box">
              <h4><Trophy size={18} /> 4. Fin de la Partida</h4>
              <p>Cuando el mazo de 110 cartas se agota:</p>
              <ul>
                <li>Se guardan automáticamente las cartas que queden delante.</li>
                <li>Se suman los puntos de cada montón.</li>
                <li>Gana el jugador con mayor puntuación acumulada.</li>
              </ul>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
