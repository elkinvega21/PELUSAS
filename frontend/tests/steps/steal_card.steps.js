import { expect } from '@playwright/test';
import { Then, When } from '@cucumber/cucumber';
import { activeCards, readCardValues, rivalCards } from './card_test_helpers.js';

Then('esa carta aleatoria coincide con una carta de un rival que sacó anteriormente', async function () {
  const currentValues = await readCardValues(activeCards(this.page));
  const opponentsValues = await readCardValues(rivalCards(this.page));
  expect(opponentsValues).toContain(this.drawnCardValue);
  this.stolenValues = opponentsValues.filter(value => currentValues.includes(value));
  expect(this.stolenValues).toContain(this.drawnCardValue);
  this.cardsBeforeSteal = currentValues;
});

When('doy click en el botón Robar', async function () {
  await this.page.getByRole('button', { name: 'ROBAR' }).click();
});

Then('esa carta se pasa a mi mazo de cartas', async function () {
  const currentValues = await readCardValues(activeCards(this.page));
  const opponentsValues = await readCardValues(rivalCards(this.page));
  expect(currentValues).toHaveLength(this.cardsBeforeSteal.length + this.stolenValues.length);

  for (const value of this.stolenValues) {
    const previousCount = this.cardsBeforeSteal.filter(cardValue => cardValue === value).length;
    const stolenCount = this.stolenValues.filter(cardValue => cardValue === value).length;
    expect(currentValues.filter(cardValue => cardValue === value)).toHaveLength(previousCount + stolenCount);
    expect(opponentsValues).not.toContain(value);
  }

  expect(this.stolenValues).toContain(this.drawnCardValue);
});
