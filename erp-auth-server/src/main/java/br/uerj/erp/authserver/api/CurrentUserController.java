package br.uerj.erp.authserver.api;

import br.uerj.erp.authserver.repository.AuthUserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrentUserController {

    private final AuthUserRepository repository;

    public CurrentUserController(AuthUserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/me")
    public Map<String, Object> currentUser(Authentication authentication) {
        return repository.findByUsername(authentication.getName())
                .map(user -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("username", user.getUsername());
                    response.put("fullName", user.getFullName());
                    response.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
                    return response;
                })
                .orElseGet(LinkedHashMap::new);
    }
}
