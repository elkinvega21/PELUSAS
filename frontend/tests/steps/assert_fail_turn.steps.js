import { Given, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { drawCard, startGame } from './card_test_helpers.js';

Given('que inicio una partida con un robo repetido preparado', async function () {
  await startGame(this, 0.3);
  await expect(this.page.getByRole('button', { name: 'SACAR CARTA' })).toBeVisible();
});

When('saco tres cartas iguales seguidas', async function () {
  // Regla 5.3: repetir un valor teniendo 3 o más cartas delante hace perder el turno.
  for (let intento = 0; intento < 3; intento += 1) {
    await drawCard(this);
  }
});
