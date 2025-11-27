package com.SebastianCornejo.Proyecto.Fullstack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PeticionInvalidaException extends RuntimeException {
    public PeticionInvalidaException(String message) {
        super(message);
    }
}
