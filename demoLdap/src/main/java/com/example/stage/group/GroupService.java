package com.example.stage.group;

import com.example.stage.exceptions.InvalidUidException;
import com.example.stage.group.dto.CreateGroupRequest;
import com.example.stage.group.dto.GroupDto;
import com.example.stage.organisation.OrganisationService;
import com.example.stage.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
public class GroupService {
    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final LdapTemplate ldapTemplate;
    private final OrganisationService organisationService;
    @Autowired
    private UserService userService;
    @Value("${spring.ldap.base}")
    private String ldapBase;

    public GroupService(LdapTemplate ldapTemplate,OrganisationService organisationService) {
        this.organisationService=organisationService;
        this.ldapTemplate = ldapTemplate;
    }
    public List<GroupDto> listGroupsByStructure(String adminStructure){
        Map<String,String> orgDescription = organisationService.listOrganizations(adminStructure,false);
        String filter = orgDescription.keySet().stream()
                .map(s -> "(o=" + s + ")")
                .collect(Collectors.joining("", "(|", ")"));
        return ldapTemplate.search(
                query().base("ou=groups")
                        .searchScope(SearchScope.SUBTREE)
                        .filter(filter),
                (AttributesMapper<GroupDto>) attrs -> {
                    List<String> members = new ArrayList<>();
                    Attribute memberAttr = attrs.get("member");
                    if (memberAttr != null) {
                        var values = memberAttr.getAll();
                        while (values.hasMore()) {
                            members.add(extractUid((String) values.next()));
                        }
                    }
                    return new GroupDto((String) attrs.get("cn").get(), members);
                }
        );
    }
    public List<GroupDto> listGroups() {
        return ldapTemplate.search(
                query().base("ou=groups").where("objectclass").is("groupOfNames"),
                (AttributesMapper<GroupDto>) attrs -> {
                    String cn = attrs.get("cn") != null
                            ? (String) attrs.get("cn").get() : null;

                    String o = attrs.get("o") != null
                            ? (String) attrs.get("o").get() : null;

                    List<String> members = new ArrayList<>();
                    Attribute memberAttr = attrs.get("member");
                    if (memberAttr != null) {
                        var values = memberAttr.getAll();
                        while (values.hasMore()) {
                            members.add(extractUid((String) values.next()));
                        }
                    }

                    return new GroupDto(cn,members);
                }
        );
    }
    /* public List<GroupDto> listGroups() {
        return ldapTemplate.search(
                query().where("objectclass").is("groupOfNames"),
                (AttributesMapper<GroupDto>) attrs -> {
                    List<String> members = new ArrayList<>();
                    Attribute memberAttr = attrs.get("member");
                    if (memberAttr != null) {
                        var values = memberAttr.getAll();
                        while (values.hasMore()) {
                            members.add(extractUid((String) values.next()));
                        }
                    }
                    return new GroupDto((String) attrs.get("cn").get(),(String) attrs.get("o").get(), members);
                }
        );
    }*/
    public void addPermission(String groupCn, String permission) {
        Name dn = LdapNameBuilder.newInstance("ou=groups").add("cn", groupCn).build();
        Attribute attr = new BasicAttribute("businessCategory", permission);
        ldapTemplate.modifyAttributes(dn, new ModificationItem[]{
                new ModificationItem(DirContext.ADD_ATTRIBUTE, attr)
        });
    }

    public void removePermission(String groupCn, String permission) {
        Name dn = LdapNameBuilder.newInstance("ou=groups").add("cn", groupCn).build();
        Attribute attr = new BasicAttribute("businessCategory", permission);
        ldapTemplate.modifyAttributes(dn, new ModificationItem[]{
                new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attr)
        });
    }

    public List<String> getPermissions(String groupCn) {
        return ldapTemplate.search(
                query().base("ou=groups").where("cn").is(groupCn),
                (AttributesMapper<List<String>>) attrs -> {
                    List<String> permissions = new ArrayList<>();
                    if (attrs.get("businessCategory") != null) {
                        var values = attrs.get("businessCategory").getAll();
                        while (values.hasMore()) {
                            permissions.add((String) values.next());
                        }
                    }
                    return permissions;
                }
        ).stream().findFirst().orElse(List.of());
    }
    public void createGroup(CreateGroupRequest request) {
        Name dn = LdapNameBuilder.newInstance("ou=groups").add("cn", request.cn()).build();
        String permissions="";
        if(request.isAdmin()) permissions += "ROLE_ADMINSGROUP,";
        if(request.isGestionnaireUtilisateur()) permissions += "ROLE_GESTIONNAIREUTILISATEURS,";
        if(request.isGestionnaireOrganisation()) permissions += "ROLE_GESTIONNAIREORGANISATION,";
        permissions=permissions.substring(0, permissions.length() - 1);
        DirContextAdapter context = new DirContextAdapter(dn);
        context.setAttributeValues("objectclass", new String[] { "top", "groupOfNames" });
        Map<String,String> orgDescription = organisationService.listOrganizations(true);
        context.setAttributeValue("cn", request.cn());
        context.setAttributeValue("businessCategory",permissions);
        // groupOfNames requires at least one member at creation time
        context.setAttributeValue("member", buildMemberDn(request.initialMemberUid()));

        ldapTemplate.bind(context);
    }

    public void deleteGroup(String cn) {
        Name dn = LdapNameBuilder.newInstance("ou=groups").add("cn", cn).build();
        ldapTemplate.unbind(dn);
    }

    public void addMember(String groupCn, String uid) {
        if(!userService.uidExists(uid)){
            throw new InvalidUidException(uid);
        }
        Name dn = LdapNameBuilder.newInstance("ou=groups").add("cn", groupCn).build();
        Attribute memberAttr = new BasicAttribute("member", buildMemberDn(uid));
        ldapTemplate.modifyAttributes(dn, new ModificationItem[] {
                new ModificationItem(DirContext.ADD_ATTRIBUTE, memberAttr)
        });
    }

    public void removeMember(String groupCn, String uid) {
        Name dn = LdapNameBuilder.newInstance("ou=groups").add("cn", groupCn).build();

        // refuse to remove the last member - groupOfNames can't have an empty member attribute
        GroupDto current = listGroups().stream()
                .filter(g -> g.cn().equals(groupCn))
                .findFirst()
                .orElseThrow(() -> new NameNotFoundException("Group not found: " + groupCn));

        if (current.members().size() <= 1) {
            throw new IllegalStateException("Cannot remove the last member of a group. Delete the group instead.");
        }

        Attribute memberAttr = new BasicAttribute("member", buildMemberDn(uid));
        ldapTemplate.modifyAttributes(dn, new ModificationItem[] {
                new ModificationItem(DirContext.REMOVE_ATTRIBUTE, memberAttr)
        });
    }

    private String buildMemberDn(String uid) {
        return "uid=" + uid + ",ou=users," + ldapBase;
    }

    private String extractUid(String memberDn) {
        // memberDn looks like: uid=jdoe,ou=users,dc=example,dc=com
        String[] parts = memberDn.split(",");
        for (String part : parts) {
            if (part.trim().startsWith("uid=")) {
                return part.trim().substring(4);
            }
        }
        return memberDn;
    }
}