package br.com.dbserver.gestao_tarefas.controller;

import org.springframework.web.bind.annotation.*;

import br.com.dbserver.gestao_tarefas.domain.usuario.DadosCadastroUsuario;
import br.com.dbserver.gestao_tarefas.domain.usuario.DadosRespostaUsuario;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import br.com.dbserver.gestao_tarefas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<DadosRespostaUsuario> cadastrar(
            @RequestBody @Valid DadosCadastroUsuario dados) {

        Usuario usuario = usuarioService.cadastrar(dados);

        DadosRespostaUsuario respostaUsuario = new DadosRespostaUsuario(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getAtivo());

        return ResponseEntity.ok(respostaUsuario);
    }

    @GetMapping
    public ResponseEntity<Page<DadosRespostaUsuario>> listar(Pageable pageable) {

        Page<DadosRespostaUsuario> pagina = usuarioService.listar(pageable)
                .map(usuario -> new DadosRespostaUsuario(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getRole(),
                        usuario.getAtivo()));

        return ResponseEntity.ok(pagina);
    }
}