package br.com.dbserver.gestao_tarefas.domain.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DadosLogin(
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email deve ser válido") String email,

        @NotBlank(message = "Senha é obrigatória") String senha) {
}
