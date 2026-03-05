package br.com.dbserver.gestao_tarefas.service;


import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.projeto.DadosCadastroProjeto;
import br.com.dbserver.gestao_tarefas.domain.projeto.Projeto;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.repository.ProjetoRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjetoService - Testes Unitários")
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ProjetoService projetoService;

    private Usuario usuarioAdmin;
    private Usuario usuarioGerente;
    private Usuario usuarioColaborador;
    private Projeto projeto;
    private DadosCadastroProjeto dadosCadastro;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNome("Admin");
        usuarioAdmin.setEmail("admin@email.com");
        usuarioAdmin.setRole(RoleUsuario.ADMIN);
        usuarioAdmin.setAtivo(true);

        usuarioGerente = new Usuario();
        usuarioGerente.setId(2L);
        usuarioGerente.setNome("Gerente");
        usuarioGerente.setEmail("gerente@email.com");
        usuarioGerente.setRole(RoleUsuario.GERENTE);
        usuarioGerente.setAtivo(true);

        usuarioColaborador = new Usuario();
        usuarioColaborador.setId(3L);
        usuarioColaborador.setNome("Colaborador");
        usuarioColaborador.setEmail("colab@email.com");
        usuarioColaborador.setRole(RoleUsuario.COLABORADOR);
        usuarioColaborador.setAtivo(true);

        projeto = new Projeto();
        projeto.setId(1L);
        projeto.setNome("Projeto Manhattan");
        projeto.setDescricao("Projeto da bomba atômica");
        projeto.setDataCriacao(LocalDateTime.now());
        projeto.setAtivo(true);
        projeto.setGerente(usuarioGerente);

        dadosCadastro = new DadosCadastroProjeto(
                "Projeto Manhattan",
                "Projeto da bomba atômica",
                2L);
    }

    @Test
    @DisplayName("ADMIN deve cadastrar projeto com sucesso")
    void testCadastrarProjetoComoAdmin() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioGerente));
        when(projetoRepository.save(any(Projeto.class))).thenReturn(projeto);

        Projeto resultado = projetoService.cadastrar(dadosCadastro, usuarioAdmin);

        assertNotNull(resultado);
        assertEquals("Projeto Manhattan", resultado.getNome());
        assertEquals(usuarioGerente.getId(), resultado.getGerente().getId());

        verify(usuarioRepository, times(1)).findById(2L);
        verify(projetoRepository, times(1)).save(any(Projeto.class));
    }

    @Test
    @DisplayName("GERENTE não deve poder cadastrar projeto")
    void testCadastrarProjetoComoGerente() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projetoService.cadastrar(dadosCadastro, usuarioGerente));

        assertEquals("Apenas ADMIN pode criar projetos", exception.getMessage());
        verify(projetoRepository, never()).save(any());
    }

    @Test
    @DisplayName("COLABORADOR não deve poder cadastrar projeto")
    void testCadastrarProjetoComoColaborador() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projetoService.cadastrar(dadosCadastro, usuarioColaborador));

        assertEquals("Apenas ADMIN pode criar projetos", exception.getMessage());
        verify(projetoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve validar que gerente tem role GERENTE ou ADMIN")
    void testCadastrarProjetoComGerenteInvalido() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuarioColaborador));

        DadosCadastroProjeto dadosComGerenteInvalido = new DadosCadastroProjeto(
                "Projeto Manhattan",
                "Projeto da bomba atômica",
                3L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projetoService.cadastrar(dadosComGerenteInvalido, usuarioAdmin));

        assertEquals("O gerente deve ter role GERENTE ou ADMIN", exception.getMessage());
        verify(projetoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao cadastrar com gerente inexistente")
    void testCadastrarProjetoGerenteNaoEncontrado() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        DadosCadastroProjeto dadosComGerenteInexistente = new DadosCadastroProjeto(
                "Projeto Manhattan",
                "Projeto da bomba atômica",
                999L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> projetoService.cadastrar(dadosComGerenteInexistente, usuarioAdmin));

        assertEquals("Gerente não encontrado", exception.getMessage());
        verify(projetoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os projetos")
    void testListarProjetos() {
        Page<Projeto> page = new PageImpl<>(
                List.of(projeto),
                PageRequest.of(0, 10),
                1);

        when(projetoRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        Page<Projeto> resultado = projetoService.listar(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());

        verify(projetoRepository, times(1)).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve listar apenas projetos ativos")
    void testListarProjetosAtivos() {
        Page<Projeto> page = new PageImpl<>(
                List.of(projeto),
                PageRequest.of(0, 10),
                1);

        when(projetoRepository.findByAtivo(true, PageRequest.of(0, 10))).thenReturn(page);

        Page<Projeto> resultado = projetoService.listarAtivos(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertTrue(resultado.getContent().get(0).getAtivo());

        verify(projetoRepository, times(1)).findByAtivo(true, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Deve buscar projeto por ID")
    void testBuscarProjetoPorId() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        Projeto resultado = projetoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Projeto Manhattan", resultado.getNome());

        verify(projetoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar projeto inexistente")
    void testBuscarProjetoInexistente() {
        when(projetoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> projetoService.buscarPorId(999L));

        assertEquals("Projeto não encontrado", exception.getMessage());
        verify(projetoRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("ADMIN deve atualizar projeto")
    void testAtualizarProjetoComoAdmin() {
        DadosCadastroProjeto dadosAtualizacao = new DadosCadastroProjeto(
                "Projeto Atualizado",
                "Descrição atualizada",
                2L);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioGerente));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocation -> {
            Projeto p = invocation.getArgument(0);
            p.setNome("Projeto Atualizado");
            p.setDescricao("Descrição atualizada");
            return p;
        });

        Projeto resultado = projetoService.atualizar(1L, dadosAtualizacao, usuarioAdmin);

        assertNotNull(resultado);
        assertEquals("Projeto Atualizado", resultado.getNome());
        assertEquals("Descrição atualizada", resultado.getDescricao());

        verify(projetoRepository, times(1)).findById(1L);
        verify(projetoRepository, times(1)).save(any(Projeto.class));
    }

    @Test
    @DisplayName("Gerente responsável deve atualizar projeto")
    void testAtualizarProjetoComoGerente() {
        DadosCadastroProjeto dadosAtualizacao = new DadosCadastroProjeto(
                "Projeto Atualizado",
                "Descrição atualizada",
                2L);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocation -> {
            Projeto p = invocation.getArgument(0);
            p.setNome("Projeto Atualizado");
            p.setDescricao("Descrição atualizada");
            return p;
        });

        Projeto resultado = projetoService.atualizar(1L, dadosAtualizacao, usuarioGerente);

        assertNotNull(resultado);
        assertEquals("Projeto Atualizado", resultado.getNome());
        assertEquals("Descrição atualizada", resultado.getDescricao());

        verify(projetoRepository, times(1)).findById(1L);
        verify(projetoRepository, times(1)).save(any(Projeto.class));
    }

    @Test
    @DisplayName("Gerente não responsável não deve atualizar projeto")
    void testAtualizarProjetoComoGerenteNaoResponsavel() {
        Usuario outroGerente = new Usuario();
        outroGerente.setId(99L);
        outroGerente.setNome("Outro Gerente");
        outroGerente.setRole(RoleUsuario.GERENTE);

        DadosCadastroProjeto dadosAtualizacao = new DadosCadastroProjeto(
                "Projeto Atualizado",
                "Descrição atualizada",
                2L);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projetoService.atualizar(1L, dadosAtualizacao, outroGerente));

        assertEquals("Apenas ADMIN ou o GERENTE responsável pode editar este projeto", exception.getMessage());
        verify(projetoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar projeto como ADMIN")
    void testDesativarProjetoComoAdmin() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        projetoService.desativar(1L, usuarioAdmin);

        assertFalse(projeto.getAtivo());
        verify(projetoRepository, times(1)).findById(1L);
        verify(projetoRepository, times(1)).save(any(Projeto.class));
    }

    @Test
    @DisplayName("Deve desativar projeto como gerente responsável")
    void testDesativarProjetoComoGerente() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        projetoService.desativar(1L, usuarioGerente);

        assertFalse(projeto.getAtivo());
        verify(projetoRepository, times(1)).findById(1L);
        verify(projetoRepository, times(1)).save(any(Projeto.class));
    }

    @Test
    @DisplayName("Colaborador não deve desativar projeto")
    void testDesativarProjetoComoColaborador() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projetoService.desativar(1L, usuarioColaborador));

        assertEquals("Apenas ADMIN ou o GERENTE responsável pode desativar este projeto", exception.getMessage());
        verify(projetoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Projeto inativo não pode receber novas tarefas")
    void testValidarProjetoInativo() {
        projeto.setAtivo(false);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projetoService.validarProjetoAtivo(1L));

        assertEquals("Projetos inativos não podem receber novas tarefas", exception.getMessage());
    }

    @Test
    @DisplayName("Projeto ativo pode receber novas tarefas")
    void testValidarProjetoAtivo() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        assertDoesNotThrow(() -> projetoService.validarProjetoAtivo(1L));
    }
}
