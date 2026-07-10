package com.example.stage.exceptions;

public class InvalidOrganizationException extends RuntimeException{
    public InvalidOrganizationException(String org){
        super("Organisation introuvable: " + org);
    }
}
