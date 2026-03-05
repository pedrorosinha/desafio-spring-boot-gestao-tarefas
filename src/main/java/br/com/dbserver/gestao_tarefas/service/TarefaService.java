package br.com.dbserver.gestao_tarefas.service;

import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.enums.StatusTarefa;
import br.com.dbserver.gestao_tarefas.domain.projeto.Projeto;
import br.com.dbserver.gestao_tarefas.domain.tarefa.DadosCadastroTarefa;
import br.com.dbserver.gestao_tarefas.domain.tarefa.Tarefa;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.repository.TarefaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TarefaService {
    private static final String MSG_TAREFA_NAO_ENCONTRADA = "Tarefa não encontrada";

    private final TarefaRepository tarefaRepository;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public TarefaService(TarefaRepository tarefaRepository, ProjetoService projetoService,
            UsuarioService usuarioService) {
        this.tarefaRepository = tarefaRepository;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public Tarefa cadastrar(DadosCadastroTarefa dados, Usuario usuarioLogado) {
        if (usuarioLogado.getRole() != RoleUsuario.ADMIN && usuarioLogado.getRole() != RoleUsuario.GERENTE) {
            throw new IllegalArgumentException("Apenas ADMIN ou GERENTE pode criar tarefas");
        }

        projetoService.validarProjetoAtivo(dados.projetoId());

        Projeto projeto = projetoService.buscarPorId(dados.projetoId());
        Usuario responsavel = usuarioService.buscarPorId(dados.responsavelId());

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(dados.titulo());
        tarefa.setDescricao(dados.descricao());
        tarefa.setStatus(dados.status() != null ? dados.status() : StatusTarefa.PENDENTE);
        tarefa.setPrioridade(dados.prioridade());
        tarefa.setProjeto(projeto);
        tarefa.setResponsavel(responsavel);

        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public Tarefa atualizar(Long id, DadosCadastroTarefa dados, Usuario usuarioLogado) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MSG_TAREFA_NAO_ENCONTRADA));

        validarPermissaoEdicao(usuarioLogado, tarefa);

        if (tarefa.getStatus() == StatusTarefa.CANCELADA && dados.status() == StatusTarefa.CONCLUIDA) {
            throw new IllegalArgumentException("Não é possível marcar como CONCLUÍDA uma tarefa CANCELADA");
        }

        tarefa.setTitulo(dados.titulo());
        tarefa.setDescricao(dados.descricao());

        if (dados.status() == StatusTarefa.CONCLUIDA && tarefa.getStatus() != StatusTarefa.CONCLUIDA) {
            tarefa.setDataConclusao(LocalDateTime.now());
        }

        tarefa.setStatus(dados.status() != null ? dados.status() : tarefa.getStatus());
        tarefa.setPrioridade(dados.prioridade());

        if (dados.responsavelId() != null
                && !dados.responsavelId().equals(tarefa.getResponsavel().getId())
                && (usuarioLogado.getRole() == RoleUsuario.ADMIN || usuarioLogado.getRole() == RoleUsuario.GERENTE)) {
            Usuario novoResponsavel = usuarioService.buscarPorId(dados.responsavelId());
            tarefa.setResponsavel(novoResponsavel);
        }

        return tarefaRepository.save(tarefa);
    }

    @Transactional
    public void cancelar(Long id, Usuario usuarioLogado) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MSG_TAREFA_NAO_ENCONTRADA));

        validarPermissaoEdicao(usuarioLogado, tarefa);

        tarefa.setStatus(StatusTarefa.CANCELADA);
        tarefaRepository.save(tarefa);
    }

    private void validarPermissaoEdicao(Usuario usuarioLogado, Tarefa tarefa) {
        if (usuarioLogado.getRole() == RoleUsuario.ADMIN) {
            return;
        }

        boolean isResponsavel = tarefa.getResponsavel().getId().equals(usuarioLogado.getId());

        if (usuarioLogado.getRole() == RoleUsuario.COLABORADOR && !isResponsavel) {
            throw new IllegalArgumentException("COLABORADOR só pode alterar tarefas em que é responsável");
        }

        if (usuarioLogado.getRole() == RoleUsuario.GERENTE) {
            boolean isGerenteDoProjeto = tarefa.getProjeto().getGerente().getId().equals(usuarioLogado.getId());
            if (!isGerenteDoProjeto && !isResponsavel) {
                throw new IllegalArgumentException(
                        "GERENTE só pode alterar tarefas dos seus projetos ou tarefas em que é responsável");
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<Tarefa> listar(Pageable pageable) {
        return tarefaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Tarefa> listarPorProjeto(Long projetoId, Pageable pageable) {
        return tarefaRepository.findByProjetoId(projetoId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Tarefa> listarPorResponsavel(Long responsavelId, Pageable pageable) {
        return tarefaRepository.findByResponsavelId(responsavelId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Tarefa> listarPorStatus(StatusTarefa status, Pageable pageable) {
        return tarefaRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Tarefa buscarPorId(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MSG_TAREFA_NAO_ENCONTRADA));
    }
}
