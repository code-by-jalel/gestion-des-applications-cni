package com.example.stage.user.dto;

public record CreateUserRequest(String uid, String sn, String givenName, String mail, String password,String o,String telephoneNumber) {}