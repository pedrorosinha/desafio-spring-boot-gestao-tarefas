package br.com.dbserver.gestao_tarefas.exception;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String error,
        String mensagem,
        String path,
        LocalDateTime timestamp
) {}