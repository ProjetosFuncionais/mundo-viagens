package br.com.mundoviagens.service;
import br.com.mundoviagens.domain.Hotel;
import br.com.mundoviagens.repository.*;
import br.com.mundoviagens.web.Dtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.util.*;
@Service
@Transactional
public class HotelService {
    private final HotelRepository hotels;
    private final ReservaRepository reservas;
    public HotelService(HotelRepository hotels, ReservaRepository reservas) { this.hotels=hotels; this.reservas=reservas; }
    @Transactional(readOnly=true)
    public List<HotelResponse> list(String cidade) {
        return (cidade == null || cidade.isBlank() ? hotels.findAllByOrderByNome() : hotels.findByCidadeIgnoreCaseOrderByNome(cidade.strip()))
            .stream().map(HotelResponse::from).toList();
    }
    @Transactional(readOnly=true)
    public HotelResponse get(UUID id) { return HotelResponse.from(find(id)); }
    public HotelResponse create(HotelRequest request) { return save(new Hotel(), request); }
    public HotelResponse update(UUID id, HotelRequest request) { return save(find(id), request); }
    private HotelResponse save(Hotel hotel, HotelRequest request) {
        hotel.setNome(request.nome().strip()); hotel.setCidade(request.cidade().strip());
        hotel.setPrecoDiaria(request.precoDiaria()); hotel.setQuartosTotais(request.quartosTotais());
        return HotelResponse.from(hotels.save(hotel));
    }
    public void delete(UUID id) {
        Hotel hotel=find(id);
        if (reservas.existsByHotelId(id)) throw new ResponseStatusException(CONFLICT, "Hotel vinculado a reservas não pode ser excluído");
        hotels.delete(hotel);
    }
    private Hotel find(UUID id) { return hotels.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Hotel não encontrado")); }
}
