import { test } from 'node:test';
import assert from 'node:assert/strict';
import { returnPath } from '../src/lib/navigation.ts';

test('retorna ao checkout escolhido depois da autenticação', () => {
  assert.equal(returnPath({ from: '/checkout/FL-001_2026-10-01' }), '/checkout/FL-001_2026-10-01');
});
test('ignora destinos externos, estado inválido e ciclos de login', () => {
  for (const from of ['//example.com', 'https://example.com', '/\\example.com', '/login', '/register?x=1', 1, null]) {
    assert.equal(returnPath({ from }), '/');
  }
  assert.equal(returnPath(null), '/');
});
