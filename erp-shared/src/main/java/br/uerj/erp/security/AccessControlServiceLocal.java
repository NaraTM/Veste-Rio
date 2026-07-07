package br.uerj.erp.security;

import jakarta.ejb.Local;
import java.util.List;

@Local
public interface AccessControlServiceLocal {
    boolean hasAnyRole(List<String> roles, String... acceptedRoles);
}
