import { Given, When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

Given('que abro el juego con el enlace de la sala {string}', async function (roomCode) {
  const baseUrl = process.env.BASE_URL || 'http://127.0.0.1:5176';
  await this.page.goto(`${baseUrl}/?room=${roomCode}`);
});

Then('veo el panel de la sala online con el código {string}', async function (expectedCode) {
  await expect(this.page.locator('.lobby-overlay')).toBeVisible();
  await expect(this.page.getByRole('button', { name: 'INICIAR PARTIDA ONLINE' })).toBeVisible();
  await expect(this.page.locator('.code-text', { hasText: expectedCode }).first()).toBeVisible();
});

When('doy click en el botón de unirme con código', async function () {
  await this.page.locator('#btn-join-code-menu').click();
  await expect(this.page.locator('.join-modal-overlay')).toBeVisible();
});

When('escribo el código de sala {string} y confirmo', async function (code) {
  await this.page.locator('.join-modal-input').fill(code);
  await this.page.locator('#btn-confirm-join-code').click();
});

