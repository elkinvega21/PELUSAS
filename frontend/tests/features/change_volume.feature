# language: es

Característica: Cambiar el volumen

  Escenario: Cambiar el volumen de 50% a 100% y volver al menú
    Dado que estoy en el menú principal
    Cuando abro la configuración
    Y ajusto el volumen al "50%"
    Entonces el volumen queda en "50%"
    Cuando ajusto el volumen al "100%"
    Entonces el volumen queda en "100%"
    Cuando vuelvo al menú principal
    Entonces veo el menú principal
