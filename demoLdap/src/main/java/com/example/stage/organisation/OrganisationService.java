package com.example.stage.organisation;

import com.example.stage.organisation.dto.CreateOrganisationRequest;
import com.example.stage.organisation.dto.OrganisationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.*;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class OrganisationService {

    private final LdapTemplate ldapTemplate;
    @Value("${spring.ldap.base}")
    private String ldapBase;
    private final AuthenticationManager authenticationManager;
    public OrganisationService(LdapTemplate ldapTemplate,AuthenticationManager authenticationManager){
        this.ldapTemplate = ldapTemplate;
        this.authenticationManager = authenticationManager;
    }
    public Map<String, String> listOrganizations(boolean reverse) {
        return listOrganizations("", reverse);
    }

    public List<String> structurePath(String structure){
        return ldapTemplate.search(
                query()
                        .base("ou=Tunisie,ou=structures")
                        .searchScope(SearchScope.SUBTREE)
                        .where("ou").is(structure)
                ,
                (ContextMapper<String>) ctx -> ((DirContextAdapter) ctx).getDn().toString()
        );
    }
    public Map<String, String> listOrganizations(String structureOu, boolean reverse) {
        Map<String, String> orgDescription = new HashMap<>();

        String structureString="ou=Tunisie,ou=structures";
        if(!structureOu.isEmpty() ) structureString = structurePath(structureOu).get(0);
        ldapTemplate.search(
                query()
                        .base(structureString)
                        .searchScope(SearchScope.SUBTREE)
                        .where("objectclass").isPresent(),
                (AttributesMapper<Void>) attrs -> {
                    String ou = attrs.get("ou") != null ? attrs.get("ou").get().toString() : null;
                    String description = attrs.get("description") != null
                            ? attrs.get("description").get().toString()
                            : null;

                    if (ou != null) {
                        if (reverse) orgDescription.put(description, ou);
                        else orgDescription.put(ou, description);
                    }
                    return null;
                }
        );

        return orgDescription;
    }

    public List<OrganisationDto> getTree(String adminStructure) {
        return getTree(adminStructure, null);
    }

    public List<OrganisationDto> getTree(String adminStructure, String search) {
        String structureString = structurePath(adminStructure).get(0);
        List<OrganisationDto> roots = ldapTemplate.search(
                query()
                        .base(structureString).where("ou").is(adminStructure),
                (ContextMapper<OrganisationDto>) ctx -> {
                    DirContextOperations dirCtx = (DirContextOperations) ctx;
                    String ou = dirCtx.getStringAttribute("ou");
                    String description = dirCtx.getStringAttribute("description");
                    String relativeDn = dirCtx.getDn().toString();
                    List<OrganisationDto> children = buildChildren(relativeDn);
                    return new OrganisationDto(ou, description, relativeDn, children);
                }
        );

        String normalizedSearch = normalizeSearch(search);
        if (normalizedSearch.isBlank()) {
            return roots;
        }

        return roots.stream()
                .map(root -> filterTree(root, normalizedSearch))
                .filter(Objects::nonNull)
                .toList();
    }

    private OrganisationDto filterTree(OrganisationDto node, String search) {
        List<OrganisationDto> matchingChildren = node.children().stream()
                .map(child -> filterTree(child, search))
                .filter(Objects::nonNull)
                .toList();

        if (matchesSearch(node, search) || !matchingChildren.isEmpty()) {
            return new OrganisationDto(node.ou(), node.description(), node.dn(), matchingChildren);
        }

        return null;
    }

    private boolean matchesSearch(OrganisationDto node, String search) {
        return containsIgnoreCase(node.ou(), search) || containsIgnoreCase(node.description(), search);
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private String normalizeSearch(String search) {
        return search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
    }

    private List<OrganisationDto> buildChildren(String baseDn) {
        return ldapTemplate.search(
                query().base(baseDn)
                        .searchScope(SearchScope.ONELEVEL)
                        .where("objectclass").is("organizationalUnit"),
                (AttributesMapper<OrganisationDto>) attrs-> {
                    String ou = (String) attrs.get("ou").get();
                    String description = attrs.get("description") != null
                            ? (String) attrs.get("description").get() : null;
                    String dn = "ou=" + ou + "," + baseDn;
                    List<OrganisationDto> children = buildChildren(dn);
                    return new OrganisationDto(ou, description, dn, children);
                }

        );
    }
    public void create(CreateOrganisationRequest request) {
        javax.naming.Name dn = LdapNameBuilder.newInstance(request.parentDn())
                .add("ou", request.ou()).build();

        DirContextAdapter ctx = new DirContextAdapter(dn);
        ctx.setAttributeValues("objectclass", new String[] { "top", "organizationalUnit" });
        ctx.setAttributeValue("ou", request.ou());
        if (request.description() != null && !request.description().isBlank()) {
            ctx.setAttributeValue("description", request.description());
        }
        ldapTemplate.bind(ctx);
    }
    public void update(String dn, String description) {
        javax.naming.Name name = LdapNameBuilder.newInstance(dn).build();
        DirContextOperations ctx = ldapTemplate.lookupContext(name);
        ctx.setAttributeValue("description", description);
        ldapTemplate.modifyAttributes(ctx);
    }
    public void delete(String dn) {
        // check no children first
        List<String> children = ldapTemplate.search(
                query().base(dn).searchScope(SearchScope.ONELEVEL)
                        .where("objectclass").is("organizationalUnit"),
                (AttributesMapper<String>) attrs -> (String) attrs.get("ou").get()
        );
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete an organisation that has sub-organisations. Delete children first.");
        }
        ldapTemplate.unbind(LdapNameBuilder.newInstance(dn).build());
    }
}
