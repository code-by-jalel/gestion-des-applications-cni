package com.example.usersByOrgApi.user;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Service;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final LdapTemplate ldapTemplate;

    public UserService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public String getOrganisationDescription(String ou) {

        List<String> descriptions = ldapTemplate.search(
                "ou=Tunisie,ou=structures",
                "(&(objectClass=organizationalUnit)(ou=" + ou + "))",
                (AttributesMapper<String>) attrs ->
                        getAttribute(attrs, "description")
        );

        return descriptions.isEmpty() ? null : descriptions.get(0);
    }
    public List<UserDto> getUsersByOrganisation(String orgName) {

        Set<String> organisations = new HashSet<>();

        collectOrganisations(
                "ou=" + orgName + ",ou=Tunisie,ou=structures",
                organisations
        );

        String filter = buildOrganisationFilter(organisations);

        return ldapTemplate.search(
                "ou=users",
                filter,
                (AttributesMapper<UserDto>) attrs -> new UserDto(
                        getAttribute(attrs, "uid"),
                        getAttribute(attrs, "cn"),
                        getAttribute(attrs, "mail"),
                        getOrganisationDescription(getAttribute(attrs, "o")),
                        getAttribute(attrs, "telephoneNumber")
                )
        );
    }


    private void collectOrganisations(String orgDn, Set<String> organisations) {

        String orgName = orgDn
                .split(",")[0]
                .replace("ou=", "");

        // Avoid infinite loops
        if (!organisations.add(orgName)) {
            return;
        }


        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

        List<String> children = ldapTemplate.search(
                orgDn,
                "(objectClass=organizationalUnit)",
                controls,
                (AttributesMapper<String>) attrs ->
                        getAttribute(attrs, "ou")
        );


        for (String child : children) {

            String childDn =
                    "ou=" + child + "," + orgDn;

            collectOrganisations(childDn, organisations);
        }
    }


    private String buildOrganisationFilter(Set<String> organisations) {

        StringBuilder filter = new StringBuilder();

        filter.append("(&(objectClass=inetOrgPerson)(|");


        for (String org : organisations) {
            filter.append("(o=")
                    .append(org)
                    .append(")");
        }


        filter.append("))");

        return filter.toString();
    }



    private String getAttribute(Attributes attrs, String attributeName) {

        try {

            return attrs.get(attributeName) != null
                    ? (String) attrs.get(attributeName).get()
                    : null;

        } catch (Exception e) {
            return null;
        }
    }
}