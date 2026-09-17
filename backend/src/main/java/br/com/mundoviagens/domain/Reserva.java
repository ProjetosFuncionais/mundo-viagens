package br.com.mundoviagens.domain;

import jakarta.persistence.*;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reservas")
public class Reserva {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id", nullable=false)
    private User user;
    @Column(nullable=false, length=40)
    private String flightId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="hotel_id")
    private Hotel hotel;
    @Embedded
    private FlightDetails flightDetails;
    @Embedded
    private HotelDetails hotelDetails;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30)
    private ReservaStatus status;
    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal total;
    @Column(nullable=false)
    private Instant createdAt;
    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal multa = BigDecimal.ZERO;
    @Column(length=50)
    private String ticketId;
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
    public FlightDetails getFlightDetails() { return flightDetails; }
    public void setFlightDetails(FlightDetails flightDetails) { this.flightDetails = flightDetails; }
    public HotelDetails getHotelDetails() { return hotelDetails; }
    public void setHotelDetails(HotelDetails hotelDetails) { this.hotelDetails = hotelDetails; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public ReservaStatus getStatus() { return status; }
    public void setStatus(ReservaStatus status) { this.status = status; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public BigDecimal getMulta() { return multa; }
    public void setMulta(BigDecimal multa) { this.multa = multa; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
}
