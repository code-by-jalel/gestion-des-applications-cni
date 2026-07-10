package com.example.stage.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("authz")
public class AuthorizationService {
    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_ADMINSGROUP");
    private static final Set<String> GESTIONNAIRE_UTILISATEURS = Set.of("ROLE_GESTIONNAIREUTILISATEURS");
    private static final Set<String> GESTIONNAIRE_ORGANISATION = Set.of("ROLE_GESTIONNAIREORGANISATION");
    public boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ADMIN_ROLES::contains);
    }
    public boolean hasGestionnaireUtilisateursRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(GESTIONNAIRE_UTILISATEURS::contains);
    }
    public boolean hasGestionnaireOrganisationRole(Authentication authentication){
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(GESTIONNAIRE_ORGANISATION::contains);
    }
}
