package br.com.dbserver.gestao_tarefas.service;

import br.com.dbserver.gestao_tarefas.domain.enums.PrioridadeTarefa;
import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.enums.StatusTarefa;
import br.com.dbserver.gestao_tarefas.domain.projeto.Projeto;
import br.com.dbserver.gestao_tarefas.domain.tarefa.DadosCadastroTarefa;
import br.com.dbserver.gestao_tarefas.domain.tarefa.Tarefa;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.repository.TarefaRepository;
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
@DisplayName("TarefaService - Testes Unitários")
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private ProjetoService projetoService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private TarefaService tarefaService;

    private Usuario usuarioAdmin;
    private Usuario usuarioGerente;
    private Usuario usuarioColaborador;
    private Projeto projeto;
    private Tarefa tarefa;
    private DadosCadastroTarefa dadosCadastro;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNome("Admin");
        usuarioAdmin.setRole(RoleUsuario.ADMIN);

        usuarioGerente = new Usuario();
        usuarioGerente.setId(2L);
        usuarioGerente.setNome("Gerente");
        usuarioGerente.setRole(RoleUsuario.GERENTE);

        usuarioColaborador = new Usuario();
        usuarioColaborador.setId(3L);
        usuarioColaborador.setNome("Colaborador");
        usuarioColaborador.setRole(RoleUsuario.COLABORADOR);

        projeto = new Projeto();
        projeto.setId(1L);
        projeto.setNome("Projeto Teste");
        projeto.setAtivo(true);
        projeto.setGerente(usuarioGerente);

        tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Tarefa Teste");
        tarefa.setDescricao("Descrição teste");
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setPrioridade(PrioridadeTarefa.MEDIA);
        tarefa.setDataCriacao(LocalDateTime.now());
        tarefa.setProjeto(projeto);
        tarefa.setResponsavel(usuarioColaborador);

        dadosCadastro = new DadosCadastroTarefa(
                "Tarefa Teste",
                "Descrição teste",
                StatusTarefa.PENDENTE,
                PrioridadeTarefa.MEDIA,
                1L,
                3L);
    }

    @Test
    @DisplayName("ADMIN deve cadastrar tarefa com sucesso")
    void testCadastrarTarefaComoAdmin() {
        when(projetoService.buscarPorId(1L)).thenReturn(projeto);
        when(usuarioService.buscarPorId(3L)).thenReturn(usuarioColaborador);
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);

        Tarefa resultado = tarefaService.cadastrar(dadosCadastro, usuarioAdmin);

        assertNotNull(resultado);
        assertEquals("Tarefa Teste", resultado.getTitulo());
        verify(projetoService, times(1)).validarProjetoAtivo(1L);
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("GERENTE deve cadastrar tarefa com sucesso")
    void testCadastrarTarefaComoGerente() {
        when(projetoService.buscarPorId(1L)).thenReturn(projeto);
        when(usuarioService.buscarPorId(3L)).thenReturn(usuarioColaborador);
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);

        Tarefa resultado = tarefaService.cadastrar(dadosCadastro, usuarioGerente);

        assertNotNull(resultado);
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("COLABORADOR não deve cadastrar tarefa")
    void testCadastrarTarefaComoColaborador() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tarefaService.cadastrar(dadosCadastro, usuarioColaborador));

        assertEquals("Apenas ADMIN ou GERENTE pode criar tarefas", exception.getMessage());
        verify(tarefaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todas as tarefas")
    void testListarTarefas() {
        Page<Tarefa> page = new PageImpl<>(
                List.of(tarefa),
                PageRequest.of(0, 10),
                1);

        when(tarefaRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        Page<Tarefa> resultado = tarefaService.listar(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(tarefaRepository, times(1)).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve listar tarefas por projeto")
    void testListarTarefasPorProjeto() {
        Page<Tarefa> page = new PageImpl<>(
                List.of(tarefa),
                PageRequest.of(0, 10),
                1);

        when(tarefaRepository.findByProjetoId(1L, PageRequest.of(0, 10))).thenReturn(page);

        Page<Tarefa> resultado = tarefaService.listarPorProjeto(1L, PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(tarefaRepository, times(1)).findByProjetoId(1L, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Deve buscar tarefa por ID")
    void testBuscarTarefaPorId() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        Tarefa resultado = tarefaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(tarefaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar tarefa inexistente")
    void testBuscarTarefaInexistente() {
        when(tarefaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tarefaService.buscarPorId(999L));

        assertEquals("Tarefa não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("ADMIN deve atualizar tarefa")
    void testAtualizarTarefaComoAdmin() {
        DadosCadastroTarefa dadosAtualizacao = new DadosCadastroTarefa(
                "Tarefa Atualizada",
                "Descrição atualizada",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA,
                1L,
                3L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa t = invocation.getArgument(0);
            t.setTitulo("Tarefa Atualizada");
            t.setStatus(StatusTarefa.EM_ANDAMENTO);
            return t;
        });

        Tarefa resultado = tarefaService.atualizar(1L, dadosAtualizacao, usuarioAdmin);

        assertNotNull(resultado);
        assertEquals("Tarefa Atualizada", resultado.getTitulo());
        assertEquals(StatusTarefa.EM_ANDAMENTO, resultado.getStatus());
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("COLABORADOR deve atualizar apenas sua tarefa")
    void testAtualizarTarefaComoColaboradorResponsavel() {
        DadosCadastroTarefa dadosAtualizacao = new DadosCadastroTarefa(
                "Tarefa Atualizada",
                "Descrição atualizada",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA,
                1L,
                3L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);

        Tarefa resultado = tarefaService.atualizar(1L, dadosAtualizacao, usuarioColaborador);

        assertNotNull(resultado);
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("COLABORADOR não deve atualizar tarefa de outro responsável")
    void testAtualizarTarefaComoColaboradorNaoResponsavel() {
        Usuario outroColaborador = new Usuario();
        outroColaborador.setId(99L);
        outroColaborador.setRole(RoleUsuario.COLABORADOR);

        DadosCadastroTarefa dadosAtualizacao = new DadosCadastroTarefa(
                "Tarefa Atualizada",
                "Descrição atualizada",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA,
                1L,
                3L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tarefaService.atualizar(1L, dadosAtualizacao, outroColaborador));

        assertEquals("COLABORADOR só pode alterar tarefas em que é responsável", exception.getMessage());
        verify(tarefaRepository, never()).save(any());
    }

    @Test
    @DisplayName("GERENTE deve atualizar tarefa do seu projeto")
    void testAtualizarTarefaComoGerenteDoProjeto() {
        DadosCadastroTarefa dadosAtualizacao = new DadosCadastroTarefa(
                "Tarefa Gerente",
                "Descrição gerente",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA,
                1L,
                3L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tarefa resultado = tarefaService.atualizar(1L, dadosAtualizacao, usuarioGerente);

        assertNotNull(resultado);
        assertEquals(StatusTarefa.EM_ANDAMENTO, resultado.getStatus());
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("GERENTE não deve atualizar tarefa de projeto que não gerencia")
    void testAtualizarTarefaComoGerenteForaDoProjeto() {
        Usuario outroGerente = new Usuario();
        outroGerente.setId(10L);
        outroGerente.setRole(RoleUsuario.GERENTE);

        Projeto outroProjeto = new Projeto();
        outroProjeto.setId(2L);
        outroProjeto.setGerente(outroGerente);
        tarefa.setProjeto(outroProjeto);

        DadosCadastroTarefa dadosAtualizacao = new DadosCadastroTarefa(
                "Tarefa Bloqueada",
                "Descrição bloqueada",
                StatusTarefa.EM_ANDAMENTO,
                PrioridadeTarefa.ALTA,
                2L,
                3L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tarefaService.atualizar(1L, dadosAtualizacao, usuarioGerente));

        assertEquals("GERENTE só pode alterar tarefas dos seus projetos ou tarefas em que é responsável",
                exception.getMessage());
        verify(tarefaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve marcar como CONCLUÍDA uma tarefa CANCELADA")
    void testNaoDeveMarcarComoConcluidaTarefaCancelada() {
        tarefa.setStatus(StatusTarefa.CANCELADA);

        DadosCadastroTarefa dadosAtualizacao = new DadosCadastroTarefa(
                "Tarefa Atualizada",
                "Descrição atualizada",
                StatusTarefa.CONCLUIDA,
                PrioridadeTarefa.ALTA,
                1L,
                3L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tarefaService.atualizar(1L, dadosAtualizacao, usuarioAdmin));

        assertEquals("Não é possível marcar como CONCLUÍDA uma tarefa CANCELADA", exception.getMessage());
    }

    @Test
    @DisplayName("Deve preencher dataConclusao ao concluir tarefa")
    void testDevePreencherDataConclusaoAoConcluir() {
        DadosCadastroTarefa dadosAtualizacao = new DadosCadastroTarefa(
                "Tarefa Atualizada",
                "Descrição atualizada",
                StatusTarefa.CONCLUIDA,
                PrioridadeTarefa.ALTA,
                1L,
                3L);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tarefa resultado = tarefaService.atualizar(1L, dadosAtualizacao, usuarioAdmin);

        assertNotNull(resultado.getDataConclusao());
        assertEquals(StatusTarefa.CONCLUIDA, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve cancelar tarefa como ADMIN")
    void testCancelarTarefaComoAdmin() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        tarefaService.cancelar(1L, usuarioAdmin);

        assertEquals(StatusTarefa.CANCELADA, tarefa.getStatus());
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("COLABORADOR deve cancelar apenas sua tarefa")
    void testCancelarTarefaComoColaboradorResponsavel() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        tarefaService.cancelar(1L, usuarioColaborador);

        assertEquals(StatusTarefa.CANCELADA, tarefa.getStatus());
        verify(tarefaRepository, times(1)).save(any(Tarefa.class));
    }

    @Test
    @DisplayName("COLABORADOR não deve cancelar tarefa de outro responsável")
    void testCancelarTarefaComoColaboradorNaoResponsavel() {
        Usuario outroColaborador = new Usuario();
        outroColaborador.setId(99L);
        outroColaborador.setRole(RoleUsuario.COLABORADOR);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tarefaService.cancelar(1L, outroColaborador));

        assertEquals("COLABORADOR só pode alterar tarefas em que é responsável", exception.getMessage());
        verify(tarefaRepository, times(1)).findById(1L);
        verify(tarefaRepository, never()).save(any());
    }

    @Test
    @DisplayName("GERENTE não deve cancelar tarefa de projeto que não gerencia")
    void testCancelarTarefaComoGerenteForaDoProjeto() {
        Usuario outroGerente = new Usuario();
        outroGerente.setId(10L);
        outroGerente.setRole(RoleUsuario.GERENTE);

        Projeto outroProjeto = new Projeto();
        outroProjeto.setId(2L);
        outroProjeto.setGerente(outroGerente);
        tarefa.setProjeto(outroProjeto);

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tarefaService.cancelar(1L, usuarioGerente));

        assertEquals("GERENTE só pode alterar tarefas dos seus projetos ou tarefas em que é responsável",
                exception.getMessage());
        verify(tarefaRepository, never()).save(any());
    }
}
