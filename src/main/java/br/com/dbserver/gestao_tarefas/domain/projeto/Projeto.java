package br.com.dbserver.gestao_tarefas.domain.projeto;

import jakarta.persistence.*;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "projetos")
@Getter
@Setter
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    private Boolean ativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerente_id", nullable = false)
    private Usuario gerente;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        this.ativo = true;
    }
}
