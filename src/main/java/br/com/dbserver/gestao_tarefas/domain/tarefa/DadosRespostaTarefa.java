package br.com.dbserver.gestao_tarefas.domain.tarefa;

import br.com.dbserver.gestao_tarefas.domain.enums.PrioridadeTarefa;
import br.com.dbserver.gestao_tarefas.domain.enums.StatusTarefa;

import java.time.LocalDateTime;

public record DadosRespostaTarefa(
        Long id,
        String titulo,
        String descricao,
        StatusTarefa status,
        PrioridadeTarefa prioridade,
        LocalDateTime dataCriacao,
        LocalDateTime dataConclusao,
        Long projetoId,
        String projetoNome,
        Long responsavelId,
        String responsavelNome
) {
    public DadosRespostaTarefa(Tarefa tarefa) {
        this(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getPrioridade(),
                tarefa.getDataCriacao(),
                tarefa.getDataConclusao(),
                tarefa.getProjeto().getId(),
                tarefa.getProjeto().getNome(),
                tarefa.getResponsavel().getId(),
                tarefa.getResponsavel().getNome()
        );
    }
}
