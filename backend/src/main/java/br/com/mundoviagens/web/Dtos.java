package br.com.mundoviagens.web;
import br.com.mundoviagens.domain.*;
import jakarta.validation.constraints.*;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

public final class Dtos {
    private Dtos() {}
    public record RegisterRequest(@NotBlank @Size(max=120) String nome,
        @NotBlank @Pattern(regexp="[0-9]{11}", message="deve conter 11 dígitos") String cpf,
        @NotBlank @Email @Size(max=254) String email,
        @NotBlank @Size(min=8, max=72) String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank @Size(max=72) String password) {}
    public record UserResponse(UUID id, String nome, String cpf, String email, Role role, boolean bloqueado) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getNome(), u.getCpf(), u.getEmail(), u.getRole(), u.getBloqueado());
        }
    }
    public record AuthResponse(String token, Instant expiresAt, UserResponse user) {}
    public record HotelRequest(@NotBlank @Size(max=120) String nome, @NotBlank @Size(max=120) String cidade,
        @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal precoDiaria,
        @NotNull @Min(1) Integer quartosTotais) {}
    public record HotelResponse(UUID id, String nome, String cidade, BigDecimal precoDiaria, int quartosTotais) {
        public static HotelResponse from(Hotel h) {
            return new HotelResponse(h.getId(), h.getNome(), h.getCidade(), h.getPrecoDiaria(), h.getQuartosTotais());
        }
    }
    public record ReservaRequest(@NotBlank @Size(max=40) String flightId, UUID hotelId, @NotNull PaymentMethod paymentMethod) {}
    public record ReservaResponse(UUID id, UUID userId, String flightId, UUID hotelId,
        FlightDetails flightDetails, HotelDetails hotelDetails, PaymentMethod paymentMethod,
        ReservaStatus status, BigDecimal total, Instant createdAt, BigDecimal multa, String ticketId) {
        public static ReservaResponse from(Reserva r) {
            return new ReservaResponse(r.getId(), r.getUser().getId(), r.getFlightId(),
                r.getHotel() == null ? null : r.getHotel().getId(), r.getFlightDetails(), r.getHotelDetails(),
                r.getPaymentMethod(), r.getStatus(), r.getTotal(), r.getCreatedAt(), r.getMulta(), r.getTicketId());
        }
    }
    public record BloqueioRequest(@NotNull Boolean bloqueado) {}
}
