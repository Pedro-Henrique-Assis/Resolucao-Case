package com.example.demo.colaborador.model;

import com.example.demo.colaborador.avaliacao.model.AvaliacaoComportamentoEntity;
import com.example.demo.colaborador.entrega.model.EntregaEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_colaborador")
public class ColaboradorEntity {

    // Utiliza UUID para ser único globalmente e para melhor performance de escrita
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID matricula;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @Column(name = "cargo", nullable = false)
    private String cargo;

    // Garante a integridade dos dados em caso de exclusão de um colaborador
    @OneToOne(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AvaliacaoComportamentoEntity avaliacaoComportamentoEntity;

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EntregaEntity> entregases = new ArrayList<>();

    public ColaboradorEntity() {
    }

    public ColaboradorEntity(String nome, LocalDate dataAdmissao, String cargo) {
        this.matricula = matricula;
        this.nome = nome;
        this.dataAdmissao = dataAdmissao;
        this.cargo = cargo;
    }

    public ColaboradorEntity(UUID matricula, String nome, LocalDate dataAdmissao, String cargo) {
        this.matricula = matricula;
        this.nome = nome;
        this.dataAdmissao = dataAdmissao;
        this.cargo = cargo;
    }

    public UUID getMatricula() {
        return matricula;
    }

    public void setMatricula(UUID matricula) {
        this.matricula = matricula;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public List<EntregaEntity> getEntregas() {
        return entregases;
    }

    public void setEntregas(List<EntregaEntity> entregases) {
        this.entregases = entregases;
    }

    public AvaliacaoComportamentoEntity getAvaliacaoComportamento() {
        return avaliacaoComportamentoEntity;
    }

    // Garante a persistência de dados entre Colaborador e AvaliacaoComportamental
    public void setAvaliacaoComportamento(AvaliacaoComportamentoEntity avaliacaoComportamentoEntity) {
        this.avaliacaoComportamentoEntity = avaliacaoComportamentoEntity;

        if (avaliacaoComportamentoEntity != null) {
            avaliacaoComportamentoEntity.setColaborador(this);
        }
    }

    public void adicionarEntrega(EntregaEntity entregaEntity) {
        entregases.add(entregaEntity);
        entregaEntity.setColaborador(this);
    }
}