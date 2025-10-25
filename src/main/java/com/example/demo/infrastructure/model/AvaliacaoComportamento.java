package com.example.demo.infrastructure.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "tb_avaliacao_comportamental")
public class AvaliacaoComportamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nota_ambiente_colaborativo")
    @Min(1) @Max(5)
    private byte notaAvaliacaoComportamental;

    @Column(name = "nota_aprendizado")
    @Min(1) @Max(5)
    private byte notaAprendizado;

    @Column(name = "nota_tomada_decisao")
    @Min(1) @Max(5)
    private byte notaTomadaDecisao;

    @Column(name = "nota_autonomia")
    @Min(1) @Max(5)
    private byte notaAutonomia;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula", referencedColumnName = "matricula", nullable = false)
    private Colaborador colaborador;

    public AvaliacaoComportamento() {
    }

    public AvaliacaoComportamento(Long id, byte notaAvaliacaoComportamental, byte notaAprendizado, byte notaTomadaDecisao, byte notaAutonomia) {
        this.id = id;
        this.notaAvaliacaoComportamental = notaAvaliacaoComportamental;
        this.notaAprendizado = notaAprendizado;
        this.notaTomadaDecisao = notaTomadaDecisao;
        this.notaAutonomia = notaAutonomia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte getNotaAvaliacaoComportamental() {
        return notaAvaliacaoComportamental;
    }

    public void setNotaAvaliacaoComportamental(byte notaAvaliacaoComportamental) {
        this.notaAvaliacaoComportamental = notaAvaliacaoComportamental;
    }

    public byte getNotaAprendizado() {
        return notaAprendizado;
    }

    public void setNotaAprendizado(byte notaAprendizado) {
        this.notaAprendizado = notaAprendizado;
    }

    public byte getNotaTomadaDecisao() {
        return notaTomadaDecisao;
    }

    public void setNotaTomadaDecisao(byte notaTomadaDecisao) {
        this.notaTomadaDecisao = notaTomadaDecisao;
    }

    public byte getNotaAutonomia() {
        return notaAutonomia;
    }

    public void setNotaAutonomia(byte notaAutonomia) {
        this.notaAutonomia = notaAutonomia;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public void setColaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
    }
}
