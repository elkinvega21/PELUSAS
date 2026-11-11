# language: es

Característica: Verificar carta obtenida

  Escenario: La carta aleatoria obtenida aparece en mi mano
    Dado que estoy en el menú principal de las cartas
    Cuando doy click en "sacar carta" o en "monton de polvo"
    Entonces obtengo una carta aleatoria
    Y se agrega al mazo del jugador actual
    Y veo la carta en el mazo