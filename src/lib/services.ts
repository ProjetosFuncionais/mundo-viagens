import { api } from './api';
export type UserRole = 'CLIENTE' | 'ATENDENTE' | 'GERENTE' | 'DONA';
export interface UserProfile { id: string; nome: string; cpf: string; email: string; role: UserRole; bloqueado: boolean }
export interface Flight {
  id: string; origem: string; destino: string; dataPartida: string; dataChegada: string; preco: number; assentosDisponiveis: number;
}
export interface Hotel { id: string; nome: string; cidade: string; precoDiaria: number; quartosTotais: number }
export interface Reserva {
  id: string; userId: string; flightId: string; hotelId: string | null;
  flightDetails: Omit<Flight, 'id'>;
  hotelDetails: Pick<Hotel, 'nome' | 'cidade' | 'precoDiaria'> | null;
  paymentMethod: 'cartao' | 'boleto'; status: 'confirmada' | 'pendente_pagamento' | 'cancelada';
  total: number; createdAt: string; multa: number; ticketId: string | null;
}
export interface RegisterInput { nome: string; cpf: string; email: string; password: string }
export interface AuthResponse { token: string; expiresAt: string; user: UserProfile }
export const authApi = {
  login: (email: string, password: string) => api<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }, false),
  register: (data: RegisterInput) => api<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify(data) }, false),
  me: () => api<UserProfile>('/auth/me'),
};
export const flightsApi = {
  search: (origem: string, destino: string, data: string) => {
    const params = new URLSearchParams({ origem, destino });
    if (data) params.set('data', data);
    return api<Flight[]>(`/voos?${params}`, {}, false);
  },
  get: (id: string) => api<Flight>(`/voos/${encodeURIComponent(id)}`, {}, false),
};
export const hotelsApi = {
  list: (cidade = '') => api<Hotel[]>(`/hoteis?${new URLSearchParams({ cidade })}`, {}, false),
  create: (hotel: Omit<Hotel, 'id'>) => api<Hotel>('/hoteis', { method: 'POST', body: JSON.stringify(hotel) }),
  update: (id: string, hotel: Omit<Hotel, 'id'>) => api<Hotel>(`/hoteis/${id}`, { method: 'PUT', body: JSON.stringify(hotel) }),
  delete: (id: string) => api<void>(`/hoteis/${id}`, { method: 'DELETE' }),
};
export const reservasApi = {
  create: (data: { flightId: string; hotelId: string | null; paymentMethod: Reserva['paymentMethod'] }) =>
    api<Reserva>('/reservas', { method: 'POST', body: JSON.stringify(data) }),
  mine: () => api<Reserva[]>('/reservas/minhas'),
  all: () => api<Reserva[]>('/reservas'),
  cancel: (id: string) => api<Reserva>(`/reservas/${id}/cancelar`, { method: 'POST' }),
  confirm: (id: string) => api<Reserva>(`/reservas/${id}/confirmar-pagamento`, { method: 'POST' }),
};
export const customersApi = {
  list: () => api<UserProfile[]>('/clientes'),
  block: (id: string, bloqueado: boolean) => api<UserProfile>(`/clientes/${id}/bloqueio`, { method: 'PATCH', body: JSON.stringify({ bloqueado }) }),
};
