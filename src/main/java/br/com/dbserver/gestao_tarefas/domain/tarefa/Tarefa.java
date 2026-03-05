package br.com.dbserver.gestao_tarefas.domain.tarefa;

import jakarta.persistence.*;
import br.com.dbserver.gestao_tarefas.domain.enums.PrioridadeTarefa;
import br.com.dbserver.gestao_tarefas.domain.enums.StatusTarefa;
import br.com.dbserver.gestao_tarefas.domain.projeto.Projeto;
import br.com.dbserver.gestao_tarefas.domain.usuario.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarefas")
@Getter
@Setter
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeTarefa prioridade;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataConclusao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusTarefa.PENDENTE;
        }
    }
}
