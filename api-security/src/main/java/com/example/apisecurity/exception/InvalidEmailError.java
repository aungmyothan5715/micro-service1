package com.example.apisecurity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidEmailError extends ResponseStatusException {
    public InvalidEmailError(){
        super(HttpStatus.UNAUTHORIZED, "Invalid Email Error!");
        //you can write exception for html status not 500 status code.
    }
}
