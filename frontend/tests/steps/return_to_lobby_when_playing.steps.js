import { Given, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

Given('que estoy en la pantalla del juego', async function () {
  const baseUrl = process.env.BASE_URL || 'http://127.0.0.1:5176';
  await this.page.goto(baseUrl);
  await this.page.getByRole('button', { name: 'JUGAR' }).click();
  await this.page.getByRole('button', { name: 'COMENZAR PARTIDA' }).click();
  await expect(this.page.locator('.square-table-screen')).toBeVisible();
});

When('doy click en la flecha de volver al menú principal', async function () {
  await this.page.getByRole('button', { name: 'Salir al menú' }).click();
});
