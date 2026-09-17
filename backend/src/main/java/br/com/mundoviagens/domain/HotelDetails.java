package br.com.mundoviagens.domain;

import jakarta.persistence.*;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

@Embeddable
public class HotelDetails {
    @Column(name="hotel_nome")
    private String nome;
    @Column(name="hotel_cidade")
    private String cidade;
    @Column(name="hotel_preco_diaria", precision=12, scale=2)
    private BigDecimal precoDiaria;
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public BigDecimal getPrecoDiaria() { return precoDiaria; }
    public void setPrecoDiaria(BigDecimal precoDiaria) { this.precoDiaria = precoDiaria; }
}
