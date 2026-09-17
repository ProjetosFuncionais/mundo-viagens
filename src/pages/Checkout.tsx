import { hotelsApi, reservasApi, Hotel } from '../lib/services';
import { errorMessage } from '../lib/api';
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/auth';
import { getFlightById, Flight } from '../lib/skyhigh';
import { Button } from '../components/ui/button';



export default function Checkout() {
  const { flightId } = useParams();
  const navigate = useNavigate();
  const { profile } = useAuth();
  
  const [flight, setFlight] = useState<Flight | null>(null);
  const [hotels, setHotels] = useState<Hotel[]>([]);
  const [selectedHotel, setSelectedHotel] = useState<string | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<'cartao' | 'boleto'>('cartao');
  const [loading, setLoading] = useState(true);
  const [booking, setBooking] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    const fetchDetails = async () => {
      setLoading(true);
      setError('');
      try {
        if (!flightId) throw new Error('Voo não encontrado');
        const flight = await getFlightById(flightId);
        const hotels = await hotelsApi.list(flight.destino);
        if (active) { setFlight(flight); setHotels(hotels); }
      } catch (error) {
        if (active) { setFlight(null); setError(errorMessage(error)); }
      } finally {
        if (active) setLoading(false);
      }
    };
    void fetchDetails();
    return () => { active = false; };
  }, [flightId]);

  if (loading) return <div className="p-8 text-center">Carregando detalhes...</div>;
  if (!flight) return <div className="p-8 text-center text-red-600">{error}</div>;

  if (profile?.bloqueado) {
    return (
      <div className="max-w-xl mx-auto p-8 mt-12 bg-red-50 text-red-700 rounded-xl border border-red-200 text-center">
        <h2 className="text-xl font-bold mb-4">Conta Bloqueada</h2>
        <p>Você teve muitos cancelamentos recentes e foi bloqueado. Entre em contato com a agência para desbloquear sua conta e voltar a fazer reservas.</p>
      </div>
    );
  }

  const handleBook = async () => {
    setBooking(true);
    setError('');
    
    try {
      await reservasApi.create({ flightId: flight.id, hotelId: selectedHotel, paymentMethod });
      navigate('/profile');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBooking(false);
    }
  };

  const hotel = hotels.find(h => h.id === selectedHotel);
  const total = flight.preco + (hotel ? hotel.precoDiaria * 3 : 0); // hardcoding 3 dias

  return (
    <div className="max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8">
      <div className="md:col-span-2 space-y-8">
        <section className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Revisão do Voo</h2>
          <div className="flex justify-between items-center mb-2">
            <span className="text-gray-600">De: <strong>{flight.origem}</strong></span>
            <span className="text-gray-600">Para: <strong>{flight.destino}</strong></span>
          </div>
          <div className="text-gray-600">
            Preço do voo: R$ {flight.preco.toFixed(2)}
          </div>
        </section>

        <section className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Adicionar Hospedagem</h2>
          {hotels.length === 0 ? (
            <p className="text-gray-500">Não há hotéis parceiros disponíveis neste destino.</p>
          ) : (
            <div className="space-y-4">
              <label className={`block border p-4 rounded-lg cursor-pointer ${selectedHotel === null ? 'border-blue-600 bg-blue-50' : 'border-gray-200'}`}>
                <input type="radio" name="hotel" className="mr-2" checked={selectedHotel === null} onChange={() => setSelectedHotel(null)} />
                Apenas passagem (sem hotel)
              </label>
              {hotels.map(h => (
                <label key={h.id} className={`block border p-4 rounded-lg cursor-pointer ${selectedHotel === h.id ? 'border-blue-600 bg-blue-50' : 'border-gray-200'}`}>
                  <div className="flex justify-between items-center">
                    <div>
                      <input type="radio" name="hotel" className="mr-2" checked={selectedHotel === h.id} onChange={() => setSelectedHotel(h.id)} />
                      <strong>{h.nome}</strong>
                    </div>
                    <span>R$ {h.precoDiaria.toFixed(2)} / dia</span>
                  </div>
                </label>
              ))}
            </div>
          )}
        </section>

        <section className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Forma de Pagamento</h2>
          <div className="space-y-4">
            <label className={`block border p-4 rounded-lg cursor-pointer ${paymentMethod === 'cartao' ? 'border-blue-600 bg-blue-50' : 'border-gray-200'}`}>
              <input type="radio" name="payment" className="mr-2" checked={paymentMethod === 'cartao'} onChange={() => setPaymentMethod('cartao')} />
              Cartão de Crédito (confirmação simulada)
            </label>
            <label className={`block border p-4 rounded-lg cursor-pointer ${paymentMethod === 'boleto' ? 'border-blue-600 bg-blue-50' : 'border-gray-200'}`}>
              <input type="radio" name="payment" className="mr-2" checked={paymentMethod === 'boleto'} onChange={() => setPaymentMethod('boleto')} />
              Boleto Bancário (confirmação pela agência)
            </label>
          </div>
        </section>
      </div>

      <div className="md:col-span-1">
        <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm sticky top-24">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Resumo da Compra</h2>
          <div className="space-y-4 mb-6 text-gray-600">
            <div className="flex justify-between">
              <span>Passagem Aérea</span>
              <span>R$ {flight.preco.toFixed(2)}</span>
            </div>
            {hotel && (
              <div className="flex justify-between">
                <span>Hotel (3 dias)</span>
                <span>R$ {(hotel.precoDiaria * 3).toFixed(2)}</span>
              </div>
            )}
            <div className="border-t border-gray-200 pt-4 flex justify-between font-bold text-lg text-gray-900">
              <span>Total</span>
              <span>R$ {total.toFixed(2)}</span>
            </div>
          </div>
          
          {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
          
          <Button className="w-full" size="lg" onClick={handleBook} disabled={booking}>
            {booking ? 'Processando...' : 'Confirmar Reserva'}
          </Button>
        </div>
      </div>
    </div>
  );
}
