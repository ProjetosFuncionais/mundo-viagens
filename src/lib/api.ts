const TOKEN_KEY = 'mundo-viagens.token';
const BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/$/, '');
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
    response = await fetch(`${BASE_URL}${path}`, { ...options, headers });
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
  return response.status === 204 ? undefined as T : response.json();
}
export const errorMessage = (error: unknown) => error instanceof Error ? error.message : 'Ocorreu um erro. Tente novamente.';
