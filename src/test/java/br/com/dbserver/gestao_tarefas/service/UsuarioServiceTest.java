package br.com.dbserver.gestao_tarefas.service;

import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.usuario.DadosCadastroUsuario;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Testes Unitários")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private DadosCadastroUsuario dadosCadastro;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("senha123");
        usuario.setRole(RoleUsuario.ADMIN);
        usuario.setAtivo(true);

        dadosCadastro = new DadosCadastroUsuario(
                "João Silva",
                "joao@email.com",
                "senha123",
                RoleUsuario.ADMIN);

        lenient().when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "enc-" + invocation.getArgument(0));
    }

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso")
    void testCadastrarUsuarioComSucesso() {
        when(usuarioRepository.existsByEmail(dadosCadastro.email())).thenReturn(false);
        when(passwordEncoder.encode(dadosCadastro.senha())).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.cadastrar(dadosCadastro);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao@email.com", resultado.getEmail());
        assertTrue(resultado.getAtivo());

        verify(usuarioRepository, times(1)).existsByEmail(dadosCadastro.email());
        verify(passwordEncoder, times(1)).encode(dadosCadastro.senha());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve falhar ao cadastrar com email duplicado")
    void testCadastrarUsuarioEmailDuplicado() {
        when(usuarioRepository.existsByEmail(dadosCadastro.email())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.cadastrar(dadosCadastro));

        assertEquals("Email já cadastrado", exception.getMessage());
        verify(usuarioRepository, times(1)).existsByEmail(dadosCadastro.email());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar usuários com paginação")
    void testListarUsuariosPaginado() {
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

        when(usuarioRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        Page<Usuario> resultado = usuarioService.listar(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalElements());
        assertEquals(2, resultado.getContent().size());

        verify(usuarioRepository, times(1)).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void testBuscarUsuarioPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("João Silva", resultado.getNome());

        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário inexistente")
    void testBuscarUsuarioInexistente() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> usuarioService.buscarPorId(999L));

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve desativar usuário")
    void testDesativarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.desativar(1L);

        assertFalse(usuario.getAtivo());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não houver usuários")
    void testListarUsuariosVazio() {
        Page<Usuario> pageVazia = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0);

        when(usuarioRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(pageVazia);

        Page<Usuario> resultado = usuarioService.listar(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(0, resultado.getTotalElements());
        assertTrue(resultado.getContent().isEmpty());

        verify(usuarioRepository, times(1)).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve cadastrar usuário com diferentes roles")
    void testCadastrarUsuarioComDiferentesRoles() {
        DadosCadastroUsuario dadosGerente = new DadosCadastroUsuario(
                "Gerente Teste",
                "gerente@email.com",
                "senha123",
                RoleUsuario.GERENTE);

        Usuario usuarioGerente = new Usuario();
        usuarioGerente.setId(2L);
        usuarioGerente.setNome("Gerente Teste");
        usuarioGerente.setEmail("gerente@email.com");
        usuarioGerente.setRole(RoleUsuario.GERENTE);
        usuarioGerente.setAtivo(true);

        when(usuarioRepository.existsByEmail(dadosGerente.email())).thenReturn(false);
        when(passwordEncoder.encode(dadosGerente.senha())).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGerente);

        Usuario resultado = usuarioService.cadastrar(dadosGerente);

        assertNotNull(resultado);
        assertEquals(RoleUsuario.GERENTE, resultado.getRole());
        assertTrue(resultado.getAtivo());

        verify(passwordEncoder, times(1)).encode(dadosGerente.senha());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao desativar usuário inexistente")
    void testDesativarUsuarioInexistente() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> usuarioService.desativar(999L));

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(999L);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cadastrar usuário como COLABORADOR")
    void testCadastrarUsuarioComoColaborador() {
        DadosCadastroUsuario dadosColaborador = new DadosCadastroUsuario(
                "Colaborador Teste",
                "colaborador@email.com",
                "senha123",
                RoleUsuario.COLABORADOR);

        Usuario usuarioColaborador = new Usuario();
        usuarioColaborador.setId(3L);
        usuarioColaborador.setNome("Colaborador Teste");
        usuarioColaborador.setEmail("colaborador@email.com");
        usuarioColaborador.setRole(RoleUsuario.COLABORADOR);
        usuarioColaborador.setAtivo(true);

        when(usuarioRepository.existsByEmail(dadosColaborador.email())).thenReturn(false);
        when(passwordEncoder.encode(dadosColaborador.senha())).thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioColaborador);

        Usuario resultado = usuarioService.cadastrar(dadosColaborador);

        assertNotNull(resultado);
        assertEquals(RoleUsuario.COLABORADOR, resultado.getRole());
        assertEquals("Colaborador Teste", resultado.getNome());
        assertTrue(resultado.getAtivo());

        verify(passwordEncoder, times(1)).encode(dadosColaborador.senha());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve desativar usuário já inativo sem erro")
    void testDesativarUsuarioJaInativo() {
        usuario.setAtivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.desativar(1L);

        assertFalse(usuario.getAtivo());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve manter campos após desativação")
    void testManterCamposAposDesativacao() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.desativar(1L);

        assertEquals("João Silva", usuario.getNome());
        assertEquals("joao@email.com", usuario.getEmail());
        assertEquals(RoleUsuario.ADMIN, usuario.getRole());
        assertFalse(usuario.getAtivo());
    }

    @Test
    @DisplayName("Deve cadastrar múltiplos usuários com emails diferentes")
    void testCadastrarMultiplosUsuarios() {
        DadosCadastroUsuario dados1 = new DadosCadastroUsuario(
                "Usuario 1",
                "user1@email.com",
                "senha123",
                RoleUsuario.COLABORADOR);

        DadosCadastroUsuario dados2 = new DadosCadastroUsuario(
                "Usuario 2",
                "user2@email.com",
                "senha456",
                RoleUsuario.GERENTE);

        when(usuarioRepository.existsByEmail("user1@email.com")).thenReturn(false);
        when(usuarioRepository.existsByEmail("user2@email.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado1 = usuarioService.cadastrar(dados1);
        Usuario resultado2 = usuarioService.cadastrar(dados2);

        assertNotNull(resultado1);
        assertNotNull(resultado2);
        assertEquals("user1@email.com", resultado1.getEmail());
        assertEquals("user2@email.com", resultado2.getEmail());

        verify(usuarioRepository, times(2)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve listar usuários com paginação customizada")
    void testListarUsuariosComPaginacaoCustomizada() {
        Page<Usuario> page = new PageImpl<>(
                List.of(usuario),
                PageRequest.of(2, 5),
                11);

        when(usuarioRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        Page<Usuario> resultado = usuarioService.listar(PageRequest.of(2, 5));

        assertNotNull(resultado);
        assertEquals(11, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals(2, resultado.getNumber());
        assertEquals(5, resultado.getSize());

        verify(usuarioRepository, times(1)).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar usuário e verificar todos os campos")
    void testBuscarUsuarioVerificarTodosCampos() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao@email.com", resultado.getEmail());
        assertEquals("senha123", resultado.getSenha());
        assertEquals(RoleUsuario.ADMIN, resultado.getRole());
        assertTrue(resultado.getAtivo());
    }

    @Test
    @DisplayName("Deve validar senha não é perdida ao cadastrar")
    void testValidarSenhaNaoCadastro() {
        when(usuarioRepository.existsByEmail(dadosCadastro.email())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        Usuario resultado = usuarioService.cadastrar(dadosCadastro);

        assertNotNull(resultado.getSenha());
        assertEquals("enc-senha123", resultado.getSenha());
    }
}
