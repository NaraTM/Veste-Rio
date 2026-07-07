package br.uerj.erp.authserver.repository;

import br.uerj.erp.authserver.domain.AuthUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<AuthUser> findByUsername(String username);
}
