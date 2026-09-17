package br.com.mundoviagens.repository;
import br.com.mundoviagens.domain.Reserva;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.Instant;
import br.com.mundoviagens.domain.ReservaStatus;
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    @EntityGraph(attributePaths={"user", "hotel"})
    List<Reserva> findByUserIdOrderByCreatedAtDesc(UUID userId);
    @EntityGraph(attributePaths={"user", "hotel"})
    List<Reserva> findAllByOrderByCreatedAtDesc();
    @Query("select r.user.id from Reserva r where r.id = :id")
    Optional<UUID> findOwnerIdById(UUID id);
    boolean existsByHotelId(UUID hotelId);
    long countByFlightIdAndStatusNot(String flightId, ReservaStatus status);
    @Query("select r.flightDetails.dataChegada from Reserva r where r.hotel.id = :hotelId and r.status <> br.com.mundoviagens.domain.ReservaStatus.cancelada and r.flightDetails.dataChegada > :earliestArrival and r.flightDetails.dataChegada < :checkout")
    List<Instant> findOverlappingArrivals(UUID hotelId, Instant earliestArrival, Instant checkout);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> findLockedById(UUID id);
}
