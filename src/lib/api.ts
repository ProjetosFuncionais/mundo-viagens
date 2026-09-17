const TOKEN_KEY = 'mundo-viagens.token';
const BASE_URL = (import.meta.env?.VITE_API_URL || '/api').replace(/\/$/, '');
export const SESSION_EXPIRED = 'mundo-viagens:session-expired';
export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const saveToken = (token: string) => localStorage.setItem(TOKEN_KEY, token);
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);
export { TOKEN_KEY };

export class ApiError extends Error {
  constructor(public status: number, message: string) { super(message); }
}
export async function api<T>(path: string, options: RequestInit = {}, authenticated = true): Promise<T> {
  const token = authenticated ? getToken() : null;
  const headers = new Headers(options.headers);
  if (options.body) headers.set('Content-Type', 'application/json');
  if (token) headers.set('Authorization', `Bearer ${token}`);
  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${path}`, { ...options, headers, signal: options.signal ?? AbortSignal.timeout(20000) });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar à API. Tente novamente.');
  }
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    if (response.status === 401 && token && getToken() === token) {
      clearToken();
      window.dispatchEvent(new Event(SESSION_EXPIRED));
    }
    throw new ApiError(response.status, body?.message || `Erro na requisição (${response.status})`);
  }
  if (response.status === 204) return undefined as T;
  try { return await response.json(); }
  catch { throw new ApiError(response.status, 'A API retornou uma resposta inválida. Verifique a configuração de VITE_API_URL.'); }
}
export const errorMessage = (error: unknown) => error instanceof Error ? error.message : 'Ocorreu um erro. Tente novamente.';
