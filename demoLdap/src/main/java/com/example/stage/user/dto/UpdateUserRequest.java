package com.example.stage.user.dto;

public record UpdateUserRequest(String uid, String sn, String givenName, String mail,String o,String telephoneNumber) {}