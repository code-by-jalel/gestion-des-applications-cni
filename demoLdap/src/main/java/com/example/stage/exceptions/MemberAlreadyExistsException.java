package com.example.stage.exceptions;

public class MemberAlreadyExistsException extends RuntimeException{
    public MemberAlreadyExistsException(String uid,String groupCn){super("membre"+uid+"existe deja dans "+groupCn);}
}
