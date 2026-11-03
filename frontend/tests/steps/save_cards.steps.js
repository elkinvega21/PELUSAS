import { Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { activeCards, readCardValues, readSafePoints } from './card_test_helpers.js';

Then('es mi turno de nuevo', async function () {
  await expect(this.page.locator('.south-title')).toContainText('Jugador 1 (TU TURNO)');
  await expect(this.page.getByRole('button', { name: 'GUARDAR' })).toBeVisible();
  this.safePointsBeforeSave = await readSafePoints(this.page);
  this.cardsBeforeSave = await readCardValues(activeCards(this.page));
});

When('doy click en el botón guardar', async function () {
  await this.page.getByRole('button', { name: 'GUARDAR' }).click();
});

Then('se suma el valor de mis cartas', async function () {
  const expectedGain = this.cardsBeforeSave.reduce((total, value) => total + value, 0);
  this.expectedSafePoints = this.safePointsBeforeSave + expectedGain;
  await expect(this.page.locator('.south-safe-badge')).toContainText(`PUNTOS: ${this.expectedSafePoints}`);
  await expect(activeCards(this.page)).toHaveCount(0);
});

Then('me sube el puntaje', async function () {
  expect(await readSafePoints(this.page)).toBeGreaterThan(this.safePointsBeforeSave);
});

Then('veo mi puntaje', async function () {
  await expect(this.page.locator('.south-safe-badge')).toBeVisible();
  await expect(this.page.locator('.south-safe-badge')).toContainText(
    `PUNTOS: ${this.expectedSafePoints}`
  );
});
