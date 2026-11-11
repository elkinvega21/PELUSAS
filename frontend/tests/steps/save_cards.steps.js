import { Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import {
  activeCards,
  drawCard,
  readCardValues,
  readSafePoints,
  setNextCardValue,
  turnTitle,
} from './card_test_helpers.js';

When('el siguiente jugador saca una carta distinta y se planta', async function () {
  await expect(this.page.locator('.south-title')).toContainText(turnTitle(2));
  await setNextCardValue(this.page, 7);
  await drawCard(this);
  await this.page.getByRole('button', { name: 'PLANTARSE' }).click();
});

Then('es mi turno de nuevo', async function () {
  await expect(this.page.locator('.south-title')).toContainText(turnTitle(1));
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
