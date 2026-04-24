package com.company.birthday.service.exception;

public class DuplicateFieldException extends RuntimeException {

    private final String fieldName;

    public DuplicateFieldException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}

