package br.com.mundoviagens.service;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import java.text.Normalizer;
import br.com.mundoviagens.repository.*;
import br.com.mundoviagens.domain.ReservaStatus;
import org.springframework.transaction.annotation.Transactional;

/** SkyHigh mock determinístico: a identidade do voo inclui a data da partida. */
@Service
@Transactional(readOnly=true)
public class FlightService {
    public record Flight(String id, String origem, String destino, Instant dataPartida, Instant dataChegada,
                         BigDecimal preco, int assentosDisponiveis) {}
    private record Route(String id, String origem, String destino, int hour, int durationMinutes, String price, int seats) {}
    private static final ZoneId ZONE=ZoneId.of("America/Sao_Paulo");
    private static final List<Route> ROUTES=List.of(
        new Route("FL-001", "São Paulo (GRU)", "Rio de Janeiro (GIG)", 10, 60, "350.00", 120),
        new Route("FL-002", "São Paulo (GRU)", "Salvador (SSA)", 14, 150, "650.00", 50),
        new Route("FL-003", "Rio de Janeiro (GIG)", "Miami (MIA)", 22, 420, "2500.00", 20),
        new Route("FL-004", "São Paulo (GRU)", "Lisboa (LIS)", 18, 720, "3800.00", 15));
    private final Clock clock;
    private final ReservaRepository reservas;
    private final FlightRouteRepository routes;
    public FlightService(Clock clock, ReservaRepository reservas, FlightRouteRepository routes) {
        this.clock=clock; this.reservas=reservas; this.routes=routes;
    }
    @Transactional
    public Flight lockForBooking(String id) {
        Flight flight=get(id);
        routes.findLockedById(flight.id().substring(0, 6))
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Rota não encontrada"));
        return get(id);
    }
    public List<Flight> search(String origem, String destino, LocalDate date) {
        LocalDate day=date == null ? LocalDate.now(clock.withZone(ZONE)).plusDays(7) : date;
        return ROUTES.stream().filter(r -> normalize(r.origem()).contains(normalize(origem)) && normalize(r.destino()).contains(normalize(destino)))
            .map(r -> flight(r, day)).filter(f -> f.dataPartida().isAfter(clock.instant())).toList();
    }
    public Flight get(String id) {
        try {
            String routeId=id.substring(0, 6);
            if (id.charAt(6) != '_' || id.length() != 17) throw new IllegalArgumentException();
            LocalDate date=LocalDate.parse(id.substring(7));
            Route route=ROUTES.stream().filter(r -> r.id().equals(routeId)).findFirst().orElseThrow();
            return flight(route, date);
        } catch (RuntimeException ex) { throw new ResponseStatusException(NOT_FOUND, "Voo não encontrado"); }
    }
    private Flight flight(Route route, LocalDate day) {
        Instant departure=day.atTime(route.hour(), 0).atZone(ZONE).toInstant();
        String id=route.id()+"_"+day;
        int available=(int)Math.max(0, route.seats()-reservas.countByFlightIdAndStatusNot(id, ReservaStatus.cancelada));
        return new Flight(id, route.origem(), route.destino(), departure,
            departure.plusSeconds(route.durationMinutes()*60L), new BigDecimal(route.price()), available);
    }
    private String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value.strip(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
