import React, { useState } from 'react';
import { Volume2, VolumeX, ArrowLeft, Sliders, Check } from 'lucide-react';
import { soundManager } from '../utils/soundManager';
import './SettingsModal.css';

export function SettingsModal({ onClose }) {
  const [isMuted, setIsMuted] = useState(soundManager.isMuted);
  const [volume, setVolume] = useState(soundManager.volume);

  const handleToggleMute = () => {
    soundManager.playButtonClick();
    const muted = soundManager.toggleMute();
    setIsMuted(muted);
  };

  const handleVolumeChange = (e) => {
    const val = parseFloat(e.target.value);
    setVolume(val);
    soundManager.setVolume(val);
  };

  return (
    <div className="settings-overlay">
      <div className="settings-card pixel-box">
        {/* Cabecera */}
        <div className="settings-header">
          <button
            type="button"
            className="pixel-btn-icon"
            onClick={() => {
              soundManager.playButtonClick();
              onClose();
            }}
            title="Cerrar configuración"
          >
            <ArrowLeft size={18} />
          </button>
          <h2 className="settings-title">CONFIGURACIÓN</h2>
        </div>

        {/* Opciones de Audio */}
        <div className="settings-body">
          {/* Silenciar / Activar Música */}
          <div className="setting-row pixel-sub-box">
            <div className="setting-info">
              <span className="setting-label">MÚSICA Y SONIDO</span>
              <span className="setting-subtext">
                {isMuted ? 'Audio silenciado' : 'Audio activado'}
              </span>
            </div>

            <button
              type="button"
              className={`retro-btn-toggle ${isMuted ? 'muted' : 'active'}`}
              onClick={handleToggleMute}
            >
              {isMuted ? <VolumeX size={18} /> : <Volume2 size={18} />}
              <span>{isMuted ? 'MUTED' : 'ON'}</span>
            </button>
          </div>

          {/* Control deslizante de Volumen */}
          <div className="setting-row pixel-sub-box">
            <div className="setting-info">
              <span className="setting-label">VOLUMEN GENERAL</span>
              <span className="setting-subtext">{Math.round(volume * 100)}%</span>
            </div>
            <input
              type="range"
              min="0"
              max="1"
              step="0.05"
              value={volume}
              disabled={isMuted}
              onChange={handleVolumeChange}
              className="pixel-range-input"
            />
          </div>
        </div>

        {/* Botón Volver */}
        <div className="settings-footer">
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
