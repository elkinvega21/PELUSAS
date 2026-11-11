# language: es

Característica: Guardar cartas

  Escenario: Guardar cartas cuando es mi turno de nuevo
    Dado que estoy en el menú principal de las cartas
    Entonces saco una carta aleatoria
    Y doy click en el botón plantarme
    Y el siguiente jugador saca una carta distinta y se planta
    Entonces es mi turno de nuevo
    Y doy click en el botón guardar
    Entonces se suma el valor de mis cartas
    Y me sube el puntaje
    Entonces veo mi puntaje
