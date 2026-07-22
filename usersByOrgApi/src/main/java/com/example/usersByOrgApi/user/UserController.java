package com.example.usersByOrgApi.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/by-org/{orgName}")
    public ResponseEntity<List<UserDto>> getUsersByOrg(@PathVariable String orgName) {
        return ResponseEntity.ok(userService.getUsersByOrganisation(orgName));
    }
}