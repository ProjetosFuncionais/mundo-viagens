package br.com.mundoviagens.domain;

import jakarta.persistence.*;

@Entity
@Table(name="flight_routes")
public class FlightRoute {
    @Id @Column(length=6)
    private String id;
    public String getId() { return id; }
}
