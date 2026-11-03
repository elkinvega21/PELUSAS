**Este .md es solo para guardarme notas sobre el desarrollo del juego**

## Pruebas funcionales de configuración

Los archivos `.feature` sí son relevantes: contienen los escenarios Gherkin que Cucumber descubre y ejecuta; los archivos `.steps.js` implementan las acciones y verificaciones de cada escenario. Cada prueba tiene su propio archivo:

- `frontend/tests/features/button_configuration.feature` y `frontend/tests/steps/button_configuration.steps.js`
- `frontend/tests/features/button_mute.feature` y `frontend/tests/steps/button_mute.steps.js`
- `frontend/tests/features/change_volume.feature` y `frontend/tests/steps/change_volume.steps.js`

Los hooks del navegador están en `frontend/tests/steps/hooks.js` y los pasos comunes en `frontend/tests/steps/common.steps.js`.

Para ejecutarlas:

1. En una terminal, inicia el frontend:
   ```powershell
   cd frontend
   npm run dev -- --host 127.0.0.1 --port 5175
   ```
2. En otra terminal, desde `frontend`, ejecuta:
   ```powershell
   npm run test:bdd
   ```

Para ejecutar un solo escenario, desde `frontend` usa el archivo `.feature` que quieras:
   ```powershell
   npx cucumber-js --config tests/cucumber.js tests/features/button_configuration.feature
   ```
   Reemplaza el nombre por `button_mute.feature` o `change_volume.feature` para ejecutar los otros escenarios.

El resultado se muestra en la terminal y también se genera el reporte HTML en `frontend/cucumber-report.html`.

FIXME: No hay pantalla de ganador, es decir, se termina el montón de polvo y no dice quien es el ganador

FIXME: El conteiner principal tiene problemas cunado hay 9 o 10 cartas
