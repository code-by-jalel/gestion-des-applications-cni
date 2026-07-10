package com.example.stage.exceptions;

public class MailAlreadyExists extends RuntimeException {
    public MailAlreadyExists(){
        super("Email existe deja");
    }
}
