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
@RequestMapping("/api/clientes")
@PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'DONA')")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users=users; }
    @GetMapping public List<UserResponse> customers() { return users.customers(); }
    @PatchMapping("/{id}/bloqueio")
    public UserResponse block(@PathVariable UUID id, @Valid @RequestBody BloqueioRequest request) {
        return users.block(id, request.bloqueado());
    }
}
