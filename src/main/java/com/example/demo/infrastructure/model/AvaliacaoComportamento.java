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
    private Byte notaAvaliacaoComportamental;

    @Column(name = "nota_aprendizado")
    @Min(1) @Max(5)
    private Byte notaAprendizado;

    @Column(name = "nota_tomada_decisao")
    @Min(1) @Max(5)
    private Byte notaTomadaDecisao;

    @Column(name = "nota_autonomia")
    @Min(1) @Max(5)
    private Byte notaAutonomia;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula", referencedColumnName = "matricula", nullable = false)
    private Colaborador colaborador;

    public AvaliacaoComportamento() {
    }

    public AvaliacaoComportamento(Long id, Byte notaAvaliacaoComportamental, Byte notaAprendizado, Byte notaTomadaDecisao, Byte notaAutonomia) {
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

    public Byte getNotaAvaliacaoComportamental() {
        return notaAvaliacaoComportamental;
    }

    public void setNotaAvaliacaoComportamental(Byte notaAvaliacaoComportamental) {
        this.notaAvaliacaoComportamental = notaAvaliacaoComportamental;
    }

    public Byte getNotaAprendizado() {
        return notaAprendizado;
    }

    public void setNotaAprendizado(Byte notaAprendizado) {
        this.notaAprendizado = notaAprendizado;
    }

    public Byte getNotaTomadaDecisao() {
        return notaTomadaDecisao;
    }

    public void setNotaTomadaDecisao(Byte notaTomadaDecisao) {
        this.notaTomadaDecisao = notaTomadaDecisao;
    }

    public Byte getNotaAutonomia() {
        return notaAutonomia;
    }

    public void setNotaAutonomia(Byte notaAutonomia) {
        this.notaAutonomia = notaAutonomia;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public void setColaborador(Colaborador colaborador) {
        this.colaborador = colaborador;
    }
}
