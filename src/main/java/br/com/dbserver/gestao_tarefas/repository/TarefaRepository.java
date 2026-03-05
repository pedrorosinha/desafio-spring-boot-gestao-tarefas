package br.com.dbserver.gestao_tarefas.repository;

import br.com.dbserver.gestao_tarefas.domain.enums.StatusTarefa;
import br.com.dbserver.gestao_tarefas.domain.tarefa.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    
    Page<Tarefa> findByProjetoId(Long projetoId, Pageable pageable);
    
    Page<Tarefa> findByResponsavelId(Long responsavelId, Pageable pageable);
    
    Page<Tarefa> findByStatus(StatusTarefa status, Pageable pageable);
    
    Page<Tarefa> findByProjetoIdAndStatus(Long projetoId, StatusTarefa status, Pageable pageable);
}
