import { hotelsApi, Hotel } from '../../lib/services';
import { errorMessage } from '../../lib/api';
import { useState, useEffect } from 'react';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';

export default function Hotels() {
  const [hotels, setHotels] = useState<Hotel[]>([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [novoNome, setNovoNome] = useState('');
  const [novaCidade, setNovaCidade] = useState('');
  const [novoPreco, setNovoPreco] = useState('');
  const [novoQuartos, setNovoQuartos] = useState('');
  const [editingId, setEditingId] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);

  const fetchHotels = async () => {
    setLoading(true);
    try {
      setError('');
      setHotels(await hotelsApi.list());
    } catch (error) { setError(errorMessage(error)); }
    finally { setLoading(false); }
  };

  useEffect(() => {
    fetchHotels();
  }, []);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    setAdding(true);
    try {
      const data = {
        nome: novoNome,
        cidade: novaCidade,
        precoDiaria: parseFloat(novoPreco),
        quartosTotais: Number(novoQuartos),
      };
      if (editingId) await hotelsApi.update(editingId, data);
      else await hotelsApi.create(data);
      setEditingId(null);
      setNovoNome('');
      setNovaCidade('');
      setNovoPreco('');
      setNovoQuartos('');
      await fetchHotels();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setAdding(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Tem certeza que deseja excluir este hotel?')) return;
    setBusy(true);
    try { await hotelsApi.delete(id); await fetchHotels(); }
    catch (error) { setError(errorMessage(error)); }
    finally { setBusy(false); }
  };
  const handleEdit = (hotel: Hotel) => {
    setEditingId(hotel.id); setNovoNome(hotel.nome); setNovaCidade(hotel.cidade);
    setNovoPreco(String(hotel.precoDiaria)); setNovoQuartos(String(hotel.quartosTotais));
  };
  const cancelEdit = () => {
    setEditingId(null); setNovoNome(''); setNovaCidade(''); setNovoPreco(''); setNovoQuartos('');
  };

  if (loading) return <div className="p-8">Carregando...</div>;

  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-bold text-gray-900">Gerenciar Hotéis Parceiros</h1>
      {error && <p role="alert" className="text-red-600">{error}</p>}
      <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
        <h2 className="text-lg font-bold mb-4">{editingId ? 'Editar Hotel' : 'Adicionar Hotel'}</h2>
        <form onSubmit={handleAdd} className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-5 gap-4 items-end">
          <div className="space-y-1.5 md:col-span-2">
            <Label htmlFor="nome">Nome do Hotel</Label>
            <Input id="nome" required value={novoNome} onChange={e => setNovoNome(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cidade">Cidade / Destino</Label>
            <Input id="cidade" required value={novaCidade} onChange={e => setNovaCidade(e.target.value)} placeholder="Ex: Miami (MIA)" />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="preco">Diária (R$)</Label>
            <Input id="preco" type="number" min="0.01" step="0.01" required value={novoPreco} onChange={e => setNovoPreco(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="quartos">Qtd. Quartos</Label>
            <Input id="quartos" type="number" min="1" step="1" required value={novoQuartos} onChange={e => setNovoQuartos(e.target.value)} />
          </div>
          <Button type="submit" disabled={adding} className="w-full md:col-span-5">
            {adding ? 'Salvando...' : editingId ? 'Salvar alterações' : 'Adicionar Hotel'}
          </Button>
          {editingId && <Button type="button" variant="outline" disabled={adding} onClick={cancelEdit}>Cancelar edição</Button>}
        </form>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 border-b border-gray-200 text-gray-600">
            <tr>
              <th className="p-4 font-medium">Nome</th>
              <th className="p-4 font-medium">Cidade</th>
              <th className="p-4 font-medium">Diária</th>
              <th className="p-4 font-medium">Quartos Totais</th>
              <th className="p-4 font-medium">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {hotels.length === 0 ? (
              <tr>
                <td colSpan={5} className="p-8 text-center text-gray-500">Nenhum hotel cadastrado.</td>
              </tr>
            ) : hotels.map(h => (
              <tr key={h.id} className="hover:bg-gray-50">
                <td className="p-4 font-medium text-gray-900">{h.nome}</td>
                <td className="p-4 text-gray-600">{h.cidade}</td>
                <td className="p-4 text-gray-600">R$ {h.precoDiaria.toFixed(2)}</td>
                <td className="p-4 text-gray-600">{h.quartosTotais}</td>
                <td className="p-4">
                  <Button variant="outline" size="sm" disabled={busy || adding} onClick={() => handleEdit(h)}>Editar</Button>
                  <Button disabled={busy || adding} variant="destructive" size="sm" onClick={() => handleDelete(h.id)}>Excluir</Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
