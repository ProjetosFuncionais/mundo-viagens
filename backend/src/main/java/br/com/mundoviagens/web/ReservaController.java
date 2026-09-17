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
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService reservas;
    public ReservaController(ReservaService reservas) { this.reservas=reservas; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse create(Authentication auth, @Valid @RequestBody ReservaRequest request) {
        return reservas.create(UUID.fromString(auth.getName()), request);
    }
    @GetMapping("/minhas") public List<ReservaResponse> mine(Authentication auth) { return reservas.mine(UUID.fromString(auth.getName())); }
    @GetMapping @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'DONA')")
    public List<ReservaResponse> all() { return reservas.all(); }
    @PostMapping("/{id}/confirmar-pagamento") @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'DONA')")
    public ReservaResponse confirm(@PathVariable UUID id) { return reservas.confirm(id); }
    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancel(@PathVariable UUID id, Authentication auth) {
        boolean admin=auth.getAuthorities().stream().anyMatch(a -> Set.of("ROLE_ATENDENTE", "ROLE_GERENTE", "ROLE_DONA").contains(a.getAuthority()));
        return reservas.cancel(id, UUID.fromString(auth.getName()), admin);
    }
}
