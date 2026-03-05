package br.com.dbserver.gestao_tarefas.controller;

import br.com.dbserver.gestao_tarefas.domain.enums.PrioridadeTarefa;
import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.enums.StatusTarefa;
import br.com.dbserver.gestao_tarefas.domain.projeto.Projeto;
import br.com.dbserver.gestao_tarefas.domain.tarefa.Tarefa;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.exception.TratadorErros;
import br.com.dbserver.gestao_tarefas.security.CustomUserDetails;
import br.com.dbserver.gestao_tarefas.service.TarefaService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TarefaController - Testes")
class TarefaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TarefaService tarefaService;

    private Usuario usuarioAdmin;
    private Projeto projeto;
    private Tarefa tarefa;
    private String jwtSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private Long jwtExpiration = 86400000L;

    @BeforeEach
    void setUp() {
        TarefaController controller = new TarefaController(tarefaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TratadorErros())
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();

        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNome("Admin User");
        usuarioAdmin.setEmail("admin@email.com");
        usuarioAdmin.setRole(RoleUsuario.ADMIN);
        usuarioAdmin.setAtivo(true);

        Usuario responsavel = new Usuario();
        responsavel.setId(2L);
        responsavel.setNome("Colaborador");
        responsavel.setRole(RoleUsuario.COLABORADOR);

        projeto = new Projeto();
        projeto.setId(1L);
        projeto.setNome("Projeto Teste");
        projeto.setAtivo(true);

        tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Implementar funcionalidade X");
        tarefa.setDescricao("Descricao da tarefa");
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setPrioridade(PrioridadeTarefa.ALTA);
        tarefa.setDataCriacao(LocalDateTime.now());
        tarefa.setProjeto(projeto);
        tarefa.setResponsavel(responsavel);

        autenticarComo(usuarioAdmin);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        CustomUserDetails details = new CustomUserDetails(usuario);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                details,
                null,
                details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String gerarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(claims)
                .subject(usuario.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("GET /tarefas - Deve listar todas as tarefas")
    void testListarTarefas() throws Exception {
        Page<Tarefa> page = new PageImpl<>(List.of(tarefa), PageRequest.of(0, 10), 1);

        when(tarefaService.listar(any())).thenReturn(page);

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/tarefas")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo", is("Implementar funcionalidade X")))
                .andExpect(jsonPath("$.content[0].status", is("PENDENTE")));

        verify(tarefaService, times(1)).listar(any());
    }

    @Test
    @DisplayName("GET /tarefas/projeto/{projetoId} - Deve listar tarefas por projeto")
    void testListarTarefasPorProjeto() throws Exception {
        Page<Tarefa> page = new PageImpl<>(List.of(tarefa), PageRequest.of(0, 10), 1);

        when(tarefaService.listarPorProjeto(eq(1L), any())).thenReturn(page);

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/tarefas/projeto/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].projetoId", is(1)));

        verify(tarefaService, times(1)).listarPorProjeto(eq(1L), any());
    }

    @Test
    @DisplayName("GET /tarefas/responsavel/{responsavelId} - Deve listar tarefas por responsavel")
    void testListarTarefasPorResponsavel() throws Exception {
        Page<Tarefa> page = new PageImpl<>(List.of(tarefa), PageRequest.of(0, 10), 1);

        when(tarefaService.listarPorResponsavel(eq(2L), any())).thenReturn(page);

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/tarefas/responsavel/2")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].responsavelId", is(2)));

        verify(tarefaService, times(1)).listarPorResponsavel(eq(2L), any());
    }

    @Test
    @DisplayName("GET /tarefas/status/{status} - Deve listar tarefas por status")
    void testListarTarefasPorStatus() throws Exception {
        Page<Tarefa> page = new PageImpl<>(List.of(tarefa), PageRequest.of(0, 10), 1);

        when(tarefaService.listarPorStatus(eq(StatusTarefa.PENDENTE), any())).thenReturn(page);

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/tarefas/status/PENDENTE")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("PENDENTE")));

        verify(tarefaService, times(1)).listarPorStatus(eq(StatusTarefa.PENDENTE), any());
    }

    @Test
    @DisplayName("GET /tarefas/{id} - Deve buscar tarefa por ID")
    void testBuscarTarefaPorId() throws Exception {
        when(tarefaService.buscarPorId(1L)).thenReturn(tarefa);

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/tarefas/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.titulo", is("Implementar funcionalidade X")))
                .andExpect(jsonPath("$.status", is("PENDENTE")));

        verify(tarefaService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("GET /tarefas/{id} - Deve retornar 400 quando tarefa nao existe")
    void testBuscarTarefaInexistente() throws Exception {
        when(tarefaService.buscarPorId(999L)).thenThrow(new IllegalArgumentException("Tarefa nao encontrada"));

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/tarefas/999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Tarefa nao encontrada")));

        verify(tarefaService, times(1)).buscarPorId(999L);
    }

    @Test
    @DisplayName("POST /tarefas/cadastrar - Deve cadastrar tarefa como ADMIN")
    void testCadastrarTarefaComoAdmin() throws Exception {
        when(tarefaService.cadastrar(any(), any())).thenReturn(tarefa);

        String token = gerarToken(usuarioAdmin);

        String json = """
                {
                    "titulo": "Implementar funcionalidade X",
                    "descricao": "Descricao da tarefa",
                    "prioridade": "ALTA",
                    "projetoId": 1,
                    "responsavelId": 2
                }
                """;

        mockMvc.perform(post("/tarefas/cadastrar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo", is("Implementar funcionalidade X")));

        verify(tarefaService, times(1)).cadastrar(any(), any());
    }

    @Test
    @DisplayName("POST /tarefas/cadastrar - Deve retornar 400 quando COLABORADOR tenta criar")
    void testCadastrarTarefaSemPermissao() throws Exception {
        Usuario colaborador = new Usuario();
        colaborador.setId(2L);
        colaborador.setEmail("colaborador@email.com");
        colaborador.setRole(RoleUsuario.COLABORADOR);
        colaborador.setAtivo(true);

        autenticarComo(colaborador);

        String token = gerarToken(colaborador);

        when(tarefaService.cadastrar(any(), any()))
                .thenThrow(new IllegalArgumentException("Apenas ADMIN ou GERENTE pode criar tarefas"));

        String json = """
                {
                    "titulo": "Implementar funcionalidade X",
                    "descricao": "Descricao da tarefa",
                    "prioridade": "ALTA",
                    "projetoId": 1,
                    "responsavelId": 2
                }
                """;

        mockMvc.perform(post("/tarefas/cadastrar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Apenas ADMIN ou GERENTE pode criar tarefas")));

        verify(tarefaService, times(1)).cadastrar(any(), any());
    }

    @Test
    @DisplayName("POST /tarefas/cadastrar - Deve retornar 400 quando projeto inativo")
    void testCadastrarTarefaEmProjetoInativo() throws Exception {
        when(tarefaService.cadastrar(any(), any()))
                .thenThrow(new IllegalArgumentException("Projetos inativos nao podem receber novas tarefas"));

        String token = gerarToken(usuarioAdmin);

        String json = """
                {
                    "titulo": "Implementar funcionalidade X",
                    "descricao": "Descricao da tarefa",
                    "prioridade": "ALTA",
                    "projetoId": 1,
                    "responsavelId": 2
                }
                """;

        mockMvc.perform(post("/tarefas/cadastrar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Projetos inativos nao podem receber novas tarefas")));

        verify(tarefaService, times(1)).cadastrar(any(), any());
    }

    @Test
    @DisplayName("PUT /tarefas/{id} - Deve atualizar tarefa")
    void testAtualizarTarefa() throws Exception {
        when(tarefaService.atualizar(any(), any(), any())).thenReturn(tarefa);

        String token = gerarToken(usuarioAdmin);

        String json = """
                {
                    "titulo": "Tarefa Atualizada",
                    "descricao": "Nova descricao",
                    "status": "EM_ANDAMENTO",
                    "prioridade": "MEDIA",
                    "projetoId": 1,
                    "responsavelId": 2
                }
                """;

        mockMvc.perform(put("/tarefas/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(tarefaService, times(1)).atualizar(any(), any(), any());
    }

    @Test
    @DisplayName("PUT /tarefas/{id} - Deve retornar 400 quando COLABORADOR tenta atualizar tarefa de outro")
    void testAtualizarTarefaSemPermissao() throws Exception {
        Usuario colaborador = new Usuario();
        colaborador.setId(3L);
        colaborador.setEmail("outro@email.com");
        colaborador.setRole(RoleUsuario.COLABORADOR);
        colaborador.setAtivo(true);

        autenticarComo(colaborador);

        String token = gerarToken(colaborador);

        when(tarefaService.atualizar(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("COLABORADOR so pode alterar tarefas em que e responsavel"));

        String json = """
                {
                    "titulo": "Tarefa Atualizada",
                    "descricao": "Nova descricao",
                    "status": "EM_ANDAMENTO",
                    "prioridade": "MEDIA",
                    "projetoId": 1,
                    "responsavelId": 2
                }
                """;

        mockMvc.perform(put("/tarefas/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("COLABORADOR so pode alterar tarefas em que e responsavel")));

        verify(tarefaService, times(1)).atualizar(any(), any(), any());
    }

    @Test
    @DisplayName("PUT /tarefas/{id} - Deve retornar 400 quando CANCELADA para CONCLUIDA")
    void testNaoDeveMarcarComoConcluidaTarefaCancelada() throws Exception {
        when(tarefaService.atualizar(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Nao e possivel marcar como CONCLUIDA uma tarefa CANCELADA"));

        String token = gerarToken(usuarioAdmin);

        String json = """
                {
                    "titulo": "Tarefa Atualizada",
                    "descricao": "Nova descricao",
                    "status": "CONCLUIDA",
                    "prioridade": "MEDIA",
                    "projetoId": 1,
                    "responsavelId": 2
                }
                """;

        mockMvc.perform(put("/tarefas/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Nao e possivel marcar como CONCLUIDA uma tarefa CANCELADA")));

        verify(tarefaService, times(1)).atualizar(any(), any(), any());
    }

    @Test
    @DisplayName("DELETE /tarefas/{id}/cancelar - Deve cancelar tarefa")
    void testCancelarTarefa() throws Exception {
        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(delete("/tarefas/1/cancelar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(tarefaService, times(1)).cancelar(any(), any());
    }

    @Test
    @DisplayName("DELETE /tarefas/{id}/cancelar - Deve retornar 400 quando tarefa nao existe")
    void testCancelarTarefaInexistente() throws Exception {
        doThrow(new IllegalArgumentException("Tarefa nao encontrada")).when(tarefaService).cancelar(any(), any());

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(delete("/tarefas/999/cancelar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Tarefa nao encontrada")));

        verify(tarefaService, times(1)).cancelar(any(), any());
    }

    @Test
    @DisplayName("GET /tarefas - Deve retornar 500 quando servico lanca excecao")
    void testListarTarefasComErro() throws Exception {
        when(tarefaService.listar(any())).thenThrow(new RuntimeException("Erro ao listar tarefas"));

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/tarefas")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem", is("Erro ao listar tarefas")));

        verify(tarefaService, times(1)).listar(any());
    }

    @Test
    @DisplayName("GET /tarefas/projeto/{projetoId} - Deve retornar lista vazia para projeto sem tarefas")
    void testListarTarefasPorProjetoVazio() throws Exception {
        Page<Tarefa> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(tarefaService.listarPorProjeto(eq(999L), any())).thenReturn(page);

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/tarefas/projeto/999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(tarefaService, times(1)).listarPorProjeto(eq(999L), any());
    }
}
