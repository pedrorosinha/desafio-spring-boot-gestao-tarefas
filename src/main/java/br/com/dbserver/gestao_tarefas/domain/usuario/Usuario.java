package br.com.dbserver.gestao_tarefas.domain.usuario;

import jakarta.persistence.*;
import br.com.dbserver.gestao_tarefas.domain.enums.RoleUsuario;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    private String senha;

    @Enumerated(EnumType.STRING)
    private RoleUsuario role;

    private Boolean ativo;
}