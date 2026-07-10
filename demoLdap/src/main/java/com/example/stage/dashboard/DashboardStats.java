package com.example.stage.dashboard;
import java.util.List;

public record DashboardStats(
        int totalUsers,
        int totalGroups,
        int totalOrganisations,
        List<OrgUserCount> usersByOrg,
        List<GroupMemberCount> topGroups
) {}

record OrgUserCount(String org, int count) {}
record GroupMemberCount(String groupName, int memberCount) {}