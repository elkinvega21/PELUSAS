# language: es

Característica: Verificar pérdida de turno por carta repetida

  Escenario: Perder el turno al robar una carta repetida
    Dado que inicio una partida con un robo repetido preparado
    Cuando saco tres cartas iguales seguidas
    Entonces cambia de turno al siguiente jugador
