package br.com.mundoviagens.domain;

import jakarta.persistence.*;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cancelamentos")
public class Cancelamento {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id", nullable=false)
    private User user;
    @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="reserva_id", nullable=false, unique=true)
    private Reserva reserva;
    @Column(nullable=false)
    private Instant dataCancelamento;
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
    public Instant getDataCancelamento() { return dataCancelamento; }
    public void setDataCancelamento(Instant dataCancelamento) { this.dataCancelamento = dataCancelamento; }
}
