import { Given, When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

When('doy click en el botón de partida vs bots', async function () {
  await this.page.locator('#btn-play-bots').click();
});

Then('veo la pantalla de carga de los 4 bots con sus nombres', async function () {
  await expect(this.page.locator('.bot-loader-overlay')).toBeVisible();
  await expect(this.page.getByText('ENTRANDO AL JUEGO...')).toBeVisible();
  await expect(this.page.getByText('Bot Chispa')).toBeVisible();
  await expect(this.page.getByText('Bot Pelusín')).toBeVisible();
  await expect(this.page.getByText('Bot Rayo')).toBeVisible();
  await expect(this.page.getByText('Bot Sombrío')).toBeVisible();
});

Given('que entro a una partida contra los bots', async function () {
  const baseUrl = process.env.BASE_URL || 'http://127.0.0.1:5176';
  await this.page.goto(baseUrl);
  await this.page.locator('#btn-play-bots').click();
  // Hacer click en entrar de inmediato o esperar
  await this.page.locator('.bot-start-now-btn').click();
  await expect(this.page.locator('.square-table-screen')).toBeVisible();
});

When('doy click en la flecha de salir durante la partida con bots', async function () {
  await this.page.getByRole('button', { name: 'Salir al menú' }).click();
});

Then('veo la notificación de advertencia de abandono', async function () {
  await expect(this.page.locator('.exit-confirm-overlay')).toBeVisible();
  await expect(this.page.getByText('¿ABANDONAR PARTIDA?')).toBeVisible();
});

When('confirmo que deseo salir y simular', async function () {
  await this.page.locator('.exit-confirm-btn').click();
});

Then('veo el resultado final donde el humano no es el ganador', async function () {
  await expect(this.page.locator('.game-over-overlay')).toBeVisible();
  await expect(this.page.locator('.abandoned-banner')).toBeVisible();

  // El ganador nunca debe ser humano
  const winnerNameLocator = this.page.locator('.winner-name');
  await expect(winnerNameLocator).toBeVisible();
  const winnerName = await winnerNameLocator.textContent();
  expect(winnerName).toContain('Bot');

  // El humano aparece con abandono
  await expect(this.page.locator('.abandoned-tag')).toBeVisible();
});
