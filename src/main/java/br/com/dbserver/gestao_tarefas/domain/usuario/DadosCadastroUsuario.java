package br.com.dbserver.gestao_tarefas.domain.usuario;

import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroUsuario (
        @NotBlank
        String nome,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String senha,

        @NotNull
        RoleUsuario role
) {}
