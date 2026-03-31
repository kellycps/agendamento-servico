package com.fiap.agendamento_servico.infrastructure.persistence.repositories;

import com.fiap.agendamento_servico.infrastructure.persistence.entities.EstabelecimentoEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaEstabelecimentoRepository extends JpaRepository<EstabelecimentoEntity, UUID> {

    List<EstabelecimentoEntity> findByEnderecoCidadeIgnoreCase(String cidade);

    @Query("SELECT DISTINCT e FROM EstabelecimentoEntity e LEFT JOIN e.servicos s "
            + "WHERE (:nome IS NULL OR LOWER(e.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) "
            + "AND (:cidade IS NULL OR LOWER(e.endereco.cidade) = LOWER(:cidade)) "
            + "AND (:avaliacaoMinima IS NULL OR e.notaMedia >= :avaliacaoMinima) "
            + "AND ((:precoMinimo IS NULL AND :precoMaximo IS NULL) "
            + "     OR (s.preco >= COALESCE(:precoMinimo, 0) AND s.preco <= COALESCE(:precoMaximo, 9999999))) "
            + "ORDER BY e.notaMedia DESC")
            
    List<EstabelecimentoEntity> filtrar(
            @Param("nome") String nome,
            @Param("cidade") String cidade,
            @Param("avaliacaoMinima") Double avaliacaoMinima,
            @Param("precoMinimo") Double precoMinimo,
            @Param("precoMaximo") Double precoMaximo);
}