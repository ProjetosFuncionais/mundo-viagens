import { reservasApi, Reserva } from '../../lib/services';
import { errorMessage } from '../../lib/api';
import { useState, useEffect } from 'react';
import { Button } from '../../components/ui/button';
import { format } from 'date-fns';

export default function Reservations() {
  const [reservas, setReservas] = useState<Reserva[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchReservas = async () => {
    setLoading(true);
    try {
      setError('');
      setReservas(await reservasApi.all());
    } catch (error) { setError(errorMessage(error)); }
    finally { setLoading(false); }
  };

  useEffect(() => {
    fetchReservas();
  }, []);

  const handleConfirmPayment = async (id: string) => {
    setBusy(true);
    try {
      await reservasApi.confirm(id);
      await fetchReservas();
    } catch (err) {
      setError(errorMessage(err));
    } finally { setBusy(false); }
  };

  if (loading) return <div className="p-8">Carregando...</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Gerenciar Reservas</h1>
      {error && <p role="alert" className="text-red-600">{error}</p>}
      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 border-b border-gray-200 text-gray-600">
            <tr>
              <th className="p-4 font-medium">Cliente (ID)</th>
              <th className="p-4 font-medium">Voo</th>
              <th className="p-4 font-medium">Data Criação</th>
              <th className="p-4 font-medium">Pagamento</th>
              <th className="p-4 font-medium">Status</th>
              <th className="p-4 font-medium">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {reservas.map(r => (
              <tr key={r.id} className="hover:bg-gray-50">
                <td className="p-4 text-gray-500 text-xs">{r.userId}</td>
                <td className="p-4 font-medium text-gray-900">{r.flightDetails?.origem.split(' ')[0]} &rarr; {r.flightDetails?.destino.split(' ')[0]}</td>
                <td className="p-4 text-gray-600">{r.createdAt ? format(new Date(r.createdAt), 'dd/MM/yyyy HH:mm') : '-'}</td>
                <td className="p-4 text-gray-600 uppercase">{r.paymentMethod}</td>
                <td className="p-4">
                  <span className={`px-2 py-1 rounded text-xs font-semibold uppercase ${
                    r.status === 'confirmada' ? 'bg-green-100 text-green-800' :
                    r.status === 'cancelada' ? 'bg-red-100 text-red-800' :
                    'bg-yellow-100 text-yellow-800'
                  }`}>
                    {r.status.replace('_', ' ')}
                  </span>
                </td>
                <td className="p-4">
                  {r.status === 'pendente_pagamento' && (
                    <Button disabled={busy} size="sm" onClick={() => handleConfirmPayment(r.id)}>
                      Confirmar Pgto
                    </Button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
