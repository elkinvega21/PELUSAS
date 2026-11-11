import { Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { turnTitle } from './card_test_helpers.js';

When('doy click en el botón plantarme', async function () {
  await this.page.getByRole('button', { name: 'PLANTARSE' }).click();
});

Then('cambia de turno al siguiente jugador', async function () {
  await expect(this.page.locator('.south-title')).toContainText(turnTitle(2));
});
