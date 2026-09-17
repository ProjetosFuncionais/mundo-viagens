import { customersApi, UserProfile } from '../../lib/services';
import { errorMessage } from '../../lib/api';
import { useState, useEffect } from 'react';
import { Button } from '../../components/ui/button';

export default function Customers() {
  const [clientes, setClientes] = useState<UserProfile[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchClientes = async () => {
    setLoading(true);
    try {
      setError('');
      setClientes(await customersApi.list());
    } catch (error) { setError(errorMessage(error)); }
    finally { setLoading(false); }
  };

  useEffect(() => {
    fetchClientes();
  }, []);

  const toggleBloqueio = async (id: string, isBlocked: boolean) => {
    setBusy(true);
    try {
      await customersApi.block(id, !isBlocked);
      await fetchClientes();
    } catch (err) {
      setError(errorMessage(err));
    } finally { setBusy(false); }
  };

  if (loading) return <div className="p-8">Carregando...</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Gerenciar Clientes</h1>
      {error && <p role="alert" className="text-red-600">{error}</p>}
      <div className="bg-white border border-gray-200 rounded-xl overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 border-b border-gray-200 text-gray-600">
            <tr>
              <th className="p-4 font-medium">Nome</th>
              <th className="p-4 font-medium">CPF</th>
              <th className="p-4 font-medium">E-mail</th>
              <th className="p-4 font-medium">Status</th>
              <th className="p-4 font-medium">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {clientes.length === 0 && <tr><td colSpan={5} className="p-8 text-center text-gray-500">Nenhum cliente cadastrado.</td></tr>}
            {clientes.map(c => (
              <tr key={c.id} className="hover:bg-gray-50">
                <td className="p-4 font-medium text-gray-900">{c.nome}</td>
                <td className="p-4 text-gray-600">{c.cpf}</td>
                <td className="p-4 text-gray-600">{c.email}</td>
                <td className="p-4">
                  {c.bloqueado ? (
                    <span className="bg-red-100 text-red-800 px-2 py-1 rounded text-xs font-semibold uppercase">Bloqueado</span>
                  ) : (
                    <span className="bg-green-100 text-green-800 px-2 py-1 rounded text-xs font-semibold uppercase">Ativo</span>
                  )}
                </td>
                <td className="p-4">
                  <Button 
                    disabled={busy}
                    variant={c.bloqueado ? 'default' : 'destructive'} 
                    size="sm" 
                    onClick={() => toggleBloqueio(c.id, c.bloqueado)}
                  >
                    {c.bloqueado ? 'Desbloquear' : 'Bloquear'}
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
