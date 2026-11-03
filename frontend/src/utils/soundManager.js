// Gestor simple de audio para el juego Pelusas

class SoundManager {
  constructor() {
    this.bgMusic = null;
    this.isMuted = false;
    this.volume = 0.4;
    this.isInitialized = false;
  }

  init() {
    if (this.isInitialized) return;
    this.bgMusic = new Audio('/audio/bg-music.weba');
    this.bgMusic.loop = true;
    this.bgMusic.volume = this.volume;
    this.isInitialized = true;
  }

  playBgMusic() {
    this.init();
    if (this.bgMusic && !this.isMuted) {
      this.bgMusic.play().catch(() => {
        // Los navegadores requieren interacción del usuario para reproducir audio
        const resumeAudio = () => {
          this.bgMusic.play();
          window.removeEventListener('click', resumeAudio);
          window.removeEventListener('keydown', resumeAudio);
        };
        window.addEventListener('click', resumeAudio, { once: true });
        window.addEventListener('keydown', resumeAudio, { once: true });
      });
    }
  }

  pauseBgMusic() {
    if (this.bgMusic) {
      this.bgMusic.pause();
    }
  }

  toggleMute() {
    this.isMuted = !this.isMuted;
    if (this.bgMusic) {
      this.bgMusic.muted = this.isMuted;
      if (!this.isMuted && this.bgMusic.paused) {
        this.bgMusic.play().catch(() => {});
      }
    }
    return this.isMuted;
  }

  setVolume(vol) {
    this.volume = Math.max(0, Math.min(1, vol));
    if (this.bgMusic) {
      this.bgMusic.volume = this.volume;
    }
  }

  playButtonClick() {
    if (this.isMuted) return;
    try {
      // const clickSound = new Audio('/audio/button-click.mp3');
      const clickSound = new Audio('/audio/pop_sound.mp3');
      clickSound.volume = Math.min(1, this.volume * 1.2);
      clickSound.play().catch(() => {});
    } catch {
      // Audio fallback
    }
  }

  playButtonStealClick(){
     if (this.isMuted) return;
    try {
      // const clickSound = new Audio('/audio/button-click.mp3');
      const clickSound = new Audio('/audio/button-click.mp3');
      clickSound.volume = Math.min(1, this.volume * 1.2);
      clickSound.play().catch(() => {});
    } catch {
      // Audio fallback
    }
  }

  playStolenCards(){
     if (this.isMuted) return;
    try {
      const clickSound = new Audio('/audio/card_stolen.mp3');
      clickSound.volume = Math.min(1, this.volume * 1.2);
      clickSound.play().catch(() => {});
    } catch {
      // Audio fallback
    }
  }
}

export const soundManager = new SoundManager();
