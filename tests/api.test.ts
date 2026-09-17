import { test, beforeEach, afterEach, mock } from 'node:test';
import assert from 'node:assert/strict';
import { api, ApiError, getToken, saveToken, SESSION_EXPIRED } from '../src/lib/api.ts';

const storage = new Map<string, string>();
const events = new EventTarget();
beforeEach(() => {
  storage.clear();
  mock.method(globalThis, 'fetch');
  Object.defineProperty(globalThis, 'localStorage', { configurable: true, value: {
    getItem: (key: string) => storage.get(key) ?? null,
    setItem: (key: string, value: string) => storage.set(key, value),
    removeItem: (key: string) => storage.delete(key),
  } });
  Object.defineProperty(globalThis, 'window', { configurable: true, value: events });
});
afterEach(() => mock.restoreAll());

test('envia autenticação e dados JSON para a API', async () => {
  saveToken('session-token');
  mock.method(globalThis, 'fetch', async (url, options) => {
    assert.equal(url, '/api/reservas');
    assert.equal(options.headers.get('Authorization'), 'Bearer session-token');
    assert.equal(options.headers.get('Content-Type'), 'application/json');
    return Response.json({ id: 'reserva' });
  });
  assert.deepEqual(await api('/reservas', { method: 'POST', body: '{}' }), { id: 'reserva' });
});
test('expiração remove a sessão e notifica a interface', async () => {
  saveToken('expired');
  let expired = false;
  events.addEventListener(SESSION_EXPIRED, () => { expired = true; }, { once: true });
  mock.method(globalThis, 'fetch', async () => Response.json({ message: 'Sessão expirada' }, { status: 401 }));
  await assert.rejects(api('/auth/me'), { message: 'Sessão expirada', status: 401 });
  assert.equal(getToken(), null);
  assert.equal(expired, true);
});
test('resposta atrasada não encerra uma sessão mais recente', async () => {
  saveToken('old');
  mock.method(globalThis, 'fetch', async () => {
    saveToken('new');
    return Response.json({ message: 'Sessão expirada' }, { status: 401 });
  });
  await assert.rejects(api('/auth/me'), ApiError);
  assert.equal(getToken(), 'new');
});
test('login público não envia nem remove o token existente', async () => {
  saveToken('current');
  mock.method(globalThis, 'fetch', async (_url, options) => {
    assert.equal(options.headers.has('Authorization'), false);
    return Response.json({ message: 'Senha inválida' }, { status: 401 });
  });
  await assert.rejects(api('/auth/login', { method: 'POST', body: '{}' }, false), ApiError);
  assert.equal(getToken(), 'current');
});
test('trata falha de conexão, conteúdo inválido e exclusão sem conteúdo', async () => {
  mock.method(globalThis, 'fetch', async () => { throw new TypeError('Failed to fetch'); });
  await assert.rejects(api('/voos'), { status: 0 });
  mock.method(globalThis, 'fetch', async () => new Response('<html>frontend</html>'));
  await assert.rejects(api('/voos'), /VITE_API_URL/);
  mock.method(globalThis, 'fetch', async () => new Response(null, { status: 204 }));
  assert.equal(await api('/hoteis/id', { method: 'DELETE' }), undefined);
});
