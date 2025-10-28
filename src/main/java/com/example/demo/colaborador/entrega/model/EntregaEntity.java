package com.example.demo.colaborador.entrega.model;

import com.example.demo.colaborador.model.ColaboradorEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "tb_entrega")
public class EntregaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "nota")
    @Min(1) @Max(5)
    private Double nota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula", referencedColumnName = "matricula", nullable = false)
    private ColaboradorEntity colaborador;

    public EntregaEntity() {
    }

    public EntregaEntity(Long id, String descricao, ColaboradorEntity colaboradorEntity) {
        this.id = id;
        this.descricao = descricao;
        this.colaborador = colaboradorEntity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public ColaboradorEntity getColaborador() {
        return colaborador;
    }

    public void setColaborador(ColaboradorEntity colaboradorEntity) {
        this.colaborador = colaboradorEntity;
    }
}