package br.com.dbserver.gestao_tarefas.controller;

import org.springframework.web.bind.annotation.*;
import br.com.dbserver.gestao_tarefas.domain.enums.StatusTarefa;
import br.com.dbserver.gestao_tarefas.domain.tarefa.DadosCadastroTarefa;
import br.com.dbserver.gestao_tarefas.domain.tarefa.DadosRespostaTarefa;
import br.com.dbserver.gestao_tarefas.domain.tarefa.Tarefa;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.security.CustomUserDetails;
import br.com.dbserver.gestao_tarefas.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<DadosRespostaTarefa> cadastrar(
            @RequestBody @Valid DadosCadastroTarefa dados,
            UriComponentsBuilder uriBuilder) {

        Usuario usuarioLogado = getUsuarioAutenticado();
        Tarefa tarefa = tarefaService.cadastrar(dados, usuarioLogado);

        URI uri = uriBuilder.path("/tarefas/{id}").buildAndExpand(tarefa.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosRespostaTarefa(tarefa));
    }

    @GetMapping
    public ResponseEntity<Page<DadosRespostaTarefa>> listar(
            @PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DadosRespostaTarefa> tarefas = tarefaService.listar(pageable)
                .map(DadosRespostaTarefa::new);
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/projeto/{projetoId}")
    public ResponseEntity<Page<DadosRespostaTarefa>> listarPorProjeto(
            @PathVariable Long projetoId,
            @PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DadosRespostaTarefa> tarefas = tarefaService.listarPorProjeto(projetoId, pageable)
                .map(DadosRespostaTarefa::new);
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/responsavel/{responsavelId}")
    public ResponseEntity<Page<DadosRespostaTarefa>> listarPorResponsavel(
            @PathVariable Long responsavelId,
            @PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DadosRespostaTarefa> tarefas = tarefaService.listarPorResponsavel(responsavelId, pageable)
                .map(DadosRespostaTarefa::new);
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<DadosRespostaTarefa>> listarPorStatus(
            @PathVariable StatusTarefa status,
            @PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DadosRespostaTarefa> tarefas = tarefaService.listarPorStatus(status, pageable)
                .map(DadosRespostaTarefa::new);
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosRespostaTarefa> buscarPorId(@PathVariable Long id) {
        Tarefa tarefa = tarefaService.buscarPorId(id);
        return ResponseEntity.ok(new DadosRespostaTarefa(tarefa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DadosRespostaTarefa> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid DadosCadastroTarefa dados) {

        Usuario usuarioLogado = getUsuarioAutenticado();
        Tarefa tarefa = tarefaService.atualizar(id, dados, usuarioLogado);
        return ResponseEntity.ok(new DadosRespostaTarefa(tarefa));
    }

    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id) {

        Usuario usuarioLogado = getUsuarioAutenticado();
        tarefaService.cancelar(id, usuarioLogado);
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
}
