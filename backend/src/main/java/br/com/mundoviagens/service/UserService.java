package br.com.mundoviagens.service;
import br.com.mundoviagens.domain.*;
import br.com.mundoviagens.repository.UserRepository;
import br.com.mundoviagens.web.Dtos.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.util.*;
@Service
public class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) { this.users=users; }
    @Transactional(readOnly=true)
    public List<UserResponse> customers() { return users.findByRoleOrderByNome(Role.CLIENTE).stream().map(UserResponse::from).toList(); }
    @Transactional
    public UserResponse block(UUID id, boolean blocked) {
        User user=users.findLockedById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cliente não encontrado"));
        if (user.getRole() != Role.CLIENTE) throw new ResponseStatusException(BAD_REQUEST, "Somente clientes podem ser gerenciados por esta operação");
        user.setBloqueado(blocked); return UserResponse.from(user);
    }
}
