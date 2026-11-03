import { Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

When('doy click en el botón jugar', async function () {
  await this.page.getByRole('button', { name: 'JUGAR' }).click();
});

Then('veo el menú de inicio', async function () {
  await expect(this.page.getByRole('heading', { name: 'NUEVA PARTIDA' })).toBeVisible();
  await expect(this.page.locator('.player-row')).toHaveCount(2);
});

When('doy click en el botón "+ jugador"', async function () {
  await this.page.getByRole('button', { name: 'JUGADOR' }).click();
  await expect(this.page.locator('.player-row')).toHaveCount(3);
  await expect(this.page.getByText('Jugador 3', { exact: true })).toBeVisible();
});

When('doy click en el botón comenzar partida', async function () {
  await this.page.getByRole('button', { name: 'COMENZAR PARTIDA' }).click();
});

Then('veo la página donde están las cartas', async function () {
  await expect(this.page.locator('.square-table-screen')).toBeVisible();
  await expect(this.page.getByText('Jugador 3', { exact: true })).toBeVisible();
  await expect(this.page.getByRole('img', { name: 'Mazo' })).toBeVisible();
});