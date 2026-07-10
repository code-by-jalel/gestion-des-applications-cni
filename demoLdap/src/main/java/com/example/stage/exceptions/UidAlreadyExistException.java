package com.example.stage.exceptions;

public class UidAlreadyExistException extends RuntimeException{
    public UidAlreadyExistException(String org){
        super("Utilisateur existe deja : " + org );
    }
}
