package br.uerj.erp.security;

import jakarta.ejb.Stateless;
import java.util.Arrays;
import java.util.List;

@Stateless(name = "AccessControlServiceBean")
public class AccessControlServiceBean implements AccessControlServiceLocal {

    @Override
    public boolean hasAnyRole(List<String> roles, String... acceptedRoles) {
        return roles != null && roles.stream().anyMatch(role -> Arrays.asList(acceptedRoles).contains(role));
    }
}
