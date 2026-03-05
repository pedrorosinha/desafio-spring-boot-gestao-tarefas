package br.com.dbserver.gestao_tarefas.domain.usuario;

import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;

public record DadosRespostaUsuario(
        Long id,
        String nome,
        String email,
        RoleUsuario role,
        Boolean ativo
) {
}
