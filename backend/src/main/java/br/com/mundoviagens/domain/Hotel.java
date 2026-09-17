package br.com.mundoviagens.domain;

import jakarta.persistence.*;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "hoteis")
public class Hotel {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable=false, length=120)
    private String nome;
    @Column(nullable=false, length=120)
    private String cidade;
    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal precoDiaria;
    @Column(nullable=false)
    private int quartosTotais;
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public BigDecimal getPrecoDiaria() { return precoDiaria; }
    public void setPrecoDiaria(BigDecimal precoDiaria) { this.precoDiaria = precoDiaria; }
    public int getQuartosTotais() { return quartosTotais; }
    public void setQuartosTotais(int quartosTotais) { this.quartosTotais = quartosTotais; }
}
