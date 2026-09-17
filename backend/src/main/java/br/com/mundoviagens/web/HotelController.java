package br.com.mundoviagens.web;
import br.com.mundoviagens.service.*;
import br.com.mundoviagens.web.Dtos.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/hoteis")
public class HotelController {
    private final HotelService hotels;
    public HotelController(HotelService hotels) { this.hotels=hotels; }
    @GetMapping public List<HotelResponse> list(@RequestParam(required=false) String cidade) { return hotels.list(cidade); }
    @GetMapping("/{id}") public HotelResponse get(@PathVariable UUID id) { return hotels.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'DONA')")
    public HotelResponse create(@Valid @RequestBody HotelRequest request) { return hotels.create(request); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'DONA')")
    public HotelResponse update(@PathVariable UUID id, @Valid @RequestBody HotelRequest request) { return hotels.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'DONA')")
    public void delete(@PathVariable UUID id) { hotels.delete(id); }
}
