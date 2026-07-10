package com.example.stage.user.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {}