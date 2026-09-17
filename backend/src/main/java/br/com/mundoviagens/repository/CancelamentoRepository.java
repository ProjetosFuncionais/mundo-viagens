package br.com.mundoviagens.repository;
import br.com.mundoviagens.domain.Cancelamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.time.Instant;
public interface CancelamentoRepository extends JpaRepository<Cancelamento, UUID> {
    long countByUserIdAndDataCancelamentoGreaterThanAndDataCancelamentoLessThanEqual(UUID userId, Instant cutoff, Instant now);
}
