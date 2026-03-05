package br.com.dbserver.gestao_tarefas.integration;

import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("AuthController - Testes de Integração")
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        usuarioRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setNome("User Test");
        usuario.setEmail("user@test.com");
        usuario.setSenha(passwordEncoder.encode("senha123"));
        usuario.setRole(RoleUsuario.COLABORADOR);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    @Test
    @DisplayName("Deve fazer login com credenciais válidas")
    void deveFazerLoginComSucesso() throws Exception {
        var loginRequest = """
                {
                    "email": "user@test.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()));
    }

    @Test
    @DisplayName("Deve retornar 401 para credenciais inválidas")
    void deveRetornar401ParaCredenciaisInvalidas() throws Exception {
        var loginRequest = """
                {
                    "email": "user@test.com",
                    "senha": "senhaErrada"
                }
                """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 401 para usuário inexistente")
    void deveRetornar401ParaUsuarioInexistente() throws Exception {
        var loginRequest = """
                {
                    "email": "inexistente@test.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 401 para usuário inativo")
    void deveRetornar401ParaUsuarioInativo() throws Exception {
        Usuario usuarioInativo = new Usuario();
        usuarioInativo.setNome("Inativo");
        usuarioInativo.setEmail("inativo@test.com");
        usuarioInativo.setSenha(passwordEncoder.encode("senha123"));
        usuarioInativo.setRole(RoleUsuario.COLABORADOR);
        usuarioInativo.setAtivo(false);
        usuarioRepository.save(usuarioInativo);

        var loginRequest = """
                {
                    "email": "inativo@test.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 400 para requisição sem email")
    void deveRetornar400SemEmail() throws Exception {
        var loginRequest = """
                {
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 para requisição sem senha")
    void deveRetornar400SemSenha() throws Exception {
        var loginRequest = """
                {
                    "email": "user@test.com"
                }
                """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest());
    }
}
