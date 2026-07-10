package com.example.stage.organisation;


import com.example.stage.organisation.dto.CreateOrganisationRequest;
import com.example.stage.organisation.dto.OrganisationDto;
import com.example.stage.organisation.dto.UpdateOrganisationRequest;
import com.example.stage.security.JwtService;
import com.example.stage.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organisation")
public class OrganisationController {
    private final OrganisationService organisationService;
    private final JwtService jwtService;
    public OrganisationController(OrganisationService organisationService, JwtService jwtService) {
        this.organisationService=organisationService;
        this.jwtService=jwtService;}
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINSGROUP','ROLE_GESTIONNAIREORGANISATION','ROLE_GESTIONNAIREUTILISATEURS')")
    public Map<String,String> listOrganisation(HttpServletRequest request,
                                               @RequestParam(required = false) String search){
        String token = JwtUtils.extractToken(request);
        String structure = jwtService.extractStructure(token);
        return organisationService.listOrganizations(structure,false);
    }
    @GetMapping("/tree")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINSGROUP','ROLE_GESTIONNAIREORGANISATION')")
    public List<OrganisationDto> getTree(HttpServletRequest request,@RequestParam(required = false)String search) {
        String structure = (String) request.getAttribute("structure");
        return organisationService.getTree(structure,search);
    }
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINSGROUP')")
    public ResponseEntity<?> create(@RequestBody CreateOrganisationRequest req) {
        organisationService.create(req);
        return ResponseEntity.ok(Map.of("message", "Organisation created"));
    }
    @PutMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINSGROUP','ROLE_GESTIONNAIREORGANISATION')")
    public ResponseEntity<?> update(@RequestBody UpdateOrganisationRequest req,
                                    @RequestParam String dn) {
        organisationService.update(dn, req.description());
        return ResponseEntity.ok(Map.of("message", "Organisation updated"));
    }
    @DeleteMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINSGROUP')")
    public ResponseEntity<?> delete(@RequestParam String dn) {
        try {
            organisationService.delete(dn);
            return ResponseEntity.ok(Map.of("message", "Organisation deleted"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
