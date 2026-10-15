package com.example.demo.exception;

public class InsufficientStockException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String message) {
        super(message);
    }
}
