package com.example.demo.colaborador.entrega.repository;

import com.example.demo.colaborador.entrega.model.EntregaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntregaRepository extends JpaRepository<EntregaEntity, Long> {
}
