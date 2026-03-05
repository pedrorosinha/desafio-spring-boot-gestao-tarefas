package br.com.dbserver.gestao_tarefas.domain.tarefa;

import br.com.dbserver.gestao_tarefas.domain.enums.PrioridadeTarefa;
import br.com.dbserver.gestao_tarefas.domain.enums.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroTarefa(
        @NotBlank(message = "Título é obrigatório")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        StatusTarefa status,

        @NotNull(message = "Prioridade é obrigatória")
        PrioridadeTarefa prioridade,

        @NotNull(message = "Projeto é obrigatório")
        Long projetoId,

        @NotNull(message = "Responsável é obrigatório")
        Long responsavelId
) {
}
