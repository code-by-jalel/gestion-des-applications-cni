package com.example.stage.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidOrganizationException.class)
    public ResponseEntity<Map<String,String>> handleInvalidOrg(InvalidOrganizationException ex){
        Map<String,String> error = new HashMap<>();
        error.put("error",ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(error);
    }
    @ExceptionHandler(UidAlreadyExistException.class)
    public ResponseEntity<Map<String,String>> handleUserAlreadyExists(UidAlreadyExistException ex){
        Map<String,String> error = new HashMap<>();
        error.put("error",ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(error);
    }
    @ExceptionHandler(MailAlreadyExists.class)
    public ResponseEntity<Map<String,String>> handleMailAlreadyExists(MailAlreadyExists ex){
        Map<String,String> error = new HashMap<>();
        error.put("error",ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(error);
    }
    @ExceptionHandler(InvalidUidException.class)
    public ResponseEntity<Map<String,String>> handleInvalidUidException(InvalidUidException ex){
        Map<String,String> error = new HashMap<>();
        error.put("error",ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(error);
    }
}
