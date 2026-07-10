package com.example.stage.exceptions;

public class InvalidUidException extends RuntimeException{
    public InvalidUidException(String uid){
        super("Utilisateur n'existe pas : "+ uid);
    }
}
