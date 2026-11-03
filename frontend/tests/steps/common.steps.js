import { Given, Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

Given('que estoy en el menú principal', async function () {
  const baseUrl = process.env.BASE_URL || 'http://127.0.0.1:5176';
  await this.page.goto(baseUrl);
  await expect(this.page.locator('#btn-play')).toBeVisible();
});

When('abro la configuración', async function () {
  await this.page.getByRole('button', { name: 'CONFIGURACIÓN' }).click();
});

When('vuelvo al menú principal', async function () {
  await this.page.getByRole('button', { name: 'VOLVER AL MENÚ' }).click();
});

Then('veo el menú principal', async function () {
  await expect(this.page.locator('#btn-play')).toBeVisible();
  await expect(this.page.locator('#btn-settings')).toBeVisible();
  await expect(this.page.getByRole('heading', { name: 'CONFIGURACIÓN' })).toHaveCount(0);
});
