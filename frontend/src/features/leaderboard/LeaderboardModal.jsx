import React, { useState, useEffect } from 'react';
import { Trophy, History, RefreshCw, X, Crown, Award, Users } from 'lucide-react';
import { obtenerRankingMasVictorias, obtenerHistorialPartidas } from '../../services/supabaseService';
import { soundManager } from '../../utils/soundManager';
import './LeaderboardModal.css';

export function LeaderboardModal({ onClose }) {
  const [activeTab, setActiveTab] = useState('ranking'); // 'ranking' | 'history'
  const [ranking, setRanking] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  const refreshData = async () => {
    setLoading(true);
    try {
      const [rankingData, historyData] = await Promise.all([
        obtenerRankingMasVictorias(),
        obtenerHistorialPartidas(),
      ]);
      setRanking(rankingData || []);
      setHistory(historyData || []);
    } catch (e) {
      console.warn('Error al cargar datos:', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    Promise.all([
      obtenerRankingMasVictorias(),
      obtenerHistorialPartidas(),
    ]).then(([rankingData, historyData]) => {
      if (!ignore) {
        setRanking(rankingData || []);
        setHistory(historyData || []);
        setLoading(false);
      }
    }).catch(() => {
      if (!ignore) setLoading(false);
    });

    return () => {
      ignore = true;
    };
  }, []);

  const champion = ranking.length > 0 ? ranking[0] : null;

  return (
    <div className="leaderboard-overlay" role="dialog" aria-modal="true" aria-label="Ranking e Historial">
      <div className="leaderboard-card pixel-box">
        {/* Cabecera */}
        <div className="leaderboard-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Trophy size={20} color="#fbbf24" />
            <h2 className="leaderboard-title">RANKING & HISTORIAL</h2>
          </div>

          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              type="button"
              className="pixel-btn-icon"
              onClick={() => {
                soundManager.playButtonClick();
                refreshData();
              }}
              title="Actualizar datos"
            >
              <RefreshCw size={16} className={loading ? 'spin-anim' : ''} />
            </button>
            <button
              type="button"
              className="pixel-btn-icon"
              onClick={() => {
                soundManager.playButtonClick();
                onClose();
              }}
              title="Cerrar"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Tarjeta del Campeón (Persona que ha ganado más veces) */}
        {champion && (
          <div className="champion-card pixel-sub-box">
            <div className="champion-badge">
              <Crown size={22} color="#fbbf24" />
              <span>MÁS VICTORIAS EN PELUSAS</span>
            </div>
            <div className="champion-info-row">
              <span className="champion-name">★ {champion.nombre}</span>
              <span className="champion-stat">{champion.victorias} {champion.victorias === 1 ? 'VICTORIA' : 'VICTORIAS'}</span>
            </div>
            <span className="champion-sub">
              Partidas jugadas: {champion.partidas || champion.victorias} | Puntos totales: {champion.puntos_totales || 0}
            </span>
          </div>
        )}

        {/* Pestañas de Navegación */}
        <div className="leaderboard-tabs-row">
          <button
            type="button"
            className={`leaderboard-tab-btn ${activeTab === 'ranking' ? 'active' : ''}`}
            onClick={() => {
              soundManager.playButtonClick();
              setActiveTab('ranking');
            }}
          >
            <Award size={14} /> CLASIFICACIÓN
          </button>
          <button
            type="button"
            className={`leaderboard-tab-btn ${activeTab === 'history' ? 'active' : ''}`}
            onClick={() => {
              soundManager.playButtonClick();
              setActiveTab('history');
            }}
          >
            <History size={14} /> HISTORIAL DE PARTIDAS
          </button>
        </div>

        {/* Contenido */}
        <div className="leaderboard-body">
          {loading ? (
            <div className="leaderboard-loading">Cargando datos desde Supabase...</div>
          ) : activeTab === 'ranking' ? (
            ranking.length === 0 ? (
              <div className="leaderboard-empty">Aún no hay partidas registradas. ¡Juega una partida para entrar al ranking!</div>
            ) : (
              <div className="ranking-table-container">
                <table className="pixel-table">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>JUGADOR</th>
                      <th>VICTORIAS</th>
                      <th>PARTIDAS</th>
                      <th>PUNTOS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {ranking.map((row, idx) => (
                      <tr key={row.nombre + idx} className={idx === 0 ? 'gold-row' : ''}>
                        <td>
                          {idx === 0 ? '🥇' : idx === 1 ? '🥈' : idx === 2 ? '🥉' : `${idx + 1}`}
                        </td>
                        <td className="player-col">
                          <strong>{row.nombre}</strong>
                        </td>
                        <td className="highlight-cell">{row.victorias}</td>
                        <td>{row.partidas || row.victorias}</td>
                        <td>{row.puntos_totales || 0}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          ) : (
            history.length === 0 ? (
              <div className="leaderboard-empty">No hay historial de partidas recientes.</div>
            ) : (
              <div className="history-list">
                {history.map((game, i) => (
                  <div key={game.id || i} className="history-row pixel-sub-box">
                    <div className="history-header-row">
                      <span className="history-winner">🏆 Ganador: {game.ganador}</span>
                      <span className="history-date">
                        {game.fecha ? new Date(game.fecha).toLocaleDateString() : 'Reciente'}
                      </span>
                    </div>
                    <div className="history-details-row">
                      <span>Modo: {game.modo || 'Local'}</span>
                      <span>Puntos: {game.puntos_ganador || 0}</span>
                      <span>Cartas: {game.cartas_ganador || 0}</span>
                    </div>
                    {game.jugadores_detalle && game.jugadores_detalle.length > 0 && (
                      <div className="history-roster">
                        <Users size={11} />
                        <span>
                          {game.jugadores_detalle.map(j => `${j.nombre} (${j.puntos} pts)`).join(', ')}
                        </span>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )
          )}
        </div>

        {/* Pie */}
        <div className="leaderboard-footer">
          <button
            type="button"
            className="retro-btn btn-settings"
            onClick={() => {
              soundManager.playButtonClick();
              onClose();
            }}
          >
            VOLVER AL MENÚ
          </button>
        </div>
      </div>
    </div>
  );
}
