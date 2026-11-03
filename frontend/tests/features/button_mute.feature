# language: es

Característica: Silenciar el audio

  Escenario: Silenciar la música y volver al menú principal
    Dado que estoy en el menú principal
    Cuando abro la configuración
    Y silencio la música y el sonido
    Entonces el audio aparece silenciado
    Cuando vuelvo al menú principal
    Entonces veo el menú principal
