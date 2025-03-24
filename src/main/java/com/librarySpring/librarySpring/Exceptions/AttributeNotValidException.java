package com.librarySpring.librarySpring.Exceptions;

import com.librarySpring.librarySpring.Interfaces.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AttributeNotValidException extends RuntimeException {
    public AttributeNotValidException(ErrorMessage error) {
        super(error.getMessage());
    }
}
