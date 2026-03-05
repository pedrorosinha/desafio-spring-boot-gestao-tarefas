package br.com.dbserver.gestao_tarefas.controller;

import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.projeto.Projeto;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.exception.TratadorErros;
import br.com.dbserver.gestao_tarefas.security.CustomUserDetails;
import br.com.dbserver.gestao_tarefas.service.ProjetoService;
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
@DisplayName("ProjetoController - Testes")
class ProjetoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjetoService projetoService;

    private Usuario usuarioAdmin;
    private Projeto projeto;
    private String jwtSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private Long jwtExpiration = 86400000L;

    @BeforeEach
    void setUp() {
        ProjetoController controller = new ProjetoController(projetoService);
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

        projeto = new Projeto();
        projeto.setId(1L);
        projeto.setNome("Projeto Manhattan");
        projeto.setDescricao("Projeto da bomba atomica");
        projeto.setDataCriacao(LocalDateTime.now());
        projeto.setAtivo(true);
        projeto.setGerente(usuarioAdmin);

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
    @DisplayName("GET /projetos - Deve listar todos os projetos")
    void testListarProjetos() throws Exception {
        Page<Projeto> page = new PageImpl<>(List.of(projeto), PageRequest.of(0, 10), 1);

        when(projetoService.listar(any())).thenReturn(page);

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/projetos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Projeto Manhattan")));

        verify(projetoService, times(1)).listar(any());
    }

    @Test
    @DisplayName("GET /projetos/ativos - Deve listar projetos ativos")
    void testListarProjetosAtivos() throws Exception {
        Page<Projeto> page = new PageImpl<>(List.of(projeto), PageRequest.of(0, 10), 1);

        when(projetoService.listarAtivos(any())).thenReturn(page);

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/projetos/ativos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].ativo", is(true)));

        verify(projetoService, times(1)).listarAtivos(any());
    }

    @Test
    @DisplayName("GET /projetos/{id} - Deve buscar projeto por ID")
    void testBuscarProjetoPorId() throws Exception {
        when(projetoService.buscarPorId(1L)).thenReturn(projeto);

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/projetos/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Projeto Manhattan")));

        verify(projetoService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("GET /projetos/{id} - Deve retornar 400 quando projeto nao existe")
    void testBuscarProjetoInexistente() throws Exception {
        when(projetoService.buscarPorId(999L)).thenThrow(new IllegalArgumentException("Projeto nao encontrado"));

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/projetos/999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Projeto nao encontrado")));

        verify(projetoService, times(1)).buscarPorId(999L);
    }

    @Test
    @DisplayName("POST /projetos/cadastrar - Deve cadastrar projeto como ADMIN")
    void testCadastrarProjetoComoAdmin() throws Exception {
        when(projetoService.cadastrar(any(), any())).thenReturn(projeto);

        String token = gerarToken(usuarioAdmin);

        String json = """
                {
                    "nome": "Projeto Manhattan",
                    "descricao": "Projeto da bomba atomica",
                    "gerenteId": 1
                }
                """;

        mockMvc.perform(post("/projetos/cadastrar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Projeto Manhattan")));

        verify(projetoService, times(1)).cadastrar(any(), any());
    }

    @Test
    @DisplayName("POST /projetos/cadastrar - Deve retornar 400 quando gerente nao encontrado")
    void testCadastrarProjetoUsuarioInexistente() throws Exception {
        when(projetoService.cadastrar(any(), any())).thenThrow(new IllegalArgumentException("Gerente nao encontrado"));

        String token = gerarToken(usuarioAdmin);

        String json = """
                {
                    "nome": "Projeto Manhattan",
                    "descricao": "Projeto da bomba atomica",
                    "gerenteId": 999
                }
                """;

        mockMvc.perform(post("/projetos/cadastrar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Gerente nao encontrado")));

        verify(projetoService, times(1)).cadastrar(any(), any());
    }

    @Test
    @DisplayName("POST /projetos/cadastrar - Deve retornar 400 quando usuario nao e ADMIN")
    void testCadastrarProjetoSemPermissao() throws Exception {
        Usuario colaborador = new Usuario();
        colaborador.setId(2L);
        colaborador.setEmail("colaborador@email.com");
        colaborador.setRole(RoleUsuario.COLABORADOR);
        colaborador.setAtivo(true);

        autenticarComo(colaborador);

        String token = gerarToken(colaborador);

        when(projetoService.cadastrar(any(), any()))
                .thenThrow(new IllegalArgumentException("Apenas ADMIN pode criar projetos"));

        String json = """
                {
                    "nome": "Projeto Manhattan",
                    "descricao": "Projeto da bomba atomica",
                    "gerenteId": 1
                }
                """;

        mockMvc.perform(post("/projetos/cadastrar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Apenas ADMIN pode criar projetos")));

        verify(projetoService, times(1)).cadastrar(any(), any());
    }

    @Test
    @DisplayName("PUT /projetos/{id} - Deve atualizar projeto")
    void testAtualizarProjeto() throws Exception {
        when(projetoService.atualizar(any(), any(), any())).thenReturn(projeto);

        String token = gerarToken(usuarioAdmin);

        String json = """
                {
                    "nome": "Projeto Manhattan Atualizado",
                    "descricao": "Nova descricao",
                    "gerenteId": 1
                }
                """;

        mockMvc.perform(put("/projetos/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(projetoService, times(1)).atualizar(any(), any(), any());
    }

    @Test
    @DisplayName("DELETE /projetos/{id} - Deve desativar projeto")
    void testDesativarProjeto() throws Exception {
        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(delete("/projetos/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(projetoService, times(1)).desativar(any(), any());
    }

    @Test
    @DisplayName("DELETE /projetos/{id} - Deve retornar 400 quando projeto nao existe")
    void testDesativarProjetoInexistente() throws Exception {
        doThrow(new IllegalArgumentException("Projeto nao encontrado")).when(projetoService).desativar(any(), any());

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(delete("/projetos/999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", is("Projeto nao encontrado")));

        verify(projetoService, times(1)).desativar(any(), any());
    }

    @Test
    @DisplayName("GET /projetos - Deve retornar 500 quando servico lanca excecao")
    void testListarProjetosComErro() throws Exception {
        when(projetoService.listar(any())).thenThrow(new RuntimeException("Erro ao listar projetos"));

        String token = gerarToken(usuarioAdmin);

        mockMvc.perform(get("/projetos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem", is("Erro ao listar projetos")));

        verify(projetoService, times(1)).listar(any());
    }
}
