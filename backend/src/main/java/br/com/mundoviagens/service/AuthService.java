package br.com.mundoviagens.service;
import br.com.mundoviagens.domain.User;
import br.com.mundoviagens.repository.UserRepository;
import br.com.mundoviagens.web.Dtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    private final Clock clock;
    private final long ttl;
    private final String dummyHash;
    public AuthService(UserRepository users, PasswordEncoder passwords, JwtEncoder encoder, Clock clock,
                       @Value("${app.jwt.ttl-seconds}") long ttl) {
        this.users=users; this.passwords=passwords; this.encoder=encoder; this.clock=clock; this.ttl=ttl;
        this.dummyHash=passwords.encode(UUID.randomUUID().toString());
    }
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email=request.email().strip().toLowerCase(Locale.ROOT);
        if (users.findByEmail(email).isPresent() || users.existsByCpf(request.cpf()))
            throw new ResponseStatusException(CONFLICT, "E-mail ou CPF já cadastrado");
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(BAD_REQUEST, "Senha deve ter no máximo 72 bytes");
        User user=new User();
        user.setNome(request.nome().strip()); user.setCpf(request.cpf()); user.setEmail(email);
        user.setPassword(passwords.encode(request.password()));
        return token(users.saveAndFlush(user));
    }
    @Transactional(readOnly=true)
    public AuthResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(UNAUTHORIZED, "E-mail ou senha inválidos");
        var user=users.findByEmail(request.email().strip().toLowerCase(Locale.ROOT));
        boolean matches=passwords.matches(request.password(), user.map(User::getPassword).orElse(dummyHash));
        if (user.isEmpty() || !matches) throw new ResponseStatusException(UNAUTHORIZED, "E-mail ou senha inválidos");
        return token(user.get());
    }
    private AuthResponse token(User user) {
        Instant now=clock.instant(), expiry=now.plusSeconds(ttl);
        var claims=JwtClaimsSet.builder().issuer("mundo-viagens").subject(user.getId().toString())
            .issuedAt(now).expiresAt(expiry).claim("role", user.getRole().name()).build();
        String token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResponse(token, expiry, UserResponse.from(user));
    }
    @Transactional(readOnly=true)
    public UserResponse me(UUID id) {
        return UserResponse.from(users.findById(id).orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Usuário não encontrado")));
    }
}
