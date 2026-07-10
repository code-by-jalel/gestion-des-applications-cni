package com.example.stage.organisation.dto;

import java.util.List;

public record OrganisationDto (String ou, String description, String dn, List<OrganisationDto> children) { }
