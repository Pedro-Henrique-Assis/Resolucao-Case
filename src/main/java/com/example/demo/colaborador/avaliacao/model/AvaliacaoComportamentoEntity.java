package com.example.demo.colaborador.avaliacao.model;

import com.example.demo.colaborador.model.ColaboradorEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "tb_avaliacao_comportamental")
public class AvaliacaoComportamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nota_ambiente_colaborativo")
    @Min(1) @Max(5)
    private Double notaAvaliacaoComportamental;

    @Column(name = "nota_aprendizado")
    @Min(1) @Max(5)
    private Double notaAprendizado;

    @Column(name = "nota_tomada_decisao")
    @Min(1) @Max(5)
    private Double notaTomadaDecisao;

    @Column(name = "nota_autonomia")
    @Min(1) @Max(5)
    private Double notaAutonomia;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula", referencedColumnName = "matricula", nullable = false)
    private ColaboradorEntity colaborador;

    public AvaliacaoComportamentoEntity() {
    }

    public AvaliacaoComportamentoEntity(Long id, Double notaAvaliacaoComportamental, Double notaAprendizado, Double notaTomadaDecisao, Double notaAutonomia) {
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

    public Double getNotaAvaliacaoComportamental() {
        return notaAvaliacaoComportamental;
    }

    public void setNotaAvaliacaoComportamental(Double notaAvaliacaoComportamental) {
        this.notaAvaliacaoComportamental = notaAvaliacaoComportamental;
    }

    public Double getNotaAprendizado() {
        return notaAprendizado;
    }

    public void setNotaAprendizado(Double notaAprendizado) {
        this.notaAprendizado = notaAprendizado;
    }

    public Double getNotaTomadaDecisao() {
        return notaTomadaDecisao;
    }

    public void setNotaTomadaDecisao(Double notaTomadaDecisao) {
        this.notaTomadaDecisao = notaTomadaDecisao;
    }

    public Double getNotaAutonomia() {
        return notaAutonomia;
    }

    public void setNotaAutonomia(Double notaAutonomia) {
        this.notaAutonomia = notaAutonomia;
    }

    public ColaboradorEntity getColaborador() {
        return colaborador;
    }

    public void setColaborador(ColaboradorEntity colaboradorEntity) {
        this.colaborador = colaboradorEntity;
    }
}