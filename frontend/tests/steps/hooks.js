import { After, AfterAll, Before, BeforeAll, setDefaultTimeout } from '@cucumber/cucumber';
import { chromium } from '@playwright/test';
import { spawn } from 'node:child_process';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const TEST_BASE_URL = process.env.BASE_URL || 'http://127.0.0.1:5176';
const FRONTEND_DIRECTORY = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
let viteProcess;

setDefaultTimeout(60000);

BeforeAll(async function () {
  if (process.env.BASE_URL) return;

  const viteEntry = resolve(FRONTEND_DIRECTORY, 'node_modules', 'vite', 'bin', 'vite.js');
  viteProcess = spawn(
    process.execPath,
    [viteEntry, '--host', '127.0.0.1', '--port', '5176', '--strictPort'],
    { cwd: FRONTEND_DIRECTORY, stdio: 'inherit', windowsHide: true }
  );

  const deadline = Date.now() + 30000;
  let lastError;

  while (Date.now() < deadline) {
    if (viteProcess.exitCode !== null) {
      throw new Error(`El servidor Vite terminó antes de iniciar (código ${viteProcess.exitCode}).`);
    }

    try {
      const response = await fetch(TEST_BASE_URL);
      if (response.ok) return;
      lastError = new Error(`Vite respondió con HTTP ${response.status}.`);
    } catch (error) {
      lastError = error;
    }

    await new Promise(resolveDelay => setTimeout(resolveDelay, 250));
  }

  throw new Error(`No se pudo iniciar el servidor de pruebas en ${TEST_BASE_URL}.`, { cause: lastError });
});

Before(async function () {
  this.browser = await chromium.launch({ headless: process.env.HEADED !== '1' });
  this.page = await this.browser.newPage();
});

After(async function () {
  await this.browser?.close();
});

AfterAll(async function () {
  if (!viteProcess || viteProcess.exitCode !== null) return;

  await new Promise(resolveExit => {
    viteProcess.once('exit', resolveExit);
    viteProcess.kill();
  });
  viteProcess = null;
});
