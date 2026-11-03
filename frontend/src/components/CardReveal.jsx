import { useEffect } from 'react';
import './CardReveal.css';

const REVEAL_DURATION_MS = 1000;

export function CardReveal({ card, imageSrc, playerName, onComplete }) {
  useEffect(() => {
    const timeoutId = window.setTimeout(onComplete, REVEAL_DURATION_MS);
    return () => window.clearTimeout(timeoutId);
  }, [onComplete]);

  return (
    <div className="card-reveal-overlay" role="dialog" aria-modal="true" aria-label="Carta revelada">
      <div className="card-reveal-content">
        <p className="card-reveal-title">¡Carta revelada!</p>
        <img
          className="card-reveal-image"
          src={imageSrc}
          alt={`Carta ${card.value}`}
        />
        <p className="card-reveal-caption">Añadiendo a la mano de {playerName}...</p>
      </div>
    </div>
  );
}
