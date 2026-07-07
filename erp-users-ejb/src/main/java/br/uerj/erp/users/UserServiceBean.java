package br.uerj.erp.users;

import br.uerj.erp.shared.domain.AppRole;
import br.uerj.erp.shared.domain.AppUser;
import br.uerj.erp.shared.domain.RoleName;
import br.uerj.erp.shared.util.PasswordCodec;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Stateless
public class UserServiceBean implements UserServiceLocal {

    @PersistenceContext(unitName = "erpPU")
    private EntityManager entityManager;

    @Override
    public List<AppUser> findAllUsers() {
        return entityManager.createQuery("select u from AppUser u order by u.username", AppUser.class).getResultList();
    }

    @Override
    public AppUser findUser(Long id) {
        return entityManager.find(AppUser.class, id);
    }

    @Override
    public AppUser findByUsername(String username) {
        List<AppUser> result = entityManager.createQuery("select u from AppUser u left join fetch u.roles where u.username = :username", AppUser.class)
                .setParameter("username", username)
                .getResultList();
        return result.isEmpty() ? null : result.getFirst();
    }

    @Override
    public List<AppRole> findAllRoles() {
        return entityManager.createQuery("select r from AppRole r order by r.name", AppRole.class).getResultList();
    }

    @Override
    public AppUser saveUser(AppUser user, List<RoleName> roles, String rawPassword) {
        AppUser managed = user.getId() == null ? new AppUser() : entityManager.find(AppUser.class, user.getId());
        managed.setUsername(user.getUsername());
        managed.setEmail(user.getEmail());
        managed.setFullName(user.getFullName());
        managed.setActive(user.isActive());
        if (rawPassword != null && !rawPassword.isBlank()) {
            managed.setPasswordHash(PasswordCodec.encode(rawPassword));
        }
        Set<AppRole> selectedRoles = new LinkedHashSet<>();
        for (RoleName roleName : roles) {
            AppRole role = entityManager.createQuery("select r from AppRole r where r.name = :name", AppRole.class)
                    .setParameter("name", roleName)
                    .getSingleResult();
            selectedRoles.add(role);
        }
        managed.setRoles(selectedRoles);
        if (managed.getId() == null) {
            entityManager.persist(managed);
        } else {
            managed = entityManager.merge(managed);
        }
        return managed;
    }

    @Override
    public void removeUser(Long id) {
        AppUser user = entityManager.find(AppUser.class, id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    @Override
    public long countUsers() {
        return entityManager.createQuery("select count(u) from AppUser u", Long.class).getSingleResult();
    }
}
