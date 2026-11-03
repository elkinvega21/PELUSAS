import { Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { activeCards, drawCard } from './card_test_helpers.js';

When('doy click en {string} o en {string}', async function (buttonLabel, deckLabel) {
  const button = buttonLabel.toLocaleLowerCase('es');
  const deck = deckLabel.toLocaleLowerCase('es');
  if (button !== 'sacar carta' || deck !== 'monton de polvo') {
    throw new Error(`Origen de carta no contemplado: "${buttonLabel}" o "${deckLabel}".`);
  }
  await drawCard(this, 'button');
});

Then('obtengo una carta aleatoria', async function () {
  if (!Number.isInteger(this.drawnCardValue)) {
    throw new Error('No se registró el valor de la carta aleatoria.');
  }
  await expect(
    this.page.locator('.south-cards-rack').getByAltText(`Carta ${this.drawnCardValue}`)
  ).toBeVisible();
});

Then('se agrega al mazo del jugador actual', async function () {
  await expect(activeCards(this.page)).toHaveCount(this.cardsBeforeDraw + 1);
});

Then('veo la carta en el mazo', async function () {
  await expect(
    this.page.locator('.south-cards-rack').getByAltText(`Carta ${this.drawnCardValue}`)
  ).toBeVisible();
});
