package br.com.dbserver.gestao_tarefas.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class TratadorErros {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiError tratarErroValidacao(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                String mensagem = ex.getFieldErrors()
                                .stream()
                                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                .findFirst()
                                .orElse("Erro de validação");

                return new ApiError(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                mensagem,
                                request.getRequestURI(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ApiError tratarRegraNegocio(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {

                return new ApiError(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public ApiError tratarAcessoNegado(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                return new ApiError(
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                "Acesso negado",
                                request.getRequestURI(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(AuthenticationException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public ApiError tratarFalhaAutenticacao(
                        AuthenticationException ex,
                        HttpServletRequest request) {

                return new ApiError(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Falha na autenticação: credenciais inválidas",
                                request.getRequestURI(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ApiError tratarErroGenerico(
                        Exception ex,
                        HttpServletRequest request) {

                return new ApiError(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());
        }
}