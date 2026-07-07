package br.uerj.erp.authserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_roles")
public class AuthRole {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(nullable = false, length = 80)
    private String description;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
