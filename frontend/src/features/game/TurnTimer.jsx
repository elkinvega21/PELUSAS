import React from 'react';
import { Clock } from 'lucide-react';
import './TurnTimer.css';

export function TurnTimer({ secondsLeft = 10, maxSeconds = 10 }) {
  const isWarning = secondsLeft <= 3;
  const percentage = Math.max(0, Math.min(100, (secondsLeft / maxSeconds) * 100));

  return (
    <div className={`turn-timer-container ${isWarning ? 'warning' : ''}`}>
      <div className={`turn-timer-label ${isWarning ? 'warning' : ''}`}>
        <Clock size={13} />
        <span>TIEMPO</span>
      </div>

      <div className="turn-timer-bar-track">
        <div
          className={`turn-timer-bar-fill ${isWarning ? 'warning' : ''}`}
          style={{ width: `${percentage}%` }}
        />
      </div>

      <span className={`turn-timer-seconds ${isWarning ? 'warning' : ''}`}>
        {secondsLeft}s
      </span>
    </div>
  );
}

