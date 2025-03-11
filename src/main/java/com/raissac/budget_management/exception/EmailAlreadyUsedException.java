package com.raissac.budget_management.exception;

public class EmailAlreadyUsedException extends RuntimeException{
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
