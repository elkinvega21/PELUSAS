import { Given, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { drawCard, turnTitle } from './card_test_helpers.js';

Given('que estoy en el menú principal de las cartas', async function () {
  const baseUrl = process.env.BASE_URL || 'http://127.0.0.1:5176';
  await this.page.addInitScript(() => {
    Math.random = () => 0.2;
  });
  await this.page.goto(baseUrl);
  await this.page.getByRole('button', { name: 'JUGAR' }).click();
  await this.page.getByRole('button', { name: 'COMENZAR PARTIDA' }).click();
  await expect(this.page.locator('.square-table-screen')).toBeVisible();
  await expect(this.page.locator('.south-title')).toContainText(turnTitle(1));
});

Then('saco una carta aleatoria', async function () {
  await drawCard(this);
});
