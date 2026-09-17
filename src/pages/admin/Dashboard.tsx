import { reservasApi, customersApi } from '../../lib/services';
import { errorMessage } from '../../lib/api';
import { useState, useEffect } from 'react';
import { Users, CreditCard, Plane } from 'lucide-react';

export default function Dashboard() {
  const [stats, setStats] = useState({
    totalReservas: 0,
    faturamento: 0,
    pendentes: 0,
    clientesBloqueados: 0
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [reservas, clientes] = await Promise.all([reservasApi.all(), customersApi.list()]);
        setStats({
          totalReservas: reservas.length,
          faturamento: reservas.filter(r => r.status === 'confirmada').reduce((sum, r) => sum + r.total, 0),
          pendentes: reservas.filter(r => r.status === 'pendente_pagamento').length,
          clientesBloqueados: clientes.filter(c => c.bloqueado).length,
        });
      } catch (error) { setError(errorMessage(error)); }
      finally { setLoading(false); }
    };
    
    fetchStats();
  }, []);

  if (loading) return <div className="p-8 text-center">Carregando métricas...</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Painel Gerencial</h1>
      {error && <p role="alert" className="text-red-600">{error}</p>}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-blue-100 text-blue-600 rounded-lg"><Plane className="w-5 h-5" /></div>
            <h3 className="text-gray-500 font-medium text-sm">Total de Reservas</h3>
          </div>
          <p className="text-3xl font-bold text-gray-900">{stats.totalReservas}</p>
        </div>
        
        <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-green-100 text-green-600 rounded-lg"><CreditCard className="w-5 h-5" /></div>
            <h3 className="text-gray-500 font-medium text-sm">Faturamento (Confirmado)</h3>
          </div>
          <p className="text-3xl font-bold text-gray-900">R$ {stats.faturamento.toFixed(2)}</p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-yellow-100 text-yellow-600 rounded-lg"><CreditCard className="w-5 h-5" /></div>
            <h3 className="text-gray-500 font-medium text-sm">Aguardando Pagamento</h3>
          </div>
          <p className="text-3xl font-bold text-gray-900">{stats.pendentes}</p>
        </div>

        <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-red-100 text-red-600 rounded-lg"><Users className="w-5 h-5" /></div>
            <h3 className="text-gray-500 font-medium text-sm">Clientes Bloqueados</h3>
          </div>
          <p className="text-3xl font-bold text-gray-900">{stats.clientesBloqueados}</p>
        </div>
      </div>
    </div>
  );
}
