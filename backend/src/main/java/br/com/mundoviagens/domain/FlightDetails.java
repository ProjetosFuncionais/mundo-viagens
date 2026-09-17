package br.com.mundoviagens.domain;

import jakarta.persistence.*;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

@Embeddable
public class FlightDetails {
    @Column(name="flight_origem", nullable=false)
    private String origem;
    @Column(name="flight_destino", nullable=false)
    private String destino;
    @Column(name="flight_partida", nullable=false)
    private Instant dataPartida;
    @Column(name="flight_chegada", nullable=false)
    private Instant dataChegada;
    @Column(name="flight_preco", nullable=false, precision=12, scale=2)
    private BigDecimal preco;
    @Column(name="flight_assentos", nullable=false)
    private int assentosDisponiveis;
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public Instant getDataPartida() { return dataPartida; }
    public void setDataPartida(Instant dataPartida) { this.dataPartida = dataPartida; }
    public Instant getDataChegada() { return dataChegada; }
    public void setDataChegada(Instant dataChegada) { this.dataChegada = dataChegada; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public int getAssentosDisponiveis() { return assentosDisponiveis; }
    public void setAssentosDisponiveis(int assentosDisponiveis) { this.assentosDisponiveis = assentosDisponiveis; }
}
