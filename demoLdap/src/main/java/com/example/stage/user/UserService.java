package com.example.stage.user;

import javax.naming.directory.*;

import com.example.stage.exceptions.InvalidOrganizationException;
import com.example.stage.exceptions.MailAlreadyExists;
import com.example.stage.exceptions.UidAlreadyExistException;
import com.example.stage.organisation.OrganisationService;
import com.example.stage.user.dto.CreateUserRequest;
import com.example.stage.user.dto.UpdateUserRequest;
import com.example.stage.user.dto.UserDto;
import com.example.stage.security.LdapPasswordEncoder;
import com.example.stage.utils.PagedResult;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.naming.Name;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class UserService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_PAGING_SESSIONS = 100;
    private static final Duration PAGING_SESSION_TTL = Duration.ofMinutes(5);

    private final LdapTemplate ldapTemplate;
    private final AuthenticationManager authenticationManager;
    private final OrganisationService organisationService;
    private final ConcurrentMap<String, PagingSession> pagingSessions = new ConcurrentHashMap<>();
    public UserService(LdapTemplate ldapTemplate, AuthenticationManager authenticationManager, OrganisationService organisationService) {
        this.ldapTemplate = ldapTemplate;
        this.authenticationManager = authenticationManager;
        this.organisationService = organisationService;
    }
    /* public List<UserDto> listUsers() {
        Map<String,String> orgDescription = organisationService.listOrganizations(false);
        ldapTemplate.search(
                query().base("ou=Tunisie,ou=structures").where("objectclass").isPresent(),
                (AttributesMapper<Void>)attrs -> {
                    String ou = (String) attrs.get("ou").get();
                    String description = attrs.get("description") != null ? (String) attrs.get("description").get() : null;
                    orgDescription.put(ou,description);
                    return null;
                }
        );
        List<UserDto> users = ldapTemplate.search(
                query().base("ou=users").where("objectclass").is("inetOrgPerson"),
                (AttributesMapper<UserDto>) attrs -> new UserDto(
                        (String) attrs.get("uid").get(),
                        (String) attrs.get("cn").get(),
                        (String) attrs.get("sn").get(),
                        attrs.get("mail") != null ? (String) attrs.get("mail").get():null,
                        attrs.get("o") != null ? (String) attrs.get("o").get():null,
                        attrs.get("telephoneNumber") != null ? (String) attrs.get("telephoneNumber").get():null,
                        attrs.get("givenName") != null ? (String) attrs.get("givenName").get():null
                )
        );
        return users.stream()
                .map(u -> new UserDto(
                        u.uid(),
                        u.cn(),
                        u.sn(),
                        u.mail(),
                        u.o() != null ? orgDescription.get(u.o()): null,
                        u.telephoneNumber(),
                        u.givenName()
                ))
                .collect(Collectors.toList());
    }*/
    public List<UserDto> listUsersByStructure(String structure){
        long start = System.currentTimeMillis();
        System.out.println("LDAP START");
        Map<String,String> orgDescription = organisationService.listOrganizations(structure,false);
        String filter = orgDescription.keySet().stream()
                .map(s -> "(o=" + s + ")")
                .collect(Collectors.joining("", "(|", ")"));
        List<UserDto> users = ldapTemplate.search(
                query().base("ou=users")
                        .searchScope(SearchScope.SUBTREE)
                        .filter(filter),
                (AttributesMapper<UserDto>) attrs -> new UserDto(
                        (String) attrs.get("uid").get(),
                        (String) attrs.get("cn").get(),
                        (String) attrs.get("sn").get(),
                        attrs.get("mail") != null ? (String) attrs.get("mail").get():null,
                        attrs.get("o") != null ? (String) attrs.get("o").get():null,
                        attrs.get("telephoneNumber") != null ? (String) attrs.get("telephoneNumber").get():null,
                        attrs.get("givenName") != null ? (String) attrs.get("givenName").get():null
                )
        );
        System.out.println("LDAP END took " + (System.currentTimeMillis() - start));
        return users.stream()
                .map(u -> new UserDto(
                        u.uid(),
                        u.cn(),
                        u.sn(),
                        u.mail(),
                        u.o() != null ? orgDescription.get(u.o()): null,
                        u.telephoneNumber(),
                        u.givenName()
                ))
                .collect(Collectors.toList());
    }
    public boolean uidExists(String uid) {
        return !ldapTemplate.search(
                query()
                        .base("ou=users")
                        .where("uid").is(uid),
                (AttributesMapper<String>) attrs ->
                        (String) attrs.get("uid").get()
        ).isEmpty();
    }
    public boolean emailExists(String mail){
        return !ldapTemplate.search(
                query()
                        .base("ou=users")
                        .where("mail").is(mail),
                (AttributesMapper<String>) attrs ->
                        (String) attrs.get("uid").get()
        ).isEmpty();
    }
    public void createUser(CreateUserRequest request) {
        if(uidExists(request.uid())){
            throw new UidAlreadyExistException(request.uid());
        }
        if(emailExists(request.mail())){
            throw new MailAlreadyExists();
        }
        Map<String,String> orgDescription = organisationService.listOrganizations(true);
        Name dn = LdapNameBuilder.newInstance("ou=users").add("uid", request.uid()).build();

        DirContextAdapter context = new DirContextAdapter(dn);
        context.setAttributeValues("objectclass", new String[] {
                "top", "person", "organizationalPerson", "inetOrgPerson"
        });
        context.setAttributeValue("uid", request.uid());
        context.setAttributeValue("cn", request.sn().concat(" " + request.givenName()));
        context.setAttributeValue("sn", request.sn());
        context.setAttributeValue("givenName", request.givenName());
        if (request.o() != null){
            String org = orgDescription.get(request.o());
            if(org ==null){
                throw new InvalidOrganizationException(request.o());
            }
            context.setAttributeValue("o",org);
        }
        if (request.mail() != null) {
            context.setAttributeValue("mail", request.mail());
        }
        context.setAttributeValue("userPassword", LdapPasswordEncoder.encodeSSHA(request.password()));
        context.setAttributeValue("telephoneNumber", request.telephoneNumber());
        ldapTemplate.bind(context);
    }
    public String getStructureForMail(String mail){
        List<String> results = ldapTemplate.search(
                query().base("ou=users").where("mail").is(mail),
                (AttributesMapper<String>) attrs ->{
                    Object o = attrs.get("o") != null ? attrs.get("o").get():null;
                    return o != null ? o.toString() : null;
                }
        );
        return results.isEmpty() ? null : results.get(0);
    }
    public void deleteUser(String username) {
        Name dn = LdapNameBuilder.newInstance("ou=users").add("uid", username).build();
        ldapTemplate.unbind(dn);
    }
    public void updateUser(String username, UpdateUserRequest request) {
        //if(!(request.uid().equals(username)) &&  uidExists(request.uid())) throw new UidAlreadyExistException(request.uid());
        Map<String,String> orgDescription = organisationService.listOrganizations(true);
        Name dn = LdapNameBuilder.newInstance("ou=users").add("uid", username).build();
        DirContextOperations ctx = ldapTemplate.lookupContext(dn);
        // String uid, String sn, String givenName, String mail,String o,String telephoneNumber
        if (request.sn() != null) ctx.setAttributeValue("sn", request.sn());
        if (request.givenName() != null) ctx.setAttributeValue("givenName", request.givenName());
        if (request.mail() != null) ctx.setAttributeValue("mail", request.mail());
        if (request.telephoneNumber() != null) ctx.setAttributeValue("telephoneNumber", request.telephoneNumber());
        if (request.o() != null){
            String org = orgDescription.get(request.o());
            if(org ==null){
                throw new InvalidOrganizationException(request.o());
            }
            ctx.setAttributeValue("o",org);
        }

        ldapTemplate.modifyAttributes(ctx);
    }
    public void changeOwnPassword(String username, String currentPassword, String newPassword) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, currentPassword)
        );
        setPassword(username, newPassword);
    }
    public void adminResetPassword(String username, String newPassword) {
        setPassword(username, newPassword);
    }
    private void setPassword(String username, String newPassword) {
        Name dn = LdapNameBuilder.newInstance("ou=users").add("uid", username).build();

        Attribute passwordAttr = new BasicAttribute("userPassword", LdapPasswordEncoder.encodeSSHA(newPassword));
        ModificationItem[] mods = new ModificationItem[] {
                new ModificationItem(DirContext.REPLACE_ATTRIBUTE, passwordAttr)
        };

        ldapTemplate.modifyAttributes(dn, mods);
    }
    public PagedResult<UserDto> listUsersByStructurePaged(String structure, int pageSize, String pageToken, String search){
        cleanupExpiredPagingSessions();

        int safePageSize = normalizePageSize(pageSize);
        String normalizedSearch = normalizeSearch(search);
        PagingSession session = findOrCreatePagingSession(structure, safePageSize, pageToken, normalizedSearch);

        synchronized (session) {
            if (pageToken != null && !pageToken.isBlank()
                    && (!session.structure.equals(structure)
                    || session.pageSize != safePageSize
                    || !session.search.equals(normalizedSearch))) {
                closePagingSession(session.token);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paging token for this request");
            }

            PagedResultsDirContextProcessor processor =
                    session.cookie == null
                            ? new PagedResultsDirContextProcessor(session.pageSize)
                            : new PagedResultsDirContextProcessor(session.pageSize, session.cookie);

            List<UserDto> users = session.ldapTemplate.search(
                    "ou=users",
                    session.filter,
                    subtreeSearchControls(),
                    (AttributesMapper<UserDto>) attrs -> new UserDto(
                            (String) attrs.get("uid").get(),
                            (String) attrs.get("cn").get(),
                            (String) attrs.get("sn").get(),
                            attrs.get("mail") != null ? (String) attrs.get("mail").get():null,
                            attrs.get("o") != null ? (String) attrs.get("o").get():null,
                            attrs.get("telephoneNumber") != null ? (String) attrs.get("telephoneNumber").get():null,
                            attrs.get("givenName") != null ? (String) attrs.get("givenName").get():null
                    ),
                    processor
            );

            users = users.stream()
                    .map(u -> new UserDto(
                            u.uid(),
                            u.cn(),
                            u.sn(),
                            u.mail(),
                            u.o() != null ? session.orgDescription.get(u.o()): null,
                            u.telephoneNumber(),
                            u.givenName()
                    ))
                    .collect(Collectors.toList());

            session.cookie = processor.getCookie();
            session.expiresAt = Instant.now().plus(PAGING_SESSION_TTL);

            if (!hasMorePages(session.cookie)) {
                closePagingSession(session.token);
                return new PagedResult<>(users, null);
            }

            return new PagedResult<>(users, session.token);
        }
    }
    private PagingSession findOrCreatePagingSession(String structure, int pageSize, String pageToken, String search) {
        if (pageToken != null && !pageToken.isBlank()) {
            PagingSession session = pagingSessions.get(pageToken);
            if (session == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paging token expired or invalid");
            }
            return session;
        }
        if (pagingSessions.size() >= MAX_PAGING_SESSIONS) {
            cleanupOldestPagingSession();
        }

        Map<String,String> orgDescription = organisationService.listOrganizations(structure,false);
        String filter = buildUsersFilter(orgDescription, search);

        String token = UUID.randomUUID().toString();
        PagingSession session = new PagingSession(token, structure, pageSize, search, filter, orgDescription);
        pagingSessions.put(token, session);
        return session;
    }
    private String buildUsersFilter(Map<String,String> orgDescription, String search) {
        String structureFilter = orgDescription.keySet().stream()
                .map(s -> "(o=" + LdapEncoder.filterEncode(s) + ")")
                .collect(Collectors.joining("", "(|", ")"));

        if (search.isBlank()) {
            return "(&(objectClass=inetOrgPerson)" + structureFilter + ")";
        }

        String encodedSearch = LdapEncoder.filterEncode(search);
        String searchFilter = "(|"
                + "(uid=*" + encodedSearch + "*)"
                + "(cn=*" + encodedSearch + "*)"
                + "(sn=*" + encodedSearch + "*)"
                + "(givenName=*" + encodedSearch + "*)"
                + "(mail=*" + encodedSearch + "*)"
                + "(telephoneNumber=*" + encodedSearch + "*)"
                + ")";

        return "(&(objectClass=inetOrgPerson)" + structureFilter + searchFilter + ")";
    }
    private String normalizeSearch(String search) {
        return search == null ? "" : search.trim();
    }
    private int normalizePageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
    private SearchControls subtreeSearchControls() {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        return controls;
    }
    private boolean hasMorePages(PagedResultsCookie cookie) {
        return cookie != null && cookie.getCookie() != null && cookie.getCookie().length > 0;
    }
    private void cleanupExpiredPagingSessions() {
        Instant now = Instant.now();
        pagingSessions.forEach((token, session) -> {
            if (session.expiresAt.isBefore(now)) {
                closePagingSession(token);
            }
        });
    }
    private void cleanupOldestPagingSession() {
        pagingSessions.values().stream()
                .min((a, b) -> a.expiresAt.compareTo(b.expiresAt))
                .ifPresent(session -> closePagingSession(session.token));
    }
    private void closePagingSession(String token) {
        PagingSession session = pagingSessions.remove(token);
        if (session != null) {
            session.close();
        }
    }
    private class PagingSession {
        private final String token;
        private final String structure;
        private final int pageSize;
        private final String search;
        private final String filter;
        private final Map<String, String> orgDescription;
        private final org.springframework.ldap.core.support.SingleContextSource contextSource;
        private final LdapTemplate ldapTemplate;
        private PagedResultsCookie cookie;
        private Instant expiresAt = Instant.now().plus(PAGING_SESSION_TTL);

        private PagingSession(String token, String structure, int pageSize, String search, String filter, Map<String, String> orgDescription) {
            this.token = token;
            this.structure = structure;
            this.pageSize = pageSize;
            this.search = search;
            this.filter = filter;
            this.orgDescription = orgDescription;
            this.contextSource = new org.springframework.ldap.core.support.SingleContextSource(
                    UserService.this.ldapTemplate.getContextSource().getReadOnlyContext()
            );
            this.ldapTemplate = new LdapTemplate(this.contextSource);
        }

        private void close() {
            try {
                contextSource.destroy();
            } catch (Exception ignored) {
            }
        }
    }

}
