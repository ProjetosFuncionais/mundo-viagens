import { reservasApi, Reserva } from '../lib/services';
import { errorMessage } from '../lib/api';
import { useEffect, useState } from 'react';
import { useAuth } from '../lib/auth';
import { Button } from '../components/ui/button';
import { format, differenceInHours } from 'date-fns';

export default function Profile() {
  const { user, profile, refreshProfile } = useAuth();
  const [reservas, setReservas] = useState<Reserva[]>([]);
  const [error, setError] = useState('');
  const [canceling, setCanceling] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReservas();
  }, [user?.id]);

  const fetchReservas = async () => {
    if (!user) return;
    try {
      setError('');
      setReservas(await reservasApi.mine());
    } catch (error) { setError(errorMessage(error)); }
    finally { setLoading(false); }
  };

  const handleCancel = async (reservaId: string, dataPartida: string) => {
    const isLessThan48h = differenceInHours(new Date(dataPartida), new Date()) < 48;
    const message = isLessThan48h
      ? 'Faltam menos de 48h para a viagem. A multa é de 20% do total. Confirma o cancelamento?'
      : 'Confirma o cancelamento? Se faltarem menos de 48h no momento da confirmação, a multa será de 20%.';
    if (!window.confirm(message)) return;
    setCanceling(reservaId);
    setError('');
    try {
      await reservasApi.cancel(reservaId);
      await Promise.all([fetchReservas(), refreshProfile()]);
    } catch (error) { setError(errorMessage(error)); }
    finally { setCanceling(null); }
  };

  if (loading) return <div className="p-8 text-center">Carregando perfil...</div>;

  return (
    <div className="space-y-8 max-w-4xl mx-auto">
      <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{profile?.nome}</h1>
          <p className="text-gray-500">{profile?.email} | CPF: {profile?.cpf}</p>
        </div>
        {profile?.bloqueado && (
          <span className="bg-red-100 text-red-800 px-3 py-1 rounded-full text-sm font-semibold">Conta Bloqueada</span>
        )}
      </div>

      {error && <p role="alert" className="text-red-600">{error}</p>}
      <section>
        <h2 className="text-xl font-bold text-gray-900 mb-4">Minhas Viagens</h2>
        {reservas.length === 0 ? (
          <div className="bg-gray-50 border border-gray-200 rounded-xl p-8 text-center text-gray-500">
            Você ainda não tem reservas.
          </div>
        ) : (
          <div className="space-y-4">
            {reservas.map(reserva => {
              const dtPartida = reserva.flightDetails?.dataPartida;
              return (
                <div key={reserva.id} className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm flex flex-col md:flex-row justify-between gap-4">
                  <div>
                    <h3 className="font-bold text-lg">{reserva.flightDetails?.origem} &rarr; {reserva.flightDetails?.destino}</h3>
                    <p className="text-sm text-gray-600">Partida: {dtPartida ? format(new Date(dtPartida), "dd/MM/yyyy 'às' HH:mm") : 'Data não disponível'}</p>
                    {reserva.hotelDetails && (
                      <p className="text-sm text-gray-600">Hospedagem: {reserva.hotelDetails.nome}</p>
                    )}
                    <p className="text-sm text-gray-600 font-medium mt-2">
                      Valor Total: R$ {reserva.total.toFixed(2)}
                    </p>
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase ${
                      reserva.status === 'confirmada' ? 'bg-green-100 text-green-800' :
                      reserva.status === 'cancelada' ? 'bg-red-100 text-red-800' :
                      'bg-yellow-100 text-yellow-800'
                    }`}>
                      {reserva.status.replace('_', ' ')}
                    </span>
                    
                    {reserva.multa > 0 && <p className="text-sm text-red-600">Multa: R$ {reserva.multa.toFixed(2)}</p>}
                    {reserva.ticketId && <p className="text-xs text-gray-500 break-all">Bilhete: {reserva.ticketId}</p>}
                    {reserva.status !== 'cancelada' && new Date(dtPartida).getTime() > Date.now() && (
                      <Button variant="outline" size="sm" disabled={canceling !== null} onClick={() => handleCancel(reserva.id, dtPartida)}>
                        Cancelar Reserva
                      </Button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
}
