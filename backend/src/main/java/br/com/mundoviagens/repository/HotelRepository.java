package br.com.mundoviagens.repository;
import br.com.mundoviagens.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface HotelRepository extends JpaRepository<Hotel, UUID> {
    List<Hotel> findAllByOrderByNome();
    List<Hotel> findByCidadeIgnoreCaseOrderByNome(String cidade);
}
