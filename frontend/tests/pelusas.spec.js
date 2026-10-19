import { test, expect } from '@playwright/test';

test.describe('Pelusas - Pruebas Funcionales Base', () => {
  test('La aplicación carga correctamente', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/Pelusas/i);
  });
});
