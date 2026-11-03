import { Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

When('doy click en el botón plantarme', async function () {
  await this.page.getByRole('button', { name: 'PLANTARSE' }).click();
});

Then('cambia de turno al siguiente jugador', async function () {
  await expect(this.page.locator('.south-title')).toContainText('Jugador 2 (TU TURNO)');
});
