# language: es

Característica: Elegir carta aleatoria

  Escenario: Elegir carta aleatoria cuando la selecciono del mazo
    Dado que estoy en el menú principal de las cartas
    Cuando doy click en "sacar carta" o en "monton de polvo"
    Entonces obtengo una carta aleatoria
    Y se agrega al mazo del jugador actual
    Entonces veo la carta en el mazo