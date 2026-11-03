import { Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

Then('veo el menú de configuración', async function () {
  await expect(this.page.getByRole('heading', { name: 'CONFIGURACIÓN' })).toBeVisible();
  await expect(this.page.getByRole('button', { name: 'VOLVER AL MENÚ' })).toBeVisible();
});
