import { expect } from '@playwright/test';

export const activeCards = page => page.locator('.south-cards-rack img');
export const rivalCards = page => page.locator('.table-seat .seat-cards-shelf img');

export async function readCardValues(cards) {
  const altTexts = await cards.evaluateAll(images => images.map(image => image.getAttribute('alt')));
  return altTexts
    .map(alt => Number(alt?.match(/^Carta (\d+)$/)?.[1]))
    .filter(Number.isInteger);
}

export async function readSafePoints(page) {
  const badgeText = await page.locator('.south-safe-badge').innerText();
  const match = badgeText.match(/PUNTOS:\s*(\d+)/);
  if (!match) {
    throw new Error(`No se pudo leer el puntaje del jugador: ${badgeText}`);
  }
  return Number(match[1]);
}

/** Texto del turno del jugador N en el tablero: "Jugador N (¡TU TURNO!)". */
export const turnTitle = numero => new RegExp(`Jugador ${numero} \\(¡?TU TURNO!?\\)`);

/** Fija el valor (1-10) de la próxima carta que saque el mazo. */
export async function setNextCardValue(page, value) {
  await page.evaluate(cardValue => {
    Math.random = () => (cardValue - 1 + 0.5) / 10;
  }, value);
}

export async function drawCard(world, trigger = 'button') {
  world.cardsBeforeDraw = await activeCards(world.page).count();

  if (trigger === 'button') {
    await world.page.getByRole('button', { name: 'SACAR CARTA' }).click();
  } else {
    await world.page.locator('.table-center-deck').click();
  }

  const reveal = world.page.getByRole('dialog', { name: 'Carta revelada' });
  await expect(reveal).toBeVisible();
  const cardAlt = await reveal.getByRole('img').getAttribute('alt');
  const match = cardAlt?.match(/^Carta (\d+)$/);
  if (!match) {
    throw new Error(`La carta revelada no tiene un valor reconocible: ${cardAlt}`);
  }
  world.drawnCardValue = Number(match[1]);
  await expect(reveal).toBeHidden();
}
