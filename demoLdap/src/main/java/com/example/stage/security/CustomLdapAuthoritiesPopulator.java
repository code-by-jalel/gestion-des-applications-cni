package com.example.stage.security;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import java.util.HashSet;
import java.util.Set;

public class CustomLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {
    private final LdapTemplate ldapTemplate;
    private final String baseLdapPath;
    public CustomLdapAuthoritiesPopulator(BaseLdapPathContextSource contextSource,
                                          String groupSearchBase,
                                          LdapTemplate ldapTemplate) {
        super(contextSource, groupSearchBase);
        this.ldapTemplate = ldapTemplate;
        this.baseLdapPath = contextSource.getBaseLdapPathAsString();
    }

    @Override
    protected Set<GrantedAuthority> getAdditionalRoles(
            DirContextOperations user, String username) {

        Set<GrantedAuthority> authorities = new HashSet<>();

        String userDn = user.getDn().toString();
        String fullUserDn = buildFullUserDn(userDn);

        ldapTemplate.search(
                "ou=groups",
                buildMemberSearchFilter(userDn, fullUserDn),
                (AttributesMapper<Object>) (attrs) -> {
                    // add every businessCategory value as an authority
                    if (attrs.get("businessCategory") != null) {
                        var values = attrs.get("businessCategory").getAll();
                        while (values.hasMore()) {
                            String permission = (String) values.next();
                            authorities.add(new SimpleGrantedAuthority(toRoleAuthority(permission)));
                        }
                    }
                    return null;
                }
        );

        return authorities;
    }

    @Override
    public Set<GrantedAuthority> getGroupMembershipRoles(String userDn, String username) {
        return new HashSet<>();
    }

    private String buildMemberSearchFilter(String userDn, String fullUserDn) {
        String encodedUserDn = LdapEncoder.filterEncode(userDn);
        String encodedFullUserDn = LdapEncoder.filterEncode(fullUserDn);

        if (encodedUserDn.equals(encodedFullUserDn)) {
            return "(member=" + encodedUserDn + ")";
        }

        return "(|(member=" + encodedUserDn + ")(member=" + encodedFullUserDn + "))";
    }

    private String buildFullUserDn(String userDn) {
        if (baseLdapPath == null || baseLdapPath.isBlank()
                || userDn.toLowerCase().endsWith(baseLdapPath.toLowerCase())) {
            return userDn;
        }

        return userDn + "," + baseLdapPath;
    }

    private String toRoleAuthority(String value) {
        String normalized = value.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            return normalized;
        }
        return "ROLE_" + normalized;
    }
}
