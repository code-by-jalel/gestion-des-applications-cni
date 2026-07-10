package com.example.stage.group.dto;

public record CreateGroupRequest(String cn, String initialMemberUid,Boolean isAdmin,Boolean isGestionnaireUtilisateur,Boolean isGestionnaireOrganisation) {}