package br.com.dbserver.gestao_tarefas.controller;

import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.usuario.DadosCadastroUsuario;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.exception.TratadorErros;
import br.com.dbserver.gestao_tarefas.service.UsuarioService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - Testes")
class UsuarioControllerTest {

        private MockMvc mockMvc;

        @Mock
        private UsuarioService usuarioService;

        private Usuario usuario;
        private String jwtSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        private Long jwtExpiration = 86400000L;

        @BeforeEach
        void setUp() {
                UsuarioController controller = new UsuarioController(usuarioService);
                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setControllerAdvice(new TratadorErros())
                                .setCustomArgumentResolvers(
                                                new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                                .build();

                usuario = new Usuario();
                usuario.setId(1L);
                usuario.setNome("João Silva");
                usuario.setEmail("joao@email.com");
                usuario.setRole(RoleUsuario.ADMIN);
                usuario.setAtivo(true);
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
        @DisplayName("GET /usuarios - Deve listar todos os usuários")
        void testListarUsuarios() throws Exception {
                Page<Usuario> page = new PageImpl<>(
                                List.of(usuario),
                                PageRequest.of(0, 10),
                                1);

                when(usuarioService.listar(any())).thenReturn(page);

                String token = gerarToken(usuario);

                mockMvc.perform(get("/usuarios")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].nome", is("João Silva")))
                                .andExpect(jsonPath("$.content[0].email", is("joao@email.com")));

                verify(usuarioService, times(1)).listar(any());
        }

        @Test
        @DisplayName("GET /usuarios - Deve retornar página vazia")
        void testListarUsuariosVazio() throws Exception {
                Page<Usuario> page = new PageImpl<>(
                                List.of(),
                                PageRequest.of(0, 10),
                                0);

                when(usuarioService.listar(any())).thenReturn(page);

                String token = gerarToken(usuario);

                mockMvc.perform(get("/usuarios")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(0)))
                                .andExpect(jsonPath("$.totalElements", is(0)));

                verify(usuarioService, times(1)).listar(any());
        }

        @Test
        @DisplayName("GET /usuarios - Deve retornar múltiplas páginas")
        void testListarUsuariosMultiplasPaginas() throws Exception {
                Usuario usuario2 = new Usuario();
                usuario2.setId(2L);
                usuario2.setNome("Maria Santos");
                usuario2.setEmail("maria@email.com");
                usuario2.setRole(RoleUsuario.GERENTE);
                usuario2.setAtivo(true);

                Page<Usuario> page = new PageImpl<>(
                                List.of(usuario, usuario2),
                                PageRequest.of(0, 10),
                                2);

                when(usuarioService.listar(any())).thenReturn(page);

                String token = gerarToken(usuario);

                mockMvc.perform(get("/usuarios?page=0&size=10")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[0].nome", is("João Silva")))
                                .andExpect(jsonPath("$.content[1].nome", is("Maria Santos")))
                                .andExpect(jsonPath("$.totalElements", is(2)));

                verify(usuarioService, times(1)).listar(any());
        }

        @Test
        @DisplayName("POST /usuarios/cadastrar - Deve cadastrar usuário com sucesso")
        void testCadastrarUsuario() throws Exception {
                when(usuarioService.cadastrar(any(DadosCadastroUsuario.class))).thenReturn(usuario);

                String json = """
                                {
                                        "nome": "João Silva",
                                        "email": "joao@email.com",
                                        "senha": "senha123",
                                        "role": "ADMIN"
                                }
                                """;

                mockMvc.perform(post("/usuarios/cadastrar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nome", is("João Silva")))
                                .andExpect(jsonPath("$.email", is("joao@email.com")))
                                .andExpect(jsonPath("$.role", is("ADMIN")))
                                .andExpect(jsonPath("$.ativo", is(true)));

                verify(usuarioService, times(1)).cadastrar(any(DadosCadastroUsuario.class));
        }

        @Test
        @DisplayName("POST /usuarios/cadastrar - Deve retornar 400 com dados inválidos")
        void testCadastrarUsuarioComDadosInvalidos() throws Exception {
                String json = """
                                {
                                        "nome": "",
                                        "email": "email-invalido",
                                        "senha": "",
                                        "role": "ADMIN"
                                }
                                """;

                mockMvc.perform(post("/usuarios/cadastrar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest());

                verify(usuarioService, times(0)).cadastrar(any());
        }

        @Test
        @DisplayName("POST /usuarios/cadastrar - Deve retornar 500 quando serviço lança exceção")
        void testCadastrarUsuarioComErroNoServico() throws Exception {
                when(usuarioService.cadastrar(any(DadosCadastroUsuario.class)))
                                .thenThrow(new RuntimeException("Erro ao cadastrar usuário"));

                String json = """
                                {
                                        "nome": "João Silva",
                                        "email": "joao@email.com",
                                        "senha": "senha123",
                                        "role": "ADMIN"
                                }
                                """;

                mockMvc.perform(post("/usuarios/cadastrar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.mensagem", is("Erro ao cadastrar usuário")));

                verify(usuarioService, times(1)).cadastrar(any(DadosCadastroUsuario.class));
        }

        @Test
        @DisplayName("POST /usuarios/cadastrar - Deve retornar 400 quando email já existe")
        void testCadastrarUsuarioComEmailDuplicado() throws Exception {
                when(usuarioService.cadastrar(any(DadosCadastroUsuario.class)))
                                .thenThrow(new IllegalArgumentException("Email já cadastrado"));

                String json = """
                                {
                                        "nome": "João Silva",
                                        "email": "joao@email.com",
                                        "senha": "senha123",
                                        "role": "ADMIN"
                                }
                                """;

                mockMvc.perform(post("/usuarios/cadastrar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.mensagem", is("Email já cadastrado")));

                verify(usuarioService, times(1)).cadastrar(any(DadosCadastroUsuario.class));
        }

        @Test
        @DisplayName("GET /usuarios - Deve retornar 500 quando serviço lança exceção")
        void testListarUsuariosComErroNoServico() throws Exception {
                when(usuarioService.listar(any()))
                                .thenThrow(new RuntimeException("Erro ao listar usuários"));

                String token = gerarToken(usuario);

                mockMvc.perform(get("/usuarios")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.mensagem", is("Erro ao listar usuários")));

                verify(usuarioService, times(1)).listar(any());
        }
}
