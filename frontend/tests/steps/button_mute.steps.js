import { Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

When('silencio la música y el sonido', async function () {
  await this.page.locator('.settings-body .setting-row').first().getByRole('button').click();
});

Then('el audio aparece silenciado', async function () {
  await expect(this.page.getByText('Audio silenciado')).toBeVisible();
  await expect(this.page.getByRole('button', { name: 'MUTED' })).toBeVisible();
});
