package br.com.mundoviagens.repository;

import br.com.mundoviagens.domain.FlightRoute;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface FlightRouteRepository extends JpaRepository<FlightRoute, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from FlightRoute r where r.id = :id")
    Optional<FlightRoute> findLockedById(String id);
}
