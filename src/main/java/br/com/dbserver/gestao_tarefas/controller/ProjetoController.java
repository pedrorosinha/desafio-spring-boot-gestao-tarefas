package br.com.dbserver.gestao_tarefas.controller;

import org.springframework.web.bind.annotation.*;

import br.com.dbserver.gestao_tarefas.domain.projeto.DadosCadastroProjeto;
import br.com.dbserver.gestao_tarefas.domain.projeto.DadosRespostaProjeto;
import br.com.dbserver.gestao_tarefas.domain.projeto.Projeto;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.security.CustomUserDetails;
import br.com.dbserver.gestao_tarefas.service.ProjetoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<DadosRespostaProjeto> cadastrar(
            @RequestBody @Valid DadosCadastroProjeto dados) {

        Usuario usuarioLogado = getUsuarioAutenticado();
        Projeto projeto = projetoService.cadastrar(dados, usuarioLogado);

        return ResponseEntity.ok(mapearParaResposta(projeto));
    }

    @GetMapping
    public ResponseEntity<Page<DadosRespostaProjeto>> listar(Pageable pageable) {
        Page<DadosRespostaProjeto> pagina = projetoService.listar(pageable)
                .map(this::mapearParaResposta);

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/ativos")
    public ResponseEntity<Page<DadosRespostaProjeto>> listarAtivos(Pageable pageable) {
        Page<DadosRespostaProjeto> pagina = projetoService.listarAtivos(pageable)
                .map(this::mapearParaResposta);

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosRespostaProjeto> buscarPorId(@PathVariable Long id) {
        Projeto projeto = projetoService.buscarPorId(id);
        return ResponseEntity.ok(mapearParaResposta(projeto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DadosRespostaProjeto> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid DadosCadastroProjeto dados) {

        Usuario usuarioLogado = getUsuarioAutenticado();
        Projeto projeto = projetoService.atualizar(id, dados, usuarioLogado);

        return ResponseEntity.ok(mapearParaResposta(projeto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(
            @PathVariable Long id) {

        Usuario usuarioLogado = getUsuarioAutenticado();
        projetoService.desativar(id, usuarioLogado);

        return ResponseEntity.noContent().build();
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("Usuário não autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new BadCredentialsException("Principal inválido");
        }
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Usuario usuario = userDetails.getUsuario();
        if (usuario == null) {
            throw new BadCredentialsException("Usuário não encontrado");
        }
        return usuario;
    }

    private DadosRespostaProjeto mapearParaResposta(Projeto projeto) {
        return new DadosRespostaProjeto(
                projeto.getId(),
                projeto.getNome(),
                projeto.getDescricao(),
                projeto.getDataCriacao(),
                projeto.getAtivo(),
                projeto.getGerente().getId(),
                projeto.getGerente().getNome());
    }
}
