import { expect } from '@playwright/test';
import { Then, When } from '@cucumber/cucumber';
import { activeCards, drawCard, readCardValues, rivalCards } from './card_test_helpers.js';

When('saco la misma carta que tiene mi rival', async function () {
  const rivalValues = await readCardValues(rivalCards(this.page));
  expect(rivalValues).toContain(this.drawnCardValue);
  this.valueToSteal = this.drawnCardValue;
  await drawCard(this);
  expect(this.drawnCardValue).toBe(this.valueToSteal);
});

Then('esa carta se pasa a mi mazo de cartas', async function () {
  // Robo automático (req. 5.2, paso 3): la carta del rival pasa a mi mesa junto con la que saqué.
  await expect(activeCards(this.page)).toHaveCount(2);
  const currentValues = await readCardValues(activeCards(this.page));
  expect(currentValues).toEqual([this.valueToSteal, this.valueToSteal]);
  const opponentsValues = await readCardValues(rivalCards(this.page));
  expect(opponentsValues).not.toContain(this.valueToSteal);
});
