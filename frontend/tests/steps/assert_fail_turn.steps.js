import { Given } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { startGame } from './card_test_helpers.js';

Given('que inicio una partida con un robo repetido preparado', async function () {
  await startGame(this, 0.3);
  await expect(this.page.getByRole('button', { name: 'SACAR CARTA' })).toBeVisible();
});
