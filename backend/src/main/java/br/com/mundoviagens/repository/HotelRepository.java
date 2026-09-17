package br.com.mundoviagens.repository;
import br.com.mundoviagens.domain.Hotel;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface HotelRepository extends JpaRepository<Hotel, UUID> {
    List<Hotel> findAllByOrderByNome();
    List<Hotel> findByCidadeIgnoreCaseOrderByNome(String cidade);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hotel h where h.id = :id")
    Optional<Hotel> findLockedById(UUID id);
}
