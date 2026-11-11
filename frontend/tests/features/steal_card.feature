# language: es

Característica: Robar carta

  Escenario: Robar cartas de mis rivales si coincide con la que acabé de sacar
    Dado que estoy en el menú principal de las cartas
    Entonces saco una carta aleatoria
    Y doy click en el botón plantarme
    Cuando saco la misma carta que tiene mi rival
    Entonces esa carta se pasa a mi mazo de cartas
