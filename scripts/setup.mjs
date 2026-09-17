import { readFileSync, writeFileSync } from 'node:fs';
import { randomBytes } from 'node:crypto';

const target = new URL('../.env', import.meta.url);
const template = readFileSync(new URL('../.env.example', import.meta.url), 'utf8');
try {
  writeFileSync(target, template.replace(/^JWT_SECRET=$/m, `JWT_SECRET=${randomBytes(32).toString('hex')}`), { flag: 'wx', mode: 0o600 });
  console.log('.env criado com um JWT_SECRET exclusivo. Preencha DB_URL, DB_USERNAME e DB_PASSWORD com os dados do Supabase.');
} catch (error) {
  if (error.code !== 'EEXIST') throw error;
  console.log('.env já existe e foi preservado. Confira as variáveis necessárias em .env.example.');
}
