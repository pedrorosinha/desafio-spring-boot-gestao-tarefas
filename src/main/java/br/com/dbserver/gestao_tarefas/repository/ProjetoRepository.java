package br.com.dbserver.gestao_tarefas.repository;

import br.com.dbserver.gestao_tarefas.domain.projeto.Projeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    Page<Projeto> findByAtivo(Boolean ativo, Pageable pageable);

    Page<Projeto> findByGerenteId(Long gerenteId, Pageable pageable);

    Page<Projeto> findByGerenteIdAndAtivo(Long gerenteId, Boolean ativo, Pageable pageable);
}
