package ads.upf.model.entities;

import io.quarkus.arc.Arc;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class Auditado {

    @Column(name = "criado_em", updatable = false)
    public LocalDateTime criadoEm;

    @Column(name = "criado_por", updatable = false)
    public String criadoPor;

    @Column(name = "atualizado_em")
    public LocalDateTime atualizadoEm;

    @Column(name = "atualizado_por")
    public String atualizadoPor;

    @PrePersist
    protected void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
        this.criadoPor = obterUsuarioLogado();
        this.atualizadoPor = obterUsuarioLogado();
    }

    @PreUpdate
    protected void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
        this.atualizadoPor = obterUsuarioLogado();
    }

    private String obterUsuarioLogado() {
        try {
            SecurityIdentity identity = Arc.container().instance(SecurityIdentity.class).get();
            if (identity != null && !identity.isAnonymous()) {
                return identity.getPrincipal().getName();
            }
        }
        catch (Exception e) {}
        return "sistema";
    }
}