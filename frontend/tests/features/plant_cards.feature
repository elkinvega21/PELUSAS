# language: es

Característica: Plantarme

  Escenario: Plantarme para no tomar mas cartas y pasar de turno
    Dado que estoy en el menú principal de las cartas
    Entonces saco una carta aleatoria
    Y doy click en el botón plantarme
    Entonces cambia de turno al siguiente jugador