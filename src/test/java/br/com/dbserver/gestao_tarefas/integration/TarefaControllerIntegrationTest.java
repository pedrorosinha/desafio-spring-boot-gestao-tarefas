package br.com.dbserver.gestao_tarefas.integration;

import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.repository.TarefaRepository;
import br.com.dbserver.gestao_tarefas.repository.UsuarioRepository;
import br.com.dbserver.gestao_tarefas.security.CustomUserDetails;
import br.com.dbserver.gestao_tarefas.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("TarefaController - Testes de Integração")
class TarefaControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private String tokenColaborador;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        tarefaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuarioAdmin = criarUsuario("admin@test.com", "senha123", RoleUsuario.ADMIN);
        Usuario usuarioColaborador = criarUsuario("colab@test.com", "senha123", RoleUsuario.COLABORADOR);

        tokenAdmin = jwtService.generateToken(new CustomUserDetails(usuarioAdmin));
        tokenColaborador = jwtService.generateToken(new CustomUserDetails(usuarioColaborador));
    }

    private Usuario criarUsuario(String email, String senha, RoleUsuario role) {
        Usuario usuario = new Usuario();
        usuario.setNome("Nome " + role);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setRole(role);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    @Test

    void adminDeveListarTarefas() throws Exception {
        mockMvc.perform(get("/tarefas")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("COLABORADOR deve conseguir listar tarefas com token válido")
    void colaboradorDeveListarTarefas() throws Exception {
        mockMvc.perform(get("/tarefas")
                .header("Authorization", "Bearer " + tokenColaborador))
                .andExpect(status().isOk());
    }
}
