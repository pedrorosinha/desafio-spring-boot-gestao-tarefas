package br.com.dbserver.gestao_tarefas.service;

import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import br.com.dbserver.gestao_tarefas.domain.projeto.DadosCadastroProjeto;
import br.com.dbserver.gestao_tarefas.domain.projeto.Projeto;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.repository.ProjetoRepository;
import br.com.dbserver.gestao_tarefas.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjetoService {
    private static final String PROJETO_NAO_ENCONTRADO = "Projeto não encontrado";
    private static final String GERENTE_NAO_ENCONTRADO = "Gerente não encontrado";

    private final ProjetoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public ProjetoService(ProjetoRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Projeto cadastrar(DadosCadastroProjeto dados, Usuario usuarioLogado) {
        if (!usuarioLogado.getRole().equals(RoleUsuario.ADMIN)) {
            throw new IllegalArgumentException("Apenas ADMIN pode criar projetos");
        }

        Usuario gerente = usuarioRepository.findById(dados.gerenteId())
                .orElseThrow(() -> new RuntimeException(GERENTE_NAO_ENCONTRADO));

        if (!gerente.getRole().equals(RoleUsuario.GERENTE) && !gerente.getRole().equals(RoleUsuario.ADMIN)) {
            throw new IllegalArgumentException("O gerente deve ter role GERENTE ou ADMIN");
        }

        Projeto projeto = new Projeto();
        projeto.setNome(dados.nome());
        projeto.setDescricao(dados.descricao());
        projeto.setGerente(gerente);

        return repository.save(projeto);
    }

    @Transactional(readOnly = true)
    public Page<Projeto> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Projeto> listarAtivos(Pageable pageable) {
        return repository.findByAtivo(true, pageable);
    }

    @Transactional(readOnly = true)
    public Projeto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException(PROJETO_NAO_ENCONTRADO));
    }

    @Transactional
    public Projeto atualizar(Long id, DadosCadastroProjeto dados, Usuario usuarioLogado) {
        Projeto projeto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(PROJETO_NAO_ENCONTRADO));

        boolean isAdmin = usuarioLogado.getRole().equals(RoleUsuario.ADMIN);
        boolean isGerenteResponsavel = projeto.getGerente().getId().equals(usuarioLogado.getId());

        if (!isAdmin && !isGerenteResponsavel) {
            throw new IllegalArgumentException("Apenas ADMIN ou o GERENTE responsável pode editar este projeto");
        }

        projeto.setNome(dados.nome());
        projeto.setDescricao(dados.descricao());

        if (isAdmin) {
            Usuario novoGerente = usuarioRepository.findById(dados.gerenteId())
                    .orElseThrow(() -> new RuntimeException(GERENTE_NAO_ENCONTRADO));

            if (!novoGerente.getRole().equals(RoleUsuario.GERENTE)
                    && !novoGerente.getRole().equals(RoleUsuario.ADMIN)) {
                throw new IllegalArgumentException("O gerente deve ter role GERENTE ou ADMIN");
            }

            projeto.setGerente(novoGerente);
        }

        return repository.save(projeto);
    }

    @Transactional
    public void desativar(Long id, Usuario usuarioLogado) {
        Projeto projeto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(PROJETO_NAO_ENCONTRADO));

        boolean isAdmin = usuarioLogado.getRole().equals(RoleUsuario.ADMIN);
        boolean isGerenteResponsavel = projeto.getGerente().getId().equals(usuarioLogado.getId());

        if (!isAdmin && !isGerenteResponsavel) {
            throw new IllegalArgumentException("Apenas ADMIN ou o GERENTE responsável pode desativar este projeto");
        }

        projeto.setAtivo(false);
        repository.save(projeto);
    }

    public void validarProjetoAtivo(Long projetoId) {
        Projeto projeto = repository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException(PROJETO_NAO_ENCONTRADO));
        if (Boolean.FALSE.equals(projeto.getAtivo())) {
            throw new IllegalArgumentException("Projetos inativos não podem receber novas tarefas");
        }
    }
}
