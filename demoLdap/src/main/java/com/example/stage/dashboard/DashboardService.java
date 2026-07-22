package com.example.stage.dashboard;

import com.example.stage.organisation.OrganisationService;
import com.example.stage.organisation.dto.OrganisationDto;
import com.example.stage.user.UserService;
import com.example.stage.group.GroupService;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class DashboardService {

    private final LdapTemplate ldapTemplate;
    private final UserService userService;
    private final GroupService groupService;
    private final OrganisationService organisationService;
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    public DashboardService(LdapTemplate ldapTemplate,
                            UserService userService,
                            GroupService groupService,
                            OrganisationService organisationService) {
        this.ldapTemplate = ldapTemplate;
        this.userService = userService;
        this.groupService = groupService;
        this.organisationService=organisationService;
    }
    private Set<String> getAllOrganisations(List<OrganisationDto> organisations) {
        return organisations.stream()
                .flatMap(org -> Stream.concat(
                        Stream.of(org.ou()),
                        getAllOrganisations(org.children() == null ? List.of() : org.children()).stream()))
                .collect(Collectors.toSet());
    }

    public DashboardStats getStats(String adminStructure) {
        Map<String,String> orgDescription = organisationService.listAllOrgnisations();
        List<OrganisationDto> orgOu = organisationService.getAllOrganisationTree();
        log.info(orgOu.toString());
        Set<String> scope = organisationService.listAllOrganisationsParentOnly();
        log.info(scope.toString());
        List<String> allUids = getUserUidsInScope(scope);
        int totalUsers = allUids.size();

        var allGroups = groupService.listGroups().stream()
                .collect(Collectors.toList());
        int totalGroups = allGroups.size();

        int totalOrganisations = scope.size();

        List<OrgUserCount> usersByOrg = orgOu.stream()
                .map(org -> {
                    Set<String> children = organisationService.flattenOrganisations(List.of(org));

                    int count = countUsersInOrg(children);

                    return new OrgUserCount(org.description(), count);
                })
                .sorted(Comparator.comparingInt(OrgUserCount::count).reversed())
                .collect(Collectors.toList());
        List<GroupMemberCount> topGroups = allGroups.stream()
                .map(g -> new GroupMemberCount(g.cn(), g.members().size()))
                .sorted(Comparator.comparingInt(GroupMemberCount::memberCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return new DashboardStats(
                totalUsers, totalGroups, totalOrganisations,
                 usersByOrg, topGroups
        );
    }

    private List<String> getUserUidsInScope(Set<String> scope) {
        if (scope.isEmpty()) return List.of();
        return ldapTemplate.search(
                query().base("ou=users").where("objectclass").is("inetOrgPerson"),
                (AttributesMapper<String>) attrs -> {
                    Object o = attrs.get("o") != null ? attrs.get("o").get() : null;
                    Object uid = attrs.get("uid") != null ? attrs.get("uid").get() : null;
                    if (o != null && scope.contains(o.toString()) && uid != null) {
                        return uid.toString();
                    }
                    return null;
                }
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private int countUsersInOrg(Set<String> organisations) {

        if (organisations.isEmpty()) {
            return 0;
        }

        String orgFilter = organisations.stream()
                .map(org -> "(o=" + org + ")")
                .collect(Collectors.joining());

        String filter = "(&(objectclass=inetOrgPerson)(|" + orgFilter + "))";

        return ldapTemplate.search(
                "ou=users",
                filter,
                (AttributesMapper<String>) attrs ->
                        attrs.get("uid").get().toString()
        ).size();
    }
}