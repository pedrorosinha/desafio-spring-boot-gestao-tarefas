package br.com.dbserver.gestao_tarefas.domain.projeto;

import java.time.LocalDateTime;

public record DadosRespostaProjeto(
        Long id,
        String nome,
        String descricao,
        LocalDateTime dataCriacao,
        Boolean ativo,
        Long gerenteId,
        String gerenteNome) {
}
