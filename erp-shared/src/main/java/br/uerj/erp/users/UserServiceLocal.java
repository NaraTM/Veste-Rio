package br.uerj.erp.users;

import br.uerj.erp.shared.domain.AppRole;
import br.uerj.erp.shared.domain.AppUser;
import br.uerj.erp.shared.domain.RoleName;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface UserServiceLocal {
    List<AppUser> findAllUsers();
    AppUser findUser(Long id);
    AppUser findByUsername(String username);
    List<AppRole> findAllRoles();
    AppUser saveUser(AppUser user, List<RoleName> roles, String rawPassword);
    void removeUser(Long id);
    long countUsers();
}
