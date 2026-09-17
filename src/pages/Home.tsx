import { errorMessage } from '../lib/api';
import { useState } from 'react';
import { searchFlights, Flight } from '../lib/skyhigh';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Plane, Search, Calendar, MapPin } from 'lucide-react';
import { Link } from 'react-router-dom';
import { format } from 'date-fns';

export default function Home() {
  const [origem, setOrigem] = useState('');
  const [destino, setDestino] = useState('');
  const [dataPartida, setDataPartida] = useState('');
  const [flights, setFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searched, setSearched] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setFlights([]);
    setSearched(true);
    try {
      const results = await searchFlights(origem, destino, dataPartida);
      setFlights(results);
    } catch (error) {
      setError(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Hero Search Section */}
      <section className="bg-blue-600 rounded-2xl p-8 sm:p-12 text-white shadow-lg relative overflow-hidden">
        <div className="absolute top-0 right-0 opacity-10">
          <Plane className="w-96 h-96 -mt-24 -mr-24" />
        </div>
        <div className="relative z-10 max-w-2xl">
          <h1 className="text-4xl sm:text-5xl font-bold mb-4 tracking-tight">Explore o mundo com a Mundo Viagens</h1>
          <p className="text-blue-100 text-lg mb-8">Encontre as melhores passagens e pacotes com integração direta à SkyHigh Airlines.</p>
          
          <form onSubmit={handleSearch} className="bg-white rounded-xl p-4 shadow-xl flex flex-col sm:flex-row gap-4 items-end">
            <div className="w-full space-y-1.5">
              <Label htmlFor="origem" className="text-gray-700">Origem</Label>
              <div className="relative">
                <MapPin className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                <Input 
                  id="origem" 
                  placeholder="Ex: São Paulo" 
                  className="pl-9 text-gray-900"
                  value={origem}
                  onChange={(e) => setOrigem(e.target.value)}
                />
              </div>
            </div>
            <div className="w-full space-y-1.5">
              <Label htmlFor="destino" className="text-gray-700">Destino</Label>
              <div className="relative">
                <MapPin className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                <Input 
                  id="destino" 
                  placeholder="Ex: Miami" 
                  className="pl-9 text-gray-900"
                  value={destino}
                  onChange={(e) => setDestino(e.target.value)}
                />
              </div>
            </div>
            <div className="w-full space-y-1.5">
              <Label htmlFor="data" className="text-gray-700">Data de Partida</Label>
              <div className="relative">
                <Calendar className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                <Input 
                  id="data" 
                  type="date" 
                  className="pl-9 text-gray-900"
                  value={dataPartida}
                  onChange={(e) => setDataPartida(e.target.value)}
                />
              </div>
            </div>
            <Button type="submit" size="lg" className="w-full sm:w-auto" disabled={loading}>
              {loading ? 'Buscando...' : <><Search className="w-4 h-4 mr-2" /> Buscar</>}
            </Button>
          </form>
        </div>
      </section>

      {error && <p role="alert" className="text-red-600">{error}</p>}
      {/* Results Section */}
      {searched && (
        <section className="space-y-4">
          <h2 className="text-2xl font-semibold text-gray-900">Resultados da busca</h2>
          {flights.length === 0 && !loading && !error ? (
            <div className="bg-gray-50 border border-gray-200 rounded-xl p-8 text-center text-gray-500">
              Nenhum voo encontrado para estes critérios.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {flights.map((flight) => (
                <div key={flight.id} className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
                  <div className="flex justify-between items-start mb-4">
                    <div>
                      <span className="inline-block px-2 py-1 bg-blue-50 text-blue-700 text-xs font-semibold rounded-md mb-2">SkyHigh Airlines</span>
                      <h3 className="font-bold text-lg text-gray-900">{flight.origem} &rarr; {flight.destino}</h3>
                    </div>
                  </div>
                  <div className="space-y-2 mb-6 text-sm text-gray-600">
                    <p><strong>Partida:</strong> {format(new Date(flight.dataPartida), "dd/MM/yyyy 'às' HH:mm")}</p>
                    <p><strong>Chegada:</strong> {format(new Date(flight.dataChegada), "dd/MM/yyyy 'às' HH:mm")}</p>
                    <p><strong>Assentos disponíveis:</strong> {flight.assentosDisponiveis}</p>
                  </div>
                  <div className="flex items-center justify-between border-t border-gray-100 pt-4">
                    <div className="text-2xl font-bold text-gray-900">
                      R$ {flight.preco.toFixed(2)}
                    </div>
                    <Link to={`/checkout/${flight.id}`}>
                      <Button>Reservar</Button>
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      )}
    </div>
  );
}
