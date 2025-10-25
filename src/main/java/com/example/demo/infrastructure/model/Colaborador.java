package com.example.demo.infrastructure.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_colaborador")
public class Colaborador {

    // Utiliza UUID para ser único globalmente e para melhor performance de escrita
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID matricula;

    @Column(name = "nome")
    private String nome;

    @Column(name = "data_admissao")
    private LocalDate dataAdmissao;

    @Column(name = "cargo")
    private String cargo;

    // Garante a integridade dos dados em caso de exclusão de um colaborador
    @OneToOne(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AvaliacaoComportamento avaliacaoComportamento;

    public Colaborador() {
    }

    public Colaborador(UUID matricula, String nome, LocalDate dataAdmissao, String cargo) {
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

    public void setNome(String nome) {
        this.nome = nome;
    }

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

    public AvaliacaoComportamento getAvaliacaoComportamento() {
        return avaliacaoComportamento;
    }

    // Garante a persistência de dados entre Colaborador e AvaliacaoComportamental
    public void setAvaliacaoComportamento(AvaliacaoComportamento avaliacaoComportamento) {
        this.avaliacaoComportamento = avaliacaoComportamento;

        if (avaliacaoComportamento != null) {
            avaliacaoComportamento.setColaborador(this);
        }
    }
}
