package com.chiacademy.software.phonecontacts.exception;

public class UserAlreadyExistsException extends RuntimeException{

    private final String value;

    public UserAlreadyExistsException(String message, String value) {
        super(message);
        this.value = value;
    }

    public String getWrongValue(){
        return value;
    }
}
