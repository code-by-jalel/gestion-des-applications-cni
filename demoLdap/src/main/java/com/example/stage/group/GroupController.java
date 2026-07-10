package com.example.stage.group;

import com.example.stage.group.dto.CreateGroupRequest;
import com.example.stage.group.dto.GroupDto;
import com.example.stage.group.dto.GroupMemberRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@PreAuthorize("hasAuthority('ROLE_ADMINSGROUP')")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /*
    @GetMapping
    public List<GroupDto> listGroups() {
        return groupService.listGroups();
    }*/
    @GetMapping
    public List<GroupDto> listGroups(HttpServletRequest request){
        String adminStructure = (String) request.getAttribute("structure");
        return groupService.listGroupsByStructure(adminStructure);
    }
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequest request) {
        groupService.createGroup(request);
        return ResponseEntity.ok(Map.of("message", "Group created"));
    }

    @DeleteMapping("/{cn}")
    public ResponseEntity<?> deleteGroup(@PathVariable String cn) {
        groupService.deleteGroup(cn);
        return ResponseEntity.ok(Map.of("message", "Group deleted"));
    }

    @PostMapping("/{cn}/members")
    public ResponseEntity<?> addMember(@PathVariable String cn, @RequestBody GroupMemberRequest request) {
        groupService.addMember(cn, request.uid());
        return ResponseEntity.ok(Map.of("message", "Member added"));
    }

    @DeleteMapping("/{cn}/members/{uid}")
    public ResponseEntity<?> removeMember(@PathVariable String cn, @PathVariable String uid) {
        try {
            groupService.removeMember(cn, uid);
            return ResponseEntity.ok(Map.of("message", "Member removed"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }
    }
    @GetMapping("/{cn}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMINSGROUP')")
    public List<String> getPermissions(@PathVariable String cn) {
        return groupService.getPermissions(cn);
    }

    @PostMapping("/{cn}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMINSGROUP')")
    public ResponseEntity<?> addPermission(@PathVariable String cn,
                                           @RequestBody Map<String, String> body) {
        groupService.addPermission(cn, body.get("permission"));
        return ResponseEntity.ok(Map.of("message", "Permission added"));
    }

    @DeleteMapping("/{cn}/permissions/{permission}")
    @PreAuthorize("hasAuthority('ROLE_ADMINSGROUP')")
    public ResponseEntity<?> removePermission(@PathVariable String cn,
                                              @PathVariable String permission) {
        groupService.removePermission(cn, permission);
        return ResponseEntity.ok(Map.of("message", "Permission removed"));
    }
}