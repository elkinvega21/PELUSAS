# language: es

Característica: Robar carta

  Escenario: Robar cartas de mis rivales si coincide con la que acabé de sacar
    Dado que estoy en el menú principal de las cartas
    Entonces saco una carta aleatoria
    Y esa carta aleatoria coincide con una carta de un rival que sacó anteriormente
    Entonces doy click en el botón Robar
    Entonces esa carta se pasa a mi mazo de cartas