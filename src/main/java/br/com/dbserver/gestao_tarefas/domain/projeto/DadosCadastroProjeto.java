package br.com.dbserver.gestao_tarefas.domain.projeto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroProjeto(
        @NotBlank String nome,

        @NotBlank String descricao,

        @NotNull Long gerenteId) {
}
