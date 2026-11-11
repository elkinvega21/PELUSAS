# language: es

Característica: Partida vs Bots y Abandono Simulado

  Escenario: Notificación de carga con 4 bots al iniciar partida vs bots
    Dado que estoy en el menú principal
    Cuando doy click en el botón de partida vs bots
    Entonces veo la pantalla de carga de los 4 bots con sus nombres

  Escenario: Confirmación de abandono en partida vs bots y victoria garantizada para la IA
    Dado que entro a una partida contra los bots
    Cuando doy click en la flecha de salir durante la partida con bots
    Entonces veo la notificación de advertencia de abandono
    Cuando confirmo que deseo salir y simular
    Entonces veo el resultado final donde el humano no es el ganador

