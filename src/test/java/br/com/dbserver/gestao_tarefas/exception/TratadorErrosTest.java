package br.com.dbserver.gestao_tarefas.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TratadorErros - Testes Unitários")
class TratadorErrosTest {

    @InjectMocks
    private TratadorErros tratadorErros;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException validationException;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("Deve tratar erro de validação com sucesso")
    void testTratarErroValidacao() {
        FieldError fieldError = new FieldError("objeto", "campo", "mensagem de erro");
        when(validationException.getFieldErrors()).thenReturn(List.of(fieldError));

        ApiError resultado = tratadorErros.tratarErroValidacao(validationException, request);

        assertNotNull(resultado);
        assertEquals(HttpStatus.BAD_REQUEST.value(), resultado.status());
        assertEquals("Bad Request", resultado.error());
        assertEquals("campo: mensagem de erro", resultado.mensagem());
        assertEquals("/api/test", resultado.path());
        assertNotNull(resultado.timestamp());
    }

    @Test
    @DisplayName("Deve tratar erro de validação sem campos")
    void testTratarErroValidacaoSemCampos() {
        when(validationException.getFieldErrors()).thenReturn(List.of());

        ApiError resultado = tratadorErros.tratarErroValidacao(validationException, request);

        assertNotNull(resultado);
        assertEquals("Erro de validação", resultado.mensagem());
    }

    @Test
    @DisplayName("Deve tratar múltiplos erros de validação")
    void testTratarMultiplosErrosValidacao() {
        FieldError erro1 = new FieldError("objeto", "nome", "não pode ser vazio");
        FieldError erro2 = new FieldError("objeto", "email", "formato inválido");
        when(validationException.getFieldErrors()).thenReturn(List.of(erro1, erro2));

        ApiError resultado = tratadorErros.tratarErroValidacao(validationException, request);

        assertNotNull(resultado);
        assertEquals("nome: não pode ser vazio", resultado.mensagem());
    }

    @Test
    @DisplayName("Deve tratar erro genérico")
    void testTratarErroGenerico() {
        Exception exception = new Exception("Erro inesperado");

        ApiError resultado = tratadorErros.tratarErroGenerico(exception, request);

        assertNotNull(resultado);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), resultado.status());
        assertEquals("Internal Server Error", resultado.error());
        assertEquals("Erro inesperado", resultado.mensagem());
        assertEquals("/api/test", resultado.path());
        assertNotNull(resultado.timestamp());
    }

    @Test
    @DisplayName("Deve tratar RuntimeException")
    void testTratarRuntimeException() {
        RuntimeException exception = new RuntimeException("Erro de runtime");

        ApiError resultado = tratadorErros.tratarErroGenerico(exception, request);

        assertNotNull(resultado);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), resultado.status());
        assertEquals("Erro de runtime", resultado.mensagem());
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException")
    void testTratarIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        ApiError resultado = tratadorErros.tratarRegraNegocio(exception, request);

        assertNotNull(resultado);
        assertEquals(400, resultado.status());
        assertEquals("Bad Request", resultado.error());
        assertEquals("Argumento inválido", resultado.mensagem());
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException com 403")
    void testTratarAcessoNegado() {
        AccessDeniedException exception = new AccessDeniedException("Sem permissão");

        ApiError resultado = tratadorErros.tratarAcessoNegado(exception, request);

        assertNotNull(resultado);
        assertEquals(HttpStatus.FORBIDDEN.value(), resultado.status());
        assertEquals("Forbidden", resultado.error());
        assertEquals("Acesso negado", resultado.mensagem());
        assertEquals("/api/test", resultado.path());
    }

    @Test
    @DisplayName("Deve incluir timestamp correto no erro")
    void testIncluirTimestampCorreto() {
        Exception exception = new Exception("Erro teste");

        ApiError resultado = tratadorErros.tratarErroGenerico(exception, request);

        assertNotNull(resultado.timestamp());
        assertTrue(resultado.timestamp().isBefore(java.time.LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Deve manter path da requisição no erro")
    void testManterPathDaRequisicao() {
        when(request.getRequestURI()).thenReturn("/usuarios/cadastrar");
        Exception exception = new Exception("Erro");

        ApiError resultado = tratadorErros.tratarErroGenerico(exception, request);

        assertEquals("/usuarios/cadastrar", resultado.path());
    }

    @Test
    @DisplayName("Deve tratar erro com mensagem null")
    void testTratarErroComMensagemNull() {
        Exception exception = new Exception((String) null);

        ApiError resultado = tratadorErros.tratarErroGenerico(exception, request);

        assertNotNull(resultado);
        assertNull(resultado.mensagem());
    }

    @Test
    @DisplayName("Deve verificar estrutura completa do ApiError")
    void testVerificarEstruturaCompletaApiError() {
        FieldError fieldError = new FieldError("usuario", "email", "não pode ser vazio");
        when(validationException.getFieldErrors()).thenReturn(List.of(fieldError));

        ApiError resultado = tratadorErros.tratarErroValidacao(validationException, request);

        assertEquals(400, resultado.status());
        assertEquals("Bad Request", resultado.error());
        assertEquals("email: não pode ser vazio", resultado.mensagem());
        assertEquals("/api/test", resultado.path());
        assertNotNull(resultado.timestamp());
    }
}
