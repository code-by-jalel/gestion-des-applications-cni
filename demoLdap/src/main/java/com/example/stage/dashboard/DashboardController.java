package com.example.stage.dashboard;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAuthority('ROLE_ADMINSGROUP')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public DashboardStats getStats(HttpServletRequest request) {
        String structure = (String) request.getAttribute("structure");
        return dashboardService.getStats(structure);
    }
}