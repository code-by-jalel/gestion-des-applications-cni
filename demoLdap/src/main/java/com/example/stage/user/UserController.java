package com.example.stage.user;

import com.example.stage.user.dto.*;
import com.example.stage.user.dto.*;
import com.example.stage.utils.PagedResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINSGROUP','ROLE_GESTIONNAIREUTILISATEURS')")
    public PagedResult<UserDto> listUsers(HttpServletRequest request,
                                          @RequestParam(defaultValue = "20")int pageSize,
                                          @RequestParam(required = false) String cookie,
                                          @RequestParam(required = false) String search) {
        String adminStructure = (String) request.getAttribute("structure");
        return userService.listUsersByStructurePaged(adminStructure,pageSize,cookie, search);
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINSGROUP','ROLE_GESTIONNAIREUTILISATEURS')")
    public ResponseEntity<?> updateUser(@PathVariable String username,
                                        @RequestBody UpdateUserRequest request) {
        userService.updateUser(username, request);
        return ResponseEntity.ok(Map.of("message", "User updated"));
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINSGROUP','ROLE_GESTIONNAIREUTILISATEURS')")
    public ResponseEntity<?> changeOwnPassword(Authentication auth,
                                               @RequestBody ChangePasswordRequest request) {
        userService.changeOwnPassword(auth.getName(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated"));
    }

    @PutMapping("/{username}/password")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINSGROUP','ROLE_GESTIONNAIREUTILISATEURS')")
    public ResponseEntity<?> adminResetPassword(@PathVariable String username,
                                                @RequestBody AdminResetPasswordRequest request) {
        userService.adminResetPassword(username, request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset"));
    }
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINSGROUP')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok(Map.of("message", "User created"));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMINSGROUP')")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
