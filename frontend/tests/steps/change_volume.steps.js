import { Then, When } from '@cucumber/cucumber';
import { expect } from '@playwright/test';

When('ajusto el volumen al {string}', async function (percent) {
  const slider = this.page.getByRole('slider');
  await slider.focus();

  if (percent === '50%') {
    await slider.press('Home');
    for (let step = 0; step < 10; step += 1) {
      await slider.press('ArrowRight');
    }
    return;
  }

  if (percent === '100%') {
    await slider.press('End');
    return;
  }

  throw new Error(`Porcentaje de volumen no contemplado: ${percent}`);
});

Then('el volumen queda en {string}', async function (percent) {
  const expectedValue = percent === '50%' ? '0.5' : '1';
  await expect(this.page.getByRole('slider')).toHaveValue(expectedValue);
  await expect(this.page.getByText(percent, { exact: true })).toBeVisible();
});
