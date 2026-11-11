import React, { useState } from 'react';
import { KeyRound, ArrowRight, X } from 'lucide-react';
import { soundManager } from '../../utils/soundManager';
import './JoinCodeModal.css';

export function JoinCodeModal({ onClose, onJoinRoom }) {
  const [code, setCode] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e?.preventDefault();
    soundManager.playButtonClick();

    const cleanCode = code.trim().toUpperCase();
    if (!cleanCode) {
      setError('Por favor escribe el código de la sala');
      return;
    }

    if (cleanCode.length < 4) {
      setError('El código ingresado es demasiado corto');
      return;
    }

    onJoinRoom(cleanCode);
  };

  return (
    <div className="join-modal-overlay" role="dialog" aria-modal="true">
      <div className="join-modal-card pixel-box">
        <div className="join-modal-header">
          <div className="join-modal-icon-badge">
            <KeyRound size={18} />
          </div>
          <h2 className="join-modal-title">UNIRSE A SALA ONLINE</h2>
          <button 
            type="button" 
            className="pixel-close-btn"
            onClick={() => {
              soundManager.playButtonClick();
              onClose();
            }}
            title="Cerrar"
          >
            <X size={16} />
          </button>
        </div>

        <p className="join-modal-desc">
          Escribe el código que te compartió tu amigo para entrar directamente a su sala de juego.
        </p>

        <form onSubmit={handleSubmit} className="join-modal-form">
          <div className="join-input-wrap">
            <input
              type="text"
              className="pixel-input join-modal-input"
              placeholder="Ej: PELU-7926"
              maxLength={15}
              autoFocus
              value={code}
              onChange={(e) => {
                setCode(e.target.value.toUpperCase());
                setError('');
              }}
            />
          </div>

          {error && <div className="join-modal-error">{error}</div>}

          <div className="join-modal-actions">
            <button
              type="button"
              className="retro-btn btn-settings join-cancel-btn"
              onClick={() => {
                soundManager.playButtonClick();
                onClose();
              }}
            >
              CANCELAR
            </button>

            <button
              type="submit"
              id="btn-confirm-join-code"
              className="retro-btn btn-play join-submit-btn"
            >
              <ArrowRight size={14} style={{ display: 'inline', marginRight: 6 }} />
              ENTRAR A LA SALA
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

