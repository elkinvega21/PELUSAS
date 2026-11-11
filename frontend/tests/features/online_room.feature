# language: es

Característica: Acceso a Sala Online por Link y por Código

  Escenario: Acceder directamente a la sala online mediante el link con parámetro de sala
    Dado que abro el juego con el enlace de la sala "PELU-7926"
    Entonces veo el panel de la sala online con el código "PELU-7926"

  Escenario: Unirme a la sala online ingresando el código desde el menú
    Dado que estoy en el menú principal
    Cuando doy click en el botón de unirme con código
    Y escribo el código de sala "PELU-8844" y confirmo
    Entonces veo el panel de la sala online con el código "PELU-8844"

