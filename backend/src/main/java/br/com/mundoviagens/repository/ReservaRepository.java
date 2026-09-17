package br.com.mundoviagens.repository;
import br.com.mundoviagens.domain.Reserva;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    @EntityGraph(attributePaths={"user", "hotel"})
    List<Reserva> findByUserIdOrderByCreatedAtDesc(UUID userId);
    @EntityGraph(attributePaths={"user", "hotel"})
    List<Reserva> findAllByOrderByCreatedAtDesc();
    @Query("select r.user.id from Reserva r where r.id = :id")
    Optional<UUID> findOwnerIdById(UUID id);
    boolean existsByHotelId(UUID hotelId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> findLockedById(UUID id);
}
