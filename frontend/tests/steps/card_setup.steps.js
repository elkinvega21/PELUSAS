import { Given, Then } from '@cucumber/cucumber';
import { drawCard, startGame } from './card_test_helpers.js';

Given('que estoy en el menú principal de las cartas', async function () {
  await startGame(this);
});

Then('saco una carta aleatoria', async function () {
  await drawCard(this);
});
