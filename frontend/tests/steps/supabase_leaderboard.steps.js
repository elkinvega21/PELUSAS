import { Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

When('abro el ranking e historial', async function () {
  await this.page.locator('#btn-leaderboard').click();
});

Then('veo el modal de ranking e historial', async function () {
  await expect(this.page.getByRole('heading', { name: 'RANKING & HISTORIAL' })).toBeVisible();
});

Then('veo las opciones de clasificación e historial de partidas', async function () {
  await expect(this.page.getByRole('button', { name: 'CLASIFICACIÓN' })).toBeVisible();
  await expect(this.page.getByRole('button', { name: 'HISTORIAL DE PARTIDAS' })).toBeVisible();
});

When('cierro el modal de ranking', async function () {
  await this.page.getByRole('button', { name: 'VOLVER AL MENÚ' }).click();
});

