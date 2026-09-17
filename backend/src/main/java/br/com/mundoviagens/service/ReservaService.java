package br.com.mundoviagens.service;
import br.com.mundoviagens.domain.*;
import br.com.mundoviagens.repository.*;
import br.com.mundoviagens.web.Dtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.time.*;
import java.math.*;
import java.util.*;

@Service
@Transactional
public class ReservaService {
    private final UserRepository users;
    private final HotelRepository hotels;
    private final ReservaRepository reservas;
    private final CancelamentoRepository cancelamentos;
    private final FlightService flights;
    private final Clock clock;
    public ReservaService(UserRepository users, HotelRepository hotels, ReservaRepository reservas,
                          CancelamentoRepository cancelamentos, FlightService flights, Clock clock) {
        this.users=users; this.hotels=hotels; this.reservas=reservas;
        this.cancelamentos=cancelamentos; this.flights=flights; this.clock=clock;
    }
    public ReservaResponse create(UUID userId, ReservaRequest request) {
        User user=lockUser(userId);
        if (user.getBloqueado()) throw new ResponseStatusException(FORBIDDEN, "Conta bloqueada para novas reservas");
        var flight=flights.get(request.flightId());
        if (!flight.dataPartida().isAfter(clock.instant())) throw new ResponseStatusException(CONFLICT, "Este voo já partiu");
        Hotel hotel=request.hotelId() == null ? null : hotels.findById(request.hotelId())
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Hotel não encontrado"));
        if (hotel != null && !hotel.getCidade().equalsIgnoreCase(flight.destino()))
            throw new ResponseStatusException(BAD_REQUEST, "Hotel deve estar no destino do voo");
        Reserva r=new Reserva(); r.setUser(user); r.setFlightId(flight.id()); r.setHotel(hotel);
        FlightDetails details=new FlightDetails();
        details.setOrigem(flight.origem()); details.setDestino(flight.destino());
        details.setDataPartida(flight.dataPartida()); details.setDataChegada(flight.dataChegada());
        details.setPreco(flight.preco()); details.setAssentosDisponiveis(flight.assentosDisponiveis());
        r.setFlightDetails(details);
        if (hotel != null) {
            HotelDetails hd=new HotelDetails(); hd.setNome(hotel.getNome()); hd.setCidade(hotel.getCidade());
            hd.setPrecoDiaria(hotel.getPrecoDiaria()); r.setHotelDetails(hd);
        }
        r.setTotal(flight.preco().add(hotel == null ? BigDecimal.ZERO : hotel.getPrecoDiaria().multiply(BigDecimal.valueOf(3))));
        r.setPaymentMethod(request.paymentMethod()); r.setCreatedAt(clock.instant());
        // Pagamento e emissão simulados, conforme o fluxo original.
        r.setStatus(request.paymentMethod() == PaymentMethod.cartao ? ReservaStatus.confirmada : ReservaStatus.pendente_pagamento);
        if (r.getStatus() == ReservaStatus.confirmada) r.setTicketId(issueTicket());
        return ReservaResponse.from(reservas.save(r));
    }
    @Transactional(readOnly=true)
    public List<ReservaResponse> mine(UUID userId) { return reservas.findByUserIdOrderByCreatedAtDesc(userId).stream().map(ReservaResponse::from).toList(); }
    @Transactional(readOnly=true)
    public List<ReservaResponse> all() { return reservas.findAllByOrderByCreatedAtDesc().stream().map(ReservaResponse::from).toList(); }
    public ReservaResponse confirm(UUID id) {
        Reserva r=lockReserva(id);
        if (r.getStatus() == ReservaStatus.cancelada) throw new ResponseStatusException(CONFLICT, "Reserva cancelada não pode ser confirmada");
        if (r.getStatus() == ReservaStatus.confirmada) return ReservaResponse.from(r);
        if (!r.getFlightDetails().getDataPartida().isAfter(clock.instant())) throw new ResponseStatusException(CONFLICT, "Este voo já partiu");
        r.setStatus(ReservaStatus.confirmada); r.setTicketId(issueTicket());
        return ReservaResponse.from(r);
    }
    public ReservaResponse cancel(UUID id, UUID actorId, boolean admin) {
        UUID ownerId=reservas.findOwnerIdById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reserva não encontrada"));
        if (!admin && !ownerId.equals(actorId)) throw new ResponseStatusException(FORBIDDEN, "Reserva pertence a outro usuário");
        // Serializa cancelamentos e criação por usuário, inclusive em requisições simultâneas.
        User user=lockUser(ownerId);
        Reserva r=lockReserva(id);
        if (r.getStatus() == ReservaStatus.cancelada) return ReservaResponse.from(r);
        Instant now=clock.instant(), departure=r.getFlightDetails().getDataPartida();
        if (!departure.isAfter(now)) throw new ResponseStatusException(CONFLICT, "Não é possível cancelar após a partida");
        boolean late=Duration.between(now, departure).compareTo(Duration.ofHours(48)) < 0;
        r.setMulta(late ? r.getTotal().multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        r.setStatus(ReservaStatus.cancelada); r.setTicketId(null);
        Cancelamento cancelamento=new Cancelamento(); cancelamento.setUser(user); cancelamento.setReserva(r); cancelamento.setDataCancelamento(now);
        cancelamentos.saveAndFlush(cancelamento);
        long count=cancelamentos.countByUserIdAndDataCancelamentoGreaterThanAndDataCancelamentoLessThanEqual(ownerId, now.minus(Duration.ofDays(30)), now);
        if (count >= 3) user.setBloqueado(true);
        return ReservaResponse.from(r);
    }
    private User lockUser(UUID id) { return users.findLockedById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário não encontrado")); }
    private Reserva lockReserva(UUID id) { return reservas.findLockedById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reserva não encontrada")); }
    private String issueTicket() { return "TKT-"+UUID.randomUUID(); }
}
